/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.icon.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.icon.IconFactory;
import io.jmix.flowui.kit.icon.JmixFontIcon;

@org.springframework.stereotype.Component("flowui_Icons")
public class IconsImpl implements Icons {

    @Override
    public Component get(IconFactory<?> icon) {
        Preconditions.checkNotNullArgument(icon);
        return get(icon.name());
    }

    @Override
    public Component get(String iconName) {
        Preconditions.checkNotNullArgument(iconName);

        if (iconName.contains(":")) {
            return createIconFromCollection(iconName);
        } else {
            return createIconByName(iconName);
        }
    }

    protected Icon createIconFromCollection(String iconName) {
        String[] parts = iconName.split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("Unexpected number of icon parts, must be two");
        }

        return new Icon(parts[0], parts[1]);
    }

    protected Component createIconByName(String iconName) {
        JmixFontIcon jmixFontIcon = JmixFontIcon.fromName(iconName);
        return jmixFontIcon != null
                ? jmixFontIcon.create()
                : VaadinIcon.valueOf(iconName).create();
    }
}
