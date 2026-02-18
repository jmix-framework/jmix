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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.sidepanellayout.*;
import jakarta.annotation.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;
import java.util.function.BiPredicate;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

/**
 * The side panel layout component provides a container for a main content area and a side panel.
 *
 * @see SidePanelLayoutCloser
 */
public class SidePanelLayout extends JmixSidePanelLayout implements ComponentContainer, HasSubParts,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    protected void initComponent() {
        SidePanelPlacement defaultPlacement = applicationContext.getBean(UiComponentProperties.class)
                .getSidePanelLayoutDefaultPlacement();

        setSidePanelPlacement(defaultPlacement);
    }

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
        if (getSidePanelContent() != null) {
            ownComponents.add(getSidePanelContent());
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
        if (getSidePanelContent() != null) {
            Optional<Component> contentComponent = findComponent(getSidePanelContent(), name);
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
