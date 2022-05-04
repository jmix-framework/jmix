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

package io.jmix.flowui.kit.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class FlowUiComponentUtils {

    private FlowUiComponentUtils() {
    }

    @Nullable
    public static String iconToSting(@Nullable VaadinIcon icon) {
        if (icon == null) {
            return null;
        }

        return icon.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    public static void addComponentsToSlot(Element element, String slot, Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", slot);
            element.appendChild(component.getElement());
        }
    }

    public static void clearSlot(Element element, String slot) {
        element.getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .forEach(element::removeChild);
    }

    public static int findActionIndexById(List<Action> actions, String actionId) {
        int index = -1;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (Objects.equals(a.getId(), actionId)) {
                index = i;
                break;
            }
        }

        return index;
    }
}
