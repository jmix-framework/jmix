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

package io.jmix.dynattrflowui.kit.meta.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the dynattr add-on's {@code dynattr:dynamicAttributesPanel} component.
 */
public class StudioDynamicAttributesPanelPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String DYNATTR_SCHEMA = "http://jmix.io/schema/dynattr/flowui";
    protected static final String DYNAMIC_ATTRIBUTES_PANEL_ELEMENT = "dynamicAttributesPanel";
    protected static final int PLACEHOLDER_FIELD_COUNT = 3;

    @Override
    public boolean isSupported(Element element) {
        return DYNATTR_SCHEMA.equals(element.getNamespaceURI())
                && DYNAMIC_ATTRIBUTES_PANEL_ELEMENT.equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        FormLayout panel = new FormLayout();

        loadComponentBaseAttributes(panel, componentElement);

        // The real panel is generated at runtime from the entity's dynamic attributes, so at design time
        // it has no fields at all - and an empty FormLayout collapses to zero height, which reads as a
        // missing component. Placeholder fields give it the shape it will have.
        for (int i = 1; i <= PLACEHOLDER_FIELD_COUNT; i++) {
            TextField field = new TextField();
            field.setLabel("Attribute " + i);
            panel.add(field);
        }

        return panel;
    }
}
