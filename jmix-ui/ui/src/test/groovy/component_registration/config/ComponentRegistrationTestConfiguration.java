/*
 * Copyright 2020 Haulmont.
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

package component_registration.config;

import component_registration.component.*;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.component.impl.ResizableTextAreaImpl;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.ui.xml.layout.loader.TextAreaLoader;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ComponentRegistrationTestConfiguration {
    public static final String TEST_FIELD_NAME = "myTestField";
    public static final Element TEST_FIELD_ELEMENT = DocumentHelper.createElement(TEST_FIELD_NAME);

    @Bean
    protected ComponentRegistration extButton() {
        return ComponentRegistrationBuilder.create(ExtButton.NAME)
                .withComponentClass(ExtWebButton.class)
                .build();
    }

    @Bean
    protected ComponentRegistration newButton() {
        return ComponentRegistrationBuilder.create(NewButton.NAME)
                .withComponentClass(NewWebButton.class)
                .withComponentLoaderClass(NewButtonLoader.class)
                .build();
    }

    @Bean
    @Order(200)
    protected ComponentRegistration orderField1() {
        return ComponentRegistrationBuilder.create(TEST_FIELD_NAME)
                .withComponentClass(TextArea.class)
                .withComponentLoaderClass(TextAreaLoader.class)
                .build();
    }

    @Bean
    @Order(100)
    protected ComponentRegistration orderField2() {
        return ComponentRegistrationBuilder.create(TEST_FIELD_NAME)
                .withComponentClass(ResizableTextAreaImpl.class)
                .build();
    }
}
