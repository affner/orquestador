package mx.com.actinver.conf;

import mx.com.actinver.orquestador.dao.DbPropertiesDao;
import mx.com.actinver.orquestador.util.DynamicProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class DynamicPropertyInjector implements BeanPostProcessor {

    private static final Logger LOG = LogManager.getLogger(DynamicPropertyInjector.class);

    @Autowired
    private DbPropertiesDao dbPropertiesDao;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {
            DynamicProperty annotation = field.getAnnotation(DynamicProperty.class);
            if (annotation != null) {
                if (!field.getType().equals(DynamicString.class)) {
                    throw new IllegalArgumentException("@DynamicProperty solo puede usarse sobre campos de tipo DynamicString");
                }

                String rawKey = annotation.value();
                String key = rawKey;
                if (rawKey.startsWith("${") && rawKey.endsWith("}")) {
                    key = rawKey.substring(2, rawKey.length() - 1);
                }

                DynamicString dynamicValue = new DynamicString(key, dbPropertiesDao);

                field.setAccessible(true);
                try {
                    field.set(bean, dynamicValue);
                } catch (IllegalAccessException e) {
                    LOG.error("Error Procesando Properties: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

}

