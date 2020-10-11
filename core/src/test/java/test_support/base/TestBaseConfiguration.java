package test_support.base;


import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.CoreSecurityConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import test_support.TestBean;

@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "test_support.base", value = "classpath:/test_support/base/base-module.properties")
public class TestBaseConfiguration {

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    TestBean testBaseBean() {
        return new TestBaseBean();
    }

    @Bean
    public CacheManager baseCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    TestBaseFooBean baseFooBean() {
        return new TestBaseFooBean();
    }

    @Bean
    TestBaseBarBean baseBarBean() {
        return new TestBaseBarBean();
    }

    @EnableWebSecurity
    static class TestSecurityConfiguration extends CoreSecurityConfiguration {
    }
}
