/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode;

import com.vaadin.flow.spring.RootMappedCondition;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import io.jmix.tabbedmode.sys.vaadin.TabbedModeVaadinServlet;
import io.jmix.tabbedmode.xml.layout.loader.WorkAreaLoader;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {FlowuiConfiguration.class})
public class TabbedModeFlowuiConfiguration {

    @Bean("tabmod_UiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.tabbedmode.action"));
        return actionsConfiguration;
    }

    @Bean
    public ComponentRegistration workAreaComponent() {
        return ComponentRegistrationBuilder.create(WorkArea.class)
                .withComponentLoader("workArea", WorkAreaLoader.class)
                .build();

    }

    @Bean("tabmod_ServletRegistrationBean")
    public ServletRegistrationBean<SpringServlet> servletRegistrationBean(
            ObjectProvider<MultipartConfigElement> multipartConfig,
            VaadinConfigurationProperties configurationProperties,
            ApplicationContext context) {
        boolean rootMapping = RootMappedCondition
                .isRootMapping(configurationProperties.getUrlMapping());
        // Calls default configuration for ServletRegistrationBean at
        // com.vaadin.flow.spring.SpringBootAutoConfiguration.configureServletRegistrationBean
        return SpringBootAutoConfiguration.configureServletRegistrationBean(multipartConfig,
                configurationProperties, new TabbedModeVaadinServlet(context, rootMapping));
    }
}
