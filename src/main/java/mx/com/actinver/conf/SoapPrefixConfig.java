package mx.com.actinver.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import javax.xml.soap.*;

@Configuration
public class SoapPrefixConfig {

    /**  Bean “oficial” que Spring-WS usará (id = messageFactory, @Primary). */
    @Bean(name = "messageFactory")
    @Primary
    public SaajSoapMessageFactory prefixedFactory() throws SOAPException {

        MessageFactory base = MessageFactory
                .newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

        return new SaajSoapMessageFactory(base) {

            @Override
            public SaajSoapMessage createWebServiceMessage() {
                SaajSoapMessage msg = super.createWebServiceMessage();
                try {
                    SOAPEnvelope env = msg.getSaajMessage()
                            .getSOAPPart()
                            .getEnvelope();

                    // prefijo soap
                    env.setPrefix("soap");
                    env.getBody().setPrefix("soap");


                    SOAPHeader hdr = env.getHeader();
                    if (hdr != null) {
                        if (hdr.hasChildNodes()) {
                            hdr.setPrefix("soap");            // hay datos
                        } else {
                            hdr.detachNode();                 // estaba vacío
                        }
                    }
                } catch (SOAPException ignored) { }
                return msg;
            }
        };
    }

}
