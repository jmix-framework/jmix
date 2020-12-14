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

package io.jmix.uidata;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.data.DataConfiguration;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.component.Component;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.settings.UserSettingService;
import io.jmix.ui.settings.UserSettingsTools;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.uidata.filter.DataFilterSupport;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource(name = "io.jmix.uidata", value = "classpath:/io/jmix/uidata/module.properties")
@JmixModule(dependsOn = {CoreConfiguration.class, DataConfiguration.class, UiConfiguration.class})
public class UiDataConfiguration {

    @Bean("ui_UiDataActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.uidata.action"));
        return actionsConfiguration;
    }

    @Bean("ui_UserSettingService")
    public UserSettingService userSettingService() {
        return new UserSettingServiceImpl();
    }

    @Bean("ui_Presentations")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public TablePresentations presentations(Component component) {
        return new TablePresentationsImpl(component);
    }

    @Bean("ui_UserSettingsTools")
    public UserSettingsTools userSettingsTools() {
        return new UserSettingsToolsImpl();
    }

    @Bean("ui_DataFilterSupport")
    @Primary
    public DataFilterSupport filterSupport() {
        return new DataFilterSupport();
    }
}

