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

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.navigation.DetailViewNavigationProcessor;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.flowui.view.navigation.ListViewNavigationProcessor;
import io.jmix.flowui.view.navigation.ViewNavigationProcessor;

public class TabbedModeViewNavigators extends ViewNavigators {

    public TabbedModeViewNavigators(DetailViewNavigationProcessor detailViewNavigationProcessor,
                                    ListViewNavigationProcessor listViewNavigationProcessor,
                                    ViewNavigationProcessor viewNavigationProcessor) {
        super(detailViewNavigationProcessor, listViewNavigationProcessor, viewNavigationProcessor);
    }

    @Override
    public <E> DetailViewNavigator<E> detailView(ListDataComponent<E> listDataComponent) {
        EnhancedDetailViewNavigator<E> viewNavigator =
                new EnhancedDetailViewNavigator<>(super.detailView(listDataComponent));
        viewNavigator.withListDataComponent(listDataComponent);

        return viewNavigator;
    }

    @Override
    public <E> DetailViewNavigator<E> detailView(EntityPickerComponent<E> picker) {
        EnhancedDetailViewNavigator<E> viewNavigator =
                new EnhancedDetailViewNavigator<>(super.detailView(picker));
        //noinspection unchecked
        viewNavigator.withField(((HasValue<?, E>) picker));

        return viewNavigator;
    }
}