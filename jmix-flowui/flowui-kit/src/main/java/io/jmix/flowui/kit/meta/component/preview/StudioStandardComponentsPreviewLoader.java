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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

import static io.jmix.flowui.kit.component.usermenu.JmixUserMenu.BUTTON_CONTENT_CLASS_NAME;

// TODO: minimal support for generic component preview?
public final class StudioStandardComponentsPreviewLoader implements StudioPreviewComponentLoader {

    @Override
    public boolean isSupported(Element element) {
        return isFragment(element) || isUserMenu(element);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        if (isFragment(componentElement)) {
            return loadFragment(componentElement);
        } else if (isUserMenu(componentElement)) {
            return loadUserMenu(componentElement);
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

    private Component loadUserMenu(Element userMenuElement) {
        JmixUserMenu<String> userMenu = new JmixUserMenu<>();
        userMenu.setUser("admin");

        userMenu.addTextItem("i1", "Item #1");
        userMenu.addTextItem("i2", "Item #2");
        userMenu.addTextItem("i3", "Item #3");

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
}

