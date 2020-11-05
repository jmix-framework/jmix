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

import com.haulmont.cuba.gui.WindowContext;
import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.Window;
import com.vaadin.ui.AbstractOrderedLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.impl.TabWindowImpl;

@Deprecated
public class WebTabWindow extends TabWindowImpl implements Window {

    @Override
    public WindowContext getContext() {
        return (WindowContext) super.getContext();
    }

    @Override
    public void expand(Component childComponent, String height, String width) {
        com.vaadin.ui.Component expandedComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);
        CubaComponentsHelper.expand((AbstractOrderedLayout) getContainer(), expandedComponent, height, width);
    }
}
