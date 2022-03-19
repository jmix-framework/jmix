/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.ui.component.JpqlFilter;
import io.jmix.ui.component.GroupFilter;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.jpqlfilter.JpqlFilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponentRegistration;
import io.jmix.ui.component.filter.registration.FilterComponentRegistrationBuilder;
import io.jmix.ui.component.groupfilter.GroupFilterConverter;
import io.jmix.ui.component.propertyfilter.PropertyFilterConverter;
import io.jmix.ui.entity.JpqlFilterCondition;
import io.jmix.ui.entity.GroupFilterCondition;
import io.jmix.ui.entity.PropertyFilterCondition;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "io.jmix.ui", value = "classpath:/io/jmix/ui/module.properties")
@Import(UiScheduleConfiguration.class)
public class UiConfiguration {

    @Bean("ui_UiControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.ui.app"));
        return uiControllers;
    }

    @Bean("ui_UiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.ui.action"));
        return actionsConfiguration;
    }

    @Bean("ui_PropertyFilterRegistration")
    public FilterComponentRegistration registerPropertyFilter() {
        return FilterComponentRegistrationBuilder.create(PropertyFilter.class,
                PropertyFilterCondition.class,
                PropertyFilterConverter.class)
                .build();
    }

    @Bean("ui_JpqlFilterRegistration")
    public FilterComponentRegistration registerJpqlFilter() {
        return FilterComponentRegistrationBuilder.create(JpqlFilter.class,
                JpqlFilterCondition.class,
                JpqlFilterConverter.class)
                .build();
    }

    @Bean("ui_GroupFilterRegistration")
    public FilterComponentRegistration registerGroupFilter() {
        return FilterComponentRegistrationBuilder.create(GroupFilter.class,
                GroupFilterCondition.class,
                GroupFilterConverter.class)
                .build();
    }
}