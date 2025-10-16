package mx.com.actinver.orquestador.ws.util;

import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BypassRouter {

    private  LocalDate corteHistorico;   // p.ej. 2022-12-31
    public  enum Decision { BYPASS, LOCAL }



    /** Decide para cada operación.  Para las demás ⇒ BYPASS. */
    public Decision decide(String operationLocalPart,
                           String llave) {
//        if ("ContestaExpedientexLlave".equals(operationLocalPart)) {
//            LocalDate periodo = extraerFechaPeriodo(llave);
//            if (periodo == null) return Decision.BYPASS;    // si no hay fecha, asumimos histórico
//            return periodo.isAfter(corteHistorico) ? Decision.LOCAL : Decision.BYPASS;
//        }
        return Decision.BYPASS; // ObtenLogin, ContestaFileHSM… siempre bypass por ahora
    }

    //–– helpers ––---------------------------------------------------------
    private static LocalDate extraerFechaPeriodo(ClsLlaveExpediente llave) {
//        if (llave == null || llave.getCampos() == null) return null;
//        for (ClsLlaveCampo c : llave.getCampos().getClsLlaveCampo()) {
//            if ("PERIODO".equalsIgnoreCase(c.getNombre())) {           // adapta el nombre real
//                try {
//                    return LocalDate.parse(c.getValor(), DateTimeFormatter.ofPattern("yyyyMMdd"));
//                } catch (Exception ignored) { }
//            }
//        }
        return null;
    }
}
