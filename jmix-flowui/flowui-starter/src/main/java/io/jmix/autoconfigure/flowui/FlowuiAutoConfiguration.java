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

package io.jmix.autoconfigure.flowui;

import io.jmix.core.*;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.*;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.sys.vaadin.SecurityContextHolderAtmosphereInterceptor;
import io.jmix.flowui.view.ViewAttributes;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.DetailWindowBuilderProcessor;
import io.jmix.flowui.view.builder.EditedEntityTransformer;
import io.jmix.flowui.view.builder.LookupWindowBuilderProcessor;
import io.jmix.flowui.view.builder.WindowBuilderProcessor;
import io.jmix.flowui.view.navigation.*;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;


@AutoConfiguration
@Import({CoreConfiguration.class, FlowuiConfiguration.class})
public class FlowuiAutoConfiguration {

    @Bean("jmix_AppUiControllers")
    @ConditionalOnMissingBean(name = "jmix_AppUiControllers")
    public ViewControllersConfiguration viewControllersConfiguration(
            ApplicationContext applicationContext,
            AnnotationScanMetadataReaderFactory metadataReaderFactory,
            JmixModules jmixModules) {

        ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList(jmixModules.getLast().getBasePackage()));
        return viewControllers;
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

    @Bean("flowui_SecurityContextHolderAtmosphereInterceptor")
    @ConditionalOnProperty(name = "jmix.ui.websocket-request-security-context-provided")
    public SecurityContextHolderAtmosphereInterceptor securityContextHolderAtmosphereInterceptor() {
        return new SecurityContextHolderAtmosphereInterceptor();
    }

    @Bean("flowui_ViewNavigators")
    @ConditionalOnMissingBean
    public ViewNavigators viewNavigators(DetailViewNavigationProcessor detailViewNavigationProcessor,
                                         ListViewNavigationProcessor listViewNavigationProcessor,
                                         ViewNavigationProcessor viewNavigationProcessor) {
        return new ViewNavigators(detailViewNavigationProcessor, listViewNavigationProcessor, viewNavigationProcessor);
    }

    @Bean("flowui_ViewNavigationProcessor")
    @ConditionalOnMissingBean
    public ViewNavigationProcessor viewNavigationProcessor(ViewSupport viewSupport,
                                                           ViewRegistry viewRegistry,
                                                           ViewNavigationSupport navigationSupport) {
        return new ViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport);
    }

    @Bean("flowui_DetailViewNavigationProcessor")
    @ConditionalOnMissingBean
    public DetailViewNavigationProcessor detailViewNavigationProcessor(ViewSupport viewSupport,
                                                                       ViewRegistry viewRegistry,
                                                                       ViewNavigationSupport navigationSupport,
                                                                       RouteSupport routeSupport) {
        return new DetailViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport, routeSupport);
    }

    @Bean("flowui_ListViewNavigationProcessor")
    @ConditionalOnMissingBean
    public ListViewNavigationProcessor listViewNavigationProcessor(ViewSupport viewSupport,
                                                                   ViewRegistry viewRegistry,
                                                                   ViewNavigationSupport navigationSupport) {
        return new ListViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport);
    }

    @Bean("flowui_DialogWindows")
    @ConditionalOnMissingBean
    public DialogWindows dialogWindows(
            WindowBuilderProcessor windowBuilderProcessor,
            DetailWindowBuilderProcessor detailBuilderProcessor,
            LookupWindowBuilderProcessor lookupBuilderProcessor,
            @Autowired(required = false) ObjectProvider<OpenedDialogWindows> openedDialogWindows) {
        return new DialogWindows(windowBuilderProcessor, detailBuilderProcessor,
                lookupBuilderProcessor, openedDialogWindows);
    }

    @Bean("flowui_WindowBuilderProcessor")
    @ConditionalOnMissingBean
    public WindowBuilderProcessor windowBuilderProcessor(ApplicationContext applicationContext,
                                                         Views views,
                                                         ViewRegistry viewRegistry,
                                                         UiAccessChecker uiAccessChecker) {
        return new WindowBuilderProcessor(applicationContext, views, viewRegistry, uiAccessChecker);
    }

    @Bean("flowui_DetailWindowBuilderProcessor")
    @ConditionalOnMissingBean
    public DetailWindowBuilderProcessor detailWindowBuilderProcessor(
            ApplicationContext applicationContext,
            Views views,
            ViewRegistry viewRegistry,
            Metadata metadata,
            ExtendedEntities extendedEntities,
            UiViewProperties viewProperties,
            UiAccessChecker uiAccessChecker,
            @Nullable List<EditedEntityTransformer> editedEntityTransformers) {
        return new DetailWindowBuilderProcessor(applicationContext, views, viewRegistry, metadata, extendedEntities,
                viewProperties, uiAccessChecker, editedEntityTransformers);
    }

    @Bean("flowui_LookupWindowBuilderProcessor")
    @ConditionalOnMissingBean
    public LookupWindowBuilderProcessor lookupWindowBuilderProcessor(
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
            UiAccessChecker uiAccessChecker
    ) {
        return new LookupWindowBuilderProcessor(applicationContext, views, viewRegistry, metadata, metadataTools,
                dataManager, fetchPlans, entityStates, extendedEntities, viewProperties, uiAccessChecker);
    }

    @Bean("flowui_RouteSupport")
    @ConditionalOnMissingBean
    public RouteSupport routeSupport(UrlParamSerializer urlParamSerializer, ServletContext servletContext) {
        return new RouteSupport(urlParamSerializer, servletContext);
    }

    @Bean("flowui_ViewAttributes")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean
    public ViewAttributes viewAttributes(String viewId) {
        return new ViewAttributes(viewId);
    }
}
