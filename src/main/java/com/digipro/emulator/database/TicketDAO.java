package com.digipro.emulator.database;

import java.sql.Timestamp;
import java.time.Instant;

public class TicketDAO {

    private final DatabaseManager db;

    public TicketDAO() {
        this.db = DatabaseManager.getInstance();
    }

    /**
     * Crea un registro de ticket vigente.
     * @param tokenBase64 Base64 de los bytes del TicketID (20 bytes).
     * @param idUsuario   FK a usuarios.id_usuario.
     * @param ip          IP de origen.
     * @param segundosVida Segundos de vigencia a partir de ahora.
     */
    public void crearTicket(String tokenBase64, int idUsuario, String ip, int segundosVida) {
        final String sql =
                "INSERT INTO tickets (id_ticket, id_usuario, fecha_creacion, fecha_expiracion, ip_origen, activo) " +
                        "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, true)";

        Instant exp = Instant.now().plusSeconds(segundosVida);
        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {
            ps.setString(1, tokenBase64);
            ps.setInt(2, idUsuario);
            ps.setTimestamp(3, Timestamp.from(exp));
            ps.setString(4, ip);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error creando ticket", e);
        }
    }

    /**
     * Valida que el ticket esté activo y no expirado.
     * @param tokenBase64 Base64 del TicketID (lo que guardamos en tickets.id_ticket).
     */
    public boolean validarVigente(String tokenBase64) {
        final String sql =
                "SELECT 1 " +
                        "  FROM tickets " +
                        " WHERE id_ticket = ? " +
                        "   AND activo = true " +
                        "   AND fecha_expiracion > CURRENT_TIMESTAMP";

        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {
            ps.setString(1, tokenBase64);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            // No propagues excepción en validación; devuelve false
            return false;
        }
    }

    /**
     * Opcional: desactiva un ticket (logout).
     */
    public void desactivar(String tokenBase64) {
        final String sql = "UPDATE tickets SET activo = false WHERE id_ticket = ?";
        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {
            ps.setString(1, tokenBase64);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[TicketDAO] No se pudo desactivar ticket: " + e.getMessage());
        }
    }
}
