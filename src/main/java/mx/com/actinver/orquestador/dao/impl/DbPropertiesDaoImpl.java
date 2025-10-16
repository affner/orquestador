package mx.com.actinver.orquestador.dao.impl;

import mx.com.actinver.orquestador.dao.DbPropertiesDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
public class DbPropertiesDaoImpl implements DbPropertiesDao {

    private static final Logger LOG = LogManager.getLogger(DbPropertiesDaoImpl.class);

    @Autowired
    @Qualifier("localDataSource")
    private DataSource dataSource;

    @Override
    public String getLiveProperty(String key) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT JSON_VALUE(prop.FCVALUE, '$.value') AS VALUE " +
                             "FROM USR_EXS_PORTAL.TA_CATALOG prop WHERE prop.FIPARENTID = 3 AND prop.FISTATUS = 1 AND JSON_VALUE(prop.FCVALUE, '$.name') = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (Exception e) {
            LOG.error("Error Obteniendo Properties: {}", e.getMessage());
        }
        return null;
    }
}