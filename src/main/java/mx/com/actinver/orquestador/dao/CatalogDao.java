package mx.com.actinver.orquestador.dao;

import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.entity.CatalogsEntity;

public interface CatalogDao {

	/**
	 * 
	 * @param input
	 * @param executor
	 * @return
	 */
	RsDto<CatalogsEntity> find(CatalogsEntity input, Long executor);

	/**
	 * 
	 * @param input
	 * @param executor
	 * @return
	 */
	RsDto<CatalogsEntity> save(CatalogsEntity input, Long executor);

	/**
	 * 
	 * @param input
	 * @param executor
	 * @return
	 */
	RsDto<CatalogsEntity> update(CatalogsEntity input, Long executor);

	/**
	 * 
	 * @param input
	 * @param executor
	 * @return
	 */
	RsDto<CatalogsEntity> delete(CatalogsEntity input, Long executor);

}