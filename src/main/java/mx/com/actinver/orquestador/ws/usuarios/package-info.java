@javax.xml.bind.annotation.XmlSchema(
        namespace = "http://Digipro.servicios/WsImagenes/WsImagenes",   // << default SIN prefijo
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
        xmlns = {
                @javax.xml.bind.annotation.XmlNs(                // prefijo ns2 para WsUsuarios
                        prefix = "ns2",
                        namespaceURI = "http://Digipro.servicios/WsUsuarios/WsUsuarios")
        }
)
package mx.com.actinver.orquestador.ws.usuarios;
