package com.digipro.emulator.database;

import java.sql.Timestamp;
import java.time.Instant;

public class LogAccesoDAO {

    private final DatabaseManager db;

    public LogAccesoDAO() {
        this.db = DatabaseManager.getInstance();
    }

    /**
     * Inserta un registro en log_accesos (no lanza excepción para no romper el flujo del WS).
     *
     * @param idTicket      token base64 del ticket o null
     * @param idDocumento   FK a documentos.id_documento o null
     * @param operacion     nombre de la operación (ObtenLogin, ContestaExpedientexLlave, ContestaFileHSM)
     * @param llaveBusqueda llave usada en la búsqueda (puede ser null)
     * @param ipOrigen      IP del cliente (puede ser null)
     * @param exitoso       true si la operación fue exitosa
     * @param mensajeError  detalle de error o null
     */
    public void registrar(String idTicket,
                          Integer idDocumento,
                          String operacion,
                          String llaveBusqueda,
                          String ipOrigen,
                          boolean exitoso,
                          String mensajeError) {

        final String sql =
                "INSERT INTO log_accesos (" +
                        " id_ticket, id_documento, operacion, llave_busqueda, fecha_hora, " +
                        " ip_origen, exitoso, mensaje_error" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {

            // 1: id_ticket
            if (idTicket != null) ps.setString(1, idTicket); else ps.setNull(1, java.sql.Types.VARCHAR);

            // 2: id_documento
            if (idDocumento != null) ps.setInt(2, idDocumento); else ps.setNull(2, java.sql.Types.INTEGER);

            // 3: operacion
            ps.setString(3, operacion);

            // 4: llave_busqueda
            if (llaveBusqueda != null) ps.setString(4, llaveBusqueda); else ps.setNull(4, java.sql.Types.VARCHAR);

            // 5: fecha_hora
            ps.setTimestamp(5, Timestamp.from(Instant.now()));

            // 6: ip_origen
            if (ipOrigen != null) ps.setString(6, ipOrigen); else ps.setNull(6, java.sql.Types.VARCHAR);

            // 7: exitoso
            ps.setBoolean(7, exitoso);

            // 8: mensaje_error
            if (mensajeError != null) ps.setString(8, mensajeError); else ps.setNull(8, java.sql.Types.VARCHAR);

            ps.executeUpdate();

        } catch (Exception e) {
            // No detener el flujo del servicio por un fallo de logging
            System.out.println("[LogAccesoDAO] No se pudo registrar log: " + e.getMessage());
        }
    }
}
