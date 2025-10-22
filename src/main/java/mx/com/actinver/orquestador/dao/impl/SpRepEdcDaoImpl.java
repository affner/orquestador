package mx.com.actinver.orquestador.dao.impl;

import lombok.NoArgsConstructor;
import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.orquestador.dao.SpRepEdcDao;
import mx.com.actinver.orquestador.entity.AccountEntity;
import mx.com.actinver.orquestador.entity.AccountRequestEntity;
import mx.com.actinver.orquestador.util.SpAccountExecutor;
import mx.com.actinver.orquestador.util.SpConfigEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@NoArgsConstructor(force = true)
@Repository
public class SpRepEdcDaoImpl implements SpRepEdcDao {

    @Autowired
    private SpAccountExecutor spAccountExecutor;

    @Override
    public RsDto<AccountEntity> find(AccountRequestEntity input, Long executor) {
        return spAccountExecutor.callSp(
                SpConfigEnum.ACC_STMT_FINDER.getStmt(),
            input,
            executor,
            AccountEntity.class,
                false
            );
    }

}
