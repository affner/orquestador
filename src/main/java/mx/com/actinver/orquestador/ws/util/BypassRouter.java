package mx.com.actinver.orquestador.ws.util;

import mx.com.actinver.orquestador.ws.Decision;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveCampo;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Router que decide si usar LEGACY o MODERN según la llave del expediente.
 */
@Component
public class BypassRouter {

    private static final Logger LOG = LogManager.getLogger(BypassRouter.class);

    /** Decide si una operación SOAP se atiende localmente o via bypass (legacy). */
    public Decision decide(String operationLocalPart, ClsLlaveExpediente llave, ChronoLocalDate corteHistorico) {
        LOG.info("operationLocalPart: {}", operationLocalPart);
        LOG.info("llave: {}", llave);
        if (llave == null || llave.getCampos() == null) {
            return Decision.LEGACY;
        }

        // Login siempre en nuevo sistema (interno)
        if ("ObtenLogin".equals(operationLocalPart)) {
            return Decision.LEGACY;
        }
        // ContestaExpedientexLlave: determinar según fecha en la llave
        if ("ContestaExpedientexLlave".equals(operationLocalPart)) {
            LocalDate periodo = extraerFechaPeriodo(llave);
            LOG.info("periodoLcldate: {}", periodo);
            if (periodo == null) {
                // Si no se pudo obtener periodo, asumimos histórico
                return Decision.LEGACY;
            }
            // Si el periodo de la llave es >= corteHistorico, es consulta "reciente" (moderna)
            // Caso contrario, se considera histórica.
            if (!periodo.isBefore(corteHistorico)) {
                return Decision.MODERN;  // a partir de la fecha de corte -> sistema nuevo
            } else {
                return Decision.LEGACY; // anterior a la fecha de corte -> legacy
            }
        }
        LOG.info("Otra Operacion");
        // Otras operaciones (incluyendo ContestaFileHSM por ahora) -> legacy
        return Decision.LEGACY;
    }

    /**
     * Extrae la fecha de periodo (tipo LocalDate) de la llave del expediente.
     * Regla: la llave comienza con mes (1 o 2 dígitos) seguido por año (4 dígitos).
     * Si no puede parsearse, devuelve null.
     */
    private static LocalDate extraerFechaPeriodo(ClsLlaveExpediente llave) {
        if (llave == null || llave.getCampos() == null) return null;

        LOG.info("llave: {}", llave);
        int idx = 0;
        for (ClsLlaveCampo campo : llave.getCampos()) {
            LOG.info("campo[{}]: {}", idx++, campo);

            if (campo == null) continue;

            String nombre = campo.getCampo();
            String valor = campo.getValor();

            LOG.info("campo.nombre='{}' campo.valor='{}'", nombre, valor);

            // Según tu XML actual el campo de interés es "Llave"
            if (nombre != null && "Llave".equalsIgnoreCase(nombre.trim())) {
                if (valor == null || valor.trim().isEmpty()) {
                    LOG.warn("Campo 'Llave' presente pero vacío");
                    return null;
                }
                try {
                    // parse flexible: mes 1-2 dígitos + año 4 dígitos
                    LocalDate fecha = parseMesAnioDesdeLlave(valor.trim());
                    if (fecha != null) {
                        LOG.info("Llave parseada correctamente -> {}", fecha);
                        return fecha;
                    } else {
                        LOG.error("No se pudo derivar mes/año desde la llave '{}'", valor);
                        return null;
                    }
                } catch (Exception e) {
                    LOG.error("extraerFechaPeriodo excepcion: {}______{}", e.getMessage(), e);
                    return null;
                }
            }
        }
        LOG.error("No se encontró campo 'Llave' en la llave del expediente");
        return null;
    }

    /**
     * Intenta extraer (mes, año) desde el inicio de la llave.
     * Devuelve LocalDate con dia=1 del mes extraido, o null si no pudo.
     *
     * Lógica:
     *  - Sanea el string para quedarse solo con dígitos.
     *  - Intenta interpretar los primeros 2 dígitos como mes y los siguientes 4 como año.
     *    Si el mes (2 dígitos) está fuera de rango (1..12) entonces intenta usar 1 dígito
     *    como mes (primer dígito) y los siguientes 4 como año.
     */
    private static LocalDate parseMesAnioDesdeLlave(String raw) {
        if (raw == null) return null;
        // conservar sólo dígitos (por si entra con guiones/espacios)
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < 5) {
            LOG.info("Valor de llave demasiado corto después de sanear: '{}'", digits);
            return null;
        }

        // Intento 1: dos dígitos para mes + cuatro dígitos para año
        try {
            if (digits.length() >= 6) {
                int mes2 = Integer.parseInt(digits.substring(0, 2));
                int anio2 = Integer.parseInt(digits.substring(2, 6));
                if (isValidYearMonth(anio2, mes2)) {
                    return LocalDate.of(anio2, mes2, 1);
                }
            }
        } catch (NumberFormatException ignored) {
            // seguir a siguiente intento
        }

        // Intento 2: un dígito para mes + cuatro dígitos para año (mes 1..9)
        try {
            int mes1 = Integer.parseInt(digits.substring(0, 1));
            int anio1 = Integer.parseInt(digits.substring(1, 5));
            if (isValidYearMonth(anio1, mes1)) {
                return LocalDate.of(anio1, mes1, 1);
            }
        } catch (Exception ignored) {
            LOG.error("error al obtener las fechas: {}", ignored.getMessage());
        }

        // No pudimos parsear
        LOG.info("parseMesAnioDesdeLlave no pudo parsear '{}' -> digits='{}'", raw, digits);
        return null;
    }

    private static boolean isValidYearMonth(int year, int month) {
        return (year >= 1900 && year <= 2100) && (month >= 1 && month <= 12);
    }
}
