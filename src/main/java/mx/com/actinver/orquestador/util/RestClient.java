package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import mx.com.actinver.common.exception.ConsecutiveServerErrorLimitReachedException;
import mx.com.actinver.orquestador.dto.DescargaCfdiResponseDto;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Setter
public class RestClient {

    private static final Logger LOG = LogManager.getLogger(RestClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;
    private String errorCodes;
    private Integer errorRetry;
    private final AtomicInteger consecutive500Errors = new AtomicInteger(0);

    @PostConstruct
    private void init() {
        try {
            // Pool de conexiones
            PoolingHttpClientConnectionManager connMgr =
                    new PoolingHttpClientConnectionManager();
            connMgr.setMaxTotal(400);
            connMgr.setDefaultMaxPerRoute(300);
            // Valida conexiones tras 10s inactivas
            connMgr.setValidateAfterInactivity(10_000);

            // Timeouts: conectar en 30s, leer en 30s
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(30_000)
                    .setSocketTimeout(30_000)
                    .build();

            // Cliente HTTP con keep-alive y limpieza automática
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connMgr)
                    .setDefaultRequestConfig(config)
                    .setKeepAliveStrategy((response, context) -> 20_000)         // 20s keep-alive
                    .evictIdleConnections(30, TimeUnit.SECONDS)                // cierra ociosas >30s
                    .evictExpiredConnections()                                 // cierra expiradas
                    .build();

            //  Factory y RestTemplate
            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            // reuse de conexiones
            factory.setConnectionRequestTimeout(5_000);  // espera del pool

            this.restTemplate = new RestTemplate(factory);

            // Interceptor para reforzar cabecera keep-alive
            this.restTemplate.getInterceptors().add((request, body, execution) -> {
                HttpHeaders headers = request.getHeaders();
                headers.setConnection("keep-alive");
                return execution.execute(request, body);
            });

        } catch (Exception e) {
            throw new RuntimeException("Error configurando RestTemplate", e);
        }
    }


    private <T> T retry(int maxRetries, long delayMillis, Callable<T> action) throws Exception {
        Objects.requireNonNull(action, "action must not be null");
        Exception lastException = null;
        //   LOG.info("-------------- Retry Method: ");
        int maxConsecutiveErrors = errorRetry != null ? errorRetry : 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                T result = action.call();
                consecutive500Errors.set(0); // Reset en caso de éxitodate parcia
                return result;
            } catch (HttpStatusCodeException e) {
                String body = e.getResponseBodyAsString();
                HttpStatus status = e.getStatusCode();

                LOG.error("HttpStatusCodeException {} - Body: {}", status.value(), body);
                if (status.is5xxServerError()) {
                    int current = consecutive500Errors.incrementAndGet();
                    if (current >= errorRetry) {
                        LOG.error("Se alcanzó el límite de errores 500 consecutivos: {}", current);
                        throw new ConsecutiveServerErrorLimitReachedException("Servicio XSA caído (500 consecutivos)");
                    }
                } else {
                    consecutive500Errors.set(0);
                }

                // Detectar códigos retryables embebidos en JSON, incluso si es un 400
                boolean isRetryable = (
                        status.is5xxServerError()
                                || status == HttpStatus.GATEWAY_TIMEOUT
                                || containsRetryableCode(body)
                                || containsRetryableErrorObject(body)
                );

                if (isRetryable) {
                    LOG.info("Reintentando por error retryable: {} (intento {}/{})", status, i + 1, maxRetries);
                    lastException = e;
                    Thread.sleep(delayMillis);
                    java.util.concurrent.locks.LockSupport.parkNanos(100_000);
                } else {
                    throw e;
                }
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Contamos todos los I/O errors (incluyendo SSLHandshake), no sólo timeouts
                lastException = e;
                int current = consecutive500Errors.incrementAndGet();
                //    LOG.error("ResourceAccessException count {}/{}: {}", current, errorRetry, e.getMessage());
                if (current >= errorRetry) {
                    throw new ConsecutiveServerErrorLimitReachedException(
                            "Servicio XSA caído (I/O errors consecutivos)"
                    );
                }
                // reintentamos igual que con un 5xx
                Thread.sleep(delayMillis);
            }

        }
        // Si agotamos maxRetries pero NO alcanzamos errorRetry consecutivos, propaga la última excepción
        throw lastException;
    }

    private boolean containsRetryableErrorObject(String body) {

        //   LOG.info("errores: {} ", errorCodes);
        if (errorCodes == null || errorCodes.isEmpty() || body == null || body.isEmpty()) {
            return false;
        }

        try {
            Map<String, List<String>> map = objectMapper.readValue(body, new TypeReference<Map<String, List<String>>>() {
            });
            List<String> errors = map.get("errors");

            if (errors == null || errors.isEmpty()) {
                return false;
            }

            for (String error : errors) {
                for (String code : errorCodes.split("\\|")) {
                    if (error.contains(code.trim())) {
                        LOG.info("Código retryable detectado en errores: {}", code.trim());
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            LOG.warn("No se pudo parsear el cuerpo de error como JSON: {}", e.getMessage());
        }

        return false;
    }


    private boolean containsRetryableCode(String body) {
        if (errorCodes == null || errorCodes.isEmpty()) return false;

        String[] codes = errorCodes.split("\\|");
        for (String code : codes) {
            if (body != null && body.contains(code.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene un token para el login, enviando usuario y password en JSON.
     */
    public String getAccessToken(String authUrl, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Enviar JSON

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    authUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().get("token").toString();
            } else {
                throw new RuntimeException("Error al obtener el token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el token: " + e.getMessage(), e);
        }
    }

    /**
     * Hace un GET ligero (HEAD bajo el capó) a la URL indicada y
     * devuelve true si el servicio responde OK (2xx).
     */
    public boolean pingUrl(String url) {
        Objects.requireNonNull(url, "url must not be null");
        try {
            // Ejecuta un HEAD y recibe un ResponseEntity<Void>
            ResponseEntity<Void> resp = restTemplate.exchange(
                    url,
                    HttpMethod.HEAD,
                    HttpEntity.EMPTY,
                    Void.class
            );
            // Comprueba si es 2xx
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            LOG.warn("Ping a {} falló: {}", url, ex.getMessage());
            return false;
        }
    }

    public <T, R> T executeExternalService(String url,
                                           HttpMethod method,
                                           R request,
                                           ParameterizedTypeReference<T> responseType,
                                           String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<R> entity = new HttpEntity<>(request, headers);

        LOG.info("Llamando al servicio externo: {} [{}]", url, method);
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
        // LOG.info("Respuesta recibida: {}", response.getBody());

        return response.getBody();
    }
    public <T> T executeExternalServiceRest(
            String url,
            HttpMethod method,
            Map<String, ?> queryParams,
            ParameterizedTypeReference<T> responseType,
            String token
    ) {
        // 1) Construir URL con query params (si aplica)
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach((k, v) -> {
                if (v == null) return;
                if (v instanceof Iterable<?>) {
                    for (Object item : (Iterable<?>) v) {
                        if (item != null) builder.queryParam(k, item);
                    }
                } else if (v.getClass().isArray()) {
                    Object[] arr = (Object[]) v;
                    for (Object item : arr) {
                        if (item != null) builder.queryParam(k, item);
                    }
                } else {
                    builder.queryParam(k, v);
                }
            });
        }
        URI uri = builder.build(true).encode(StandardCharsets.UTF_8).toUri();

        // 2) Headers
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
        }
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 3) Entity (sin body)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 4) Exchange
        LOG.info("Invocando servicio externo: {} [{}]", uri, method);
        ResponseEntity<T> response = restTemplate.exchange(uri, method, entity, responseType);

        LOG.info("Respuesta HTTP {} recibida desde {}", response.getStatusCode(), uri);
        return response.getBody();
    }



    public byte[] descargarCfdiFile(String url
    ) {
        // Set headers to accept any binary stream
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        LOG.info("Llamando al servicio externo: {} [{}]", url, HttpMethod.GET.name());

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            LOG.error("Error al descargar cfdi de XSA Status code: " + response.getStatusCode());
            return null;
        }
    }

}
