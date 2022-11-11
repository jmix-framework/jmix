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
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.stereotype.Component;

@Component("flowui_Views")
public class ViewsImpl implements Views {

    protected ViewRegistry viewRegistry;

    public ViewsImpl(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Override
    public View create(String viewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        return create(viewInfo.getControllerClass());
    }

    @Override
    public <T extends View> T create(Class<T> viewClass) {
        return Instantiator.get(UI.getCurrent()).getOrCreate(viewClass);
    }
}
