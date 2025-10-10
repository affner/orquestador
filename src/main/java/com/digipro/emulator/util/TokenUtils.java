package com.digipro.emulator.util;

import com.digipro.emulator.ws.usuarios.IDTicket;

import java.util.Base64;

public class TokenUtils {

    /** Devuelve el token (Base64 de los 20 bytes) almacenado en 'tickets.id_ticket' */
    public static String tokenFrom(IDTicket t) {
        if (t == null || t.getTicketID() == null || t.getTicketID().length == 0) return null;
        return Base64.getEncoder().encodeToString(t.getTicketID());
    }

    /** Verificación simple: esperamos 20 bytes => Base64 ~ 28 chars */
    public static boolean isValidFormat(IDTicket t) {
        return t != null && t.getTicketID() != null && t.getTicketID().length == 20;
    }
}
