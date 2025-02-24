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

package io.jmix.tabbedmode.builder.navigation;

import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.DetailViewNavigationProcessor;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.builder.DetailViewBuilder;
import io.jmix.tabbedmode.builder.DetailViewBuilderAdapter;
import io.jmix.tabbedmode.builder.DetailViewBuilderProcessor;
import io.jmix.tabbedmode.builder.ViewOpeningContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("tabmod_TabbedDetailViewNavigationProcessor")
public class TabbedModeDetailViewNavigationProcessor extends DetailViewNavigationProcessor {

    protected final Views views;
    protected final DetailViewBuilderProcessor detailViewBuilderProcessor;

    public TabbedModeDetailViewNavigationProcessor(ViewSupport viewSupport,
                                                   ViewRegistry viewRegistry,
                                                   ViewNavigationSupport navigationSupport,
                                                   RouteSupport routeSupport,
                                                   Views views,
                                                   DetailViewBuilderProcessor detailViewBuilderProcessor) {
        super(viewSupport, viewRegistry, navigationSupport, routeSupport);

        this.views = views;
        this.detailViewBuilderProcessor = detailViewBuilderProcessor;
    }

    @Override
    public void processNavigation(DetailViewNavigator<?> navigator) {
        DetailViewBuilder<?, ?> builder = new DetailViewBuilderAdapter<>(navigator,
                getViewClass(navigator),
                detailViewBuilderProcessor::build, this::openView);

        builder.open();
    }

    protected void openView(ViewOpeningContext context) {
        views.open(context);
    }
}
