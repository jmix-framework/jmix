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
import io.jmix.flowui.facet.FragmentDataLoadCoordinator;
import io.jmix.flowui.facet.dataloadcoordinator.OnFragmentEventLoadTrigger;
import io.jmix.flowui.facet.dataloadcoordinator.OnViewEventLoadTrigger;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.flowui.view.View;

public class FragmentDataLoadCoordinatorImpl extends AbstractDataLoadCoordinator
        implements FragmentDataLoadCoordinator {

    public FragmentDataLoadCoordinatorImpl(ReflectionCacheManager reflectionCacheManager,
                                           QueryParamValuesManager queryParamValuesManager) {
        super(reflectionCacheManager, queryParamValuesManager);
    }

    @Override
    public void addOnFragmentEventLoadTrigger(DataLoader loader, Class<?> eventClass) {
        triggers.add(new OnFragmentEventLoadTrigger(getOwnerNN(), reflectionCacheManager, loader, eventClass));
    }

    @Override
    protected void addOnDefaultEventLoadTrigger(DataLoader loader) {
        // if the loader has no parameters in a query, add trigger on Fragment.ReadyEvent
        addOnFragmentEventLoadTrigger(loader, Fragment.ReadyEvent.class);
    }

    @Override
    protected Component findComponent(String componentId) {
        return FragmentUtils.getComponent(getOwnerNN(), componentId);
    }

    @Override
    protected HasDataComponents getOwnerData() {
        return FragmentUtils.getFragmentData(getOwnerNN());
    }

    @Override
    protected Fragment<?> getOwnerNN() {
        return (Fragment<?>) super.getOwnerNN();
    }

    @Override
    public void addOnViewEventLoadTrigger(DataLoader loader, Class<?> eventClass) {
        View<?> view = FragmentUtils.getHostView(getOwnerNN());
        triggers.add(new OnViewEventLoadTrigger(view, reflectionCacheManager, loader, eventClass));

    }
}
