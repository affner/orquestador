package mx.com.actinver.conf;

import mx.com.actinver.orquestador.dao.DbPropertiesDao;

public class DynamicString {
    private final String key;
    private final DbPropertiesDao dbPropertiesDao;

    public DynamicString(String key, DbPropertiesDao dbPropertiesDao) {
        this.key = key;
        this.dbPropertiesDao = dbPropertiesDao;
    }

    @Override
    public String toString() {
        return dbPropertiesDao.getLiveProperty(key);
    }
}
