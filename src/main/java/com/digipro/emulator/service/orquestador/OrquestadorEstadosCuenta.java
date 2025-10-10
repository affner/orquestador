package com.digipro.emulator.service.orquestador;

import com.digipro.emulator.adapter.FileSystemAdapter;
import com.digipro.emulator.database.DatabaseManager;
import com.digipro.emulator.database.DocumentoDAO;
import com.digipro.emulator.ws.generated.ArrayOfClsFileHSM;

/**
 * Orquestador FASE 1
 *
 * - La LLAVE es MMYYYYNNCONTRATO (ej. 10202501456).
 *   También aceptamos mes sin cero a la izquierda (ej. 2202501456 = 02/2025).
 *
 * - Se parsea la llave para obtener MES y AÑO.
 * - Se calcula un número interno en formato MMYYYY (como entero) para comparar:
 *      periodoLlaveMMYYYY = mes(1..12) * 10000 + anio(yyyy)
 *      ej. 10/2025 -> 10*10000 + 2025 = 102025
 *          02/2025 -> 2*10000 + 2025 = 22025   (equivale a "022025" sin el cero)
 *
 * - El corte en database.properties (routing.fecha.corte) puede venir como:
 *      "102025" (MMYYYY)  ó  "202510" (YYYYMM)
 *   Se normaliza a MMYYYY (entero) antes de comparar.
 *
 * Decisión:
 *   - Si periodoLlaveMMYYYY >= periodoCorteMMYYYY → ESTADOS DE CUENTA EN LÍNEA ( Fase 1).
 *   - Si el simulado falla y fallback=true → HISTÓRICO IDPORTAL.
 *   - Si periodoLlaveMMYYYY < periodoCorteMMYYYY → HISTÓRICO IDPORTAL.
 */
public class OrquestadorEstadosCuenta {

    private final DatabaseManager db;
    private final EstadosDeCuentaEnLineaProvider onlineProvider;
    private final HistoricoIDPortalProvider historicoProvider;

    public OrquestadorEstadosCuenta(DatabaseManager db,
                                    EstadosDeCuentaEnLineaProvider onlineProvider,
                                    HistoricoIDPortalProvider historicoProvider) {
        this.db = db;
        this.onlineProvider = onlineProvider;
        this.historicoProvider = historicoProvider;
    }

    /** Factory con dependencias por defecto del proyecto. */
    public static OrquestadorEstadosCuenta createDefault(DocumentoDAO documentoDAO, FileSystemAdapter fs) {
        DatabaseManager db = DatabaseManager.getInstance();
        return new OrquestadorEstadosCuenta(
                db,
                new EstadosDeCuentaEnLineaProvider(db),
                new HistoricoIDPortalProvider(documentoDAO, fs)
        );
    }

    /** Decide la fuente según el periodo MMYYYY derivado de la llave (sin modificar la llave ni el WSDL). */
    public ArrayOfClsFileHSM resolver(String llave, int tipoDocID) throws Exception {
        if (llave == null || llave.isBlank()) {
            return historicoProvider.obtener(llave, tipoDocID);
        }

        // 1) Parseo robusto: acepta mes 1 o 2 dígitos al inicio (ej. 2/2025 o 02/2025)
        ParserLlaveExpediente.Llave k = ParserLlaveExpediente.parseFlexible(llave);

        // 2) MMYYYY interno (como entero) para comparar: mes*10000 + anio
        int periodoLlaveMMYYYY = toPeriodoMMYYYY(k.mes, k.anio);

        // 3) Lee y normaliza el corte desde properties a MMYYYY (entero)
        int periodoCorteMMYYYY = parseCorteToMMYYYY(db.getProperty("routing.fecha.corte"), /*def*/ 102025);

        // Log informativo (no afecta contrato)
        System.out.println("[ORQUESTADOR] llave=" + llave
                + " → mes=" + k.mes + ", anio=" + k.anio
                + " | periodoLlaveMMYYYY=" + periodoLlaveMMYYYY
                + " | periodoCorteMMYYYY=" + periodoCorteMMYYYY);

        // 4) Enrutamiento
        if (periodoLlaveMMYYYY >= periodoCorteMMYYYY) {
            System.out.println("[ORQUESTADOR] Fuente: RECIENTE (ESTADOS DE CUENTA EN LÍNEA)");
            ArrayOfClsFileHSM sim = onlineProvider.obtenerSimulado(llave, k.anio, k.mes, k.contrato);
            if (sim != null && !sim.getClsFileHSM().isEmpty()) {
                return sim; // Simulado OK
            }
            System.out.println("[ORQUESTADOR] Fallback → Fuente: HISTÓRICO IDPORTAL");
            return historicoProvider.obtener(llave, tipoDocID);
        } else {
            System.out.println("[ORQUESTADOR] Fuente: HISTÓRICO IDPORTAL");
            return historicoProvider.obtener(llave, tipoDocID);
        }
    }



    /** Convierte (mes, año) a entero MMYYYY (ej. 10/2025 -> 102025 ; 2/2025 -> 22025). */
    private static int toPeriodoMMYYYY(int mes, int anio) {
        return (mes * 10000) + anio;
    }

    /**
     * Normaliza un texto de corte a entero MMYYYY.
     * Acepta:
     *   - "MMYYYY" (p.ej. 102025) -> 102025
     *   - "YYYYMM" (p.ej. 202510) -> 102025
     * Si no reconoce el formato, usa 'def'.
     */
    private static int parseCorteToMMYYYY(String prop, int def) {
        try {
            if (prop == null) return def;
            String v = prop.trim();
            if (v.length() == 6) {
                // Intento como MMYYYY
                int mm = Integer.parseInt(v.substring(0, 2));
                int yyyy = Integer.parseInt(v.substring(2, 6));
                if (isValidYearMonth(yyyy, mm)) return toPeriodoMMYYYY(mm, yyyy);

                // Intento como YYYYMM
                int yyyy2 = Integer.parseInt(v.substring(0, 4));
                int mm2 = Integer.parseInt(v.substring(4, 6));
                if (isValidYearMonth(yyyy2, mm2)) return toPeriodoMMYYYY(mm2, yyyy2);
            }

            // Fallback: si viene como entero "crudo" (ej. 22025 para 02/2025), úsalo
            return Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean isValidYearMonth(int year, int month) {
        return (year >= 1900 && year <= 2100) && (month >= 1 && month <= 12);
    }
}
