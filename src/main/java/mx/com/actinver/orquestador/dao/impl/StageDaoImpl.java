package mx.com.actinver.orquestador.dao.impl;

import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.dao.StageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@NoArgsConstructor(force = true)
@Repository
public class StageDaoImpl implements StageDao {

    @Autowired
    @Qualifier("localDataSource")
    private DataSource dataSource;


//    @Override
//    public CancelationRequestDto getStageByIdProd(Long idProd, int flowId) {
//        String sql = "SELECT s.FJDATA " +
//                "FROM USR_EXS_PORTAL.TA_PROD_STAGES s " +
//                "WHERE s.FIPRODID = ? AND s.FILINEID = ?";
//
//        try (Connection conn = DataSourceUtils.getConnection(dataSource);
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setLong(1, idProd);
//            ps.setInt(2, flowId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    String json = rs.getString("FJDATA");
//                    return MappingHelper.toClass(json, CancelationRequestDto.class);
//                }
//            }
//
//        } catch (Exception ex) {
//            throw new RuntimeException("Error al obtener y parsear FJDATA para idProd=" + idProd + ", flowId=" + flowId, ex);
//        }
//
//        return null;
//    }

}
