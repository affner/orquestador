package com.digipro.emulator.database;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para acceso a los documentos y archivos físicos asociados.
 */
public class DocumentoDAO {

    private final DatabaseManager db;

    public DocumentoDAO() {
        this.db = DatabaseManager.getInstance();
    }

    // ===================== MAPEO A DTO =====================

    private static DocumentoDTO map(ResultSet rs) throws Exception {
        DocumentoDTO d = new DocumentoDTO();
        d.idDocumento   = rs.getInt("id_documento");
        d.negocio       = rs.getString("negocio");
        d.proyectoId    = rs.getInt("proyecto_id");
        d.tipoDocId     = rs.getInt("tipo_doc_id");
        d.llaveBusqueda = rs.getString("llave_busqueda");
        d.periodo       = rs.getString("periodo");
        d.contrato      = rs.getString("contrato");  // ← CORREGIDO: getString
        d.descripcion   = rs.getString("descripcion");

        if (rs.getDate("fecha_documento") != null) {
            d.fechaDocumento = rs.getDate("fecha_documento").toLocalDate();
        }

        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) {
            d.fechaCreacion = new java.util.Date(fc.getTime());
        }

        d.idArchivo     = rs.getInt("id_archivo");
        d.rutaRelativa  = rs.getString("ruta_relativa");
        d.nombreArchivo = rs.getString("nombre_archivo");
        d.extension     = rs.getString("extension");
        d.tamanoBytes   = rs.getLong("tamano_bytes");
        d.consecutivo   = 1; // Se actualizará en el servicio según el orden
        return d;
    }

    // ===================== CONSULTAS =====================

    /**
     * Busca TODOS los documentos por llave (retorna lista).
     * Se utiliza cuando TipoDocID = 0 (todos los documentos).
     */
    public List<DocumentoDTO> findByLlave(String llave) {
        final String sql =
                "SELECT d.*, a.ruta_relativa, a.nombre_archivo, a.extension, a.tamano_bytes, a.fecha_creacion " +
                        "FROM documentos d " +
                        "JOIN archivos_fisicos a ON a.id_archivo = d.id_archivo " +
                        "WHERE d.llave_busqueda = ? " +
                        "ORDER BY d.tipo_doc_id, d.id_documento";

        List<DocumentoDTO> resultados = new ArrayList<>();
        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {

            ps.setString(1, llave);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando documentos por llave", e);
        }
        return resultados;
    }

    /**
     * Busca documentos por llave Y tipo de documento específico.
     * Se utiliza cuando TipoDocID = 1, 2, 3, etc. (tipo específico).
     */
    public List<DocumentoDTO> findByLlaveYTipo(String llave, int tipoDocID) {
        final String sql =
                "SELECT d.*, a.ruta_relativa, a.nombre_archivo, a.extension, a.tamano_bytes, a.fecha_creacion " +
                        "FROM documentos d " +
                        "JOIN archivos_fisicos a ON a.id_archivo = d.id_archivo " +
                        "WHERE d.llave_busqueda = ? AND d.tipo_doc_id = ? " +
                        "ORDER BY d.id_documento";

        List<DocumentoDTO> resultados = new ArrayList<>();
        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {

            ps.setString(1, llave);
            ps.setInt(2, tipoDocID);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando documentos por llave y tipo", e);
        }
        return resultados;
    }

    /**
     * Busca un documento por su ID (clave primaria).
     * Se utiliza para ContestaFileHSM.
     */
    public DocumentoDTO findByIdDocumento(long idDocumento) {
        final String sql =
                "SELECT d.*, a.ruta_relativa, a.nombre_archivo, a.extension, a.tamano_bytes, a.fecha_creacion " +
                        "FROM documentos d " +
                        "JOIN archivos_fisicos a ON a.id_archivo = d.id_archivo " +
                        "WHERE d.id_documento = ?";

        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {

            ps.setLong(1, idDocumento);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando documento por ID", e);
        }
        return null;
    }
}