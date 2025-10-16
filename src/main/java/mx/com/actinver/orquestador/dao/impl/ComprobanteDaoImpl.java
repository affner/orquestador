package mx.com.actinver.orquestador.dao.impl;

import mx.com.actinver.orquestador.dao.ComprobanteDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Implementación de la interfaz {@link ComprobanteDao} que proporciona métodos para acceder a la información de los comprobantes
 * almacenados en la base de datos.
 *
 * Esta clase se encarga de construir consultas SQL dinámicas basadas en los filtros proporcionados en el {@link ComprobanteRequestDto}
 * y de ejecutar estas consultas para obtener los resultados paginados.
 */
@Repository
public class ComprobanteDaoImpl implements ComprobanteDao {

    /**
     * DataSource para la conexión a la base de datos XSA.
     */
    @Autowired
    @Qualifier("xsaDataSource")
    private DataSource xsaDataSource;

//    /**
//     * Construye la cláusula WHERE de la consulta SQL basándose en los filtros proporcionados en el {@link ComprobanteRequestDto}.
//     *
//     * @param sqlBuilder   StringBuilder que contiene la consulta SQL base.
//     * @param params       Lista de parámetros que se utilizarán en la consulta preparada.
//     * @param requestDto   DTO que contiene los filtros para la consulta.
//     * @return La cláusula WHERE construida.
//     */
//    private String buildWhereClause(StringBuilder sqlBuilder, List<Object> params, ComprobanteRequestDto requestDto) {
//        if (requestDto != null) {
//            if (requestDto.getIdComprobante() != null) {
//                sqlBuilder.append(" AND ID_COMPROBANTE = ?");
//                params.add(requestDto.getIdComprobante());
//            }
//
//            if (requestDto.getUuid() != null) {
//                sqlBuilder.append(" AND UUID = ?");
//                params.add(requestDto.getUuid());
//            }
//
//            if (requestDto.getFechaCertificacion() != null) {
//                sqlBuilder.append(" AND TRUNC(FECHA_CERTIFICACION) = TO_DATE(?,'YYYY-MM-DD')");
//                params.add(requestDto.getFechaCertificacion());
//            }
//        }
//        return sqlBuilder.toString();
//    }

    /**
     * Establece los parámetros en el PreparedStatement basándose en la lista de objetos proporcionada.
     *
     * @param ps     PreparedStatement en el que se establecerán los parámetros.
     * @param params Lista de parámetros que se establecerán en el PreparedStatement.
     * @throws java.sql.SQLException Si ocurre un error al establecer los parámetros.
     */
    private void setParameters(PreparedStatement ps, List<Object> params) throws java.sql.SQLException {
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Long) {
                ps.setLong(paramIndex, (Long) param);
            } else if (param instanceof String) {
                ps.setString(paramIndex, (String) param);
            } else if (param instanceof Timestamp) {
                ps.setTimestamp(paramIndex, (Timestamp) param);
            } else if (param instanceof Integer) {
                ps.setInt(paramIndex, (Integer) param);
            }
            paramIndex++;
        }
    }
//
//    /**
//     * Obtiene una lista de comprobantes basándose en los filtros proporcionados en el {@link ComprobanteRequestDto}.
//     *
//     * @param comprobanteRequestDto DTO que contiene los filtros para la consulta.
//     * @return Una lista de {@link ComprobanteResultDto} que cumplen con los filtros proporcionados.
//     * @throws RuntimeException Si ocurre un error al ejecutar la consulta o al procesar los resultados.
//     */
//    @Override
//    public List<ComprobanteResultDto> getComprobantesByFilter(ComprobanteRequestDto comprobanteRequestDto) {
//        List<ComprobanteResultDto> comprobantesResponseDtoList = new ArrayList<>();
//
//        StringBuilder baseSqlBuilder = new StringBuilder("SELECT ID_COMPROBANTE, NUM_CUENTA ,SERIE, RFC_EMISOR, RFC_RECEPTOR, FECHA, ")
//                .append("PERIODO, FECHA_CERTIFICACION, UUID, NUM_CERTIFICADO, CADENA_ORIGINAL, SELLO_CFDI, ")
//                .append("SELLO_SAT, QRCODE, XML_SAT, XML_CFDI, TIPO, ESTATUS_FISCAL, TOTAL FROM USR_XSA1.COMPROBANTES WHERE 1=1");
//
//        List<Object> params = new ArrayList<>();
//        String whereClause = buildWhereClause(baseSqlBuilder, params, comprobanteRequestDto);
//
//        int pageNumber = (comprobanteRequestDto != null && comprobanteRequestDto.getPageNumber() != null) ? comprobanteRequestDto.getPageNumber() : 0;
//        int pageSize = (comprobanteRequestDto != null && comprobanteRequestDto.getPageSize() != null && comprobanteRequestDto.getPageSize() > 0) ? comprobanteRequestDto.getPageSize() : 10;
//
//        int startRow = pageNumber * pageSize;
//        int endRow = startRow + pageSize;
//
//        String paginatedSqlBuilder = "SELECT * FROM (" +
//                " SELECT a.*, ROWNUM rnum FROM (" +
//                whereClause +
//                " ORDER BY ID_COMPROBANTE ASC" +
//                ") a WHERE ROWNUM <= ?" +
//                ") WHERE rnum > ?";
//
//        params.add(endRow);
//        params.add(startRow);
//
//        String finalSql = paginatedSqlBuilder;
//
//        try (
//                Connection xsaConn = xsaDataSource.getConnection();
//                PreparedStatement selectPs = xsaConn.prepareStatement(finalSql)
//        ) {
//            setParameters(selectPs, params);
//
//            try (ResultSet rs = selectPs.executeQuery()) {
//                while (rs.next()) {
//                    ComprobanteResultDto comprobanteResultDto = new ComprobanteResultDto();
//                    comprobanteResultDto.setIdComprobante(rs.getLong("ID_COMPROBANTE"));
//                    comprobanteResultDto.setNumCuenta(rs.getString("NUM_CUENTA"));
//                    comprobanteResultDto.setSerie(rs.getString("SERIE"));
//                    comprobanteResultDto.setRfcEmisor(rs.getString("RFC_EMISOR"));
//                    comprobanteResultDto.setRfcReceptor(rs.getString("RFC_RECEPTOR"));
//                    comprobanteResultDto.setFecha(rs.getString("FECHA"));
//                    comprobanteResultDto.setPeriodo(rs.getString("PERIODO"));
//                    comprobanteResultDto.setFechaCertificacion(rs.getDate("FECHA_CERTIFICACION"));
//                    comprobanteResultDto.setUuid(rs.getString("UUID"));
//                    comprobanteResultDto.setNumCertificado(rs.getString("NUM_CERTIFICADO"));
//                    comprobanteResultDto.setCadenaOriginal(rs.getString("CADENA_ORIGINAL"));
//                    comprobanteResultDto.setSelloCfdi(rs.getString("SELLO_CFDI"));
//                    comprobanteResultDto.setSelloSat(rs.getString("SELLO_SAT"));
//                    comprobanteResultDto.setQrCode(rs.getString("QRCODE"));
//                    comprobanteResultDto.setEstatus(rs.getString("ESTATUS_FISCAL"));
//                    comprobanteResultDto.setXmlCfdi(rs.getBytes("XML_CFDI"));
//                    comprobanteResultDto.setXmlSat(rs.getBytes("XML_SAT"));
//                    comprobanteResultDto.setTotal(rs.getString("TOTAL"));
//
//                    comprobantesResponseDtoList.add(comprobanteResultDto);
//                }
//
//            } catch (SQLException e) { // Captura SQLException específica para ejecución de consulta
//                throw new RuntimeException("Error al ejecutar la consulta SQL para obtener comprobantes. Mensaje: " + e.getMessage(), e);
//            } catch (Exception e) {
//                throw new RuntimeException("Error inesperado durante el procesamiento del ResultSet para obtener comprobantes: " + e.getMessage(), e);
//            }
//        } catch (SQLTransientConnectionException e) { // Específico para problemas de conexión transitorios
//            throw new RuntimeException("No fue posible establecer una conexión temporal con la base de datos para obtener comprobantes. Intente de nuevo.", e);
//        } catch (SQLException e) { // Otros errores SQL
//            // Oracle JDBC driver codes for common connection issues (check your specific driver docs)
//            String sqlState = e.getSQLState();
//            int errorCode = e.getErrorCode();
//
//            if (sqlState != null && sqlState.startsWith("08")) { // SQLState 08 is for connection errors
//                throw new RuntimeException("Error de comunicación con la base de datos al obtener comprobantes. Verifique la disponibilidad.", e);
//            } else if (errorCode == 1033 || errorCode == 1034 || errorCode == 12505 || errorCode == 12541 || errorCode == 17002) {
//                throw new RuntimeException("La base de datos no está disponible o la conexión ha sido rechazada para obtener comprobantes.", e);
//            } else {
//                // Otros errores SQL que no son de conexión directa
//                throw new RuntimeException("Error en la base de datos al obtener comprobantes. Contacte a soporte. Mensaje: " + e.getMessage(), e);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Ocurrió un error inesperado en el DAO al obtener comprobantes: " + e.getMessage(), e);
//        }
//        return comprobantesResponseDtoList;
//    }
//
//    /**
//     * Cuenta el número de comprobantes que cumplen con los filtros proporcionados en el {@link ComprobanteRequestDto}.
//     *
//     * @param comprobanteRequestDto DTO que contiene los filtros para la consulta.
//     * @return El número de comprobantes que cumplen con los filtros proporcionados.
//     * @throws RuntimeException Si ocurre un error al ejecutar la consulta o al procesar los resultados.
//     */
//    @Override
//    public long countComprobantesByFilter(ComprobanteRequestDto comprobanteRequestDto) {
//        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM USR_XSA1.COMPROBANTES WHERE 1=1");
//        List<Object> params = new ArrayList<>();
//        String whereClause = buildWhereClause(countSqlBuilder, params, comprobanteRequestDto);
//
//        String finalCountSql = countSqlBuilder.toString();
//        long count = 0;
//
//        try (
//                Connection xsaConn = xsaDataSource.getConnection();
//                PreparedStatement countPs = xsaConn.prepareStatement(finalCountSql)
//        ) {
//            setParameters(countPs, params);
//
//            try (ResultSet rs = countPs.executeQuery()) {
//                if (rs.next()) {
//                    count = rs.getLong(1);
//                }
//            } catch (SQLException e) {
//                throw new RuntimeException("Error al ejecutar la consulta SQL para contar comprobantes. Mensaje: " + e.getMessage(), e);
//            } catch (Exception e) {
//                throw new RuntimeException("Error inesperado durante el procesamiento del ResultSet para contar comprobantes: " + e.getMessage(), e);
//            }
//        } catch (SQLTransientConnectionException e) {
//            throw new RuntimeException("No fue posible establecer una conexión temporal con la base de datos para contar comprobantes. Intente de nuevo.", e);
//        } catch (SQLException e) {
//            String sqlState = e.getSQLState();
//            int errorCode = e.getErrorCode();
//
//            if (sqlState != null && sqlState.startsWith("08")) {
//                throw new RuntimeException("Error de comunicación con la base de datos al contar comprobantes. Verifique la disponibilidad.", e);
//            } else if (errorCode == 1033 || errorCode == 1034 || errorCode == 12505 || errorCode == 12541 || errorCode == 17002) {
//                throw new RuntimeException("La base de datos no está disponible o la conexión ha sido rechazada para contar comprobantes.", e);
//            } else {
//                throw new RuntimeException("Error en la base de datos al contar comprobantes. Contacte a soporte. Mensaje: " + e.getMessage(), e);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Ocurrió un error inesperado en el DAO al contar comprobantes: " + e.getMessage(), e);
//        }
//        return count;
//    }
}