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

package io.jmix.searchui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.searchui.component.FullTextFilter;
import io.jmix.searchui.component.fulltextfilter.FullTextFilterConverter;
import io.jmix.searchui.component.SearchField;
import io.jmix.searchui.entity.FullTextFilterCondition;
import io.jmix.searchui.component.impl.FullTextFilterImpl;
import io.jmix.searchui.component.impl.SearchFieldImpl;
import io.jmix.searchui.component.loader.FullTextFilterLoader;
import io.jmix.searchui.component.loader.SearchFieldLoader;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.component.filter.registration.FilterComponentRegistration;
import io.jmix.ui.component.filter.registration.FilterComponentRegistrationBuilder;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@EnableTransactionManagement
@JmixModule(dependsOn = {CoreConfiguration.class, UiConfiguration.class})
@PropertySource(name = "io.jmix.searchui", value = "classpath:/io/jmix/searchui/module.properties")
public class SearchUiConfiguration {

    @Bean("search_SearchUiControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.searchui"));
        return uiControllers;
    }

    @Bean
    public ComponentRegistration searchField() {
        return ComponentRegistrationBuilder.create(SearchField.NAME)
                .withComponentClass(SearchFieldImpl.class)
                .withComponentLoaderClass(SearchFieldLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration fullTextFilter() {
        return ComponentRegistrationBuilder.create(FullTextFilter.NAME)
                .withComponentClass(FullTextFilterImpl.class)
                .withComponentLoaderClass(FullTextFilterLoader.class)
                .build();
    }

    /**
     * Registers a full-text filter and condition for using in the {@link io.jmix.ui.component.Filter} UI component.
     */
    @Bean("search_FullTextFilterRegistration")
    public FilterComponentRegistration registerFullTextFilter() {
        return FilterComponentRegistrationBuilder.create(FullTextFilter.class,
                FullTextFilterCondition.class,
                FullTextFilterConverter.class)
                .build();
    }
}
