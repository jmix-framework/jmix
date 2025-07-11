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

package io.jmix.flowui.impl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import io.jmix.flowui.Views;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.stereotype.Component;

/**
 * A class responsible for creating {@link View} instances based on view ids or view classes.
 * <p>
 * This implementation handles the instantiation of views registered in the {@link ViewRegistry}.
 */
@Component("flowui_Views")
public class ViewsImpl implements Views {

    protected ViewRegistry viewRegistry;

    public ViewsImpl(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Override
    public View create(String viewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        return createInternal(viewInfo.getControllerClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends View> T create(Class<T> viewClass) {
        String id = ViewDescriptorUtils.getInferredViewId(viewClass);
        return (T) create(id);
    }

    protected <T extends View> T createInternal(Class<T> viewClass) {
        return Instantiator.get(UI.getCurrent()).getOrCreate(viewClass);
    }
}
