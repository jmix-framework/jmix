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

package io.jmix.grapesjs;

import io.jmix.core.annotation.JmixModule;
import io.jmix.grapesjs.component.GrapesJsHtmlEditor;
import io.jmix.grapesjs.component.GrapesJsNewsletterHtmlEditor;
import io.jmix.grapesjs.component.GrapesJsWebpageHtmlEditor;
import io.jmix.grapesjs.component.impl.GrapesJsHtmlEditorImpl;
import io.jmix.grapesjs.component.impl.GrapesJsNewsletterHtmlEditorImpl;
import io.jmix.grapesjs.component.impl.GrapesJsWebpageHtmlEditorImpl;
import io.jmix.grapesjs.xml.layout.loader.GrapesJsHtmlEditorLoader;
import io.jmix.grapesjs.xml.layout.loader.GrapesJsNewsletterHtmlEditorLoader;
import io.jmix.grapesjs.xml.layout.loader.GrapesJsWebpageHtmlEditorLoader;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@JmixModule(dependsOn = UiConfiguration.class)
public class GrapesJsConfiguration {

    @Bean
    public ComponentRegistration grapesJsHtmlEditor() {
        return ComponentRegistrationBuilder.create(GrapesJsHtmlEditor.NAME)
                .withComponentClass(GrapesJsHtmlEditorImpl.class)
                .withComponentLoaderClass(GrapesJsHtmlEditorLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration grapesJsNewsletterHtmlEditor() {
        return ComponentRegistrationBuilder.create(GrapesJsNewsletterHtmlEditor.NAME)
                .withComponentClass(GrapesJsNewsletterHtmlEditorImpl.class)
                .withComponentLoaderClass(GrapesJsNewsletterHtmlEditorLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration grapesJsWebpageHtmlEditor() {
        return ComponentRegistrationBuilder.create(GrapesJsWebpageHtmlEditor.NAME)
                .withComponentClass(GrapesJsWebpageHtmlEditorImpl.class)
                .withComponentLoaderClass(GrapesJsWebpageHtmlEditorLoader.class)
                .build();
    }
}
