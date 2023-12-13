/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.icon.FontIcon;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;

public class FontIconLoader extends AbstractIconLoader<FontIcon> {

    @Override
    protected FontIcon createComponent() {
        return factory.create(FontIcon.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadString(element, "fontFamily", resultComponent::setFontFamily);
        loadString(element, "charCode", resultComponent::setCharCode);
        loadString(element, "ligature", resultComponent::setLigature);

        loadIconClassNames(resultComponent, element);
    }

    protected void loadIconClassNames(FontIcon component, Element element) {
        loadString(element, "iconClassNames")
                .map(this::split)
                .map(list -> list.toArray(String[]::new))
                .ifPresent(component::setIconClassNames);
    }

    protected List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(split -> !Strings.isNullOrEmpty(split))
                .toList();
    }
}
