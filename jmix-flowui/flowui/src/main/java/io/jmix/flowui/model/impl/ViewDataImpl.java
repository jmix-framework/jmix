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

package io.jmix.flowui.model.impl;

import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link ViewData} interface.
 * Provides functionality for managing the data API elements of a {@link View}.
 */
@Component("flowui_ViewData")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewDataImpl extends AbstractDataComponentsHolder implements ViewData {

    protected String viewId;

    @Override
    @Nullable
    public String getViewId() {
        return viewId;
    }

    @Override
    public void setViewId(@Nullable String viewId) {
        this.viewId = viewId;
    }

    @Nullable
    @Override
    protected String getOwnerId() {
        return getViewId();
    }
}
