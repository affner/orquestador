package com.digipro.emulator.database;

public class UsuarioDAO {

    private final DatabaseManager db;

    public UsuarioDAO() {
        this.db = DatabaseManager.getInstance();
    }

    /**
     * Valida usuario y contraseña.
     * Contraseña en TEXTO PLANO (sin conversión).
     */
    public UsuarioDTO validar(String username, String password) {
        final String sql =
                "SELECT id_usuario, username, nombre_completo, email, rol, activo, " +
                        "grupo_admin_id, cliente_id, perfil_usuario_id, no_identidad " +
                        "FROM usuarios " +
                        "WHERE username = ? AND password = ? AND activo = true";

        try (var cn = db.getConnection();
             var ps = cn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    UsuarioDTO u = new UsuarioDTO();
                    u.idUsuario         = rs.getInt("id_usuario");
                    u.username          = rs.getString("username");
                    u.nombreCompleto    = rs.getString("nombre_completo");
                    u.email             = rs.getString("email");
                    u.rol               = rs.getString("rol");
                    u.activo            = rs.getBoolean("activo");
                    u.grupoAdminId      = rs.getInt("grupo_admin_id");
                    u.clienteId         = rs.getInt("cliente_id");
                    u.perfilUsuarioId   = rs.getInt("perfil_usuario_id");
                    u.noIdentidad       = rs.getInt("no_identidad");
                    return u;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error validando usuario", e);
        }
        return null;
    }
}