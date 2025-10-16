package mx.com.actinver.conf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.ws.config.annotation.EnableWs;

@EnableWs
@SpringBootApplication(scanBasePackages = "mx.com.actinver")
public class ApplicationConfig extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationConfig.class, args);
	}

	// Para desplegar como WAR en un contenedor externo (WildFly, etc.)
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ApplicationConfig.class);
	}
}
