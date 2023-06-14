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

import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.testassist.navigation.TestDetailViewnavigationProcessor;
import io.jmix.flowui.testassist.navigation.TestListViewNavigationProcessor;
import io.jmix.flowui.testassist.navigation.TestViewNavigationProcessor;
import io.jmix.flowui.testassist.navigation.ViewNavigationDelegate;
import io.jmix.flowui.view.ViewAttributes;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

@Configuration
public class FlowuiTestAssistConfiguration {

    @Bean("ui_PropagationExceptionHandler")
    @Order(JmixOrder.LOWEST_PRECEDENCE - 90)
    public PropagationExceptionHandler testAssistExceptionHandler() {
        return new PropagationExceptionHandler();
    }

    @Bean
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ViewAttributes testViewAttributes(String viewId) {
        return new TestViewAttributes(viewId);
    }

    @Bean("ui_ViewNavigationDelegate")
    public ViewNavigationDelegate<? extends AbstractViewNavigator> viewNavigationDelegate(
            ViewNavigationSupport navigationSupport, ViewSupport viewSupport) {
        return new ViewNavigationDelegate<>(navigationSupport, viewSupport);
    }

    @Primary
    @Bean("ui_TestDetailViewNavigationProcessor")
    public DetailViewNavigationProcessor detailViewNavigationProcessor(ViewSupport viewSupport,
                                                                       ViewRegistry viewRegistry,
                                                                       ViewNavigationSupport navigationSupport,
                                                                       RouteSupport routeSupport, Metadata metadata,
                                                                       MetadataTools metadataTools,
                                                                       ViewNavigationDelegate<?> navigationDelegate) {
        return new TestDetailViewnavigationProcessor(viewSupport, viewRegistry, navigationSupport, routeSupport,
                metadata, metadataTools, (ViewNavigationDelegate<DetailViewNavigator<?>>) navigationDelegate);
    }

    @Primary
    @Bean("ui_TestListViewNavigationProcessor")
    public ListViewNavigationProcessor listViewNavigationProcessor(ViewSupport viewSupport,
                                                                   ViewRegistry viewRegistry,
                                                                   ViewNavigationSupport navigationSupport,
                                                                   ViewNavigationDelegate<?> navigationDelegate) {
        return new TestListViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                (ViewNavigationDelegate<ListViewNavigator<?>>) navigationDelegate);
    }

    @Primary
    @Bean("ui_TestViewNavigationProcessor")
    public ViewNavigationProcessor viewNavigationProcessor(ViewSupport viewSupport,
                                                           ViewRegistry viewRegistry,
                                                           ViewNavigationSupport navigationSupport,
                                                           ViewNavigationDelegate<?> navigationDelegate) {
        return new TestViewNavigationProcessor(viewSupport, viewRegistry, navigationSupport,
                (ViewNavigationDelegate<ViewNavigator>) navigationDelegate);
    }
}
