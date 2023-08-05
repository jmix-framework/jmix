/*
 * Copyright 2023 Haulmont.
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

package io.jmix.searchflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.component.genericfilter.registration.FilterComponentRegistration;
import io.jmix.flowui.component.genericfilter.registration.FilterComponentRegistrationBuilder;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.searchflowui.component.FullTextFilter;
import io.jmix.searchflowui.component.SearchField;
import io.jmix.searchflowui.entity.FullTextFilterCondition;
import io.jmix.searchflowui.loader.FullTextFilterLoader;
import io.jmix.searchflowui.loader.SearchFieldLoader;
import io.jmix.searchflowui.utils.FullTextFilterConverter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@EnableTransactionManagement
@JmixModule(dependsOn = {CoreConfiguration.class, FlowuiConfiguration.class})
@PropertySource(name = "io.jmix.searchflowui", value = "classpath:/io/jmix/searchflowui/module.properties")
public class SearchFlowUiConfiguration {

    @Bean("search_SearchFlowuiControllers")
    public ViewControllersConfiguration screens(ApplicationContext applicationContext,
                                                AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("io.jmix.searchflowui"));
        return viewControllers;
    }

    @Bean
    public ComponentRegistration searchField() {
        return ComponentRegistrationBuilder.create(SearchField.class)
                .withComponentLoader("searchField", SearchFieldLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration fullTextFilter() {
        return ComponentRegistrationBuilder.create(FullTextFilter.class)
                .withComponentLoader("fullTextFilter", FullTextFilterLoader.class)
                .build();
    }

    @Bean
    public FilterComponentRegistration registerFullTextFilter() {
        return FilterComponentRegistrationBuilder.create(FullTextFilter.class,
                        FullTextFilterCondition.class,
                        FullTextFilterConverter.class)
                .build();
    }
//
//    /**
//     * Registers a full-text filter and condition for using in the {@link io.jmix.ui.component.Filter} UI component.
//     */
//    @Bean("search_FullTextFilterRegistration")
//    public FilterComponentRegistration registerFullTextFilter() {
//        return FilterComponentRegistrationBuilder.create(FullTextFilter.class,
//                FullTextFilterCondition.class,
//                FullTextFilterConverter.class)
//                .build();
//    }
}
