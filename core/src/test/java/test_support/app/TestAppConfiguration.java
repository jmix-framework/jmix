package test_support.app;


import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.JmixProperty;
import io.jmix.core.impl.JmixMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@JmixModule(properties = {
        @JmixProperty(name = "jmix.core.fetchPlansConfig", value = "test_support/app/fetch-plans.xml", append = true),
        @JmixProperty(name = "prop1", value = "app_prop1", append = true),
        @JmixProperty(name = "prop2", value = "app_prop2"),
        @JmixProperty(name = "prop3", value = "app_prop3")
})
@PropertySource("classpath:/test_support/app/app.properties")
public class TestAppConfiguration {

    @Autowired
    Environment environment;

    @Bean
    TestBean testBean() {
        return new TestBean(environment.getProperty("prop1"));
    }

    @Bean
    MessageSource messageSource(JmixModules modules, Resources resources) {
        JmixMessageSource messageSource = new JmixMessageSource(modules, resources);
        messageSource.addBasenames("test_support/app/some_messages");
        return messageSource;
    }
}
