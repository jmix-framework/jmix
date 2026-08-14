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
import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Studio preview loader for the view's non-visual elements: the {@code data} and {@code facets}
 * blocks and their children. They render nothing at runtime, so the preview builds an invisible
 * stand-in - Studio wraps these in its own {@code FlowInvisibleComponent}, which hides the content
 * anyway.
 * <p>
 * Claiming them keeps every tag of a view descriptor owned by the framework, so Studio's reflection
 * fallback isn't needed for anything a view can contain (decision 0012).
 * <p>
 * Deliberately excluded, because the same tag names are real components and the component meaning
 * must win: {@code genericFilter} and {@code propertyFilter} (also {@code urlQueryParameters}
 * children) and {@code component} (also a {@code settings} child). Actions and grid columns are
 * excluded too - they are objects rather than components and have their own SPI roles.
 */
class StudioNonVisualElementsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final Set<String> ELEMENTS = Set.of(
            // data
            StudioXmlElements.DATA,
            StudioXmlElements.INSTANCE,
            StudioXmlElements.COLLECTION,
            StudioXmlElements.KEY_VALUE_INSTANCE,
            StudioXmlElements.KEY_VALUE_COLLECTION,
            StudioXmlElements.LOADER,
            StudioXmlElements.FETCH_PLAN,
            StudioXmlElements.QUERY,
            StudioXmlElements.PROPERTIES,
            StudioXmlElements.PROPERTY,
            StudioXmlElements.CONDITION,
            StudioXmlElements.AND,
            StudioXmlElements.OR,

            // facets
            StudioXmlElements.FACETS,
            StudioXmlElements.DATA_LOAD_COORDINATOR,
            StudioXmlElements.REFRESH,
            StudioXmlElements.URL_QUERY_PARAMETERS,
            StudioXmlElements.PAGINATION,
            StudioXmlElements.DATA_GRID_FILTER,
            StudioXmlElements.TIMER,
            StudioXmlElements.SETTINGS
    );

    /**
     * Used by tests to iterate the full supported set.
     */
    public static Set<String> supportedElements() {
        return ELEMENTS;
    }

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && ELEMENTS.contains(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        Div stub = new Div();
        stub.setVisible(false);
        return stub;
    }
}
