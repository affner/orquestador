package mx.com.actinver.orquestador.dao.impl;

import lombok.NoArgsConstructor;
import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.dao.CatalogDao;
import mx.com.actinver.orquestador.entity.CatalogsEntity;
import mx.com.actinver.orquestador.util.SpActionEnum;
import mx.com.actinver.orquestador.util.SpConfigEnum;
import mx.com.actinver.orquestador.util.SpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@NoArgsConstructor(force = true)
@Repository
public class CatalogDaoImpl implements CatalogDao {

    @Autowired
	@Qualifier("localDataSource")
    private DataSource dataSource;

	private static final String SQL =
			"SELECT c.FCVALUE " +
					" FROM   USR_EXS_PORTAL.TA_CATALOG c " +
					" WHERE  c.FICATALOGID = ? " +
					"  AND  c.FISTATUS    = 1 ";

    @Override
    public RsDto<CatalogsEntity> find(CatalogsEntity input, Long executor) {
		return SpHelper.builder(dataSource, SpConfigEnum.CATALOGS_CRUD, CatalogsEntity.class)
				.input(input)
				.action(SpActionEnum.LIST)
				.executor(executor)
				.build()
				.execute();
    }

	@Override
	public RsDto<CatalogsEntity> save(CatalogsEntity input, Long executor) {
		return SpHelper.builder(dataSource, SpConfigEnum.CATALOGS_CRUD, CatalogsEntity.class)
				.input(input)
				.action(SpActionEnum.SAVE)
				.executor(executor)
				.build()
				.execute();
	}

	@Override
	public RsDto<CatalogsEntity> update(CatalogsEntity input, Long executor) {
		return SpHelper.builder(dataSource, SpConfigEnum.CATALOGS_CRUD, CatalogsEntity.class)
				.input(input)
				.action(SpActionEnum.UPDATE)
				.executor(executor)
				.build()
				.execute();
	}

	@Override
	public RsDto<CatalogsEntity> delete(CatalogsEntity input, Long executor) {
		return SpHelper.builder(dataSource, SpConfigEnum.CATALOGS_CRUD, CatalogsEntity.class)
				.input(input)
				.action(SpActionEnum.DELETE)
				.executor(executor)
				.build()
				.execute();
	}

	
}