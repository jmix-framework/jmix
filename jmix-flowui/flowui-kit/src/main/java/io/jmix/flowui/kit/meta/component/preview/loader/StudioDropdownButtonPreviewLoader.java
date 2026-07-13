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

import java.util.Set;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.meta.component.preview.ComponentCreationResult;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for {@code dropdownButton} and {@code comboButton}: instantiates the
 * component and builds its declared {@code items} (text/action items, separators) so the
 * designer preview shows the menu instead of an empty overlay.
 * <p>
 * Unlike {@link StudioGridPreviewLoader}, items here are built <b>unconditionally</b>, without a
 * {@link StudioPreviewEnvironment} handshake gate: Studio's designer has never rendered dropdown
 * items on any released version (item tags model as plain elements with no live Vaadin instance
 * attached, and the designer's menu-item post-init only targets the {@code JmixMenuBar} family,
 * not {@link AbstractDropdownButton}), so no Studio version can double-add items built here. The
 * environment is only used to improve {@code msg://} text resolution; without it (old Studio,
 * routed through the 2-arg {@link #load(Element, Element)} to {@link StudioPreviewEnvironment#NOOP
 * NOOP}) the raw message key is shown as-is, which is still strictly better than an empty menu.
 * <p>
 * Limitations inherent to a data/context-less preview:
 * <ul>
 *     <li>{@code componentItem} is skipped: building its nested component needs the runtime
 *     {@code LayoutLoader}, which isn't available to a spring-free kit loader.</li>
 *     <li>Icons (button, {@code dropdownIcon}, action) are only loaded from the {@code icon}/
 *     {@code dropdownIcon} attribute (a {@code VaadinIcon} name); a nested {@code <icon>}
 *     component element would need the same {@code LayoutLoader} and is skipped.</li>
 *     <li>{@code actionItem ref} is resolved by a best-effort search for a matching
 *     {@code <action id="...">} declared anywhere in the view XML (e.g. under an {@code <actions>}
 *     block); if the reference can't be found there, the item falls back to showing its id as
 *     plain text so the menu still renders something.</li>
 * </ul>
 * Action-building and {@code ref}-resolution logic is shared with other preview loaders via
 * {@link PreviewActionSupport}.
 */
public class StudioDropdownButtonPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String DROPDOWN_BUTTON_ELEMENT = "dropdownButton";
    protected static final String COMBO_BUTTON_ELEMENT = "comboButton";

    protected static final String ITEMS_ELEMENT = "items";
    protected static final String TEXT_ITEM_ELEMENT = "textItem";
    protected static final String ACTION_ITEM_ELEMENT = "actionItem";
    protected static final String SEPARATOR_ELEMENT = "separator";
    protected static final String COMPONENT_ITEM_ELEMENT = "componentItem";
    protected static final String ACTION_ELEMENT = "action";

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && (DROPDOWN_BUTTON_ELEMENT.equals(element.getName())
                        || COMBO_BUTTON_ELEMENT.equals(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        AbstractDropdownButton button = COMBO_BUTTON_ELEMENT.equals(componentElement.getName())
                ? new ComboButton()
                : new DropdownButton();

        loadComponentBaseAttributes(button, componentElement);
        loadBoolean(componentElement, "openOnHover", button::setOpenOnHover);
        BaseComponentLoaderSupport.loadIconSetIcon(componentElement).ifPresent(button::setIcon);
        if (button instanceof ComboButton comboButton) {
            BaseComponentLoaderSupport.loadIconSetIcon(componentElement, "dropdownIcon")
                    .ifPresent(comboButton::setDropdownIcon);
        }

        Element itemsElement = componentElement.element(ITEMS_ELEMENT);
        if (itemsElement != null) {
            loadItems(button, itemsElement, viewElement, environment);
        }

        return button;
    }

    @Override
    public Set<String> ownedAspects(Element componentElement) {
        return componentElement.element(ITEMS_ELEMENT) != null
                ? Set.of(ComponentCreationResult.ITEMS)
                : Set.of();
    }

    /**
     * Builds one dropdown item per {@code items} child, in document order.
     */
    protected void loadItems(AbstractDropdownButton button, Element itemsElement, Element viewElement,
                             StudioPreviewEnvironment environment) {
        for (Element childElement : itemsElement.elements()) {
            switch (childElement.getName()) {
                case TEXT_ITEM_ELEMENT -> loadTextItem(button, childElement, environment);
                case ACTION_ITEM_ELEMENT -> loadActionItem(button, childElement, viewElement, environment);
                case SEPARATOR_ELEMENT -> button.addSeparator();
                case COMPONENT_ITEM_ELEMENT -> {
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
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        Element actionElement = itemElement.element(ACTION_ELEMENT);
        if (actionElement != null) {
            button.addItem(id, PreviewActionSupport.buildAction(actionElement, id, environment));
            return;
        }

        String ref = loadString(itemElement, "ref").orElse(null);
        if (ref == null) {
            // Neither an inline action nor a ref: runtime throws, preview skips silently.
            return;
        }

        Element refActionElement = PreviewActionSupport.findDescendantAction(viewElement, ref);
        if (refActionElement != null) {
            button.addItem(id, PreviewActionSupport.buildAction(refActionElement, ref, environment));
        } else {
            // Reference can't be resolved in preview (e.g. wired some other way): fall back to
            // the id as visible text so the menu still shows something for this item.
            button.addItem(id, id);
        }
    }
}
