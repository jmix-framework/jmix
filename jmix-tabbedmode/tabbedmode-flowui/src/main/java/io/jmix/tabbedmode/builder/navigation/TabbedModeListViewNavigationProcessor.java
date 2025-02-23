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
import io.jmix.flowui.view.navigation.ListViewNavigationProcessor;
import io.jmix.flowui.view.navigation.ListViewNavigator;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.builder.ViewBuilder;
import io.jmix.tabbedmode.builder.ViewBuilderAdapter;
import io.jmix.tabbedmode.builder.ViewBuilderProcessor;
import io.jmix.tabbedmode.builder.ViewOpeningContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("tabmod_TabbedListViewNavigationProcessor")
public class TabbedModeListViewNavigationProcessor extends ListViewNavigationProcessor {

    protected final Views views;
    protected final ViewBuilderProcessor viewBuilderProcessor;

    public TabbedModeListViewNavigationProcessor(ViewSupport viewSupport,
                                                 ViewRegistry viewRegistry,
                                                 ViewNavigationSupport navigationSupport,
                                                 Views views,
                                                 ViewBuilderProcessor viewBuilderProcessor) {
        super(viewSupport, viewRegistry, navigationSupport);

        this.views = views;
        this.viewBuilderProcessor = viewBuilderProcessor;
    }

    @Override
    public void processNavigation(ListViewNavigator<?> navigator) {
        ViewBuilder<?> builder = new ViewBuilderAdapter<>(navigator,
                getViewClass(navigator),
                viewBuilderProcessor::build, this::openView);
        builder.open();
    }

    protected void openView(ViewOpeningContext context) {
        views.open(context);
    }
}
