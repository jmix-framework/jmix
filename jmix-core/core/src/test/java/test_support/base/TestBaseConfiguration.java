package test_support.base;


import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import test_support.TestBean;

import java.util.ArrayList;
import java.util.List;

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

    @Bean
    AuthenticationManager authenticationManager(UserRepository userRepository) {
        List<AuthenticationProvider> providers = new ArrayList<>();
        SystemAuthenticationProvider systemAuthenticationProvider = new SystemAuthenticationProvider(userRepository);
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        providers.add(systemAuthenticationProvider);
        providers.add(daoAuthenticationProvider);
        return new ProviderManager(providers);
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }
}
