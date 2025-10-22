package mx.com.actinver.orquestador.service;

import mx.com.actinver.orquestador.dto.DocumentResponseDto;
import mx.com.actinver.orquestador.dto.TemplateContent;

public interface DocumentsService {


	<T> DocumentResponseDto exsDocumenter(T input, TemplateContent template, Long executor);
	}
