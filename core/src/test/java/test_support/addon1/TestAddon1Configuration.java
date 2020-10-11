package test_support.addon1;

import io.jmix.core.annotation.JmixModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
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
    @Primary
    public CacheManager addonCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public TestAddonFooBean addonFooBean() {
        return new TestAddonFooBean();
    }

    @Bean
    @Primary
    public TestAddonBarBean addonBarBean() {
        return new TestAddonBarBean();
    }
}
