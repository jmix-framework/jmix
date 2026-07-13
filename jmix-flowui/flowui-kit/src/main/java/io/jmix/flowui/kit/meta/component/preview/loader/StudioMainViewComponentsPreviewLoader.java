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

package io.jmix.flowui.kit.meta.component.preview.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Studio preview loader for the plain-Vaadin structural elements of a main view layout.
 * <p>
 * {@code mainView} and {@code view} roots are not handled here: they map to Spring-managed
 * classes, out of reach for this spring-free kit loader.
 */
public class StudioMainViewComponentsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final Map<String, Supplier<Component>> FACTORIES = Map.of(
            "appLayout", AppLayout::new,
            "initialLayout", VerticalLayout::new,
            "navigationBar", Div::new,
            "drawerLayout", Div::new,
            "layout", VerticalLayout::new
    );

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && FACTORIES.containsKey(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        Component component = FACTORIES.get(componentElement.getName()).get();
        loadComponentBaseAttributes(component, componentElement);
        return component;
    }
}
