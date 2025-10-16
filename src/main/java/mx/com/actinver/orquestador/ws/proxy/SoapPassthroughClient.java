package mx.com.actinver.orquestador.ws.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**  POST-ea el XML tal-cual y devuelve EXACTAMENTE el XML que respondió el servidor remoto. */
@Component
public class SoapPassthroughClient {

    private final RestTemplate rt;

    @Autowired
    public SoapPassthroughClient(RestTemplateBuilder b) {

        this.rt = b.setConnectTimeout(java.time.Duration.ofSeconds(5))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }

    public String forward(String url, String rawSoap) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.TEXT_XML);             //  text/xml  a //  SOAP 1.1
        h.setAccept(Collections.singletonList(MediaType.TEXT_XML));

        HttpEntity<String> req = new HttpEntity<>(rawSoap, h);
        ResponseEntity<String> resp = rt.exchange(url,
                HttpMethod.POST,
                req,
                String.class);
        return resp.getBody();  //  XML
    }
}
