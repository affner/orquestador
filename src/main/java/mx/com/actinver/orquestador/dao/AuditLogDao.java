package mx.com.actinver.orquestador.dao;

import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.entity.AuditLogEntity;

public interface AuditLogDao {

    RsDto<AuditLogEntity> find(AuditLogEntity input, Long executor);

    RsDto<AuditLogEntity> save(AuditLogEntity input, Long executor);
}
