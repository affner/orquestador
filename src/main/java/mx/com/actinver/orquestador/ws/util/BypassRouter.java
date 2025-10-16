package mx.com.actinver.orquestador.ws.util;

import mx.com.actinver.orquestador.ws.generated.ClsLlaveCampo;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class BypassRouter {
    private final LocalDate corteHistorico = LocalDate.of(2025, 10, 15);  // Fecha de corte (15/10/2025)

    public enum Decision {BYPASS, LOCAL}

    /** Decide si una operación SOAP se atiende localmente o via bypass (legacy). */
    public Decision decide(String operationLocalPart, ClsLlaveExpediente llave) {

        if (llave == null || llave.getCampos() == null) {
            return Decision.BYPASS;
        }

        // Login siempre en nuevo sistema (interno)
        if ("ObtenLogin".equals(operationLocalPart)) {
            return Decision.LOCAL;
        }
        // ContestaExpedientexLlave: determinar según fecha en la llave
        if ("ContestaExpedientexLlave".equals(operationLocalPart)) {
            LocalDate periodo = extraerFechaPeriodo(llave);
            if (periodo == null) {
                // Si no se pudo obtener periodo, asumimos histórico
                return Decision.BYPASS;
            }
            // Si el periodo de la llave es >= corteHistorico, es consulta "reciente" (moderna)
            // Caso contrario, se considera histórica.
            if (!periodo.isBefore(corteHistorico)) {
                return Decision.LOCAL;  // a partir de la fecha de corte -> sistema nuevo
            } else {
                return Decision.BYPASS; // anterior a la fecha de corte -> legacy
            }
        }
        // Otras operaciones (incluyendo ContestaFileHSM por ahora) -> legacy
        return Decision.BYPASS;
    }

    /** Extrae la fecha de periodo (tipo LocalDate) de la llave del expediente. */
    private static LocalDate extraerFechaPeriodo(ClsLlaveExpediente llave) {
        if (llave == null || llave.getCampos() == null) return null;
        // Buscar un campo de nombre "PERIODO" dentro de la llave
        for (ClsLlaveCampo campo : llave.getCampos()) {
            if ("PERIODO".equalsIgnoreCase(campo.getNombre())) {
                try {
                    // Se espera el valor en formato YYYYMMDD según convenio
                    return LocalDate.parse(campo.getValor(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                } catch (Exception e) {
                    // Valor de fecha no parseable
                    return null;
                }
            }
        }
        return null;  // No se encontró campo PERIODO
    }

}
