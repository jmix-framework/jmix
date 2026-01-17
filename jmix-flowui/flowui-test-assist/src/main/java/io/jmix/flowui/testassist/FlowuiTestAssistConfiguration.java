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

package io.jmix.flowui.testassist;

import com.vaadin.flow.spring.VaadinScopesConfig;
import io.jmix.core.*;
import io.jmix.flowui.*;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.testassist.navigation.TestDetailViewNavigationProcessor;
import io.jmix.flowui.testassist.navigation.TestListViewNavigationProcessor;
import io.jmix.flowui.testassist.navigation.TestViewNavigationProcessor;
import io.jmix.flowui.testassist.navigation.ViewNavigationDelegate;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.List;

@Configuration
@ComponentScan
@Import({VaadinScopesConfig.class})
@PropertySource(name = "io.jmix.flowui.testassist", value = "classpath:/io/jmix/flowui/testassist/module.properties")
public class FlowuiTestAssistConfiguration {

    @Bean("ui_PropagationExceptionHandler")
    @Order(JmixOrder.LOWEST_PRECEDENCE - 90)
    public PropagationExceptionHandler propagationExceptionHandler() {
        return new PropagationExceptionHandler();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ViewAttributes testViewAttributes(String viewId) {
        return new TestViewAttributes(viewId);
    }

    @Bean("ui_ViewNavigationDelegate")
    public ViewNavigationDelegate<? extends AbstractViewNavigator> viewNavigationDelegate() {
        return new ViewNavigationDelegate<>();
    }

    @Bean("flowui_ViewNavigators")
    public ViewNavigators viewNavigators(DetailViewNavigationProcessor detailViewNavigationProcessor,
                                         ListViewNavigationProcessor listViewNavigationProcessor,
                                         ViewNavigationProcessor viewNavigationProcessor) {
        return new ViewNavigators(detailViewNavigationProcessor, listViewNavigationProcessor, viewNavigationProcessor);
    }

    @Bean("ui_TestDetailViewNavigationProcessor")
    public DetailViewNavigationProcessor detailViewNavigationProcessor(ViewSupport viewSupport,
                                                                       ViewRegistry viewRegistry,
                                                                       ViewNavigationSupport navigationSupport,
                                                                       RouteSupport routeSupport,
                                                                       ViewNavigationDelegate<?> navigationDelegate) {
        return new TestDetailViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport, routeSupport,
                (ViewNavigationDelegate<DetailViewNavigator<?>>) navigationDelegate);
    }

    @Bean("ui_TestListViewNavigationProcessor")
    public ListViewNavigationProcessor listViewNavigationProcessor(ViewSupport viewSupport,
                                                                   ViewRegistry viewRegistry,
                                                                   ViewNavigationSupport navigationSupport,
                                                                   ViewNavigationDelegate<?> navigationDelegate) {
        return new TestListViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                (ViewNavigationDelegate<ListViewNavigator<?>>) navigationDelegate);
    }

    @Bean("ui_TestViewNavigationProcessor")
    public ViewNavigationProcessor viewNavigationProcessor(ViewSupport viewSupport,
                                                           ViewRegistry viewRegistry,
                                                           ViewNavigationSupport navigationSupport,
                                                           ViewNavigationDelegate<?> navigationDelegate) {
        return new TestViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                (ViewNavigationDelegate<ViewNavigator>) navigationDelegate);
    }

    @Bean("flowui_DialogWindows")
    public DialogWindows dialogWindows(
            WindowBuilderProcessor windowBuilderProcessor,
            DetailWindowBuilderProcessor detailBuilderProcessor,
            LookupWindowBuilderProcessor lookupBuilderProcessor,
            @Autowired(required = false) ObjectProvider<OpenedDialogWindows> openedDialogWindows) {
        return new DialogWindows(windowBuilderProcessor, detailBuilderProcessor,
                lookupBuilderProcessor, openedDialogWindows);
    }

    @Bean("flowui_WindowBuilderProcessor")
    public WindowBuilderProcessor windowBuilderProcessor(ApplicationContext applicationContext,
                                                         Views views,
                                                         ViewRegistry viewRegistry,
                                                         UiAccessChecker uiAccessChecker) {
        return new WindowBuilderProcessor(applicationContext, views, viewRegistry, uiAccessChecker);
    }

    @Bean("flowui_DetailWindowBuilderProcessor")
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
    public RouteSupport routeSupport(UrlParamSerializer urlParamSerializer, ServletContext servletContext) {
        return new RouteSupport(urlParamSerializer, servletContext);
    }

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
    public void onApplicationContextRefresh(ContextRefreshedEvent event) {
        UiTestUtils.setApplicationContext(event.getApplicationContext());
    }
}
