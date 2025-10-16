package mx.com.actinver.orquestador.dao;

import java.util.List;

public interface ProdTmpDao {

    void batchInsert(List<List<Object>> batchData, Integer batchSize);

    void batchUpdate(List<List<Object>> batchData, Integer batchSize);

}