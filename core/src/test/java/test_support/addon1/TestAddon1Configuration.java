package test_support.addon1;

import io.jmix.core.annotation.JmixModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import test_support.TestBean;
import test_support.base.TestBaseConfiguration;

@Configuration
@ComponentScan
@JmixModule(dependsOn = TestBaseConfiguration.class)
@PropertySource(name = "test_support.addon1", value = "test_support/addon1/addon1-module.properties")
public class TestAddon1Configuration {

    @Bean
    TestBean testAddon1Bean() {
        return new TestAddon1Bean();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
