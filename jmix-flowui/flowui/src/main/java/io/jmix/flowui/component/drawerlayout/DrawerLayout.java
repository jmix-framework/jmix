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

package io.jmix.flowui.component.drawerlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.drawerlayout.JmixDrawerLayout;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

import static io.jmix.flowui.component.UiComponentUtils.findFragment;

@Tag("jmix-drawer-layout")
@JsModule("./src/drawer-layout/jmix-drawer-layout.js")
public class DrawerLayout extends JmixDrawerLayout implements ComponentContainer, HasSubParts {

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return Optional.empty();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return List.of();
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        if (getLayout() != null) {
            Optional<Component> component = findComponent(getLayout(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }
        if (!getDrawerHeaderComponents().isEmpty()) {
            for (Component component : getDrawerHeaderComponents()) {
                Optional<Component> headerComponent = findComponent(component, name);
                if (headerComponent.isPresent()) {
                    return headerComponent.get();
                }
            }
        }
        if (!getDrawerContentComponents().isEmpty()) {
            for (Component component : getDrawerContentComponents()) {
                Optional<Component> contentComponent = findComponent(component, name);
                if (contentComponent.isPresent()) {
                    return contentComponent.get();
                }
            }
        }
        if (!getDrawerFooterComponents().isEmpty()) {
            for (Component component : getDrawerFooterComponents()) {
                Optional<Component> footerComponent = findComponent(component, name);
                if (footerComponent.isPresent()) {
                    return footerComponent.get();
                }
            }
        }
        return null;
    }

    protected Optional<Component> findComponent(Component component, String id) {
        BiPredicate<Component, String> idComparator = findFragment(component) == null
                ? UiComponentUtils::sameId
                : FragmentUtils::sameId;

        // TODO: pinyazhin should we search inside the fragment?
        if (idComparator.test(component, id)) {
            return Optional.of(component);
        } else if (UiComponentUtils.sameId(component, id)) {
            return Optional.of(component);
        }

        return UiComponentUtils.findComponent(component, id);
    }
}
