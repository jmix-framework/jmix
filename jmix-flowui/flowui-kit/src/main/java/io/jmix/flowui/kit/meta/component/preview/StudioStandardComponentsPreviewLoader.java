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
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

import static io.jmix.flowui.kit.component.usermenu.JmixUserMenu.BUTTON_CONTENT_CLASS_NAME;

public final class StudioStandardComponentsPreviewLoader implements StudioPreviewComponentLoader {

    /** Item tags the preview can actually render; used to decide XML items vs. fallback placeholders. */
    private static final Set<String> RENDERABLE_ITEM_NAMES = Set.of(
            StudioXmlElements.TEXT_ITEM, StudioXmlElements.ACTION_ITEM,
            StudioXmlElements.VIEW_ITEM, StudioXmlElements.SEPARATOR);

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
                && StudioXmlElements.FRAGMENT.equals(element.getName());
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
                && StudioXmlElements.USER_MENU.equals(element.getName());
    }

    private Component loadUserMenu(Element userMenuElement, Element viewElement, StudioPreviewEnvironment environment) {
        JmixUserMenu<String> userMenu = new JmixUserMenu<>();
        userMenu.setUser("admin");

        Element itemsElement = userMenuElement.element(StudioXmlElements.ITEMS);
        if (hasRenderableItem(itemsElement)) {
            loadItems(userMenu, itemsElement, viewElement, environment, true);
        } else {
            PreviewActionSupport.addPlaceholderItems(userMenu::addTextItem);
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

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadItems(
            M menu, Element itemsElement, Element viewElement, StudioPreviewEnvironment environment,
            boolean nestingAllowed) {
        for (Element childElement : itemsElement.elements()) {
            switch (childElement.getName()) {
                case StudioXmlElements.TEXT_ITEM ->
                        loadTextItem(menu, childElement, viewElement, environment, nestingAllowed);
                case StudioXmlElements.ACTION_ITEM -> loadActionItem(menu, childElement, viewElement, environment);
                case StudioXmlElements.VIEW_ITEM -> loadViewItem(menu, childElement, environment);
                case StudioXmlElements.SEPARATOR -> menu.addSeparator();
                case StudioXmlElements.COMPONENT_ITEM -> {

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
            Element nestedItemsElement = itemElement.element(StudioXmlElements.ITEMS);
            if (hasRenderableItem(nestedItemsElement)) {
                loadItems(item.getSubMenu(), nestedItemsElement, viewElement, environment, false);
            }
        }
    }

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadActionItem(
            M menu, Element itemElement, Element viewElement, StudioPreviewEnvironment environment) {
        PreviewActionSupport.loadActionItem(itemElement, viewElement, environment,
                menu::addActionItem, menu::addTextItem);
    }

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
