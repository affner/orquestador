package mx.com.actinver.orquestador.dao.impl;

import lombok.NoArgsConstructor;
import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.dao.AuditLogDao;
import mx.com.actinver.orquestador.entity.AuditLogEntity;
import mx.com.actinver.orquestador.util.SpActionEnum;
import mx.com.actinver.orquestador.util.SpConfigEnum;
import mx.com.actinver.orquestador.util.SpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@NoArgsConstructor(force = true)
@Repository
public class AuditLogDaoImpl implements AuditLogDao {

    @Autowired
    @Qualifier("localDataSource")
    private DataSource dataSource;

    @Override
    public RsDto<AuditLogEntity> find(AuditLogEntity input, Long executor) {
        return SpHelper.builder(dataSource, SpConfigEnum.PROCESS_PROGRESSES_CRUD, AuditLogEntity.class)
                .input(input)
                .action(SpActionEnum.LIST)
                .executor(executor)
                .build()
                .execute();
    }

    @Override
    public RsDto<AuditLogEntity> save(AuditLogEntity input, Long executor) {
        return SpHelper.builder(dataSource, SpConfigEnum.PROCESS_PROGRESSES_CRUD, AuditLogEntity.class)
                .input(input)
                .action(SpActionEnum.SAVE)
                .executor(executor)
                .build()
                .execute();
    }

}
