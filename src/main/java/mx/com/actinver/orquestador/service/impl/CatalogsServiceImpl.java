package mx.com.actinver.orquestador.service.impl;

import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.common.exception.NotFoundException;

import mx.com.actinver.orquestador.dao.CatalogDao;
import mx.com.actinver.orquestador.dto.CatalogsDto;
import mx.com.actinver.orquestador.entity.CatalogsEntity;
import mx.com.actinver.orquestador.service.CatalogsService;
import mx.com.actinver.orquestador.util.MappingHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatalogsServiceImpl implements CatalogsService {

	@Autowired
	private CatalogDao catalogDao;

	@Override
	public RsDto<CatalogsDto> find(CatalogsDto input, Long executor) {
		RsDto<CatalogsEntity> rs = catalogDao.find(CatalogsEntity.builder(input).build(), executor);

		if (rs.getContent().isEmpty()) {
			throw new NotFoundException("Catalog Not Found");
		}

		return rs.map(e -> CatalogsDto.builder(e).build());
	}




	/**
	 * Permite encontrar configuraciones especificas. <br>
	 * Orden de busqueda:
	 * <ol>
	 * <li>Por tipo de estado</li>
	 * <li>Por negocio</li>
	 * </ol>
	 *
	 * @param <T>       Tipo de salida
	 * @param executor  Identificador del ejecutor
	 * @param businessId   businessId
	 * @param fieldName Campo objetivo
	 * @param clss      Clase de salida
	 * @return Clase de salida con las configuraciones.
	 */
	@Override
	public <T> T getConfigByTypeIdBusinessId(Long executor, Long businessId, String fieldName, Class<T> clss) {
		T rs = null;

		// 1. Buscar por business

		CatalogsDto	filters = CatalogsDto.builder().catalogId(businessId).build();
		CatalogsDto cfg = find(filters, executor).getFirst();

			if (StringUtils.isNotBlank(cfg.getValue())) {
				rs = MappingHelper.byFieldToClass(cfg.getValue(), fieldName, clss);
			}


		return rs;
	}

}