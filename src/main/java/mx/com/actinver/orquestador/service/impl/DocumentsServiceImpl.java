package mx.com.actinver.orquestador.service.impl;


import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.common.exception.ForbiddenException;
import mx.com.actinver.conf.DynamicString;
import mx.com.actinver.orquestador.dto.DocumentResponseDto;
import mx.com.actinver.orquestador.dto.OnDemandDto;
import mx.com.actinver.orquestador.dto.TemplateContent;
import mx.com.actinver.orquestador.service.DocumentsService;
import mx.com.actinver.orquestador.util.DynamicProperty;
import mx.com.actinver.orquestador.util.FilenameHelper;
import mx.com.actinver.orquestador.util.MappingHelper;
import mx.com.actinver.orquestador.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class DocumentsServiceImpl implements DocumentsService {

	private static final Logger LOG = LogManager.getLogger();

    @Autowired
    private RestClient restClient;

	@DynamicProperty("${api-extream.url}")
    private DynamicString apiExsUrl;

	@DynamicProperty("${api-extream.user}")
    private DynamicString apiExsUsr;

	@DynamicProperty("${api-extream.password}")
    private DynamicString apiExsPwd;

	/**
	 * Permite llamar al servicio del API Exstream para crear el documento.
	 * 
	 * @param <T>      Tipo de entrada.
	 * @param input    Dato de la solicitud.
	 * @param template Datos de la plantilla.
	 * @param executor Identificador del usuario ejecutor
	 * @return {@link DocumentResponseDto}
	 */
	public <T> DocumentResponseDto exsDocumenter(T input, TemplateContent template, Long executor) {
		DocumentResponseDto rs = DocumentResponseDto.builder().build();
		
		try {
			OnDemandDto.Request<T> request = OnDemandDto.Request.<T>builder()
					.executor(executor)
					.templateId(template.getTemplateId())
					.templateVersion(template.getTemplateVersion())
					.target(input)
					.build();
			String jwt = restClient.getAccessToken(apiExsUrl + "/oauth/token", apiExsUsr.toString(), apiExsPwd.toString());
			String url = apiExsUrl + "/api/on-demand/documenters";
			HttpMethod method = HttpMethod.POST;
			ParameterizedTypeReference<RsDto<OnDemandDto.Response>> rsType = new ParameterizedTypeReference<RsDto<OnDemandDto.Response>>() {
			};
			
			LOG.info("[{}][{}] -> {}", method, url, MappingHelper.toJson(request));

			RsDto<OnDemandDto.Response> srvRs = restClient.executeExternalService(url, method, request, rsType, jwt);

			if (srvRs.isNotEmpty()) {
				String processId = srvRs.getFirst().getProcessId();
				String filePath = srvRs.getFirst().getFiles().get(0).getPath();

				rs.setProcessId(processId);
				rs.setFileName(FilenameHelper.getFilenameWithExtension(filePath));
				rs.setFileContent(Files.readAllBytes(Paths.get(filePath)));
			}
		} catch (IOException e) {
			LOG.error("Error al leer el archivo: {}", e);
			throw new ForbiddenException("Error al leer el archivo", e);
		} catch (Exception e) {
			LOG.error("Error al generar el documento: {}", e);
			throw new RuntimeException("Error al generar el documento", e);
		}

		return rs;
	}
	
}