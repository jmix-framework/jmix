/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
