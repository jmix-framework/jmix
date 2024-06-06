/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview;

import java.util.List;
import javax.annotation.Nullable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.dom4j.Element;

// TODO: minimal support for generic component preview?
public final class StudioStandardComponentsPreviewLoader implements StudioPreviewComponentLoader {

    private static final String FRAGMENT_SCHEMA = "http://jmix.io/schema/flowui/fragment";
    private static final List<String> SUPPORTED_FRAGMENT_SCHEMAS = List.of(VIEW_SCHEMA, FRAGMENT_SCHEMA);

    @Override
    public boolean isSupported(Element element) {
        return isFragment(element);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        if (isFragment(componentElement)) {
            return loadFragment(componentElement);
        } else {
            return null;
        }
    }

    private Component loadFragment(Element fragment) {
        if (FRAGMENT_SCHEMA.equals(fragment.getNamespaceURI())) {
            return new VerticalLayout();
        } else {
            return new Image("icons/studio-fragment-preview.svg", "FRAGMENT");
        }
    }

    private boolean isFragment(Element element) {
        return SUPPORTED_FRAGMENT_SCHEMAS.contains(element.getNamespaceURI())
                && "fragment".equals(element.getName());
    }
}

