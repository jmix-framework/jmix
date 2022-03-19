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

package io.jmix.pivottable;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.pivottable.component.PivotTable;
import io.jmix.pivottable.component.impl.PivotTableImpl;
import io.jmix.pivottable.loader.PivotTableLoader;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ComponentScan
@JmixModule(dependsOn = UiConfiguration.class)
public class PivotTableConfiguration {

    @Bean("ui_PivotTableControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.pivottable.screen"));
        return uiControllers;
    }

    @Bean("ui_PivotTableActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.pivottable.action"));
        return actionsConfiguration;
    }

    @Bean
    public ComponentRegistration pivotTable() {
        return ComponentRegistrationBuilder.create(PivotTable.NAME)
                .withComponentClass(PivotTableImpl.class)
                .withComponentLoaderClass(PivotTableLoader.class)
                .withTag("pivotTable")
                .build();
    }
}
