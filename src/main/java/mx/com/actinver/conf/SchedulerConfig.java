package mx.com.actinver.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Value("${scheduler.pool-size:10}")
    private int poolSize;

    @Value("${scheduler.thread-name-prefix:dynamic-scheduler-}")
    private String threadNamePrefix;

    /** ThreadPoolTaskScheduler para programar tareas dinámicas. */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(poolSize);
        ts.setThreadNamePrefix(threadNamePrefix);
        return ts;
    }
}