package mx.com.actinver.orquestador.util;

import lombok.Builder;
import mx.com.actinver.common.dto.RsDto;
import mx.com.actinver.common.dto.RsDto.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase de apoyo para ejecutar un SP.<br>
 * Retorna como respuesta un {@link RsDto} que contiene el resultado del SP.
 * 
 * @param <T> tipo de entrada
 * @param <R> tipo de salida
 */
@Builder
public class SpHelper<T, R> {

	private static final Logger LOG = LogManager.getLogger(SpHelper.class);

	private static final int _REF_CURSOR_ = -10;

	private DataSource dataSource;

	private SpConfigEnum spCfg;

	private Class<R> resultType;

	private T input;

	private SpActionEnum action;

	private Long executor;	

	private static <T, R> SpHelperBuilder<T, R> builder() {
		return new SpHelperBuilder<T, R>();
	}

	public static <T, R> SpHelperBuilder<T, R> builder(DataSource dataSource, SpConfigEnum spCfg, Class<R> resultType) {
		SpHelperBuilder<T, R> builder = builder();

		builder.dataSource(dataSource).spCfg(spCfg).resultType(resultType);

		return builder;
	}

	public RsDto<R> execute() {
		if (Objects.isNull(dataSource)) {
			throw new NullPointerException("La fuente de datos es obligatoria (\"dataSource\").");
		} else if (Objects.isNull(spCfg)) {
			throw new NullPointerException("La definicion del SP es obligatoria (\"spCfg\").");
		} else if (StringUtils.isBlank(spCfg.getStmt())) {
			throw new NullPointerException("La sentencia SQL del SP es obligatoria (\"stmt\").");
		} else if (Objects.isNull(resultType)) {
			throw new NullPointerException("La clase de resultado es obligatoria (\"resultType\").");
		} else if (Objects.isNull(input)) {
			throw new NullPointerException("Los datos de entrada son obligatorios (\"input\").");
		}

		return connection(input, action, executor);
	}

	private RsDto<R> connection(T input, SpActionEnum action, Long executor) {
		RsDto<R> response = new RsDto<>();

		LOG.info("input: [{}], spCfg: [{}], action: [{}], executor: [{}]",
				Objects.nonNull(input) ? MappingHelper.toJson(input) : StringUtils.EMPTY, spCfg,
				Objects.nonNull(action) ? action : StringUtils.EMPTY,
				Objects.nonNull(executor) ? executor : StringUtils.EMPTY);

		try (Connection connection = dataSource.getConnection()) {
			response = prepareCall(connection, input, action, executor);
		} catch (SQLException e) {
			LOG.error("(SP) Conexion", e);

			throw new RuntimeException("Conexion", e);
		}

		return response;
	}

	private RsDto<R> prepareCall(Connection connection, T input, SpActionEnum action, Long executor) {
		RsDto<R> response = new RsDto<>();

		try (CallableStatement cstmt = connection.prepareCall(spCfg.getStmt())) {
			int idx = 0;

			cstmt.setString(++idx, MappingHelper.toJson(input));

			if (Objects.nonNull(action)) {
				cstmt.setString(++idx, action.name());
			}

			if (Objects.nonNull(executor)) {
				if (executor <= -1) {
					cstmt.setNull(++idx, Types.NULL);
				} else {
					cstmt.setLong(++idx, executor);
				}
			}

			int idxRs = ++idx;
			int idxPg = spCfg.isPageable() ? ++idx : -1;
			int idxCd = ++idx;
			int idxMsg = ++idx;

			cstmt.registerOutParameter(idxRs, _REF_CURSOR_);
			if (idxPg > -1) {
				cstmt.registerOutParameter(idxPg, _REF_CURSOR_);
			}
			cstmt.registerOutParameter(idxCd, Types.INTEGER);
			cstmt.registerOutParameter(idxMsg, Types.VARCHAR);

			cstmt.execute();

			int respCode = cstmt.getInt(idxCd);
			if (respCode != 0) {
				String respMsg = cstmt.getString(idxMsg);

				throw new SQLException(String.format("[%s] %s", respCode, respMsg));
			}
			
			List<R> result = getResult(cstmt, idxRs);
			
			if (spCfg.isPageable()) {
				response = getPagination(cstmt, idxPg, result);
			} else {
				response = RsDto.builder(result).build();
			}
		} catch (SQLException e) {
			LOG.error("(SP) Sentencia", e);

			throw new RuntimeException("Sentencia", e);
		}

		return response;
	}

	private List<R> getResult(CallableStatement cstmt, int idxRs) {
		List<R> result = new ArrayList<>();

		try (ResultSet rs = (ResultSet) cstmt.getObject(idxRs)) {
			while (rs != null && rs.next()) {
				result.add(MappingHelper.toClass(rs.getString("FJDATOS"), resultType));
			}
		} catch (SQLException e) {
			LOG.error("(SP) Resultado", e);

			throw new RuntimeException("Resultado", e);
		}

		return result;
	}

	private RsDto<R> getPagination(CallableStatement cstmt, int idxPg, List<R> result) {
		Pagination pagination = Pagination.builder().build();

		try (ResultSet rs = (ResultSet) cstmt.getObject(idxPg)) {
			while (rs != null && rs.next()) {
				int idx = 0;

				Long totalElements = rs.getLong(++idx);
				Long pageSize = rs.getLong(++idx);
				Long totalPages = rs.getLong(++idx);

				pagination = Pagination.builder(totalPages, totalElements, pageSize).build();
			}
		} catch (SQLException e) {
			LOG.error("(SP) Paginacion", e);

			throw new RuntimeException("Paginacion", e);
		}

		return RsDto.builder(pagination, result).build();
	}

}
