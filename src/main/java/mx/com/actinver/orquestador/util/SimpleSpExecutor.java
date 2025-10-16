package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.common.dto.RsDto.Pagination;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleSpExecutor {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    private static final int _REF_CURSOR_ = -10;

    public SimpleSpExecutor(@Qualifier("localDataSource") DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    public <T, R> RsDto<R> callSp(
            String callStmt,    // p.ej "{ CALL ...SP_TO_CANCEL(?, ?, ?, ?, ?, ?) }"
            T input,            // tu DTO de parámetros
            Long userId,        // vl_iUsuario
            Class<R> dtoClass,  // CancelBlockDto.class
            boolean pageable    // true si tiene cursor de páginas
    ) {
        return jdbc.execute(
                new CallableStatementCreator() {
                    @Override
                    public CallableStatement createCallableStatement(Connection con) throws SQLException {
                        CallableStatement c = con.prepareCall(callStmt);
                        // 1) JSON de entrada
                        try {
                            c.setString(1, json.writeValueAsString(input));
                        } catch (JsonProcessingException e) {
                            throw new SQLException("Error convirtiendo input a JSON", e);
                        }
                        // 2) usuario
                        c.setLong(2, userId);
//                        // 3) primer cursor
//                        c.registerOutParameter(3, _REF_CURSOR_);
//                        // índices de código y mensaje
//                        int idxCode = pageable ? 5 : 4;
//                        int idxMsg  = pageable ? 6 : 5;
//                        // si es pageable, registrar segundo cursor en la posición 6
//                        if (pageable) {
//                            c.registerOutParameter(6, _REF_CURSOR_);
//                        }
//                        // código y mensaje
//                        c.registerOutParameter(idxCode, Types.INTEGER);
//                        c.registerOutParameter(idxMsg, Types.VARCHAR);
                        // 3) cursor de resultados
                        c.registerOutParameter(3, _REF_CURSOR_);
                        // 4) código de respuesta
                        c.registerOutParameter(4, Types.INTEGER);
                        // 5) mensaje
                        c.registerOutParameter(5, Types.VARCHAR);
                        // 6) cursor de paginación (si aplica)
                        if (pageable) {
                            c.registerOutParameter(6, _REF_CURSOR_);
                        }
                        return c;
                    }
                },
                new org.springframework.jdbc.core.CallableStatementCallback<RsDto<R>>() {
                    @Override
                    public RsDto<R> doInCallableStatement(CallableStatement c) throws SQLException {
                        c.execute();

                        // 1) leer filas del primer cursor
                        List<R> rows = new ArrayList<>();
//                        try (ResultSet rs = (ResultSet) c.getObject(3)) {
//                            while (rs != null && rs.next()) {
//                                // SP de obtención de UUIDs
//                                if (dtoClass.equals(CancelBlockDto.class)) {
//                                    @SuppressWarnings("unchecked")
//                                    R dto = (R) CancelBlockDto.builder()
//                                            .fcUuid(rs.getString(1))
//                                            .build();
//                                    rows.add(dto);
//                                }
//                                // SP de confirmación: devuelve un mensaje plano en la primera columna
//                                else if (dtoClass.equals(CancelResultDto.class)) {
//                                    String mensaje = rs.getString(1);
//                                    @SuppressWarnings("unchecked")
//                                    R dto = (R) CancelResultDto.builder()
//                                            .fcRes(mensaje)
//                                            .build();
//                                    rows.add(dto);
//                                }
//                                // cualquier otro SP que sí devuelva JSON en esa columna
//                                else {
//                                    String jsonRow = rs.getString(1);
//                                    R dto = json.readValue(jsonRow, dtoClass);
//                                    rows.add(dto);
//                                }
//                            }
//                        } catch (IOException ex) {
//                            throw new SQLException("Error parseando fila JSON", ex);
//                        }

                        // 2) obtener código y mensaje
                        int code = c.getInt(4);
                        String msg = c.getString(5);

                        RsDto<R> result = RsDto.<R>builder(rows)
                                .message(code + " - " + msg)
                                .build();

                        // 3) si es pageable, leer paginación
                        if (pageable) {
                            try (ResultSet pg = (ResultSet) c.getObject(6)) {
                                if (pg != null && pg.next()) {
                                    Pagination pag = Pagination.builder(
                                            pg.getLong("FINUMPAGES"),
                                            pg.getLong("FINUMREGS"),
                                            pg.getLong("FIRECPERPAGE")
                                    ).build();
                                    result.setPagination(pag);
                                }
                            }
                        }

                        return result;
                    }
                }
        );
    }
}
