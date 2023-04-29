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

package io.jmix.flowui;

import com.vaadin.flow.spring.VaadinServletContextInitializer;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.component.genericfilter.registration.FilterComponentRegistration;
import io.jmix.flowui.component.genericfilter.registration.FilterComponentRegistrationBuilder;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.jpqlfilter.JpqlFilterConverter;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.GroupFilterConverter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterConverter;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import io.jmix.flowui.entity.filter.JpqlFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.JmixVaadinServletContextInitializer;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "io.jmix.flowui", value = "classpath:/io/jmix/flowui/module.properties")
public class FlowuiConfiguration {

    @Bean("flowui_ViewControllers")
    public ViewControllersConfiguration views(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("io.jmix.flowui.app"));
        return viewControllers;
    }

    @Bean("flowui_UiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.flowui.action"));
        return actionsConfiguration;
    }

    @Bean("flowui_PropertyFilterRegistration")
    public FilterComponentRegistration registerPropertyFilter() {
        return FilterComponentRegistrationBuilder.create(PropertyFilter.class,
                        PropertyFilterCondition.class,
                        PropertyFilterConverter.class)
                .build();
    }

    @Bean("flowui_JpqlFilterRegistration")
    public FilterComponentRegistration registerJpqlFilter() {
        return FilterComponentRegistrationBuilder.create(JpqlFilter.class,
                        JpqlFilterCondition.class,
                        JpqlFilterConverter.class)
                .build();
    }

    @Bean("flowui_GroupFilterRegistration")
    public FilterComponentRegistration registerGroupFilter() {
        return FilterComponentRegistrationBuilder.create(GroupFilter.class,
                        GroupFilterCondition.class,
                        GroupFilterConverter.class)
                .build();
    }

    @Bean
    @Primary
    public VaadinServletContextInitializer jmixVaadinServletContextInitializer(ApplicationContext applicationContext) {
        return new JmixVaadinServletContextInitializer(applicationContext);
    }
}
