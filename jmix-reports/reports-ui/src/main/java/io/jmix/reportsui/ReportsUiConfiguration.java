/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reportsui.screen.definition.edit.crosstab.CrossTabTableDecorator;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.uidata.UiDataConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {UiConfiguration.class, UiDataConfiguration.class, ReportsConfiguration.class})
@PropertySource(name = "io.jmix.reportsui", value = "classpath:/io/jmix/reportsui/module.properties")
public class ReportsUiConfiguration {

    @Bean("report_ReportsUiControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.reportsui.screen"));
        return uiControllers;
    }

    @Bean("report_ReportsUiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.reportsui.action"));
        return actionsConfiguration;
    }

    @Bean("report_DataSetFactory")
    public DataSetFactory dataSetFactory() {
        return new DataSetFactory();
    }

    @Bean("report_CrossTabOrientationTableDecorator")
    public CrossTabTableDecorator crossTabTableDecorator() {
        return new CrossTabTableDecorator();
    }

}
