package mx.com.actinver.orquestador.service.impl;

import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.conf.DynamicString;
import mx.com.actinver.orquestador.dao.SpRepEdcDao;
import mx.com.actinver.orquestador.dto.*;
import mx.com.actinver.orquestador.entity.AccountEntity;
import mx.com.actinver.orquestador.entity.AccountRequestEntity;
import mx.com.actinver.orquestador.service.DocumentsService;
import mx.com.actinver.orquestador.util.CatalogIdsEnum;
import mx.com.actinver.orquestador.util.DynamicProperty;
import mx.com.actinver.orquestador.util.RestClient;
import mx.com.actinver.orquestador.constant.Decision;
import mx.com.actinver.orquestador.ws.generated.*;
import mx.com.actinver.orquestador.ws.util.PassthroughSoapClient;
import mx.com.actinver.orquestador.service.WsImagenesService;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.ObtenLoginResponse;
import mx.com.actinver.orquestador.ws.usuarios.RRespuesta;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;
import mx.com.actinver.orquestador.ws.util.SoapUtils;
import mx.com.actinver.orquestador.ws.util.WsImagenesPrefixMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WsImagenesServiceImpl implements WsImagenesService {
    private static final Logger LOG = LogManager.getLogger(WsImagenesServiceImpl.class);
    private static final Locale MX_LOCALE = new Locale("es", "MX");
    private static final DateTimeFormatter DIGITALIZATION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a", MX_LOCALE);


    private PassthroughSoapClient client;

    @Autowired
    private SoapUtils soapRequestUtils;

    @Autowired
    private RestClient restClient;

    @Autowired
    private SpRepEdcDao spRepEdcDao;

    @Autowired
    private DocumentsService docsService;

    @DynamicProperty("${id-portal.url}")
    private DynamicString idPortalUrl;

    @DynamicProperty("${migration.start.date}")
    private DynamicString migrationCutoverDate;

    @DynamicProperty("${api-stamp.url}")
    private DynamicString descargaCfdiServiceUrl;

    @DynamicProperty("${api-stamp.user}")
    private DynamicString apiStampUserName;

    @DynamicProperty("${api-stamp.password}")
    private DynamicString apiStampPassword;

    @DynamicProperty("${api-xsa.url}")
    private DynamicString xsaUrl;


    // ----------------- implementación existente (dominio) -----------------

    private IDTicket obtenLogin(String userID, String pwd, int proyectoID, String ip, String origen, Respuesta respuestaHolder) {
        // comportamiento demo (igual que antes)
        if ("demo".equals(userID) && "demo".equals(pwd)) {
            IDTicket t = new IDTicket();
            t.setTicketID("TICKET-12345");
            t.setUsrID(userID);
            t.setIp(ip);
            t.setProyectoID(proyectoID);
            try {
                XMLGregorianCalendar x = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(new GregorianCalendar());
                t.setFechaExpiracion(x);
            } catch (Exception ignored) {
            }
            respuestaHolder.setRespuestaID("0");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("OK");
            return t;
        } else {
            respuestaHolder.setRespuestaID("2002");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("Usuario o contraseña incorrecta");
            return null;
        }
    }


    private ArrayOfClsFileHSM contestaExpedientexLlave(IDTicket ticket, ClsLlaveExpediente llave, short proyID, short expedienteID, int tipoDocID, Respuesta respuestaHolder) {
        ArrayOfClsFileHSM arr = new ArrayOfClsFileHSM();

        LlaveMetadataDto metadata = soapRequestUtils.extraerLlaveMetadata(llave, tipoDocID);
//        Integer tipoDato = soapRequestUtils.extraerTipoDato(llave);
        LOG.info("metadata: {} tipoDocID: {}", metadata, tipoDocID);

        if (metadata == null) {
            respuestaHolder.setRespuestaID("1");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("No se pudo interpretar la llave del expediente");
            return arr;
        }

        List<FileVariant> variants = determineVariants(tipoDocID);
        LOG.info("Variants a solicitar: {}", variants);

        int consecutivo = 1;
        for (FileVariant variant : variants) {
            DescargaCfdiResponseDto fileResponse = invokeDescargaCfdi(metadata, variant.getFileType());
            if (fileResponse == null || fileResponse.getFileData() == null || fileResponse.getFileData().length == 0) {
                LOG.warn("Respuesta vacía del servicio descargaCfdi para tipo {}", variant.getFileType());
                continue;
            }
            ClsFileHSM file = toClsFileHsm(fileResponse, variant, consecutivo++);
            arr.getClsFileHSM().add(file);
        }

        if (arr.getClsFileHSM().isEmpty()) {
            respuestaHolder.setRespuestaID("0");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("No se encontraron documentos para la llave especificada");
        } else {
            respuestaHolder.setRespuestaID("0");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("Expediente obtenido correctamente");
        }
        return arr;
    }

    private List<FileVariant> determineVariants(int tipoDocID) {
        List<FileVariant> variants = new ArrayList<>();

        if (tipoDocID == 0) {
            variants.add(FileVariant.PDF);
            variants.add(FileVariant.XML);
        } else if (tipoDocID == 1) {
            variants.add(FileVariant.PDF);
        } else if (tipoDocID == 2) {
            variants.add(FileVariant.XML);
        } else if (tipoDocID == 3) {
            variants.add(FileVariant.PDF);
        }
        return variants;
    }

    private DescargaCfdiResponseDto invokeDescargaCfdi(LlaveMetadataDto metadata, String fileType) {


        DescargaCfdiRequestDto requestDto = buildDescargaCfdiRequest(metadata, fileType);
        if (requestDto == null) {
            LOG.warn("No se pudo construir la petición para descargaCfdi (metadata incompleta)");
            return null;
        }
        Long executor = 4L;  // hardcode temporal a un canal digital fijo
        try {
//            String token = restClient.getAccessToken(descargaCfdiServiceUrl + "/oauth/token", apiStampUserName.toString(), apiStampPassword.toString());
//
//            DescargaCfdiResponseDto response = restClient.executeExternalServiceRest(
//                    descargaCfdiServiceUrl + "/api/descargaCfdi",
//                    HttpMethod.GET,
//                    params,
//                    new ParameterizedTypeReference<DescargaCfdiResponseDto>() {
//                    },
//                    token
//            );
                // funcionalidad migrada
            LOG.info("getDescargaCfdi: {}", requestDto);
            requestDto.setValidityId(String.valueOf(CatalogIdsEnum.CON_VALIDEZ.getId()));
            return getDescargaCfdi(requestDto, executor);
        } catch (Exception e) {
            LOG.error("Error invocando descargaCfdi con fileType={}", fileType, e);
            return null;
        }
    }


    private DescargaCfdiRequestDto buildDescargaCfdiRequest(LlaveMetadataDto metadata, String fileType) {
        if (metadata == null) {
            return null;
        }
        return DescargaCfdiRequestDto.builder()
                .contractId(StringUtils.hasText(metadata.getContrato()) ? metadata.getContrato() : null)
                .businessId(StringUtils.hasText(metadata.getNegocio()) ? metadata.getNegocio() : null)
                .year(metadata.getYear() != null ? metadata.getYear().toString() : null)
                .month(metadata.getMonth() != null ? String.format("%02d", metadata.getMonth()) : null)
                .fileType(StringUtils.hasText(fileType) ? fileType.toUpperCase(Locale.ROOT) : null)
                .build();
    }

    private Map<String, Object> buildQueryParams(DescargaCfdiRequestDto requestDto) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (StringUtils.hasText(requestDto.getContractId())) {
            params.put("contractId", requestDto.getContractId());
        }
        if (StringUtils.hasText(requestDto.getYear())) {
            params.put("year", requestDto.getYear());
        }
        if (StringUtils.hasText(requestDto.getMonth())) {
            params.put("month", requestDto.getMonth());
        }
        if (StringUtils.hasText(requestDto.getBusinessId())) {
            params.put("businessId", requestDto.getBusinessId());
        }
        if (StringUtils.hasText(requestDto.getValidityId())) {
            params.put("validityId", requestDto.getValidityId());
        }
        if (StringUtils.hasText(requestDto.getFileType())) {
            params.put("fileType", requestDto.getFileType());
        }
        if (StringUtils.hasText(requestDto.getCredit())) {
            params.put("credit", requestDto.getCredit());
        }
        return params;
    }


    private ClsFileHSM toClsFileHsm(DescargaCfdiResponseDto response, FileVariant variant, int consecutivo) {
        ClsFileHSM file = new ClsFileHSM();
        file.setDocID((long) consecutivo);
        file.setDocPID(0L);
        file.setTipoDocID(variant.getTipoDocId());
        file.setTipoDocIdGrupo(0L);
        file.setDescripcion(determineDescripcion(response, variant));
        file.setConsecutivo(consecutivo);
        file.setSeparador(Boolean.FALSE);
        file.setExt(variant.getExtension());
        file.setFechaDigitalizacion(formatDigitalizationDate());
        file.setArrayFile(response.getFileData());
        file.setCreatedBy(0L);
        return file;
    }

    private String determineDescripcion(DescargaCfdiResponseDto response, FileVariant variant) {
        if (response != null && StringUtils.hasText(response.getFileName())) {
            return response.getFileName();
        }
        return variant.getDefaultDescription();
    }

    private String formatDigitalizationDate() {
        String formatted = LocalDateTime.now().format(DIGITALIZATION_DATE_FORMATTER);
        return formatted.replace('\u00A0', ' ');
    }

    private static final class FileVariant {
        private static final FileVariant PDF = new FileVariant("PDF", ".PDF", 1, "Presentación PDF(Documento Principal)");
        private static final FileVariant XML = new FileVariant("XML", ".XML", 2, "Presentación XML(Documento Principal)");

        private final String fileType;
        private final String extension;
        private final int tipoDocId;
        private final String defaultDescription;

        private FileVariant(String fileType, String extension, int tipoDocId, String defaultDescription) {
            this.fileType = fileType;
            this.extension = extension;
            this.tipoDocId = tipoDocId;
            this.defaultDescription = defaultDescription;
        }

        public String getFileType() {
            return fileType;
        }

        public String getExtension() {
            return extension;
        }

        public int getTipoDocId() {
            return tipoDocId;
        }

        public String getDefaultDescription() {
            return defaultDescription;
        }

        @Override
        public String toString() {
            return "FileVariant{" +
                    "fileType='" + fileType + '\'' +
                    ", extension='" + extension + '\'' +
                    ", tipoDocId=" + tipoDocId +
                    '}';
        }
    }

    private ClsFileHSM contestaFileHSM(long docID, int proyID, long expedienteID, IDTicket ticket, Respuesta respuestaHolder) {
        ClsFileHSM f = new ClsFileHSM();
        f.setDocID(docID);
        f.setDescripcion("Contenido binario demo");
        f.setArrayFile(new byte[]{0, 1, 2, 3});
        respuestaHolder.setRespuestaID("0");
        respuestaHolder.setCategoria("4000");
        respuestaHolder.setDescripcionRespuesta("OK");
        return f;
    }

    // ----------------- nuevos métodos: devuelven StreamSource listos para el endpoint -----------------

    @Override
    public StreamSource obtenLoginResponse(ObtenLoginRequest req) throws Exception {
        LOG.info("[Service] ObtenLogin called - user={}, proyectoID={}", req.getUserID(), req.getProyectoID());

        Respuesta r = new Respuesta();
        IDTicket t = obtenLogin(req.getUserID(), req.getStrPwd(), req.getProyectoID(),
                req.getIP(), req.getStrOrigen(), r);

        ObtenLoginResponse resp = new ObtenLoginResponse();
        resp.setIdTicket(t); // si t==null, se queda null

        // poblar RRespuesta tal como antes (si quieres leer desde r puedes mapearlo;
        // para mantener comportamiento previo lo dejamos hardcodeado como el endpoint anterior)
        RRespuesta rr = new RRespuesta();
        rr.setCodigo("2002");
        rr.setMensaje("Información");
        rr.setDetalle("(2002). Usuario o contraseña incorrecta");
        resp.setRRespuesta(rr);

        JAXBContext jc = JAXBContext.newInstance(ObtenLoginResponse.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WsImagenesPrefixMapper());

        StringWriter sw = new StringWriter();
        m.marshal(resp, sw);

        String xml = sw.toString();
        LOG.debug("[Service] ObtenLogin response XML (len={}): {}", xml.length(), xml.length() > 2000 ? xml.substring(0, 2000) + "...(truncated)" : xml);

        return new StreamSource(new StringReader(xml));
    }

    @Override
    public ResponseEntity<String> contestaExpedientexLlaveProcess(String rawXml, String op) throws Exception {
        Decision decision;
        ClsLlaveExpediente llaveExp = null;

        try {
            LOG.info("ContestaExpedientexLlave (rawXml start):\n{}", rawXml.length() > 2000 ? rawXml.substring(0, 2000) + "...(truncated)" : rawXml);
            ContestaExpedientexLlaveRequest requestObj = soapRequestUtils.unmarshalFromSoapBody(rawXml, ContestaExpedientexLlaveRequest.class);
            LOG.info("requestObj: {}", requestObj);

            llaveExp = requestObj != null ? requestObj.getLlave() : null;
            LOG.info("llaveExp: {}", llaveExp);
        } catch (Exception e) {
            LOG.error("Error unmarshalling ContestaExpedientexLlaveRequest", e);
        }
        LOG.info("migrationCutoverDate: {}", migrationCutoverDate);
        ChronoLocalDate corteHistorico = soapRequestUtils.parseCutoverDateOrDefault(migrationCutoverDate);
        LOG.info("corteHistorico: {}", corteHistorico);

        decision = soapRequestUtils.decide(op, llaveExp, corteHistorico);

        this.client = new PassthroughSoapClient(idPortalUrl.toString());
        LOG.info("decision: {}", decision);
        // Ejecutar según la decisión
        if (decision == Decision.MODERN) {

            LOG.info("peticion a Interna: ");
            // Procesamiento interno (consulta moderna)
            LOG.info("procesarInternamente: {}", op);

            ContestaExpedientexLlaveRequest req = ContestaExpedientexLlaveRequest.builder(rawXml).build();
            StreamSource streamSource = contestaExpedientexLlaveResponse(req);
            String respuestaXml = soapRequestUtils.streamSourceToString(streamSource);
            LOG.info("Respuesta interna generada para {}: {}", op, respuestaXml);
            return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(respuestaXml);

        } else {
            return bypass(rawXml);
        }

    }

    @Override
    public StreamSource contestaExpedientexLlaveResponse(ContestaExpedientexLlaveRequest req) throws Exception {
        LOG.info("[Service] ContestaExpedientexLlave - proyID={}, expedID={}, tipoDocID={}",
                req.getProyID(), req.getExpedienteID(), req.getTipoDocID());


        Respuesta r = new Respuesta();
        ArrayOfClsFileHSM arr = contestaExpedientexLlave(req.getTicket(), req.getLlave(),
                req.getProyID().shortValue(), req.getExpedienteID().shortValue(), req.getTipoDocID(), r);

        ContestaExpedientexLlaveResponse response = new ContestaExpedientexLlaveResponse();
        response.setContestaExpedientexLlaveResult(arr);
        response.setTicket(req.getTicket());
        response.setRRespuesta(r);

        JAXBContext jc = JAXBContext.newInstance(ContestaExpedientexLlaveResponse.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WsImagenesPrefixMapper());

        StringWriter sw = new StringWriter();
        m.marshal(response, sw);

        String responseXml = sw.toString();


        LOG.info("[Service] ContestaExpedientexLlave response (truncated 2000):\n{}",
                (responseXml.length() > 2000 ? responseXml.substring(0, 2000) + "...(truncated)" : responseXml));
        LOG.info("[Service] ContestaExpedientexLlave finished - items={}", arr != null ? arr.getClsFileHSM().size() : 0);

        return new StreamSource(new StringReader(responseXml));
    }

    @Override
    public StreamSource contestaFileHSMResponse(ContestaFileHSMRequest req) throws Exception {
        LOG.info("[Service] ContestaFileHSM - docID={}, proyID={}", req.getDocID(), req.getProyID());

        Respuesta r = new Respuesta();
        ClsFileHSM f = contestaFileHSM(req.getDocID(), req.getProyID(), req.getDocIDPadreExp(), req.getTicket(), r);

        ContestaFileHSMResponse response = new ContestaFileHSMResponse();
        response.setContestaFileHSMResult(f);
        response.setTicket(req.getTicket());
        response.setRRespuesta(r);

        JAXBContext jc = JAXBContext.newInstance(ContestaFileHSMResponse.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WsImagenesPrefixMapper());

        StringWriter sw = new StringWriter();
        m.marshal(response, sw);

        String responseXml = sw.toString();


        LOG.info("[Service] ContestaFileHSM response (truncated 2000):\n{}",
                (responseXml.length() > 2000 ? responseXml.substring(0, 2000) + "...(truncated)" : responseXml));
        LOG.info("[Service] ContestaFileHSM finished - returnedDocID={}", f != null ? f.getDocID() : null);

        return new StreamSource(new StringReader(responseXml));
    }

    @Override
    public ResponseEntity<String> bypass(String rawXml) {
        // Reenvío íntegro al sistema legacy
        try {
            LOG.info("peticion a idPortal: {}", idPortalUrl);
            String rawOut = client.invokeRaw(rawXml);
            return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(rawOut);
        } catch (IOException io) {
            // Error comunicando con legacy: devolver Fault SOAP
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_XML)
                    .body(buildSoapFault(io.getMessage()));
        }
    }

    public String buildSoapFault(String faultString) {

        // escapamos caracteres XML básicos por seguridad
        String msg = faultString == null ? "" :
                faultString.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");

        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<soap:Fault>" +
                "<faultcode>soap:Server</faultcode>" +
                "<faultstring xml:lang=\"es\">" + msg + "</faultstring>" +
                "</soap:Fault>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }


    public DescargaCfdiResponseDto getDescargaCfdi(DescargaCfdiRequestDto descargaCfdiRequestDto,Long executor) {
        DescargaCfdiResponseDto response = new DescargaCfdiResponseDto();

        try{
            AccountRequestEntity accountRequestEntity = AccountRequestEntity.builder()
                    .contractId(descargaCfdiRequestDto.getContractId())
                    .year(descargaCfdiRequestDto.getYear())
                    .month(descargaCfdiRequestDto.getMonth())
                    .businessId(descargaCfdiRequestDto.getBusinessId())
                    .validityId(descargaCfdiRequestDto.getValidityId())
                    .credit(descargaCfdiRequestDto.getCredit())
                    .build();

            AccountDto accountDto = new AccountDto();
            LOG.info("spRepEdcDao.find: {}", accountRequestEntity);
            RsDto<AccountEntity> rs =  spRepEdcDao.find(accountRequestEntity, executor);
            LOG.info("rs: {}", rs);
            if(descargaCfdiRequestDto.getCredit()!= null){
                if (rs.getContent() == null || rs.getContent().isEmpty() ){
                    LOG.warn("No se encontró información para el credito: {} ",descargaCfdiRequestDto.getCredit());
                    response.setMessage("No se encontró información para el credito: "+ descargaCfdiRequestDto.getCredit());
                    return response;
                }
                accountDto = AccountDto.builder(rs.getContent().get(0)).build();
                LOG.info("Respuesta de spRepEdcDao con crédito {}: {}", accountRequestEntity.getCredit(), accountDto.getXsaInfo());
            }else{
                if (rs.getFirst() == null ){
                    LOG.warn("No se encontró información para el contrato: {} ",descargaCfdiRequestDto.getContractId());
                    response.setMessage("No se encontró información para el contrato: "+ descargaCfdiRequestDto.getContractId());
                    return response;
                }
                LOG.info("Respuesta de spRepEdcDao {}", accountDto.getXsaInfo());
                accountDto = AccountDto.builder(rs.getFirst()).build();
            }

            LOG.info("accountDto: {}", accountDto);
            if (accountDto.getXsaInfo() == null || accountDto.getXsaInfo().getUuid() == null){
                LOG.warn("No se encontró el UUID para la solicitud: {}", descargaCfdiRequestDto);
                response.setMessage("No se encontró el UUID para la solicitud: {}" + descargaCfdiRequestDto);
                return response;
            }
            String uuid = accountDto.getXsaInfo().getUuid();
            String digitalDate = accountDto.getXsaInfo().getFecha();
            LOG.info("Respuesta de spRepEdcDao UUID: {}", uuid);
            LOG.info("uuid: {}", uuid);
            if (descargaCfdiRequestDto.getFileType().equalsIgnoreCase(FileType.ZIP.name())) {
                LOG.info("descargaZip: {}", uuid);
                String urlPdf = buildUrlXsa(FileType.PDF.extension, uuid);
                String urlXml = buildUrlXsa(FileType.XML.extension, uuid);

                byte[] fileContentsPDF = descargarCfdiFileXsa(descargaCfdiRequestDto,accountDto.getVersion(), executor);
                byte[] fileContentsXML = restClient.descargarCfdiFile(urlXml);

                response.setFileName(buildFileName(uuid, FileType.ZIP.extension));
                response.setFileDescription(FileType.ZIP.description);
                response.setFileType(FileType.ZIP.extension);
                response.setDigitalDate(digitalDate);

                response.setFileData(createZip(fileContentsPDF, buildFileName(uuid, FileType.PDF.extension),
                        fileContentsXML,  buildFileName(uuid , FileType.XML.extension)));
                return response;
            }else{
                byte[] fileContents = null;
                if (descargaCfdiRequestDto.getFileType().equalsIgnoreCase(FileType.PDF.name())) {
                    LOG.info("descargaPDF: {}", uuid);
                    fileContents = descargarCfdiFileXsa(descargaCfdiRequestDto,accountDto.getVersion(), executor);
                }else{
                    LOG.info("descargaXML: {}", uuid);
                    fileContents = restClient.descargarCfdiFile(buildUrlXsa(descargaCfdiRequestDto.getFileType(), uuid));

                }
                if (fileContents != null) {
                    response.setFileName(buildFileName(accountDto.getXsaInfo().getUuid(), descargaCfdiRequestDto.getFileType()));
                    response.setFileData(fileContents);
                    response.setFileType(descargaCfdiRequestDto.getFileType().toLowerCase());
                    response.setFileDescription(FileType.fromExtension(descargaCfdiRequestDto.getFileType()).getDescription());
                    response.setDigitalDate(digitalDate);
                    return response;
                }
            }
        } catch (Exception e) {
            LOG.error("Error al obtener el cfdi: ", e);
            response.setFileData(null);
            response.setMessage("Error al obtener el cfdi: " + e.getMessage());
        }
        return response;
    }

    private String buildUrlXsa(String fileType, String uuid){
        return xsaUrl.toString()
                + "/descargas?"
                +"type="+fileType.toUpperCase()
                +"&tipo=I"
                +"&uuid="+uuid;
    }

    enum FileType {
        PDF("pdf", "Presentación PDF(Documento Principal)"),
        XML("xml", "Presentación XML(Represtacion xml de cfdi)"),
        ZIP("zip", "Presentación ZIP(Documento empaquetado)");
        private final String extension;

        private final String description;

        FileType(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }

        public static FileType fromExtension(String extension) {
            return Arrays.stream(values())
                    .filter(type -> type.extension.equalsIgnoreCase(extension))
                    .findFirst()
                    .orElse(null);
        }
    }
    public byte[] descargarCfdiFileXsa(DescargaCfdiRequestDto descargaCfdiRequestDto,
                                       TemplateContent templateContent,
                                       Long executor) {
        DocumentResponseDto  responseDto  = null;
        try{
            AccStmtRequestDto input = AccStmtRequestDto.builder()
                    .year(Long.valueOf(descargaCfdiRequestDto.getYear()))
                    .month(Long.valueOf(descargaCfdiRequestDto.getMonth()))
                    .business(Long.valueOf(descargaCfdiRequestDto.getBusinessId()))
                    .contract(Long.valueOf(descargaCfdiRequestDto.getContractId()))
                    .credit(Long.valueOf(descargaCfdiRequestDto.getCredit()))
                    .validity(Long.valueOf(descargaCfdiRequestDto.getValidityId())) //CatalogIdsEnum.CON_VALIDEZ.getId()
                    .build();

            responseDto = docsService.exsDocumenter(input,templateContent,
                    executor);

            return responseDto.getFileContent();
        }catch (Exception e){
            LOG.error("Error al obtener el cfdi: ", e);
        }
        return null;
    }

    private byte[] createZip(byte[] file1, String name1, byte[] file2, String name2) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry1 = new ZipEntry(name1);
            zos.putNextEntry(entry1);
            zos.write(file1);
            zos.closeEntry();

            ZipEntry entry2 = new ZipEntry(name2);
            zos.putNextEntry(entry2);
            zos.write(file2);
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        }
    }

    private String buildFileName(String uuid, String fileType){
        return uuid + "." + fileType.toLowerCase();
    }
}
