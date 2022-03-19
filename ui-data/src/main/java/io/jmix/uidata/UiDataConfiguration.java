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

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.data.DataConfiguration;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.app.propertyfilter.dateinterval.RelativeDateTimeMomentProvider;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.filter.FilterMetadataTools;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.presentation.PresentationsManager;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.settings.*;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.uidata.dateinterval.RelativeDateTimeMomentProviderImpl;
import io.jmix.uidata.filter.UiDataFilterMetadataTools;
import io.jmix.uidata.filter.UiDataFilterSupport;
import io.jmix.uidata.settings.PresentationsManagerImpl;
import io.jmix.uidata.settings.ScreenSettingsManagerImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource(name = "io.jmix.uidata", value = "classpath:/io/jmix/uidata/module.properties")
@JmixModule(dependsOn = {DataConfiguration.class, UiConfiguration.class})
public class UiDataConfiguration {

    @Bean("ui_UiDataActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.uidata.action"));
        return actionsConfiguration;
    }

    @Bean("ui_UiDataControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.uidata.app"));
        return uiControllers;
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

    @Bean("ui_UiSettingsCache")
    public UiSettingsCache uiSettingsCache() {
        return new UiSettingsCacheImpl();
    }

    @Bean("ui_ScreenSettingsManager")
    public ScreenSettingsManager screenSettingsManager() {
        return new ScreenSettingsManagerImpl();
    }

    @Bean("ui_PresentationsManager")
    public PresentationsManager presentationsManager(ComponentSettingsRegistry settingsRegistry) {
        return new PresentationsManagerImpl(settingsRegistry);
    }

    @Bean("ui_UiDataFilterSupport")
    @Primary
    public FilterSupport filterSupport() {
        return new UiDataFilterSupport();
    }

    @Bean("ui_UiDataFilterMetadataTools")
    @Primary
    public FilterMetadataTools filterMetadataTools(MetadataTools metadataTools,
                                                   UiComponentProperties uiComponentProperties,
                                                   AccessManager accessManager,
                                                   QueryTransformerFactory queryTransformerFactory,
                                                   Metadata metadata) {
        return new UiDataFilterMetadataTools(metadataTools, uiComponentProperties, accessManager,
                queryTransformerFactory, metadata);
    }

    @Bean("ui_RelativeDateTimeMomentProvider")
    public RelativeDateTimeMomentProvider relativeDateTimeMomentProvider() {
        return new RelativeDateTimeMomentProviderImpl();
    }
}
