package com.digipro.emulator.util;

import com.digipro.emulator.ws.usuarios.Respuesta;

import java.lang.reflect.Method;
import java.util.Objects;

public class ResponseBuilder {


    public static Respuesta ok(String codigo, String mensaje) {
        return buildInfo(codigo, "Información", mensaje != null ? mensaje : "");
    }

    public static Respuesta ok(String codigo, String mensaje, String detalle) {
        return buildInfo(codigo, mensaje != null ? mensaje : "Información", detalle != null ? detalle : "");
    }

    public static Respuesta error(String codigo, String mensaje, String detalle) {
        return buildError(codigo != null ? codigo : "500",
                mensaje != null ? mensaje : "ERROR",
                detalle);
    }

     /**
     * Respuesta exitosa de autenticación (código 0)
     * Compatible 1:1 con QA
     */
    public static Respuesta authOk() {
        Respuesta r = new Respuesta();

        setIfExists(r, "setRespuestaID", String.class, "0");
        setIfExists(r, "setCategoria", String.class, "4000");
        setIfExists(r, "setDescripcionCategoria", String.class, "Información");
        setIfExists(r, "setDescripcionRespuesta", String.class, "(0). ");
        setIfExists(r, "setRespuestaToString", String.class, "[Información]: (0). ");

        setIfExists(r, "setCodigo", String.class, "0");
        setIfExists(r, "setMensaje", String.class, "Información");
        setIfExists(r, "setDetalle", String.class, "(0). ");

        return r;
    }

    /**
     * Credenciales inválidas (código 2002)
     * Compatible 1:1 con QA
     */
    public static Respuesta credencialesInvalidas() {
        Respuesta r = new Respuesta();

        setIfExists(r, "setRespuestaID", String.class, "2002");
        setIfExists(r, "setCategoria", String.class, "4000");
        setIfExists(r, "setDescripcionCategoria", String.class, "Información");
        setIfExists(r, "setDescripcionRespuesta", String.class, "(2002). Usuario o contraseña incorrecta");
        setIfExists(r, "setRespuestaToString", String.class, "[Información]: (2002). Usuario o contraseña incorrecta");

        setIfExists(r, "setCodigo", String.class, "2002");
        setIfExists(r, "setMensaje", String.class, "Información");
        setIfExists(r, "setDetalle", String.class, "(2002). Usuario o contraseña incorrecta");

        return r;
    }

    public static Respuesta okExpediente() {
        return buildInfo("0", "Información", "Expediente Obtenido correctamente");
    }

    public static Respuesta okSinResultados() {
        return buildInfo("0", "Información", "SIN_RESULTADOS");
    }

    public static Respuesta ticketInvalido() {
        return buildError("403", "TICKET_INVALIDO", "Ticket inexistente o expirado");
    }

    public static Respuesta errorProcesoContesta(String detalle) {
        return buildError("7001", "ContestaExpedientexLlave", detalle);
    }

    /**
     * Error cuando NO se encuentra el expediente (código 7001).
     * Mensaje exacto según el log del servicio original.
     */
    public static Respuesta errorExpedienteNoEncontrado(int proyectoID, int expedienteID, String llave) {
        String detalle = String.format(
                "Ocurrio un error en el proceso de ConstestaExp. Proyecto:%d, ExpID:%d, Llave:1[Llave=[%s]]. " +
                        "Detalles: --> No se encontró el expediente\nNo cuenta con permisos a nivel Jerarquia de Grupos",
                proyectoID, expedienteID, llave
        );
        return buildError("7001", "ContestaExpedientexLlave", detalle);
    }

    public static Respuesta errorSistema(String detalle) {
        return buildError("500", "ERROR", detalle);
    }

    // ========= Núcleo con compatibilidad por reflexión =========

    private static Respuesta buildInfo(String respuestaIdOCodigo, String categoriaOMensaje, String descripcion) {
        Respuesta r = new Respuesta();

        setIfExists(r, "setRespuestaID", String.class, nonNull(respuestaIdOCodigo));
        setIfExists(r, "setCategoria", String.class, "4000");
        setIfExists(r, "setDescripcionCategoria", String.class, nonNull(categoriaOMensaje));
        setIfExists(r, "setDescripcionRespuesta", String.class, nonNull(descripcion));
        setIfExists(r, "setRespuestaToString", String.class, "[Información]: (" + nonNull(respuestaIdOCodigo) + "). ");

        setIfExists(r, "setCodigo", String.class, nonNull(respuestaIdOCodigo));
        setIfExists(r, "setMensaje", String.class, nonNull(categoriaOMensaje));
        setIfExists(r, "setDetalle", String.class, nonNull(descripcion));

        return r;
    }

    private static Respuesta buildError(String respuestaIdOCodigo, String categoriaOMensaje, String detalle) {
        Respuesta r = new Respuesta();

        setIfExists(r, "setRespuestaID", String.class, nonNull(respuestaIdOCodigo));
        setIfExists(r, "setCategoria", String.class, nonNull(respuestaIdOCodigo));
        setIfExists(r, "setDescripcionCategoria", String.class, nonNull(categoriaOMensaje));
        setIfExists(r, "setDescripcionRespuesta", String.class, nonNull(detalle));
        setIfExists(r, "setRespuestaToString", String.class, nonNull(detalle != null ? detalle : categoriaOMensaje));

        setIfExists(r, "setCodigo", String.class, nonNull(respuestaIdOCodigo));
        setIfExists(r, "setMensaje", String.class, nonNull(categoriaOMensaje));
        setIfExists(r, "setDetalle", String.class, nonNull(detalle));

        return r;
    }

    // ========= Utilidades =========

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    private static void setIfExists(Object target, String methodName, Class<?> paramType, Object value) {
        Objects.requireNonNull(target);
        try {
            Method m = target.getClass().getMethod(methodName, paramType);
            m.invoke(target, value);
        } catch (Exception ignore) {
            // método no existe en esta versión del stub
        }
    }
}