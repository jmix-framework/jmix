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

package io.jmix.flowui.facet.impl;

import com.vaadin.flow.component.Component;
import io.jmix.core.impl.QueryParamValuesManager;
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.ViewDataLoadCoordinator;
import io.jmix.flowui.facet.dataloadcoordinator.OnViewEventLoadTrigger;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;

public class ViewDataLoadCoordinatorImpl extends AbstractDataLoadCoordinator
        implements ViewDataLoadCoordinator {

    public ViewDataLoadCoordinatorImpl(ReflectionCacheManager reflectionCacheManager,
                                       QueryParamValuesManager queryParamValuesManager) {
        super(reflectionCacheManager, queryParamValuesManager);
    }

    // implementation of ViewDataLoadCoordinator interface shouldn't be removed
    @Override
    public void addOnViewEventLoadTrigger(DataLoader loader, Class<?> eventClass) {
        triggers.add(new OnViewEventLoadTrigger(getOwnerNN(), reflectionCacheManager, loader, eventClass));
    }

    @Override
    protected void addOnDefaultEventLoadTrigger(DataLoader loader) {
        // if the loader has no parameters in a query, add trigger on View.BeforeShowEvent
        addOnViewEventLoadTrigger(loader, View.BeforeShowEvent.class);
    }

    @Override
    protected Component findComponent(String componentId) {
        return UiComponentUtils.getComponent(getOwnerNN(), componentId);
    }

    @Override
    protected HasDataComponents getOwnerData() {
        return ViewControllerUtils.getViewData(getOwnerNN());
    }

    @Override
    protected View<?> getOwnerNN() {
        return (View<?>) super.getOwnerNN();
    }
}
