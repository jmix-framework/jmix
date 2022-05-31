package component_registration;

import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import org.junit.jupiter.api.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentsRegistrationTestConfiguration {

    @Bean
    @Order(200)
    public ComponentRegistration firstTestJmixButton() {
        return ComponentRegistrationBuilder.create(TestFirstJmixButton.class)
                .replaceComponent(JmixButton.class)
                .build();
    }

    @Bean
    @Order(300)
    public ComponentRegistration secondTestJmixButton() {
        return ComponentRegistrationBuilder.create(TestSecondJmixButton.class)
                .replaceComponent(TestFirstJmixButton.class)
                .build();
    }

    @Bean
    @Order(100)
    public ComponentRegistration testThirdJmixButton() {
        return ComponentRegistrationBuilder.create(TestThirdJmixButton.class)
                .replaceComponent(JmixButton.class)
                .withComponentLoader("button", ExtButtonLoader.class)
                .build();
    }
}
