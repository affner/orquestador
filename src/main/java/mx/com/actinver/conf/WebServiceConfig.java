package mx.com.actinver.conf;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.Map;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    /* ------------------------------------------------------------------
     * Legacy SOAP  ►  /WSImagenesMock/*
     * ------------------------------------------------------------------ */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext ctx) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(ctx);
        servlet.setTransformWsdlLocations(true);

        // ✓ SOAP en /WSImagenesMock/*
        ServletRegistrationBean<MessageDispatcherServlet> reg =
                new ServletRegistrationBean<>(servlet, "/WSImagenesMock/*");
        reg.setName("MessageDispatcherServlet");
        reg.setLoadOnStartup(1);
        return reg;
    }



    @Bean
    public org.springframework.boot.CommandLineRunner debugServletsAndWsdlBeans(ServletContext servletContext,
                                                                                org.springframework.context.ApplicationContext ctx) {
        return args -> {
            System.out.println("---- Registered servlets ----");
            servletContext.getServletRegistrations().forEach((name, reg) ->
                    System.out.println("Servlet: " + name + " -> mappings: " + reg.getMappings())
            );

            System.out.println("---- Wsdl11Definition beans ----");
            Map<String, Wsdl11Definition> beans = ctx.getBeansOfType(Wsdl11Definition.class);
            beans.forEach((name, bean) -> {
                System.out.println("Bean: " + name + " -> class: " + bean.getClass().getName());
                // Si es SimpleWsdl11Definition, intenta leer el campo privado "wsdl" por reflection
                if (bean instanceof SimpleWsdl11Definition) {
                    try {
                        Field f = bean.getClass().getDeclaredField("wsdl");
                        f.setAccessible(true);
                        Object val = f.get(bean);
                        if (val instanceof Resource) {
                            Resource r = (Resource) val;
                            System.out.println("  resource (via reflection) exists? " + r.exists() + " - desc: " + r.getDescription());
                        } else {
                            System.out.println("  field 'wsdl' exists but is not Resource, value=" + val);
                        }
                    } catch (NoSuchFieldException nsf) {
                        // try on superclass (defensive)
                        try {
                            Field f = bean.getClass().getSuperclass().getDeclaredField("wsdl");
                            f.setAccessible(true);
                            Object val = f.get(bean);
                            System.out.println("  (superclass) field 'wsdl' value: " + val);
                        } catch (Exception e) {
                            System.out.println("  no se encontró field 'wsdl' via reflection: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("  error leyendo field 'wsdl' por reflection: " + e.getMessage());
                    }
                }
            });
        };
    }
}
