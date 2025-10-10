package com.digipro.emulator.service;

import com.digipro.emulator.adapter.FileSystemAdapter;
import com.digipro.emulator.database.DatabaseManager;
import com.digipro.emulator.database.DocumentoDAO;
import com.digipro.emulator.database.DocumentoDTO;
import com.digipro.emulator.database.LogAccesoDAO;
import com.digipro.emulator.database.UsuarioDAO;
import com.digipro.emulator.database.UsuarioDTO;
import com.digipro.emulator.service.orquestador.OrquestadorEstadosCuenta; // << NUEVO
import com.digipro.emulator.util.ResponseBuilder;
import com.digipro.emulator.util.TokenUtils;
import com.digipro.emulator.ws.generated.ArrayOfClsFileHSM;
import com.digipro.emulator.ws.generated.ClsFileHSM;
import com.digipro.emulator.ws.generated.ClsLlaveCampo;
import com.digipro.emulator.ws.generated.ClsLlaveExpediente;
import com.digipro.emulator.ws.generated.WsImagenesSoap;
import com.digipro.emulator.ws.usuarios.IDTicket;
import com.digipro.emulator.ws.usuarios.Respuesta;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * - ContestaExpedientexLlave: valida ticket y luego delega en el ORQUESTADOR para decidir
 *   entre ESTADOS DE CUENTA EN LÍNEA (reciente) y HISTÓRICO IDPORTAL (lógica actual).
 *   Mantiene opción de responder RAW/JAXB como antes.
 * - ContestaFileHSM: sin cambios de contrato.
 */
@WebService(
        serviceName = "WsImagenes",
        portName = "WsImagenesSoap",
        targetNamespace = "http://Digipro.servicios/WsImagenes/WsImagenes",
        endpointInterface = "com.digipro.emulator.ws.generated.WsImagenesSoap"
)
public class WsImagenesImpl implements WsImagenesSoap {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DocumentoDAO documentoDAO = new DocumentoDAO();
    private final LogAccesoDAO logDAO = new LogAccesoDAO();
    private final TicketManager ticketManager = new TicketManager();
    private final FileSystemAdapter fileAdapter = new FileSystemAdapter();

    // << NUEVO: orquestador (usa documentoDAO + fileAdapter por dentro)
    private final OrquestadorEstadosCuenta orquestadorEC =
            OrquestadorEstadosCuenta.createDefault(documentoDAO, fileAdapter);

    private final SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
    private final DatabaseManager db = DatabaseManager.getInstance();

    @Resource
    private WebServiceContext wsContext;

    public WsImagenesImpl() {
        System.out.println("[WsImagenesImpl] Servicio inicializado");
    }

    // ======================= ObtenLogin =======================
    @WebMethod(operationName = "ObtenLogin")
    @Override
    public void obtenLogin(
            @WebParam(name = "UserID")     String userID,
            @WebParam(name = "strPwd")     String strPwd,
            @WebParam(name = "ProyectoID") int proyectoID,
            @WebParam(name = "IP")         String IP,
            @WebParam(name = "strOrigen")  String strOrigen,
            @WebParam(name = "rRespuesta", mode = WebParam.Mode.INOUT) Holder<Respuesta> rRespuesta,
            @WebParam(name = "ObtenLoginResult", mode = WebParam.Mode.OUT) Holder<IDTicket> obtenLoginResult) {

        final String ipFinal = (IP == null || IP.isEmpty()) ? "127.0.0.1" : IP;

        System.out.println("[ObtenLogin] Intento de autenticación - Usuario: " + userID + ", ProyID: " + proyectoID);

        try {
            UsuarioDTO u = usuarioDAO.validar(userID, strPwd);
            if (u == null) {
                // EXACTO mensaje de QA para credenciales inválidas
                if (propBool("ws.qa.force.raw.login.credenciales", true)) {
                    String body =
                            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                                    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                                    + "<soap:Body>"
                                    + "  <ObtenLoginResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">"
                                    + "    <rRespuesta>"
                                    + "      <RespuestaID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">2002</RespuestaID>"
                                    + "      <Categoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">4000</Categoria>"
                                    + "      <DescripcionCategoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">Información</DescripcionCategoria>"
                                    + "      <DescripcionRespuesta xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">(2002). Usuario o contraseña incorrecta</DescripcionRespuesta>"
                                    + "      <RespuestaToString xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">[Información]: (2002). Usuario o contraseña incorrecta</RespuestaToString>"
                                    + "    </rRespuesta>"
                                    + "  </ObtenLoginResponse>"
                                    + "</soap:Body>"
                                    + "</soap:Envelope>";
                    writeRawSoap(body);
                } else {
                    rRespuesta.value = ResponseBuilder.credencialesInvalidas();
                }
                obtenLoginResult.value = null;
                logDAO.registrar(null, null, "ObtenLogin", null, ipFinal, false, "Credenciales inválidas");
                return;
            }

            IDTicket ticket = ticketManager.generarTicket(u, proyectoID, ipFinal, (strOrigen != null ? strOrigen : "ws"));

            boolean forceRaw = propBool("ws.qa.force.raw", false);
            if (forceRaw || ticketStubIncompleto(ticket)) {
                System.out.println("[ObtenLogin] Modo SOAP crudo QA activado (forceRaw=" + forceRaw + ")");
                String resultFragment = ticketManager.buildTicketXmlFragment(ticket);

                String body =
                        "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" "
                                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                                + "  <soap:Body>"
                                + "    <ObtenLoginResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">"
                                +          resultFragment
                                + "      <rRespuesta>"
                                + "        <RespuestaID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">0</RespuestaID>"
                                + "        <Categoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">4000</Categoria>"
                                + "        <DescripcionCategoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">Información</DescripcionCategoria>"
                                + "        <DescripcionRespuesta xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">(0). </DescripcionRespuesta>"
                                + "        <RespuestaToString xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">[Información]: (0). </RespuestaToString>"
                                + "      </rRespuesta>"
                                + "    </ObtenLoginResponse>"
                                + "  </soap:Body>"
                                + "</soap:Envelope>";

                writeRawSoap(body);
                logDAO.registrar(TokenUtils.tokenFrom(ticket), null, "ObtenLogin", null, ipFinal, true, null);
                return;
            }

            System.out.println("[ObtenLogin] Autenticación exitosa - Usuario: " + userID + " (modo JAXB)");
            rRespuesta.value = ResponseBuilder.authOk();
            obtenLoginResult.value = ticket;
            logDAO.registrar(TokenUtils.tokenFrom(ticket), null, "ObtenLogin", null, ipFinal, true, null);

        } catch (Exception e) {
            System.err.println("[ObtenLogin] Error: " + e.getMessage());
            e.printStackTrace();
            rRespuesta.value = ResponseBuilder.error("500", "ERROR", e.getMessage());
            obtenLoginResult.value = null;
            logDAO.registrar(null, null, "ObtenLogin", null, ipFinal, false, e.getMessage());
        }
    }

    // ============== ContestaExpedientexLlave ===================
    @WebMethod(operationName = "ContestaExpedientexLlave")
    @Override
    public void contestaExpedientexLlave(
            @WebParam(name = "Ticket") Holder<IDTicket> ticket,
            @WebParam(name = "llave")  ClsLlaveExpediente llave,
            @WebParam(name = "ProyID") short proyID,
            @WebParam(name = "ExpedienteID") short expedienteID,
            @WebParam(name = "TipoDocID") int tipoDocID,
            @WebParam(name = "rRespuesta", mode = WebParam.Mode.INOUT) Holder<Respuesta> rRespuesta,
            @WebParam(name = "ContestaExpedientexLlaveResult", mode = WebParam.Mode.OUT) Holder<ArrayOfClsFileHSM> contestaExpedientexLlaveResult) {

        String valorBusqueda = null;

        try {
            System.out.println("[ContestaExpedientexLlave] Inicio - ProyID: " + proyID + ", ExpID: " + expedienteID + ", TipoDocID: " + tipoDocID);

            // ====== 1) Validación detallada de ticket ======
            TicketState tstate = validarTicketDetallado(ticket != null ? ticket.value : null);
            if (tstate != TicketState.OK) {
                // Construye respuesta RAW exacta de QA con (2052) o (2053)
                String bodyErr = buildTicketErrorEnvelope(ticket != null ? ticket.value : null,
                        tstate == TicketState.EXPIRED ? "2052" : "2053",
                        tstate == TicketState.EXPIRED
                                ? "El ticket suministrado ha expirado. Por favor, reintente el login"
                                : "El ticket suministrado no es válido o está corrupto");
                writeRawSoap(bodyErr);

                // Para logs internos, seguimos llenando rRespuesta
                rRespuesta.value = ResponseBuilder.ticketInvalido();
                contestaExpedientexLlaveResult.value = null;

                logDAO.registrar(
                        tokenSafe(ticket != null ? ticket.value : null),
                        null,
                        "ContestaExpedientexLlave",
                        null,
                        resolveIP(ticket != null ? ticket.value : null),
                        false,
                        (tstate == TicketState.EXPIRED ? "Ticket expirado (2052)" : "Ticket inválido (2053)"));
                return;
            }

            // ====== 2) Extraer llave ======
            valorBusqueda = extraerValorLlave(llave);
            if (valorBusqueda == null || valorBusqueda.isEmpty()) {
                rRespuesta.value = ResponseBuilder.error("400", "LLAVE_VACIA", "La llave no contiene valor");
                contestaExpedientexLlaveResult.value = null;
                logDAO.registrar(tokenSafe(ticket.value), null, "ContestaExpedientexLlave",
                        null, resolveIP(ticket.value), false, "Llave vacía");
                return;
            }

            // ====== 3) ORQUESTADOR: resolver fuente (RECENTE/HISTÓRICO) ======
            ArrayOfClsFileHSM array = orquestadorEC.resolver(valorBusqueda, tipoDocID);

            // ====== 4) Sin resultados → devolver mensaje QA (7001 + texto largo) ======
            if (array == null || array.getClsFileHSM().isEmpty()) {
                String xmlNoExp = buildNoExpedienteEnvelope(
                        ticket.value, proyID, expedienteID, valorBusqueda);
                writeRawSoap(xmlNoExp);

                rRespuesta.value = ResponseBuilder.errorExpedienteNoEncontrado(proyID, expedienteID, valorBusqueda);
                contestaExpedientexLlaveResult.value = null;

                logDAO.registrar(tokenSafe(ticket.value), null, "ContestaExpedientexLlave",
                        valorBusqueda, resolveIP(ticket.value), false, "No se encontró el expediente");
                return;
            }

            // tomar primer DocID para logging
            Long primerId = getLong(array.getClsFileHSM().get(0), "getDocID", 0L);
            Integer primerDocID = (primerId != null ? primerId.intValue() : null);

            // ====== 5) Responder RAW o JAXB como antes ======
            boolean forceRawContesta = propBool("ws.qa.force.raw.contesta", true);
            if (forceRawContesta) {
                List<String> rawItems = new ArrayList<>();
                int idx = 1;
                for (ClsFileHSM f : array.getClsFileHSM()) {
                    long docId = getLong(f, "getDocID", 0L);
                    int tipo = getInt(f, "getTipoDocID", 0);
                    String ext = str(getStr(f, "getExt"), "");
                    if (!ext.isEmpty() && !ext.startsWith(".")) ext = "." + ext;
                    String fechaDig = str(getStr(f, "getFechaDigitalizacion"), "");
                    byte[] bytes = getBytesHSM(f);

                    String itemXml =
                            "        <clsFileHSM>\n" +
                                    "          <DocID>" + docId + "</DocID>\n" +
                                    "          <DocPID>0</DocPID>\n" +
                                    "          <TipoDocID>" + tipo + "</TipoDocID>\n" +
                                    "          <TipoDocIdGrupo>0</TipoDocIdGrupo>\n" +
                                    "          <Descripcion>Documento</Descripcion>\n" +
                                    "          <Consecutivo>" + (getInt(f, "getConsecutivo", idx)) + "</Consecutivo>\n" +
                                    "          <Separador>false</Separador>\n" +
                                    "          <Ext>" + escapeXml(ext.toUpperCase()) + "</Ext>\n" +
                                    "          <FechaDigitalizacion>" + escapeXml(fechaDig) + "</FechaDigitalizacion>\n" +
                                    "          <ArrayFile>" + Base64.getEncoder().encodeToString(bytes != null ? bytes : new byte[0]) + "</ArrayFile>\n" +
                                    "          <CreatedBy>0</CreatedBy>\n" +
                                    "        </clsFileHSM>\n";
                    rawItems.add(itemXml);
                    idx++;
                }

                String body = buildContestaLlaveRawEnvelope(rawItems,
                        /*respuesta OK*/ "0", "4000", "Información", "Expediente Obtenido correctamente");
                writeRawSoap(body);
                rRespuesta.value = ResponseBuilder.okExpediente();
                logDAO.registrar(tokenSafe(ticket.value), primerDocID, "ContestaExpedientexLlave",
                        valorBusqueda, resolveIP(ticket.value), true, null);
                return;
            }

            // JAXB normal
            rRespuesta.value = ResponseBuilder.okExpediente();
            contestaExpedientexLlaveResult.value = array;
            logDAO.registrar(tokenSafe(ticket.value), primerDocID, "ContestaExpedientexLlave",
                    valorBusqueda, resolveIP(ticket.value), true, null);

        } catch (Exception e) {
            System.err.println("[ContestaExpedientexLlave] ERROR: " + e.getMessage());
            e.printStackTrace();
            rRespuesta.value = ResponseBuilder.errorProcesoContesta(e.getMessage());
            contestaExpedientexLlaveResult.value = null;
            logDAO.registrar(tokenSafe(ticket != null ? ticket.value : null), null, "ContestaExpedientexLlave",
                    valorBusqueda, resolveIP(ticket != null ? ticket.value : null), false, e.getMessage());
        }
    }

    // =================== ContestaFileHSM (por DocID) ===================
    @WebMethod(operationName = "ContestaFileHSM")
    @Override
    public void contestaFileHSM(
            @WebParam(name = "DocID") long docID,
            @WebParam(name = "ProyID") int proyID,
            @WebParam(name = "DocIDPadreExp") long expedienteID,
            @WebParam(name = "Ticket") Holder<IDTicket> ticket,
            @WebParam(name = "rRespuesta", mode = WebParam.Mode.INOUT) Holder<Respuesta> rRespuesta,
            @WebParam(name = "ContestaFileHSMResult", mode = WebParam.Mode.OUT) Holder<ClsFileHSM> contestaFileHSMResult) {

        System.out.println("[ContestaFileHSM] Inicio - DocID: " + docID);

        try {
            if (validarTicketDetallado(ticket != null ? ticket.value : null) != TicketState.OK) {
                String bodyErr = buildTicketErrorEnvelope(ticket != null ? ticket.value : null,
                        "2053", "El ticket suministrado no es válido o está corrupto");
                writeRawSoap(bodyErr);
                rRespuesta.value = ResponseBuilder.ticketInvalido();
                contestaFileHSMResult.value = null;
                logDAO.registrar(tokenSafe(ticket.value), null, "ContestaFileHSM",
                        null, resolveIP(ticket.value), false, "Ticket inválido (2053)");
                return;
            }

            DocumentoDTO d = documentoDAO.findByIdDocumento(docID);
            if (d == null) {
                rRespuesta.value = ResponseBuilder.okSinResultados();
                contestaFileHSMResult.value = null;
                logDAO.registrar(tokenSafe(ticket.value), null, "ContestaFileHSM",
                        null, resolveIP(ticket.value), false, "DocID no encontrado");
                return;
            }

            byte[] contenido = fileAdapter.readFile(d.rutaRelativa);
            if (contenido == null || contenido.length == 0) {
                rRespuesta.value = ResponseBuilder.error("404", "ARCHIVO_NO_ENCONTRADO", d.rutaRelativa);
                contestaFileHSMResult.value = null;
                logDAO.registrar(tokenSafe(ticket.value), d.idDocumento, "ContestaFileHSM",
                        null, resolveIP(ticket.value), false, "Archivo no encontrado en disco");
                return;
            }

            ClsFileHSM f = toClsFileHSM(d, contenido);
            rRespuesta.value = ResponseBuilder.okExpediente();
            contestaFileHSMResult.value = f;

            logDAO.registrar(tokenSafe(ticket.value), d.idDocumento, "ContestaFileHSM",
                    null, resolveIP(ticket.value), true, null);

        } catch (Exception e) {
            System.err.println("[ContestaFileHSM] ERROR: " + e.getMessage());
            e.printStackTrace();
            rRespuesta.value = ResponseBuilder.error("7002", "ContestaFileHSM", e.getMessage());
            contestaFileHSMResult.value = null;
            logDAO.registrar(tokenSafe(ticket.value), null, "ContestaFileHSM",
                    null, resolveIP(ticket.value), false, e.getMessage());
        }
    }



    private enum TicketState { OK, EXPIRED, INVALID }

    /** Valida con detalle para distinguir EXPIRED (2052) vs INVALID (2053). */
    private TicketState validarTicketDetallado(IDTicket t) {
        try {
            if (t == null) return TicketState.INVALID;

            // 1) Si validar() dice vigente => OK
            if (ticketManager.validar(t)) return TicketState.OK;

            // 2) Si tenemos FechaExpiracion en el ticket, comparamos
            XMLGregorianCalendar xexp = getXmlCal(t, "getFechaExpiracion");
            if (xexp != null) {
                ZonedDateTime exp = xexp.toGregorianCalendar().toZonedDateTime();
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
                if (now.isAfter(exp)) return TicketState.EXPIRED;
            }

            // 3) Si no podemos saber, lo marcamos inválido/corrupto
            return TicketState.INVALID;
        } catch (Exception ignore) {
            return TicketState.INVALID;
        }
    }

    private String tokenSafe(IDTicket t) {
        try { return TokenUtils.tokenFrom(t); } catch (Exception e) { return null; }
    }

    private boolean propBool(String key, boolean def) {
        try {
            String v = db.getProperty(key);
            if (v == null) return def;
            return "true".equalsIgnoreCase(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean hasSetter(Object target, String method, Class<?> type) {
        try {
            target.getClass().getMethod(method, type);
            return true;
        } catch (Throwable ignore) { return false; }
    }

    private boolean ticketStubIncompleto(IDTicket t) {
        boolean ok = true;
        ok &= hasSetter(t, "setIP", String.class);
        ok &= hasSetter(t, "setVersionAplicacionID", int.class);
        ok &= hasSetter(t, "setFechaHoraInicio", javax.xml.datatype.XMLGregorianCalendar.class);
        ok &= hasSetter(t, "setFechaHoraUltimoAcceso", javax.xml.datatype.XMLGregorianCalendar.class);
        ok &= hasSetter(t, "setTiempoVida", int.class);
        ok &= hasSetter(t, "setTiempoRestante", int.class);
        ok &= hasSetter(t, "setNombreCompleto", String.class);
        ok &= hasSetter(t, "setNombreUsuario", String.class);
        ok &= hasSetter(t, "setGrupoAdminID", int.class);
        ok &= hasSetter(t, "setClienteID", int.class);
        ok &= hasSetter(t, "setPerfilUsuarioID", int.class);
        ok &= hasSetter(t, "setTiempoVidaPwd", int.class);
        ok &= hasSetter(t, "setTiempoActualizoPwd", int.class);
        ok &= hasSetter(t, "setNoIdentidad", int.class);
        ok &= hasSetter(t, "setFechaUltLogin", javax.xml.datatype.XMLGregorianCalendar.class);
        ok &= hasSetter(t, "setDuracionDias", int.class);
        ok &= hasSetter(t, "setAvisoCaducidadPwdDias", int.class);
        ok &= hasSetter(t, "setFechaCreacionPwd", javax.xml.datatype.XMLGregorianCalendar.class);
        return !ok;
    }

    private void writeRawSoap(String soapXml) {
        try {
            MessageContext mc = wsContext.getMessageContext();
            Object resp = mc.get(MessageContext.SERVLET_RESPONSE);
            if (resp == null) throw new IllegalStateException("No HttpServletResponse in MessageContext");

            Class<?> respCls = resp.getClass();
            Method setStatus = respCls.getMethod("setStatus", int.class);
            Method setContentType = respCls.getMethod("setContentType", String.class);
            Method getWriter = respCls.getMethod("getWriter");

            setStatus.invoke(resp, 200);
            setContentType.invoke(resp, "application/soap+xml; charset=utf-8"); // SOAP 1.2
            Object writer = getWriter.invoke(resp);

            Class<?> writerCls = writer.getClass(); // PrintWriter
            Method write = writerCls.getMethod("write", String.class);
            Method flush = writerCls.getMethod("flush");
            write.invoke(writer, soapXml);
            flush.invoke(writer);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo escribir SOAP crudo", ex);
        }
    }

    /** Envelope RAW para éxito de ContestaExpedientexLlave. */
    private String buildContestaLlaveRawEnvelope(List<String> rawItems,
                                                 String respuestaID, String categoria,
                                                 String descCategoria, String descRespuesta) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
                .append("<soap:Body>")
                .append("<ContestaExpedientexLlaveResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">")
                .append("<ContestaExpedientexLlaveResult>\n");
        for (String it : rawItems) sb.append(it);
        sb.append("</ContestaExpedientexLlaveResult>")
                .append("<rRespuesta>")
                .append("<RespuestaID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(escapeXml(respuestaID)).append("</RespuestaID>")
                .append("<Categoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(escapeXml(categoria)).append("</Categoria>")
                .append("<DescripcionCategoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(escapeXml(descCategoria)).append("</DescripcionCategoria>")
                .append("<DescripcionRespuesta xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">").append(escapeXml(descRespuesta)).append("</DescripcionRespuesta>")
                .append("<RespuestaToString xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">[")
                .append(escapeXml(descCategoria)).append("]: (").append(escapeXml(respuestaID)).append("). </RespuestaToString>")
                .append("</rRespuesta>")
                .append("</ContestaExpedientexLlaveResponse>")
                .append("</soap:Body></soap:Envelope>");
        return sb.toString();
    }

    /** Envelope RAW para ticket inválido/expirado, con bloque <Ticket> y mensajes 2052/2053. */
    private String buildTicketErrorEnvelope(IDTicket t, String codigoInfo, String textoInfo) {
        String ticketNode = buildTicketNode(t, codigoInfo);
        String desc = "[Información]: (" + codigoInfo + "). " + textoInfo;

        String stack =
                "System.Exception: " + desc + "\n" +
                        "   at Digipro.WSImagenes.WsImagenes.ContestaExpedientexLlave(IDTicket& Ticket, clsLlaveExpediente llave, Int16 ProyID, Int16 ExpedienteID, Int32 TipoDocID, Respuesta& rRespuesta)";

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + "<soap:Body>"
                + "  <ContestaExpedientexLlaveResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">"
                +       ticketNode
                + "    <rRespuesta>"
                + "      <RespuestaID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">7001</RespuestaID>"
                + "      <Categoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">7001</Categoria>"
                + "      <DescripcionCategoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">ContestaExpedientexLlave</DescripcionCategoria>"
                + "      <DescripcionRespuesta xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">" + escapeXml(desc) + "</DescripcionRespuesta>"
                + "      <RespuestaToString xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">" + escapeXml(stack) + "</RespuestaToString>"
                + "    </rRespuesta>"
                + "  </ContestaExpedientexLlaveResponse>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    /** Envelope RAW para "No se encontró el expediente" (7001 + texto largo con dos líneas). */
    private String buildNoExpedienteEnvelope(IDTicket t, int proyectoID, int expedienteID, String llaveValor) {
        String ticketNode = buildTicketNode(t, null);
        String linea1 = "Ocurrio un error en el proceso de ConstestaExp. Proyecto:"
                + proyectoID + ", ExpID:" + expedienteID + ", Llave:1[Llave=[" + llaveValor + "]]. Detalles: --> No se encontró el expediente";
        String linea2 = "No cuenta con permisos a nivel Jerarquia de Grupos";

        String desc = linea1 + "\n" + linea2;

        String stack = "System.Exception: " + linea1 + "\n" + linea2 + "\n"
                + "   at Digipro.WSImagenes.BusinessContestaExp.ContestaExpediente(clsLlaveExpediente Llave, Int16 ExpedienteID, Int32 TipoDocID, Int32 proyectoID, Int32 GrupoAdmin)\n"
                + "   at Digipro.WSImagenes.WsImagenes.ContestaExpedientexLlave(IDTicket& Ticket, clsLlaveExpediente llave, Int16 ProyID, Int16 ExpedienteID, Int32 TipoDocID, Respuesta& rRespuesta)";

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + "<soap:Body>"
                + "  <ContestaExpedientexLlaveResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">"
                +       ticketNode
                + "    <rRespuesta>"
                + "      <RespuestaID xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">7001</RespuestaID>"
                + "      <Categoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">7001</Categoria>"
                + "      <DescripcionCategoria xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">ContestaExpedientexLlave</DescripcionCategoria>"
                + "      <DescripcionRespuesta xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">" + escapeXml(desc) + "</DescripcionRespuesta>"
                + "      <RespuestaToString xmlns=\"http://Digipro.servicios/WsUsuarios/WsUsuarios\">" + escapeXml(stack) + "</RespuestaToString>"
                + "    </rRespuesta>"
                + "  </ContestaExpedientexLlaveResponse>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    /** Construye el bloque <Ticket> con los metadatos presentes en el IDTicket (o defaults). */
    private String buildTicketNode(IDTicket t, String codigoInfo205X) {
        String ns = "http://Digipro.servicios/WsUsuarios/WsUsuarios";

        String token = tokenSafe(t);
        String usr   = str(getStr(t, "getUsrID"), "actinverWS");
        String ip    = str(getStr(t, "getIP"), "192.168.1.100");
        int proyecto = getInt(t, "getProyectoID", 3);
        int verApp   = getInt(t, "getVersionAplicacionID", 1);

        XMLGregorianCalendar xinicio = getXmlCal(t, "getFechaHoraInicio");
        XMLGregorianCalendar xultimo = getXmlCal(t, "getFechaHoraUltimoAcceso");
        String sInicio = xinicio != null ? xinicio.toXMLFormat() : nowXml();
        String sUltimo = xultimo != null ? xultimo.toXMLFormat() : sInicio;

        int tv  = getInt(t, "getTiempoVida", 1000);
        int tr  = calcTiempoRestante(t);
        if (codigoInfo205X != null && ("2052".equals(codigoInfo205X) || "2053".equals(codigoInfo205X))) {
            if (tr >= 0) tr = -1; // negativa para clientes QA estrictos
        }

        String nombreCompleto = str(getStr(t, "getNombreCompleto"), usr + " " + usr + ", " + usr);
        String nombreUsr      = str(getStr(t, "getNombreUsuario"), usr);
        int grupoAdmin        = getInt(t, "getGrupoAdminID", 3);
        int clienteID         = getInt(t, "getClienteID", 1);
        int perfil            = getInt(t, "getPerfilUsuarioID", 9);
        int tvPwd             = getInt(t, "getTiempoVidaPwd", 228);
        int taPwd             = getInt(t, "getTiempoActualizoPwd", 16126);
        int noIdentidad       = getInt(t, "getNoIdentidad", 10);

        XMLGregorianCalendar xUltLogin = getXmlCal(t, "getFechaUltLogin");
        String sUltLogin = xUltLogin != null ? xUltLogin.toXMLFormat() : "0001-01-01T00:00:00";
        int durDias      = getInt(t, "getDuracionDias", 0);
        int avisoPwdDias = getInt(t, "getAvisoCaducidadPwdDias", 0);

        XMLGregorianCalendar xCrePwd = getXmlCal(t, "getFechaCreacionPwd");
        String sCrePwd = xCrePwd != null ? xCrePwd.toXMLFormat() : "0001-01-01T00:00:00";

        return "  <Ticket>\n" +
                "    <TicketID xmlns=\"" + ns + "\">" + escapeXml(str(token, "")) + "</TicketID>\n" +
                "    <UsrID xmlns=\"" + ns + "\">" + escapeXml(usr) + "</UsrID>\n" +
                "    <IP xmlns=\"" + ns + "\">" + escapeXml(ip) + "</IP>\n" +
                "    <ProyectoID xmlns=\"" + ns + "\">" + proyecto + "</ProyectoID>\n" +
                "    <VersionAplicacionID xmlns=\"" + ns + "\">" + verApp + "</VersionAplicacionID>\n" +
                "    <FechaHoraInicio xmlns=\"" + ns + "\">" + escapeXml(sInicio) + "</FechaHoraInicio>\n" +
                "    <FechaHoraUltimoAcceso xmlns=\"" + ns + "\">" + escapeXml(sUltimo) + "</FechaHoraUltimoAcceso>\n" +
                "    <TiempoVida xmlns=\"" + ns + "\">" + tv + "</TiempoVida>\n" +
                "    <TiempoRestante xmlns=\"" + ns + "\">" + tr + "</TiempoRestante>\n" +
                "    <NombreCompleto xmlns=\"" + ns + "\">" + escapeXml(nombreCompleto) + "</NombreCompleto>\n" +
                "    <NombreUsuario xmlns=\"" + ns + "\">" + escapeXml(nombreUsr) + "</NombreUsuario>\n" +
                "    <GrupoAdminID xmlns=\"" + ns + "\">" + grupoAdmin + "</GrupoAdminID>\n" +
                "    <ClienteID xmlns=\"" + ns + "\">" + clienteID + "</ClienteID>\n" +
                "    <PerfilUsuarioID xmlns=\"" + ns + "\">" + perfil + "</PerfilUsuarioID>\n" +
                "    <TiempoVidaPwd xmlns=\"" + ns + "\">" + tvPwd + "</TiempoVidaPwd>\n" +
                "    <TiempoActualizoPwd xmlns=\"" + ns + "\">" + taPwd + "</TiempoActualizoPwd>\n" +
                "    <NoIdentidad xmlns=\"" + ns + "\">" + noIdentidad + "</NoIdentidad>\n" +
                "    <FechaUltLogin xmlns=\"" + ns + "\">" + escapeXml(sUltLogin) + "</FechaUltLogin>\n" +
                "    <DuracionDias xmlns=\"" + ns + "\">" + durDias + "</DuracionDias>\n" +
                "    <AvisoCaducidadPwdDias xmlns=\"" + ns + "\">" + avisoPwdDias + "</AvisoCaducidadPwdDias>\n" +
                "    <FechaCreacionPwd xmlns=\"" + ns + "\">" + escapeXml(sCrePwd) + "</FechaCreacionPwd>\n" +
                "  </Ticket>\n";
    }

    // ==== util getters por reflexión, y helpers de tiempo ====

    private static String str(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }

    private static String getStr(Object obj, String getter) {
        try {
            if (obj == null) return null;
            Method m = obj.getClass().getMethod(getter);
            Object v = m.invoke(obj);
            return (v != null) ? v.toString() : null;
        } catch (Throwable ignore) { return null; }
    }

    private static int getInt(Object obj, String getter, int def) {
        try {
            if (obj == null) return def;
            Method m = obj.getClass().getMethod(getter);
            Object v = m.invoke(obj);
            if (v instanceof Number) return ((Number) v).intValue();
            if (v != null) return Integer.parseInt(v.toString());
            return def;
        } catch (Throwable ignore) { return def; }
    }

    private static Long getLong(Object obj, String getter, long def) {
        try {
            if (obj == null) return def;
            Method m = obj.getClass().getMethod(getter);
            Object v = m.invoke(obj);
            if (v instanceof Number) return ((Number) v).longValue();
            if (v != null) return Long.parseLong(v.toString());
            return def;
        } catch (Throwable ignore) { return def; }
    }

    private static byte[] getBytesHSM(Object f) {
        // intenta varios getters posibles para el arreglo de bytes
        String[] getters = new String[] {
                "getArrayFile", "getArchivo", "getArchivoHSM", "getArrArchivo", "getBytes", "getContenido", "getContenidoArchivo"
        };
        for (String g : getters) {
            try {
                Method m = f.getClass().getMethod(g);
                Object v = m.invoke(f);
                if (v instanceof byte[]) return (byte[]) v;
            } catch (Throwable ignore) {}
        }
        return null;
    }

    private static XMLGregorianCalendar getXmlCal(Object obj, String getter) {
        try {
            if (obj == null) return null;
            Method m = obj.getClass().getMethod(getter);
            Object v = m.invoke(obj);
            if (v instanceof XMLGregorianCalendar) return (XMLGregorianCalendar) v;
            return null;
        } catch (Throwable ignore) { return null; }
    }

    private static String nowXml() {
        try {
            ZoneId tzMx = ZoneId.of("America/Mexico_City");
            var zdt = ZonedDateTime.now(tzMx);
            var gc = java.util.GregorianCalendar.from(zdt);
            var x = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            return x.toXMLFormat();
        } catch (Exception e) {
            return "0001-01-01T00:00:00";
        }
    }

    private static int calcTiempoRestante(IDTicket t) {
        try {
            XMLGregorianCalendar xExp = getXmlCal(t, "getFechaExpiracion");
            if (xExp == null) return getInt(t, "getTiempoRestante", 1000);
            ZonedDateTime exp = xExp.toGregorianCalendar().toZonedDateTime();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
            long secs = java.time.Duration.between(now, exp).getSeconds();
            return (int) secs; // puede ser negativo si expiró
        } catch (Exception e) {
            return getInt(t, "getTiempoRestante", -1);
        }
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String extraerValorLlave(ClsLlaveExpediente llave) {
        if (llave == null || llave.getCampos() == null) return null;
        var campos = llave.getCampos().getClsLlaveCampo();
        if (campos == null) return null;
        for (ClsLlaveCampo c : campos) {
            if (c != null && c.getValor() != null && !c.getValor().trim().isEmpty()) {
                return c.getValor().trim();
            }
        }
        return null;
    }

    private String resolveIP(IDTicket t) {
        try {
            var m = t.getClass().getMethod("getIP");
            Object v = m.invoke(t);
            if (v instanceof String && !((String) v).isEmpty()) return (String) v;
        } catch (Throwable ignore) { }
        return "127.0.0.1";
    }

    private ClsFileHSM toClsFileHSM(DocumentoDTO d, byte[] contenido) {
        ClsFileHSM f = new ClsFileHSM();
        f.setDocID((long) d.idDocumento);
        // En tu stub, el binario se llama ArrayFile:
        try { f.getClass().getMethod("setArrayFile", byte[].class).invoke(f, contenido); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setDocPID", long.class).invoke(f, 0L); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setTipoDocID", int.class).invoke(f, d.tipoDocId); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setTipoDocIdGrupo", long.class).invoke(f, 0L); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setCreatedBy", long.class).invoke(f, 0L); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setDescripcion", String.class).invoke(f,
                d.descripcion != null ? d.descripcion : "Documento"); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setConsecutivo", int.class).invoke(f, d.consecutivo); } catch (Throwable ignore) {}
        try { f.getClass().getMethod("setSeparador", boolean.class).invoke(f, false); } catch (Throwable ignore) {}

        String ext = d.extension;
        if (ext != null && !ext.isEmpty()) {
            if (!ext.startsWith(".")) ext = "." + ext;
            try { f.getClass().getMethod("setExt", String.class).invoke(f, ext); } catch (Throwable ignore) {}
        }
        if (d.fechaCreacion != null) {
            try { f.getClass().getMethod("setFechaDigitalizacion", String.class)
                    .invoke(f, sdfFecha.format(d.fechaCreacion)); } catch (Throwable ignore) {}
        }
        return f;
    }
}
