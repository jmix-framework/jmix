/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.HBoxLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.impl.HBoxLayoutImpl;

import java.util.function.Consumer;

@Deprecated
public class WebHBoxLayout extends HBoxLayoutImpl implements HBoxLayout {

    @Override
    public void expand(Component childComponent, String height, String width) {
        com.vaadin.ui.Component expandedComponent = ComponentsHelper.getComposition(childComponent);
        CubaComponentsHelper.expand(component, expandedComponent, height, width);
    }

    @Override
    public void removeLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        internalRemoveLayoutClickListener(listener);
    }
}
