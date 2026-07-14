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

import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.component.usermenu.HasActionMenuItems;
import io.jmix.flowui.kit.component.usermenu.HasTextMenuItems;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

import static io.jmix.flowui.kit.component.usermenu.JmixUserMenu.BUTTON_CONTENT_CLASS_NAME;

/**
 * Studio preview loader for the standard {@code fragment} placeholder and for {@code userMenu}:
 * instantiates a {@link JmixUserMenu} and builds its declared {@code items} (text/action/view
 * items, separators) so the designer preview shows the real menu instead of hardcoded placeholders.
 * <p>
 * Unlike {@link io.jmix.flowui.kit.meta.component.preview.loader.StudioGridPreviewLoader}, items
 * here are built <b>unconditionally</b>, without a {@link StudioPreviewEnvironment} handshake gate:
 * {@link JmixUserMenu} is a {@code Composite<JmixMenuBar>} that is neither {@code HasComponents} nor
 * {@code HasMenuItemsEnhanced}, so Studio's designer has never grafted or injected items onto a
 * userMenu preview on any released version (verified: no {@code userMenu} handling anywhere in
 * Studio's designer sources) — no Studio version can double-add items built here. The environment
 * is only used to improve {@code msg://} text resolution; without it (old Studio, routed through
 * the 2-arg {@link #load(Element, Element)} to {@link StudioPreviewEnvironment#NOOP NOOP}) the raw
 * message key is shown as-is, which is still strictly better than a placeholder.
 * <p>
 * Limitations inherent to a data/context-less preview:
 * <ul>
 *     <li>{@code componentItem} is skipped: building its nested component needs the runtime
 *     {@code LayoutLoader}, which isn't available to a spring-free kit loader.</li>
 *     <li>{@code viewItem} is approximated as a plain text item (resolved {@code text}, else
 *     {@code viewId}, else the item's {@code id}) plus its {@code icon} attribute: opening the
 *     referenced view isn't meaningful in a static preview.</li>
 *     <li>{@code actionItem ref} is resolved by a best-effort search for a matching
 *     {@code <action id="...">} declared anywhere in the view XML (e.g. under an {@code <actions>}
 *     block); if the reference can't be found there, the item falls back to showing its id as
 *     plain text so the menu still renders something.</li>
 *     <li>Nested {@code <items>} under a {@code textItem} are built <b>one level deep only</b>
 *     (via {@link TextUserMenuItem#getSubMenu()}): a further-nested {@code <items>} inside one of
 *     those sub-items is not built. {@code actionItem}/{@code viewItem} never declare nested
 *     {@code <items>} per the layout XSD, so nesting only applies to {@code textItem}.</li>
 *     <li>If {@code <items>} is present but has no child the preview can render (e.g. only
 *     {@code componentItem} children, or none at all), the loader falls back to the 3 hardcoded
 *     placeholder items rather than showing an empty menu.</li>
 * </ul>
 */
// TODO: minimal support for generic component preview?
public final class StudioStandardComponentsPreviewLoader implements StudioPreviewComponentLoader {

    private static final String ITEMS_ELEMENT = "items";
    private static final String TEXT_ITEM_ELEMENT = "textItem";
    private static final String ACTION_ITEM_ELEMENT = "actionItem";
    private static final String VIEW_ITEM_ELEMENT = "viewItem";
    private static final String SEPARATOR_ELEMENT = "separator";
    private static final String COMPONENT_ITEM_ELEMENT = "componentItem";
    private static final String ACTION_ELEMENT = "action";

    /** Item tags the preview can actually render; used to decide XML items vs. fallback placeholders. */
    private static final Set<String> RENDERABLE_ITEM_NAMES =
            Set.of(TEXT_ITEM_ELEMENT, ACTION_ITEM_ELEMENT, VIEW_ITEM_ELEMENT, SEPARATOR_ELEMENT);

    @Override
    public boolean isSupported(Element element) {
        return isFragment(element) || isUserMenu(element);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        if (isFragment(componentElement)) {
            return loadFragment(componentElement);
        } else if (isUserMenu(componentElement)) {
            return loadUserMenu(componentElement, viewElement, environment);
        } else {
            return null;
        }
    }

    private boolean isFragment(Element element) {
        return hasViewOrFragmentSchema(element)
                && "fragment".equals(element.getName());
    }

    private Component loadFragment(Element fragment) {
        if (FRAGMENT_SCHEMA.equals(fragment.getNamespaceURI())) {
            return new VerticalLayout();
        } else {
            return new Image("icons/studio-fragment-preview.svg", "FRAGMENT");
        }
    }

    private boolean isUserMenu(Element element) {
        return hasViewOrFragmentSchema(element)
                && "userMenu".equals(element.getName());
    }

    private Component loadUserMenu(Element userMenuElement, Element viewElement, StudioPreviewEnvironment environment) {
        JmixUserMenu<String> userMenu = new JmixUserMenu<>();
        userMenu.setUser("admin");

        Element itemsElement = userMenuElement.element(ITEMS_ELEMENT);
        if (hasRenderableItem(itemsElement)) {
            loadItems(userMenu, itemsElement, viewElement, environment, true);
        } else {
            userMenu.addTextItem("i1", "Item #1");
            userMenu.addTextItem("i2", "Item #2");
            userMenu.addTextItem("i3", "Item #3");
        }

        userMenu.setButtonRenderer(user -> {
            Div wrapper = new Div();
            wrapper.setClassName(BUTTON_CONTENT_CLASS_NAME);

            Avatar avatar = new Avatar();
            avatar.setName(user);
            avatar.getElement().setAttribute("tabindex", "-1");
            avatar.setClassName(BUTTON_CONTENT_CLASS_NAME + "-user-avatar");

            Span name = new Span();
            name.setText(user);
            name.setClassName(BUTTON_CONTENT_CLASS_NAME + "-user-name");

            wrapper.add(avatar, name);
            return wrapper;
        });

        return userMenu;
    }

    private boolean hasRenderableItem(@Nullable Element itemsElement) {
        if (itemsElement == null) {
            return false;
        }
        for (Element child : itemsElement.elements()) {
            if (RENDERABLE_ITEM_NAMES.contains(child.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds one user menu item per {@code items} child, in document order. {@code nestingAllowed}
     * caps sub-item building to one level: a {@code textItem}'s own nested {@code <items>} is only
     * built when {@code true}, and the recursive call for that sub-level passes {@code false} so a
     * further-nested {@code <items>} is not built.
     */
    private <M extends HasTextMenuItems & HasActionMenuItems> void loadItems(
            M menu, Element itemsElement, Element viewElement, StudioPreviewEnvironment environment,
            boolean nestingAllowed) {
        for (Element childElement : itemsElement.elements()) {
            switch (childElement.getName()) {
                case TEXT_ITEM_ELEMENT -> loadTextItem(menu, childElement, viewElement, environment, nestingAllowed);
                case ACTION_ITEM_ELEMENT -> loadActionItem(menu, childElement, viewElement, environment);
                case VIEW_ITEM_ELEMENT -> loadViewItem(menu, childElement, environment);
                case SEPARATOR_ELEMENT -> menu.addSeparator();
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

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadTextItem(
            M menu, Element itemElement, Element viewElement, StudioPreviewEnvironment environment,
            boolean nestingAllowed) {
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        String text = loadString(itemElement, "text")
                .map(value -> PreviewActionSupport.resolveText(environment, value))
                .orElse(null);
        if (text == null) {
            // Runtime throws without resolvable text; JmixUserMenu#addTextItem also requires
            // non-null text (unlike dropdownButton's items), so preview skips silently too.
            return;
        }

        TextUserMenuItem item = BaseComponentLoaderSupport.loadIconSetIcon(itemElement)
                .<TextUserMenuItem>map(icon -> menu.addTextItem(id, text, icon))
                .orElseGet(() -> menu.addTextItem(id, text));

        if (nestingAllowed) {
            Element nestedItemsElement = itemElement.element(ITEMS_ELEMENT);
            if (hasRenderableItem(nestedItemsElement)) {
                loadItems(item.getSubMenu(), nestedItemsElement, viewElement, environment, false);
            }
        }
    }

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadActionItem(
            M menu, Element itemElement, Element viewElement, StudioPreviewEnvironment environment) {
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        Element actionElement = itemElement.element(ACTION_ELEMENT);
        if (actionElement != null) {
            menu.addActionItem(id, PreviewActionSupport.buildAction(actionElement, id, environment));
            return;
        }

        String ref = loadString(itemElement, "ref").orElse(null);
        if (ref == null) {
            // Neither an inline action nor a ref: runtime throws, preview skips silently.
            return;
        }

        Element refActionElement = PreviewActionSupport.findDescendantAction(viewElement, ref);
        if (refActionElement != null) {
            menu.addActionItem(id, PreviewActionSupport.buildAction(refActionElement, ref, environment));
        } else {
            // Reference can't be resolved in preview (e.g. wired some other way): fall back to
            // the id as visible text so the menu still shows something for this item.
            menu.addTextItem(id, id);
        }
    }

    /**
     * Approximates {@code viewItem} as a plain text item: {@code text} (resolved), else
     * {@code viewId}, else the item's {@code id}, plus its {@code icon} attribute. Opening the
     * referenced view isn't meaningful in a static preview, so only the visible label is built.
     */
    private <M extends HasTextMenuItems> void loadViewItem(M menu, Element itemElement,
                                                           StudioPreviewEnvironment environment) {
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        String text = loadString(itemElement, "text")
                .map(value -> PreviewActionSupport.resolveText(environment, value))
                .or(() -> loadString(itemElement, "viewId"))
                .orElse(id);

        BaseComponentLoaderSupport.loadIconSetIcon(itemElement)
                .ifPresentOrElse(
                        icon -> menu.addTextItem(id, text, icon),
                        () -> menu.addTextItem(id, text));
    }
}
