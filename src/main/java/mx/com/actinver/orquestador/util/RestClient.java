package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import mx.com.actinver.common.exception.ConsecutiveServerErrorLimitReachedException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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
//
//    public StampResponseDto callXsaStamp(String url, HttpMethod method, StampRequestDto request) throws Exception {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Content-Type", "application/json");
//            headers.set("Accept", "application/json");
//
//            String body = objectMapper.writeValueAsString(request);
//            //    LOG.info("Enviando JSON: {}", body);
//
//            HttpEntity<String> entity = new HttpEntity<>(body, headers);
//            // antes de la llamada
//            long start = System.currentTimeMillis();
//            ResponseEntity<String> response = retry(3, 2000, () ->
//                    restTemplate.exchange(url, method, entity, String.class)
//            );
//            long elapsed = System.currentTimeMillis() - start;
//            LOG.info("XSA responded in {} ms for folio {}", elapsed, request.getFolio());
//
//
//            String json = response.getBody();
//            //  LOG.info("Respuesta recibida: {} - Código: {}", json, response.getStatusCodeValue());
//
//            StampResponseDto dto = objectMapper.readValue(json, StampResponseDto.class);
//            dto.setRequest(request);
//            return dto;
//
//        } catch (HttpStatusCodeException e) {
//            String responseBody = e.getResponseBodyAsString();
//            LOG.debug("Error HTTP {}: {}", e.getStatusCode(), responseBody);
//
//            StampResponseDto error = new StampResponseDto();
//            error.setRequest(request);
//
//            try {
//                StampResponseDto parsed = objectMapper.readValue(responseBody, StampResponseDto.class);
//                parsed.setRequest(request);
//                return parsed;
//            } catch (Exception innerParseEx) {
//                List<String> fallbackErrors = new ArrayList<>();
//                try {
//                    Map<String, List<String>> simpleErrorMap = objectMapper.readValue(
//                            responseBody, new TypeReference<Map<String, List<String>>>() {
//                            });
//                    List<String> errors = simpleErrorMap.get("errors");
//                    if (errors != null && !errors.isEmpty()) {
//                        error.setErrors(errors);
//                        error.getErrors().add("Error 500 HTTP");
//                    } else {
//                        fallbackErrors.add("Error 500 HTTP " + e.getStatusCode() + ": " + responseBody);
//                        error.setErrors(fallbackErrors);
//                    }
//                } catch (Exception fallbackEx) {
//
//                    fallbackErrors.add("Error 500 HTTP " + e.getStatusCode() + ": " + responseBody);
//                    error.setErrors(fallbackErrors);
//                }
//                return error;
//            }
//
//        } catch (ConsecutiveServerErrorLimitReachedException e) {
//            throw e; // Propaga el error para que se detenga
//        } catch (ResourceAccessException e) {
//            LOG.error("I/O error en callXsaStamp: {}", e.getMessage(), e);
//            StampResponseDto dto = new StampResponseDto();
//            dto.setRequest(request);
//            dto.setErrors(Collections.singletonList("I/O error: " + e.getMessage()));
//            return dto;
//        } catch (Exception e) {
//            LOG.error("Error inesperado: {}", e.getMessage());
//
//            StampResponseDto error = new StampResponseDto();
//            error.setRequest(request);
//
//            List<String> fallbackErrors = new ArrayList<>();
//            String errorMessage = e.getMessage();
//
//            if (e instanceof HttpStatusCodeException) {
//                HttpStatusCodeException httpEx = (HttpStatusCodeException) e;
//                String responseBody = httpEx.getResponseBodyAsString();
//                try {
//                    Map<String, List<String>> errorMap = objectMapper.readValue(responseBody,
//                            new TypeReference<Map<String, List<String>>>() {
//                            });
//                    List<String> errors = errorMap.get("errors");
//                    if (errors != null && !errors.isEmpty()) {
//                        error.setErrors(errors);
//                        return error;
//                    }
//                } catch (Exception parseEx) {
//                    LOG.warn("No se pudo parsear body en catch final: {}", parseEx.getMessage());
//                    errorMessage = responseBody; // preferimos mostrar el body crudo
//                }
//            }
//
//            fallbackErrors.add("Error 525 missed path: " + errorMessage);
//            error.setErrors(fallbackErrors);
//            return error;
//        }
//
//
//    }
//
//    /**
//     * Este método invoca el endpoint de cancelación (“/cfdis/cancelar”) y
//     * parsea la respuesta en una lista de CancelationResultDto.
//     *
//     * @param url     URL completa del endpoint (ej. https://miXsaHost/.../cfdis/cancelar)
//     * @param method  HttpMethod.POST
//     * @param request Un CancelationXsaRequestDto (contiene motivo + lista de uuids)
//     *
//     * @return Lista de CancelationResultDto con el resultado para cada UUID.
//     */
//    public List<CancelationResultDto> callXsaCancelStamp(
//            String url,
//            HttpMethod method,
//            mx.com.actinver.orquestador.dto.CancelationXsaRequestDto request
//    ) throws Exception {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Content-Type", "application/json");
//            headers.set("Accept", "application/json");
//
//            // Convertimos el DTO a JSON:
//            String body = objectMapper.writeValueAsString(request);
//            HttpEntity<String> entity = new HttpEntity<>(body, headers);
//            LOG.info("-------------- antes de peticion");
//            // Reintentamos la llamada según la lógica de retry:
//            ResponseEntity<String> response = retry(3, 2000, () ->
//                    restTemplate.exchange(url, method, entity, String.class)
//            );
//            LOG.info("Response : {}", response);
//            String json = response.getBody();
//            // La API de cancelación regresa un array de objetos:
//            // [ { "uuid":"...", "status":"202", "descripcion":"EN_PROCESO" }, ... ]
//            return objectMapper.readValue(
//                    json,
//                    new TypeReference<List<CancelationResultDto>>() {
//                    }
//            );
//
//        } catch (HttpStatusCodeException e) {
//            String responseBody = e.getResponseBodyAsString();
//            HttpStatus statusCode = e.getStatusCode();
//            LOG.error("Error HTTP {} en cancelación: {}", statusCode.value(), responseBody);
//
//            // Intentamos parsear como List<CancelationResultDto> si el servicio lo devolvió así:
//            try {
//                return objectMapper.readValue(
//                        responseBody,
//                        new TypeReference<List<CancelationResultDto>>() {
//                        }
//                );
//            } catch (Exception parseEx) {
//                LOG.error("-----parseEx: {}", parseEx.getMessage(), parseEx);
//                // Si no se pudo parsear, armamos un fallback con un solo item de error:
//                CancelationResultDto fallback = CancelationResultDto.builder()
//                        .uuid("N/A")
//                        .status(String.valueOf(statusCode.value()))
//                        .descripcion("ERROR NO PARSEABLE: " + responseBody)
//                        .build();
//                return Collections.singletonList(fallback);
//            }
//
//        } catch (ConsecutiveServerErrorLimitReachedException e) {
//            LOG.error("-----e: {}", e.getMessage(), e);
//            throw e; // Propagar para que el flujo principal lo capture
//        } catch (IOException e) {
//            LOG.error("IOException en cancelación: {}", e.getMessage());
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            LOG.error("Error inesperado en cancelación: {}", e.getMessage(), e);
//            CancelationResultDto fallback = CancelationResultDto.builder()
//                    .uuid("N/A")
//                    .status("500")
//                    .descripcion("ERROR INESPERADO: " + e.getLocalizedMessage())
//                    .build();
//            return Collections.singletonList(fallback);
//        }
//    }

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

}
