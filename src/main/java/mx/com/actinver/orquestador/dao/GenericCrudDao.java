package mx.com.actinver.orquestador.dao;

import mx.com.actinver.common.dto.RsDto;

public interface GenericCrudDao<T> {

    RsDto<T> find(T input, Long executor);

    RsDto<T> save(T input, Long executor);

    RsDto<T> update(T input, Long executor);

    RsDto<T> delete(T input, Long executor);
}
