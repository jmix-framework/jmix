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
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.FilterMetadataTools;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowuidata.genericfilter.UiDataFilterMetadataTools;
import io.jmix.flowuidata.genericfilter.UiDataGenericFilterSupport;
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

    @Bean("flowui_UiDataActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.flowuidata.action"));
        return actionsConfiguration;
    }

    @Bean("flowui_UiDataGenericFilterSupport")
    @Primary
    public GenericFilterSupport genericFilterSupport(Actions actions,
                                                     UiComponents uiComponents,
                                                     DataManager dataManager,
                                                     GenericFilterConfigurationConverter genericFilterConfigurationConverter,
                                                     CurrentAuthentication currentAuthentication,
                                                     DataComponents dataComponents,
                                                     Metadata metadata) {
        return new UiDataGenericFilterSupport(actions, uiComponents, dataManager,
                genericFilterConfigurationConverter, currentAuthentication, dataComponents, metadata);
    }

    @Bean("flowui_UiDataFilterMetadataTools")
    @Primary
    public FilterMetadataTools filterMetadataTools(MetadataTools metadataTools,
                                                   UiComponentProperties uiComponentProperties,
                                                   AccessManager accessManager,
                                                   QueryTransformerFactory queryTransformerFactory,
                                                   Metadata metadata) {
        return new UiDataFilterMetadataTools(metadataTools, uiComponentProperties, accessManager,
                queryTransformerFactory, metadata);
    }
}
