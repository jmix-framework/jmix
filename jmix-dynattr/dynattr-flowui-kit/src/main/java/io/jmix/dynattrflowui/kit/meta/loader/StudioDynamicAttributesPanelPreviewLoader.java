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
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the dynattr add-on's {@code dynattr:dynamicAttributesPanel}
 * component. There is no spring-free way to render the real thing: the runtime
 * {@code io.jmix.dynattrflowui.panel.DynamicAttributesPanel} needs five Spring beans
 * (UiComponentsGenerator, UiComponents, Messages, DynAttrMetadata, ViewValidation) plus a live
 * data container and the category/attribute metadata stored in the database - none of which exist
 * at design time, and the kit ships no spring-free variant to fall back to.
 * <p>
 * This loader instead renders an empty {@link FormLayout} - close in spirit to the panel's actual
 * generated-field layout, but empty since there is no metadata to generate fields from. The
 * pure-XML attributes {@code fieldWidth} and {@code categoryFieldVisible} only affect those
 * generated fields, so they're meaningless on this empty placeholder and intentionally unsupported;
 * likewise {@code dataContainer}, which is a runtime data binding.
 */
public class StudioDynamicAttributesPanelPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String DYNATTR_SCHEMA = "http://jmix.io/schema/dynattr/flowui";
    protected static final String DYNAMIC_ATTRIBUTES_PANEL_ELEMENT = "dynamicAttributesPanel";

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

        return panel;
    }
}
