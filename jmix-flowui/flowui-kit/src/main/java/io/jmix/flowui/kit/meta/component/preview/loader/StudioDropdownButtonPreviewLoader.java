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
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.xml.layout.support.ComponentLoaderUtils;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Studio preview loader for {@code dropdownButton} and {@code comboButton}
 */
public class StudioDropdownButtonPreviewLoader implements StudioPreviewComponentLoader {

    /** Item tags this loader renders; the rest fall back to placeholders. {@code componentItem} is not one. */
    private static final Set<String> RENDERABLE_ITEM_NAMES = Set.of(
            StudioXmlElements.TEXT_ITEM, StudioXmlElements.ACTION_ITEM, StudioXmlElements.SEPARATOR);

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && (StudioXmlElements.DROPDOWN_BUTTON.equals(element.getName())
                || StudioXmlElements.COMBO_BUTTON.equals(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        AbstractDropdownButton button = StudioXmlElements.COMBO_BUTTON.equals(componentElement.getName())
                ? new ComboButton() : new DropdownButton();

        loadComponentBaseAttributes(button, componentElement);
        loadBoolean(componentElement, "openOnHover", button::setOpenOnHover);
        ComponentLoaderUtils.loadIconSetIcon(componentElement).ifPresent(button::setIcon);
        if (button instanceof ComboButton comboButton) {
            ComponentLoaderUtils.loadIconSetIcon(componentElement, "dropdownIcon")
                    .ifPresent(comboButton::setDropdownIcon);
        }

        Element itemsElement = componentElement.element(StudioXmlElements.ITEMS);
        if (PreviewActionSupport.hasRenderableItem(itemsElement, RENDERABLE_ITEM_NAMES)) {
            loadItems(button, itemsElement, viewElement, environment);
        } else if (environment != StudioPreviewEnvironment.NOOP) {
            loadPlaceholderItems(button);
        }

        return button;
    }

    /**
     * Nothing renderable declared in {@code <items>}: mirrors Studio's old {@code postInitHasMenuItems}
     * placeholder so Studio itself can stay silent. Old Studio (NOOP) still adds its own, so this is
     * skipped then.
     */
    protected void loadPlaceholderItems(AbstractDropdownButton button) {
        PreviewActionSupport.addPlaceholderItems(button::addItem);
    }

    /**
     * Builds one dropdown item per {@code items} child, in document order.
     */
    protected void loadItems(AbstractDropdownButton button, Element itemsElement, Element viewElement,
                             StudioPreviewEnvironment environment) {
        for (Element childElement : itemsElement.elements()) {
            switch (childElement.getName()) {
                case StudioXmlElements.TEXT_ITEM -> loadTextItem(button, childElement, environment);
                case StudioXmlElements.ACTION_ITEM -> loadActionItem(button, childElement, viewElement, environment);
                case StudioXmlElements.SEPARATOR -> button.addSeparator();
                case StudioXmlElements.COMPONENT_ITEM -> {
                    // componentItem needs the runtime LayoutLoader to build nested content:
                    // not available to a spring-free kit loader, so skipped in preview.
                }
                default -> {
                    // unknown items child: skipped silently in preview
                }
            }
        }
    }

    protected void loadTextItem(AbstractDropdownButton button, Element itemElement,
                                StudioPreviewEnvironment environment) {
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        String text = loadString(itemElement, "text")
                .map(value -> PreviewActionSupport.resolveText(environment, value))
                .orElse(null);
        button.addItem(id, text);
    }

    protected void loadActionItem(AbstractDropdownButton button, Element itemElement, Element viewElement,
                                  StudioPreviewEnvironment environment) {
        PreviewActionSupport.loadActionItem(itemElement, viewElement, environment,
                button::addItem, button::addItem);
    }
}
