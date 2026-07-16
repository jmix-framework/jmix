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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.badge.Badge;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.IconLoaderSupport;

public class BadgeLoader extends AbstractComponentLoader<Badge> {

    protected IconLoaderSupport iconLoaderSupport;

    @Override
    protected Badge createComponent() {
        return factory.create(Badge.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadText(resultComponent, element);
        componentLoader().loadWhiteSpace(resultComponent, element);

        loadInteger(element, "number", resultComponent::setNumber);
        loadString(element, "role", resultComponent::setRole);

        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        iconLoaderSupport().loadIcon(element, resultComponent::setIcon);
    }

    protected IconLoaderSupport iconLoaderSupport() {
        if (iconLoaderSupport == null) {
            iconLoaderSupport = applicationContext.getBean(IconLoaderSupport.class, context);
        }
        return iconLoaderSupport;
    }
}
