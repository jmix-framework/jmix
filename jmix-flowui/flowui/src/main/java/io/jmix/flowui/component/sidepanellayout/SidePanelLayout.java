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

package io.jmix.flowui.component.sidepanellayout;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.sidepanellayout.JmixSidePanelLayout;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

/**
 * The drawer layout component provides a container for a main content area and a drawer panel.
 *
 * @see SidePanelLayoutToggle
 */
public class SidePanelLayout extends JmixSidePanelLayout implements ComponentContainer, HasSubParts {

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        List<Component> ownComponents = new ArrayList<>();
        if (getContent() != null) {
            ownComponents.add(getContent());
        }
        if (getDrawerContent() != null) {
            ownComponents.add(getDrawerContent());
        }
        return ownComponents.isEmpty() ? Collections.emptyList() : List.copyOf(ownComponents);
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        if (getContent() != null) {
            Optional<Component> component = findComponent(getContent(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }
        if (getDrawerContent() != null) {
            Optional<Component> contentComponent = findComponent(getDrawerContent(), name);
            if (contentComponent.isPresent()) {
                return contentComponent.get();
            }
        }
        return null;
    }

    protected Optional<Component> findComponent(Component component, String id) {
        BiPredicate<Component, String> idComparator = UiComponentUtils.findFragment(component) == null
                ? UiComponentUtils::sameId
                : FragmentUtils::sameId;

        if (idComparator.test(component, id)) {
            return Optional.of(component);
        } else if (UiComponentUtils.sameId(component, id)) {
            return Optional.of(component);
        }

        return UiComponentUtils.findComponent(component, id);
    }
}
