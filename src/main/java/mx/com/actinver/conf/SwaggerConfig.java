package mx.com.actinver.conf;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Value("${spring.application.name}")
	private String appName;
	
	@Value("${spring.application.version}")
	private String appVersion;
	
	private static final String PACKAGE = "mx.com.actinver";

	private static final String JWT = "JWT";
	
	private static final String AUTHORIZATION = "Authorization";
	
	private static final String HEADER = "header";
	
	private static final String GLOBAL = "global";
	
	private static final String ACCESS_EVERYTHING = "accessEverything";
	
	private static final String DESCRIPTION = "Api Documentation";
	
	private static final String LICENSE = "Apache 2.0";
	
	private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0";	
	
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(securityScheme()))
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(PACKAGE))
                .paths(PathSelectors.any())
                .build();
    }
    
	private ApiKey securityScheme() {
		return new ApiKey(JWT, AUTHORIZATION, HEADER);
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(securityReferences()).build();
	}

	private List<SecurityReference> securityReferences() {
		AuthorizationScope[] scopes = { new AuthorizationScope(GLOBAL, ACCESS_EVERYTHING) };
		
		return Arrays.asList(new SecurityReference(JWT, scopes));
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title(appName)
				.version(appVersion)
				.description(DESCRIPTION)
				.license(LICENSE)
				.licenseUrl(LICENSE_URL)
				.build();
	}
	
}
