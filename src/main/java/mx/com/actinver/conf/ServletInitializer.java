package mx.com.actinver.conf;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// FORZAR explícitamente la clase de arranque que contiene @EnableWs
		return application.sources(mx.com.actinver.conf.ApplicationConfig.class);
	}
}
