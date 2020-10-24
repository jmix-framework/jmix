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

package com.haulmont.cuba.gui.components;

import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.*;
import io.jmix.ui.widget.JmixFormLayout;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

@Deprecated
public final class CubaComponentsHelper {

    private CubaComponentsHelper() {
    }

    public static String getFilterComponentPath(Filter filter) {
        StringBuilder sb = new StringBuilder(filter.getId() != null ? filter.getId() : "filterWithoutId");
        Frame frame = filter.getFrame();
        while (frame != null) {
            sb.insert(0, ".");
            String s = frame.getId() != null ? frame.getId() : "frameWithoutId";
            if (s.contains(".")) {
                s = "[" + s + "]";
            }
            sb.insert(0, s);
            if (frame instanceof Window) {
                break;
            }
            frame = frame.getFrame();
        }
        return sb.toString();
    }

    public static boolean convertFieldGroupCaptionAlignment(FieldGroup.FieldCaptionAlignment captionAlignment) {
        return captionAlignment == FieldGroup.FieldCaptionAlignment.LEFT;
    }

    @Nullable
    protected static Component findChildComponent(FieldGroup fieldGroup, com.vaadin.ui.Component target) {
        com.vaadin.ui.Component vaadinSource = fieldGroup.unwrap(JmixFormLayout.class);
        Collection<Component> components = fieldGroup.getFields().stream()
                .map(FieldGroup.FieldConfig::getComponentNN)
                .collect(Collectors.toList());

        return ComponentsHelper.findChildComponent(components, vaadinSource, target);
    }

    @Nullable
    protected static Component findChildComponent(Collection<Component> components,
                                                  com.vaadin.ui.Component vaadinSource,
                                                  com.vaadin.ui.Component target) {
        com.vaadin.ui.Component targetComponent = ComponentsHelper.getDirectChildComponent(target, vaadinSource);

        for (Component component : components) {
            com.vaadin.ui.Component unwrapped = component.unwrapComposition(com.vaadin.ui.Component.class);
            if (unwrapped == targetComponent) {
                Component child = null;

                if (component instanceof HasComponents) {
                    child = ComponentsHelper.findChildComponent((HasComponents) component, target);
                }


                if (component instanceof HasButtonsPanel) {
                    ButtonsPanel buttonsPanel = ((HasButtonsPanel) component).getButtonsPanel();
                    if (buttonsPanel != null) {
                        if (ComponentsHelper.getVaadinSource(buttonsPanel) == target) {
                            return buttonsPanel;
                        } else {
                            child = ComponentsHelper.findChildComponent(buttonsPanel, target);
                        }
                    }
                }

                if (component instanceof FieldGroup) {
                    FieldGroup fieldGroup = (FieldGroup) component;
                    child = ComponentsHelper.findChildComponent(fieldGroup, target);
                }

                return child != null ? child : component;
            }
        }
        return null;
    }
}
