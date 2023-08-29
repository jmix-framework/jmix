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

package io.jmix.dynattrflowui;

import io.jmix.core.DataManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattrflowui.propertyfilter.DynAttrPropertyFilterSupport;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {DynAttrConfiguration.class, FlowuiConfiguration.class})
@PropertySource(name = "io.jmix.dynattrflowui", value = "classpath:/io/jmix/dynattrflowui/module.properties")
public class DynAttrUiConfiguration {

    @Bean("dynat_DynAttrPropertyFilterSupport")
    @Primary
    public PropertyFilterSupport propertyFilterSupport(Messages messages,
                                                       MessageTools messageTools,
                                                       MetadataTools metadataTools,
                                                       DataManager dataManager,
                                                       DatatypeRegistry datatypeRegistry,
                                                       DynAttrMetadata dynAttrMetadata) {
        return new DynAttrPropertyFilterSupport(messages, messageTools, metadataTools, dataManager, datatypeRegistry,
                dynAttrMetadata);
    }

    @Bean("dynat_ViewControllersConfiguration")
    public ViewControllersConfiguration views(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("io.jmix.dynattrflowui.view"));
        return viewControllers;
    }
}
