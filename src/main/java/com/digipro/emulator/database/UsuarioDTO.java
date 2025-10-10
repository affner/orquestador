package com.digipro.emulator.database;

public class UsuarioDTO {
    public Integer idUsuario;
    public String  username;
    public String  nombreCompleto;
    public String  email;
    public String  rol;
    public boolean activo;

    // Campos adicionales para el ticket
    public Integer grupoAdminId;
    public Integer clienteId;
    public Integer perfilUsuarioId;
    public Integer noIdentidad;
}