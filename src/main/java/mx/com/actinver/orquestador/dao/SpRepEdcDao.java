package mx.com.actinver.orquestador.dao;

import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.entity.AccountEntity;
import mx.com.actinver.orquestador.entity.AccountRequestEntity;

public interface SpRepEdcDao {

    RsDto<AccountEntity> find(AccountRequestEntity input, Long executor);
}
