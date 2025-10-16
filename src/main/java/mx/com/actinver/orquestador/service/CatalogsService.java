package mx.com.actinver.orquestador.service;

import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.dto.CatalogsDto;

public interface CatalogsService {

	/**
	 * 
	 * @param input
	 * @param executor
	 * @return
	 */
	RsDto<CatalogsDto> find(CatalogsDto input, Long executor);

	<T> T getConfigByTypeIdBusinessId(Long executor, Long businessId, String fieldName, Class<T> clss);
}
