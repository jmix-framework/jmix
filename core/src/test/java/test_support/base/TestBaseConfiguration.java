package test_support.base;


import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import test_support.TestBean;

@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "test_support.base", value = "classpath:/test_support/base/base-module.properties")
public class TestBaseConfiguration {

    @Bean
    TestBean testBaseBean() {
        return new TestBaseBean();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
