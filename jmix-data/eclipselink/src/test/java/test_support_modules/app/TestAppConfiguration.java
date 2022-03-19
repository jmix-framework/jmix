package test_support_modules.app;


import io.jmix.core.annotation.JmixModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@JmixModule
@PropertySource("classpath:/test_support_modules/app/app.properties")
public class TestAppConfiguration {
}
