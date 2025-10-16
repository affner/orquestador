package mx.com.actinver.orquestador.dao;

import mx.com.actinver.orquestador.dto.ProdScheduleDto;

import java.util.List;

public interface ProdScheduleDao {

    List<ProdScheduleDto> fetchToday();
}
