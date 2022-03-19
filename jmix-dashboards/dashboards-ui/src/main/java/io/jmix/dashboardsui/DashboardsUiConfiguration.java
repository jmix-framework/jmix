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

package io.jmix.dashboardsui;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.dashboards.DashboardsConfiguration;
import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.component.impl.*;
import io.jmix.dashboardsui.dashboard.tools.DashboardModelConverter;
import io.jmix.dashboardsui.dashboard.tools.factory.CanvasComponentsFactory;
import io.jmix.dashboardsui.loader.DashboardLoader;
import io.jmix.dashboardsui.loader.PaletteButtonLoader;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.ui.xml.layout.loader.CssLayoutLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@JmixModule(dependsOn = {DashboardsConfiguration.class, UiConfiguration.class})
@PropertySource(name = "io.jmix.dashboardsui", value = "classpath:/io/jmix/dashboardsui/module.properties")
public class DashboardsUiConfiguration {

    @Bean("dropModelConverter")
    public DashboardModelConverter dropModelConverter(ApplicationContext applicationContext) {
        DashboardModelConverter dropModelConverter = new DashboardModelConverter();
        dropModelConverter.setFactory((CanvasComponentsFactory) applicationContext.getBean("dshbrd_CanvasDropComponentsFactory"));
        return dropModelConverter;
    }

    @Bean("uiModelConverter")
    public DashboardModelConverter uiModelConverter(ApplicationContext applicationContext) {
        DashboardModelConverter dropModelConverter = new DashboardModelConverter();
        dropModelConverter.setFactory((CanvasComponentsFactory) applicationContext.getBean(("dshbrd_CanvasUiComponentsFactory")));
        return dropModelConverter;
    }

    @Bean("dshbrd_UiControllersConfiguration")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.dashboardsui.screen"));
        return uiControllers;
    }

    @Bean
    ComponentRegistration dashboard() {
        return ComponentRegistrationBuilder.create(Dashboard.NAME)
                .withComponentClass(DashboardImpl.class)
                .withTag("dashboard")
                .withComponentLoaderClass(DashboardLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration paletteButton() {
        return ComponentRegistrationBuilder.create(PaletteButton.NAME)
                .withComponentClass(PaletteButton.class)
                .withTag("paletteButton")
                .withComponentLoaderClass(PaletteButtonLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasRootLayout() {
        return ComponentRegistrationBuilder.create(CanvasRootLayout.NAME)
                .withComponentClass(CanvasRootLayout.class)
                .withTag("canvasRootLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasVerticalLayout() {
        return ComponentRegistrationBuilder.create(CanvasVerticalLayout.NAME)
                .withComponentClass(CanvasVerticalLayout.class)
                .withTag("canvasVerticalLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasHorizontalLayout() {
        return ComponentRegistrationBuilder.create(CanvasHorizontalLayout.NAME)
                .withComponentClass(CanvasHorizontalLayout.class)
                .withTag("canvasHorizontalLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasCssLayout() {
        return ComponentRegistrationBuilder.create(CanvasCssLayout.NAME)
                .withComponentClass(CanvasCssLayout.class)
                .withTag("canvasCssLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasGridLayout() {
        return ComponentRegistrationBuilder.create(CanvasGridLayout.NAME)
                .withComponentClass(CanvasGridLayout.class)
                .withTag("canvasGridLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasWidgetLayout() {
        return ComponentRegistrationBuilder.create(CanvasWidgetLayout.NAME)
                .withComponentClass(CanvasWidgetLayout.class)
                .withTag("canvasWidgetLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }

    @Bean
    ComponentRegistration canvasResponsiveLayout() {
        return ComponentRegistrationBuilder.create(CanvasResponsiveLayout.NAME)
                .withComponentClass(CanvasResponsiveLayout.class)
                .withTag("canvasResponsiveLayout")
                .withComponentLoaderClass(CssLayoutLoader.class)
                .build();
    }
}
