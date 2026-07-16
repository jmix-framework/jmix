package ${packageName};

import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ${registrationClassName} {

    @Bean
    public ComponentRegistration ${xmlElement}() {
        return ComponentRegistrationBuilder.create(${componentClassName}.class)
                .withComponentLoader("${xmlElement}", ${loaderClassName}.class)
                .build();
    }
}
