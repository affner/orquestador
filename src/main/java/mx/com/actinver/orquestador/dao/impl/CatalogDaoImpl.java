package mx.com.actinver.orquestador.dao.impl;

import mx.com.actinver.orquestador.dao.CatalogDao;
import mx.com.actinver.orquestador.entity.CatalogsEntity;
import mx.com.actinver.orquestador.util.SpConfigEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class CatalogDaoImpl extends AbstractCrudDaoImpl<CatalogsEntity> implements CatalogDao {

	@Autowired
	public CatalogDaoImpl(@Qualifier("localDataSource") DataSource dataSource) {
		super(dataSource, CatalogsEntity.class);
	}

	@Override
	protected SpConfigEnum getStoredProcedureCfg() {
		return SpConfigEnum.CATALOGS_CRUD;
	}
}