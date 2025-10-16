package mx.com.actinver.orquestador.dao.impl;

import lombok.NoArgsConstructor;
import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.dao.GenericCrudDao;
import mx.com.actinver.orquestador.util.SpActionEnum;
import mx.com.actinver.orquestador.util.SpConfigEnum;
import mx.com.actinver.orquestador.util.SpHelper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@NoArgsConstructor(force = true)
@Repository
public abstract class AbstractCrudDaoImpl<T> implements GenericCrudDao<T> {

    private final DataSource dataSource;
    private final Class<T> entityClass;

    protected AbstractCrudDaoImpl(DataSource dataSource, Class<T> entityClass) {
        this.dataSource = dataSource;
        this.entityClass = entityClass;
    }

    /**
     * Cada DAO concreto indicará cuál es su SP a ejecutar.
     * Puede ser un String literal o algo que venga de un enum,
     * pero la idea es que aquí quede definido.
     */
    protected abstract SpConfigEnum getStoredProcedureCfg();

    private RsDto<T> crudAction(T input, SpActionEnum action, Long executor) {
        // Aquí usas SpHelper o tu lógica preferida para invocar el SP
        return SpHelper.builder(dataSource, getStoredProcedureCfg(), entityClass)
                .input(input)
                .action(action)
                .executor(executor)
                .build()
                .execute();
    }

    @Override
    public RsDto<T> find(T input, Long executor) {
        return crudAction(input, SpActionEnum.LIST, executor);
    }

    @Override
    public RsDto<T> save(T input, Long executor) {
        return crudAction(input, SpActionEnum.SAVE, executor);
    }

    @Override
    public RsDto<T> update(T input, Long executor) {
        return crudAction(input, SpActionEnum.UPDATE, executor);
    }

    @Override
    public RsDto<T> delete(T input, Long executor) {
        return crudAction(input, SpActionEnum.DELETE, executor);
    }
}

