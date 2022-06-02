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

package io.jmix.autoconfigure.ui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@AutoConfiguration
@Import({CoreConfiguration.class, UiConfiguration.class})
public class UiAutoConfiguration {

    @Bean("jmix_AppUiControllers")
    @ConditionalOnMissingBean(name = "jmix_AppUiControllers")
    public UiControllersConfiguration uiControllersConfiguration(
            ApplicationContext applicationContext,
            AnnotationScanMetadataReaderFactory metadataReaderFactory,
            JmixModules jmixModules) {

        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList(jmixModules.getLast().getBasePackage()));
        return uiControllers;
    }

    @Bean("jmix_AppUiActions")
    @ConditionalOnMissingBean(name = "jmix_AppUiActions")
    public ActionsConfiguration actionsConfiguration(
            ApplicationContext applicationContext,
            AnnotationScanMetadataReaderFactory metadataReaderFactory,
            JmixModules jmixModules) {

        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList(jmixModules.getLast().getBasePackage()));
        return actionsConfiguration;
    }
}
