package com.digipro.emulator.service.orquestador;

import com.digipro.emulator.adapter.FileSystemAdapter;
import com.digipro.emulator.database.DocumentoDAO;
import com.digipro.emulator.database.DocumentoDTO;
import com.digipro.emulator.ws.generated.ArrayOfClsFileHSM;
import com.digipro.emulator.ws.generated.ClsFileHSM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula tu lógica ACTUAL (DAO + lectura de archivos).
 * En Fase 2 sustituir por la consulta oficial a ID Portal histórico.
 */
public class HistoricoIDPortalProvider {

    private final DocumentoDAO documentoDAO;
    private final FileSystemAdapter fileAdapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HistoricoIDPortalProvider(DocumentoDAO dao, FileSystemAdapter fs) {
        this.documentoDAO = dao;
        this.fileAdapter = fs;
    }

    public ArrayOfClsFileHSM obtener(String llave, int tipoDocID) {
        List<DocumentoDTO> docs = (tipoDocID <= 0)
                ? documentoDAO.findByLlave(llave)
                : documentoDAO.findByLlaveYTipo(llave, tipoDocID);

        ArrayOfClsFileHSM arr = new ArrayOfClsFileHSM();
        if (docs == null || docs.isEmpty()) return arr;

        int consecutivo = 1;
        for (DocumentoDTO d : new ArrayList<>(docs)) {
            try {
                byte[] contenido = fileAdapter.readFile(d.rutaRelativa);
                if (contenido == null) continue;

                d.consecutivo = consecutivo++;

                ClsFileHSM f = new ClsFileHSM();
                f.setDocID(d.idDocumento != null ? d.idDocumento.longValue() : 0L);

                // setters tolerantes (según stub generado):
                HsmSetterUtil.setArchivo(f, contenido);
                HsmSetterUtil.setLlave(f, d.llaveBusqueda);
                HsmSetterUtil.setTipoDocId(f, d.tipoDocId != null ? d.tipoDocId : 0);

                String ext = d.extension;
                if (ext != null && !ext.isEmpty() && !ext.startsWith(".")) ext = "." + ext;
                String extFinal = (ext == null ? "" : ext.toUpperCase());
                HsmSetterUtil.setExt(f, extFinal);

                if (d.fechaCreacion != null) {
                    try {
                        f.getClass().getMethod("setFechaDigitalizacion", String.class)
                                .invoke(f, sdf.format(d.fechaCreacion));
                    } catch (Throwable ignore) {}
                }
                HsmSetterUtil.setNombreArchivo(f, d.getNombreArchivo());

                // consecutivo (opcional si existe el setter)
                try { f.getClass().getMethod("setConsecutivo", int.class).invoke(f, d.getConsecutivo()); } catch (Throwable ignore) {}

                arr.getClsFileHSM().add(f);

            } catch (Exception ex) {
                System.err.println("[HistoricoIDPortal] No se pudo leer " + d.rutaRelativa + " -> " + ex.getMessage());
            }
        }
        return arr;
    }
}
