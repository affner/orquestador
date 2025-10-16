package mx.com.actinver.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

@Configuration
public class XsaDbConfig {

	private static final Logger LOG = LogManager.getLogger(XsaDbConfig.class);

	@Value("${spring.datasource.xsa.jndi-name}")
	private String jndiName;

	/**
	 * DataSource “XSA” que apunta a la base externa USR_XSA1.COMPROBANTES.
	 */
	@Bean(name = "xsaDataSource")
	public DataSource xsaDataSource() {
		JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		try {
			return dsLookup.getDataSource(jndiName);
		} catch (Exception ex) {
			LOG.error("Error al obtener el DataSource XSA (COMPROBANTES)", ex);
			return null;
		}
	}
}
