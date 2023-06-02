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

package io.jmix.flowuidata;

import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.DataConfiguration;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.flowui.Actions;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.FilterMetadataTools;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowuidata.genericfilter.FlowiDataFilterMetadataTools;
import io.jmix.flowuidata.genericfilter.FlowuiDataGenericFilterSupport;
import io.jmix.flowuidata.genericfilter.GenericFilterConfigurationConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@Configuration
@ComponentScan
@EnableTransactionManagement
@JmixModule(dependsOn = {DataConfiguration.class, FlowuiConfiguration.class})
public class FlowuiDataConfiguration {

    @Bean("flowui_FlowuiDataActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.flowuidata.action"));
        return actionsConfiguration;
    }

    @Bean("flowui_FlowuiDataGenericFilterSupport")
    @Primary
    public GenericFilterSupport genericFilterSupport(Actions actions,
                                                     UiComponents uiComponents,
                                                     DataManager dataManager,
                                                     GenericFilterConfigurationConverter genericFilterConfigurationConverter,
                                                     CurrentAuthentication currentAuthentication,
                                                     DataComponents dataComponents,
                                                     Metadata metadata) {
        return new FlowuiDataGenericFilterSupport(actions, uiComponents, dataManager,
                genericFilterConfigurationConverter, currentAuthentication, dataComponents, metadata);
    }

    @Bean("flowui_FlowuiDataFilterMetadataTools")
    @Primary
    public FilterMetadataTools filterMetadataTools(MetadataTools metadataTools,
                                                   FlowuiComponentProperties flowuiComponentProperties,
                                                   AccessManager accessManager,
                                                   QueryTransformerFactory queryTransformerFactory,
                                                   Metadata metadata) {
        return new FlowiDataFilterMetadataTools(metadataTools, flowuiComponentProperties, accessManager,
                queryTransformerFactory, metadata);
    }
}
