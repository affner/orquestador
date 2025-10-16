package mx.com.actinver.orquestador.dao.impl;

import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.entity.ProductionsEntity;
import mx.com.actinver.orquestador.util.SpConfigEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@NoArgsConstructor(force = true)
@Repository
public class ProductionsDaoImpl extends AbstractCrudDaoImpl<ProductionsEntity> {

    @Autowired
    public ProductionsDaoImpl(@Qualifier("localDataSource") DataSource dataSource) {
        super(dataSource, ProductionsEntity.class);
    }

    @Override
    protected SpConfigEnum getStoredProcedureCfg() {
        return SpConfigEnum.PRODUCTIONS_CRUD;
    }

}
