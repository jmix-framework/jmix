package test_support.app;

import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.JmixMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@JmixModule
@PropertySource("classpath:/test_support/app/app.properties")
public class TestAppConfiguration {

    @Autowired
    Environment environment;

    @Bean
    MessageSource messageSource(JmixModules modules, Resources resources) {
        JmixMessageSource messageSource = new JmixMessageSource(modules, resources);
        messageSource.addBasenames("test_support/app/some_messages");
        return messageSource;
    }

    @Bean
    @Primary
    TestFileStorage testFileStorage() {
        return new TestFileStorage();
    }

    @Bean
    TestFileStorage testFileStorage2() {
        return new TestFileStorage();
    }
}
