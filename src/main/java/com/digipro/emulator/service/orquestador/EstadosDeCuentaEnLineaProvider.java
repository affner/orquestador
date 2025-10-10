package com.digipro.emulator.service.orquestador;

import com.digipro.emulator.database.DatabaseManager;
import com.digipro.emulator.ws.generated.ArrayOfClsFileHSM;
import com.digipro.emulator.ws.generated.ClsFileHSM;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class EstadosDeCuentaEnLineaProvider {

    private final DatabaseManager db;

    public EstadosDeCuentaEnLineaProvider(DatabaseManager db) {
        this.db = db;
    }

    /** Retorna null si falla y routing.fallback.habilitado=true (para que el orquestador caiga a HISTÓRICO). */
    public ArrayOfClsFileHSM obtenerSimulado(String llave, int anio, int mes, String contrato) throws Exception {
        String pdfPath = db.getProperty("estadoscuenta.simulado.pdf");

        // DatabaseManager.getProperty solo acepta 1 parámetro → calculamos fallback manualmente
        String fb = db.getProperty("routing.fallback.habilitado");
        boolean fallback = (fb == null || fb.isBlank()) ? true : Boolean.parseBoolean(fb.trim());

        try {
            if (pdfPath == null || pdfPath.isBlank()) {
                throw new IllegalStateException("estadoscuenta.simulado.pdf no configurado");
            }
            Path p = Paths.get(pdfPath);
            byte[] bytes = Files.readAllBytes(p);

            ClsFileHSM doc = new ClsFileHSM();

            // Algunos stubs piden long/Long: usar 0L
            try {
                doc.getClass().getMethod("setDocID", long.class).invoke(doc, 0L);
            } catch (Throwable t) {
                try {
                    doc.getClass().getMethod("setDocID", Long.class).invoke(doc, Long.valueOf(0L));
                } catch (Throwable ignore) { /* no-op */ }
            }

            // Setters tolerantes para variaciones del stub
            HsmSetterUtil.setNombreArchivo(doc, String.format("EdoCta_%d%02d_%s.pdf", anio, mes, contrato));
            HsmSetterUtil.setArchivo(doc, bytes);
            HsmSetterUtil.setExt(doc, "pdf");
            HsmSetterUtil.setLlave(doc, llave);
            HsmSetterUtil.setTipoDocId(doc, 1); // ajusta si tu catálogo usa otro ID

            ArrayOfClsFileHSM arr = new ArrayOfClsFileHSM();
            arr.getClsFileHSM().add(doc);
            return arr;

        } catch (Exception ex) {
            if (fallback) {
                System.out.println("[ORQUESTADOR] Falla simulado → Fallback HISTÓRICO IDPORTAL: " + ex.getMessage());
                return null;
            }
            throw ex;
        }
    }
}
