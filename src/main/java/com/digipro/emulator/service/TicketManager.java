package com.digipro.emulator.service;

import com.digipro.emulator.database.DatabaseManager;
import com.digipro.emulator.database.TicketDAO;
import com.digipro.emulator.database.UsuarioDTO;
import com.digipro.emulator.util.TokenUtils;
import com.digipro.emulator.ws.usuarios.IDTicket;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.GregorianCalendar;

/**
 * Gestor de Tickets para el emulador.
 * Compatible con la respuesta de QA para ObtenLogin.
 *
 * application.properties (opcionales):
 *   ticket.longitud.bytes=20
 *   ticket.tiempo.vida.minutos=240
 *   ticket.qa.version.aplicacion.id=1
 *   ticket.qa.tiempo.vida=1000
 *   ticket.qa.tiempo.vida.pwd=228
 *   ticket.qa.tiempo.actualizo.pwd=16129
 *   ticket.qa.force.sample=false
 *   ticket.qa.sample.ticket.base64=njpX1uy6JtU0Bm6CS4LtmwtGvKM=
 *   ticket.qa.sample.usr=actinverWS
 *   ticket.qa.sample.ip=192.168.1.100
 *   ticket.qa.sample.proyecto=3
 *
 *   // Controlan campos opcionales en el XML crudo del login (para evitar errores de unmarshalling del cliente):
 *   ws.qa.raw.include.ip=false
 *   ws.qa.raw.include.nombrecompleto=false
 *   ws.qa.raw.include.nombreusuario=false
 */
public class TicketManager {

    private final DatabaseManager db;
    private final TicketDAO ticketDAO;
    private final SecureRandom rnd;

    public TicketManager() {
        this.db = DatabaseManager.getInstance();
        this.ticketDAO = new TicketDAO();
        this.rnd = new SecureRandom();
    }

    /** Invoca un setter si existe en el stub. No falla si no existe. */
    private static void invokeIfPresent(Object target, String method, Class<?> type, Object arg) {
        try {
            Method m = target.getClass().getMethod(method, type);
            m.invoke(target, arg);
        } catch (Throwable ignore) { }
    }

    /** Lee un entero desde properties con default. */
    private int intProp(String key, int def) {
        try {
            String v = db.getProperty(key);
            if (v == null || v.trim().isEmpty()) return def;
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /** Lee un boolean desde properties con default. */
    private boolean boolProp(String key, boolean def) {
        try {
            String v = db.getProperty(key);
            if (v == null || v.trim().isEmpty()) return def;
            return "true".equalsIgnoreCase(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /** Lee un string desde properties con default. */
    private String strProp(String key, String def) {
        String v = db.getProperty(key);
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }

    // ========= helpers de reflexión para leer del IDTicket =========
    private static String getString(Object bean, String getter, String def) {
        try {
            Method m = bean.getClass().getMethod(getter);
            Object v = m.invoke(bean);
            return (v instanceof String && !((String) v).isEmpty()) ? (String) v : def;
        } catch (Throwable ignore) { return def; }
    }

    private static Integer getInt(Object bean, String getter, Integer def) {
        try {
            Method m = bean.getClass().getMethod(getter);
            Object v = m.invoke(bean);
            if (v instanceof Number) return ((Number) v).intValue();
            return def;
        } catch (Throwable ignore) { return def; }
    }

    /**
     * Genera un IDTicket con datos REALES
     */
    public IDTicket generarTicket(UsuarioDTO usuario, int proyectoID, String ip, String origen) {
        try {
            // Zona horaria MX (como QA)
            ZoneId tzMx = ZoneId.of("America/Mexico_City");

            // TTL real para la expiración en BD
            final int lenBytes   = intProp("ticket.longitud.bytes", 20);
            final int ttlMinReal = intProp("ticket.tiempo.vida.minutos", 240);
            final int ttlSec     = Math.max(60, ttlMinReal * 60);

            // Valores QA esperados
            final int qaVersionAplicacionID = intProp("ticket.qa.version.aplicacion.id", 1);
            final int qaTiempoVida          = intProp("ticket.qa.tiempo.vida", ttlMinReal);      // ej. 1000
            final int qaTiempoRestante      = intProp("ticket.qa.tiempo.vida", ttlMinReal);      // ej. 1000
            final int qaTiempoVidaPwd       = intProp("ticket.qa.tiempo.vida.pwd", ttlMinReal);  // ej. 228
            final int qaTiempoActualizoPwd  = intProp("ticket.qa.tiempo.actualizo.pwd", 0);      // ej. 16129

            final boolean forceSample = boolProp("ticket.qa.force.sample", false);

            // Campos base
            String ipFinal   = (ip != null ? ip : strProp("ticket.qa.sample.ip", "127.0.0.1"));
            String usrFinal  = (usuario != null && usuario.username != null && !usuario.username.isEmpty())
                    ? usuario.username : strProp("ticket.qa.sample.usr", "actinverWS");
            int proyectoFinal = (proyectoID > 0) ? proyectoID : intProp("ticket.qa.sample.proyecto", 3);

            // TicketID
            String base64Token = strProp("ticket.qa.sample.ticket.base64", null);
            byte[] raw;
            if (forceSample && base64Token != null) {
                raw = Base64.getDecoder().decode(base64Token);
            } else {
                raw = new byte[lenBytes];
                rnd.nextBytes(raw);
                base64Token = Base64.getEncoder().encodeToString(raw);
            }

            // Persistir en BD con TTL real
            ticketDAO.crearTicket(base64Token, (usuario != null ? usuario.idUsuario : 0), ipFinal, ttlSec);

            // Fechas
            XMLGregorianCalendar xNow = toXmlCal(ZonedDateTime.now(tzMx));
            XMLGregorianCalendar xExp = toXmlCal(ZonedDateTime.now(tzMx).plusSeconds(ttlSec));
            XMLGregorianCalendar xDefault = toXmlCalDefault();

            // Construcción del IDTicket
            IDTicket t = new IDTicket();

            // TicketID soportando String y/o byte[]
            invokeIfPresent(t, "setTicketID", String.class, base64Token);
            invokeIfPresent(t, "setTicketID", byte[].class, raw);

            // Básicos
            invokeIfPresent(t, "setUsrID", String.class, usrFinal);
            invokeIfPresent(t, "setIP", String.class, ipFinal);
            invokeIfPresent(t, "setProyectoID", int.class, proyectoFinal);
            invokeIfPresent(t, "setActivo", boolean.class, true);

            // Identidad
            String nombreCompleto = (usuario != null && usuario.nombreCompleto != null && !usuario.nombreCompleto.isEmpty())
                    ? usuario.nombreCompleto
                    : (usrFinal + " " + usrFinal + ", " + usrFinal); // ejemplo QA
            invokeIfPresent(t, "setNombreCompleto", String.class, nombreCompleto);
            invokeIfPresent(t, "setNombreUsuario", String.class, usrFinal);
            invokeIfPresent(t, "setGrupoAdminID", int.class, usuario != null && usuario.grupoAdminId != null ? usuario.grupoAdminId : 3);
            invokeIfPresent(t, "setClienteID", int.class,   usuario != null && usuario.clienteId    != null ? usuario.clienteId    : 1);
            invokeIfPresent(t, "setPerfilUsuarioID", int.class, usuario != null && usuario.perfilUsuarioId != null ? usuario.perfilUsuarioId : 9);
            invokeIfPresent(t, "setNoIdentidad", int.class, usuario != null && usuario.noIdentidad  != null ? usuario.noIdentidad  : 10);

            // Política Pwd
            int tvPwd = forceSample ? intProp("ticket.qa.tiempo.vida.pwd", 228) : qaTiempoVidaPwd;
            int taPwd = forceSample ? intProp("ticket.qa.tiempo.actualizo.pwd", 16129) : qaTiempoActualizoPwd;
            invokeIfPresent(t, "setTiempoVidaPwd", int.class, tvPwd);
            invokeIfPresent(t, "setTiempoActualizoPwd", int.class, taPwd);

            // Campos QA clave
            int verApp      = forceSample ? intProp("ticket.qa.version.aplicacion.id", 1) : qaVersionAplicacionID;
            int tiempoVida  = forceSample ? intProp("ticket.qa.tiempo.vida", 1000)       : qaTiempoVida;
            int tiempoRest  = forceSample ? intProp("ticket.qa.tiempo.vida", 1000)       : qaTiempoRestante;
            invokeIfPresent(t, "setVersionAplicacionID", int.class, verApp);
            invokeIfPresent(t, "setTiempoVida", int.class, tiempoVida);
            invokeIfPresent(t, "setTiempoRestante", int.class, tiempoRest);
            invokeIfPresent(t, "setDuracionDias", int.class, 0);
            invokeIfPresent(t, "setAvisoCaducidadPwdDias", int.class, 0);
            invokeIfPresent(t, "setExpirado", boolean.class, false);

            // Fechas expuestas
            invokeIfPresent(t, "setFechaCreacion", XMLGregorianCalendar.class, xNow);
            invokeIfPresent(t, "setFechaExpiracion", XMLGregorianCalendar.class, xExp);
            invokeIfPresent(t, "setFechaHoraInicio", XMLGregorianCalendar.class, xNow);
            invokeIfPresent(t, "setFechaHoraUltimoAcceso", XMLGregorianCalendar.class, xNow);
            invokeIfPresent(t, "setFechaUltLogin", XMLGregorianCalendar.class, xDefault);
            invokeIfPresent(t, "setFechaCreacionPwd", XMLGregorianCalendar.class, xDefault);

            System.out.println("[TicketManager] QA-compatible generado | forceSample=" + forceSample +
                    " | Token(Base64)=" + base64Token);

            return t;

        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar ticket", e);
        }
    }

    /** Valida vigencia del ticket contra la tabla 'tickets'. */
    public boolean validar(IDTicket t) {
        String token = TokenUtils.tokenFrom(t);
        if (token == null) return false;
        return ticketDAO.validarVigente(token);
    }

    /** ZonedDateTime -> XMLGregorianCalendar (con zona). */
    private static XMLGregorianCalendar toXmlCal(ZonedDateTime zdt) throws Exception {
        GregorianCalendar gc = GregorianCalendar.from(zdt);
        XMLGregorianCalendar x = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        x.setMillisecond(zdt.getNano() / 1_000_000);
        int offsetMinutes = zdt.getOffset().getTotalSeconds() / 60;
        x.setTimezone(offsetMinutes);
        return x;
    }

    /** 0001-01-01T00:00:00 sin zona explícita (como en QA). */
    private static XMLGregorianCalendar toXmlCalDefault() throws Exception {
        XMLGregorianCalendar x = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        x.setYear(1);
        x.setMonth(1);
        x.setDay(1);
        x.setHour(0);
        x.setMinute(0);
        x.setSecond(0);
        x.setFractionalSecond(null);
        x.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        return x;
    }

    /**
     * Construye el fragmento exacto de <ObtenLoginResult>.
     * Usa valores reales del IDTicket (token/usuario/proyecto), con fallback a properties si faltan.
     * Los nodos opcionales (<IP>, <NombreCompleto>, <NombreUsuario>) se controlan por properties
     * para evitar errores de unmarshalling en clientes/stubs que no los admiten.
     *
     * Props (todas por defecto false):
     *   ws.qa.raw.include.ip=false
     *   ws.qa.raw.include.nombrecompleto=false
     *   ws.qa.raw.include.nombreusuario=false
     */
    public String buildTicketXmlFragment(IDTicket t) {
        // valores REALES desde t (con fallback)
        String tokenB64 = TokenUtils.tokenFrom(t);
        if (tokenB64 == null || tokenB64.isEmpty()) {
            tokenB64 = strProp("ticket.qa.sample.ticket.base64", "njpX1uy6JtU0Bm6CS4LtmwtGvKM=");
        }
        String usr = getString(t, "getUsrID", strProp("ticket.qa.sample.usr", "actinverWS"));
        Integer proyecto = getInt(t, "getProyectoID", intProp("ticket.qa.sample.proyecto", 3));
        String nombreCompleto = getString(t, "getNombreCompleto", usr + " " + usr + ", " + usr);
        String nombreUsuario  = getString(t, "getNombreUsuario", usr);
        String ip = getString(t, "getIP", strProp("ticket.qa.sample.ip", "192.168.1.100"));

        int verApp       = intProp("ticket.qa.version.aplicacion.id", 1);
        int tv           = intProp("ticket.qa.tiempo.vida", 1000);
        int tr           = intProp("ticket.qa.tiempo.vida", 1000);
        int tvPwd        = intProp("ticket.qa.tiempo.vida.pwd", 228);
        int taPwd        = intProp("ticket.qa.tiempo.actualizo.pwd", 16129);

        boolean includeIP             = boolProp("ws.qa.raw.include.ip", false);
        boolean includeNombreCompleto = boolProp("ws.qa.raw.include.nombrecompleto", false);
        boolean includeNombreUsuario  = boolProp("ws.qa.raw.include.nombreusuario", false);

        try {
            XMLGregorianCalendar xNow = toXmlCal(ZonedDateTime.now(ZoneId.of("America/Mexico_City")));
            String nowStr = xNow.toXMLFormat();

            StringBuilder sb = new StringBuilder();
            sb.append("<ObtenLoginResult>");
            sb.append("<TicketID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(tokenB64).append("</TicketID>");
            sb.append("<UsrID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(usr).append("</UsrID>");

            // Campos opcionales que pueden romper a algunos clientes
            if (includeIP) {
                sb.append("<IP xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(ip).append("</IP>");
            }

            sb.append("<ProyectoID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(proyecto).append("</ProyectoID>");
            sb.append("<VersionAplicacionID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(verApp).append("</VersionAplicacionID>");
            sb.append("<FechaHoraInicio xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(nowStr).append("</FechaHoraInicio>");
            sb.append("<FechaHoraUltimoAcceso xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(nowStr).append("</FechaHoraUltimoAcceso>");
            sb.append("<TiempoVida xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(tv).append("</TiempoVida>");
            sb.append("<TiempoRestante xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(tr).append("</TiempoRestante>");

            if (includeNombreCompleto) {
                sb.append("<NombreCompleto xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(nombreCompleto).append("</NombreCompleto>");
            }
            if (includeNombreUsuario) {
                sb.append("<NombreUsuario xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(nombreUsuario).append("</NombreUsuario>");
            }

            sb.append("<GrupoAdminID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">3</GrupoAdminID>");
            sb.append("<ClienteID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">1</ClienteID>");
            sb.append("<PerfilUsuarioID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">9</PerfilUsuarioID>");
            sb.append("<TiempoVidaPwd xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(tvPwd).append("</TiempoVidaPwd>");
            sb.append("<TiempoActualizoPwd xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(taPwd).append("</TiempoActualizoPwd>");
            sb.append("<NoIdentidad xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">10</NoIdentidad>");
            sb.append("<FechaUltLogin xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">0001-01-01T00:00:00</FechaUltLogin>");
            sb.append("<DuracionDias xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">0</DuracionDias>");
            sb.append("<AvisoCaducidadPwdDias xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">0</AvisoCaducidadPwdDias>");
            sb.append("<FechaCreacionPwd xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">0001-01-01T00:00:00</FechaCreacionPwd>");
            sb.append("</ObtenLoginResult>");

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo construir XML de ticket QA", e);
        }
    }
}
