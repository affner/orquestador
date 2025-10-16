package mx.com.actinver.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

@Configuration
public class PtlExsDbConfig {

	private static final Logger LOG = LogManager.getLogger(PtlExsDbConfig.class);

	@Value("${spring.datasource.jndi-name}")
	private String jndiName;

	/**
	 * DataSource “local” que apunta a la base donde está TA_PROD_TMP.
	 */
	@Bean(name = "localDataSource")
	public DataSource localDataSource() {
		JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		try {
			return dsLookup.getDataSource(jndiName);
		} catch (Exception ex) {
			LOG.error("Error al obtener el DataSource local (TA_PROD_TMP)", ex);
			throw new IllegalStateException("No se pudo obtener el DataSource local", ex);
		}

	}
}
