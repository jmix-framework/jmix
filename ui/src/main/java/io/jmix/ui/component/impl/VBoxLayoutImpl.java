/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component.impl;

import com.vaadin.server.Sizeable;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.widget.JmixVerticalActionsLayout;

public class VBoxLayoutImpl extends AbstractBox<JmixVerticalActionsLayout> implements VBoxLayout {

    public VBoxLayoutImpl() {
        component = createComponent();
        initComponent(component);
    }

    protected JmixVerticalActionsLayout createComponent() {
        return new JmixVerticalActionsLayout();
    }

    protected void initComponent(JmixVerticalActionsLayout component) {
        component.setWidth(100, Sizeable.Unit.PERCENTAGE);
    }

    @Override
    public ExpandDirection getExpandDirection() {
        return ExpandDirection.VERTICAL;
    }
}