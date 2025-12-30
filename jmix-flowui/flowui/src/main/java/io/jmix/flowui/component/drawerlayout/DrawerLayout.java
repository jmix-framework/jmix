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
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.function.BiPredicate;

import static io.jmix.flowui.component.UiComponentUtils.findFragment;

@Tag("jmix-drawer-layout")
@JsModule("./src/drawer-layout/jmix-drawer-layout.js")
public class DrawerLayout extends JmixDrawerLayout implements ComponentContainer, HasSubParts,
        InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    protected void initComponent() {
        getThemeNames().add("dimmed-curtain");
    }

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
