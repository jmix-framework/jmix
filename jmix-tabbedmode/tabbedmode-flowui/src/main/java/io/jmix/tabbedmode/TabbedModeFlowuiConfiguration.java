/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode;

import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.RootMappedCondition;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import io.jmix.core.*;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.*;
import io.jmix.flowui.Views;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.DetailWindowBuilderProcessor;
import io.jmix.flowui.view.builder.EditedEntityTransformer;
import io.jmix.flowui.view.builder.LookupWindowBuilderProcessor;
import io.jmix.flowui.view.builder.WindowBuilderProcessor;
import io.jmix.flowui.view.navigation.*;
import io.jmix.securityflowui.authentication.LoginViewSupport;
import io.jmix.tabbedmode.builder.DetailViewBuilderProcessor;
import io.jmix.tabbedmode.builder.LookupViewBuilderProcessor;
import io.jmix.tabbedmode.builder.ViewBuilderProcessor;
import io.jmix.tabbedmode.builder.dialog.TabbedModeDetailWindowBuilderProcessor;
import io.jmix.tabbedmode.builder.dialog.TabbedModeLookupWindowBuilderProcessor;
import io.jmix.tabbedmode.builder.dialog.TabbedModeWindowBuilderProcessor;
import io.jmix.tabbedmode.builder.navigation.TabbedModeDetailViewNavigationProcessor;
import io.jmix.tabbedmode.builder.navigation.TabbedModeListViewNavigationProcessor;
import io.jmix.tabbedmode.builder.navigation.TabbedModeViewNavigationProcessor;
import io.jmix.tabbedmode.builder.navigation.TabbedModeViewNavigators;
import io.jmix.tabbedmode.component.router.JmixRouterLink;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheet;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import io.jmix.tabbedmode.navigation.TabbedModeRouteSupport;
import io.jmix.tabbedmode.security.authentication.TabbedModeLoginViewSupport;
import io.jmix.tabbedmode.sys.vaadin.TabbedModeVaadinServlet;
import io.jmix.tabbedmode.xml.layout.loader.MainTabSheetLoader;
import io.jmix.tabbedmode.xml.layout.loader.WorkAreaLoader;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {FlowuiConfiguration.class})
public class TabbedModeFlowuiConfiguration {

    @Bean("tabmod_UiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.tabbedmode.action"));
        return actionsConfiguration;
    }

    @Bean
    public ComponentRegistration workAreaComponent() {
        return ComponentRegistrationBuilder.create(WorkArea.class)
                .withComponentLoader(WorkAreaLoader.TAG, WorkAreaLoader.class)
                .build();

    }

    @Bean
    public ComponentRegistration mainTabSheetComponent() {
        return ComponentRegistrationBuilder.create(MainTabSheet.class)
                .withComponentLoader(MainTabSheetLoader.TAG, MainTabSheetLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration routerLinkComponent() {
        return ComponentRegistrationBuilder.create(JmixRouterLink.class)
                .replaceComponent(RouterLink.class)
                .build();
    }

    @Bean("tabmod_ServletRegistrationBean")
    public ServletRegistrationBean<SpringServlet> servletRegistrationBean(
            ObjectProvider<MultipartConfigElement> multipartConfig,
            VaadinConfigurationProperties configurationProperties,
            ApplicationContext context) {
        boolean rootMapping = RootMappedCondition
                .isRootMapping(configurationProperties.getUrlMapping());
        // Calls default configuration for ServletRegistrationBean at
        // com.vaadin.flow.spring.SpringBootAutoConfiguration.configureServletRegistrationBean
        return SpringBootAutoConfiguration.configureServletRegistrationBean(multipartConfig,
                configurationProperties, new TabbedModeVaadinServlet(context, rootMapping));
    }

    /* View Navigators */

    @Primary
    @Bean("tabmod_TabbedViewNavigators")
    public ViewNavigators tabbedModeViewNavigators(
            DetailViewNavigationProcessor detailViewNavigationProcessor,
            ListViewNavigationProcessor listViewNavigationProcessor,
            ViewNavigationProcessor viewNavigationProcessor) {
        return new TabbedModeViewNavigators(detailViewNavigationProcessor,
                listViewNavigationProcessor, viewNavigationProcessor);
    }

    @Primary
    @Bean("tabmod_TabbedViewNavigationProcessor")
    public ViewNavigationProcessor tabbedModeViewNavigationProcessor(ViewSupport viewSupport,
                                                                     ViewRegistry viewRegistry,
                                                                     ViewNavigationSupport navigationSupport,
                                                                     io.jmix.tabbedmode.Views views,
                                                                     ViewBuilderProcessor viewBuilderProcessor) {
        return new TabbedModeViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                views, viewBuilderProcessor);
    }

    @Primary
    @Bean("tabmod_TabbedDetailViewNavigationProcessor")
    public DetailViewNavigationProcessor tabbedModeDetailViewNavigationProcessor(
            ViewSupport viewSupport,
            ViewRegistry viewRegistry,
            ViewNavigationSupport navigationSupport,
            RouteSupport routeSupport,
            io.jmix.tabbedmode.Views views,
            DetailViewBuilderProcessor detailViewBuilderProcessor
    ) {
        return new TabbedModeDetailViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                routeSupport, views, detailViewBuilderProcessor);
    }

    @Primary
    @Bean("tabmod_TabbedListViewNavigationProcessor")
    public ListViewNavigationProcessor tabbedModeListViewNavigationProcessor(ViewSupport viewSupport,
                                                                             ViewRegistry viewRegistry,
                                                                             ViewNavigationSupport navigationSupport,
                                                                             io.jmix.tabbedmode.Views views,
                                                                             ViewBuilderProcessor viewBuilderProcessor) {
        return new TabbedModeListViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                views, viewBuilderProcessor);
    }

    /* Window Builder */

    @Primary
    @Bean("tabmod_TabbedWindowBuilderProcessor")
    public WindowBuilderProcessor tabbedModeWindowBuilderProcessor(ApplicationContext applicationContext,
                                                                   Views views,
                                                                   ViewRegistry viewRegistry,
                                                                   UiAccessChecker uiAccessChecker,
                                                                   ViewBuilderProcessor viewBuilderProcessor) {
        return new TabbedModeWindowBuilderProcessor(applicationContext, views, viewRegistry,
                uiAccessChecker, viewBuilderProcessor);
    }

    @Primary
    @Bean("tabmod_TabbedDetailWindowBuilderProcessor")
    public DetailWindowBuilderProcessor tabbedModeDetailWindowBuilderProcessor(
            ApplicationContext applicationContext,
            Views views,
            ViewRegistry viewRegistry,
            Metadata metadata,
            ExtendedEntities extendedEntities,
            UiViewProperties viewProperties,
            UiAccessChecker uiAccessChecker,
            List<EditedEntityTransformer> editedEntityTransformers,
            DetailViewBuilderProcessor detailViewBuilderProcessor) {
        return new TabbedModeDetailWindowBuilderProcessor(applicationContext, views, viewRegistry, metadata,
                extendedEntities, viewProperties, uiAccessChecker,
                editedEntityTransformers, detailViewBuilderProcessor);
    }

    @Primary
    @Bean("tabmod_TabbedLookupWindowBuilderProcessor")
    public LookupWindowBuilderProcessor tabbedModeLookupWindowBuilderProcessor(
            ApplicationContext applicationContext,
            Views views,
            ViewRegistry viewRegistry,
            Metadata metadata,
            MetadataTools metadataTools,
            DataManager dataManager,
            FetchPlans fetchPlans,
            EntityStates entityStates,
            ExtendedEntities extendedEntities,
            UiViewProperties viewProperties,
            UiAccessChecker uiAccessChecker,
            LookupViewBuilderProcessor lookupViewBuilderProcessor
    ) {
        return new TabbedModeLookupWindowBuilderProcessor(applicationContext, views, viewRegistry, metadata,
                metadataTools, dataManager, fetchPlans, entityStates, extendedEntities, viewProperties,
                uiAccessChecker, lookupViewBuilderProcessor);
    }

    /* Other */

    @Primary
    @Bean("tabmod_TabbedModeLoginViewSupport")
    public LoginViewSupport tabbedModeLoginViewSupport() {
        return new TabbedModeLoginViewSupport();
    }

    @Primary
    @Bean("tabmod_TabbedModeRouteSupport")
    public RouteSupport tabbedModeRouteSupport(UrlParamSerializer urlParamSerializer,
                                               ServletContext servletContext,
                                               UiEventPublisher uiEventPublisher) {
        return new TabbedModeRouteSupport(urlParamSerializer, servletContext, uiEventPublisher);
    }
}
