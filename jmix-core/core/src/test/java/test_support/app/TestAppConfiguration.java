package test_support.app;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.MessageSourceBasenames;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import test_support.TestBean;

@Configuration
@JmixModule
@PropertySource("classpath:/test_support/app/app.properties")
@MessageSourceBasenames({"test_support/app/some_messages"})
public class TestAppConfiguration {

    @Autowired
    Environment environment;

    @Bean
    @Primary
    TestFileStorage testFileStorage() {
        return new TestFileStorage("testFs");
    }

    @Bean
    TestFileStorage testFileStorage2() {
        return new TestFileStorage("testFs2");
    }

    @Bean
    TestBean testAppBean() {
        return new TestAppBean();
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public TestAppBarBean appBarBean() {
        return new TestAppBarBean();
    }

    @Bean
    @Primary
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }
}
