/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.html;

import com.vaadin.flow.component.HtmlContainer;
import io.jmix.flowui.xml.layout.loader.container.AbstractContainerLoader;

public abstract class AbstractHtmlContainerLoader<T extends HtmlContainer> extends AbstractContainerLoader<T> {

    @Override
    public void initComponent() {
        super.initComponent();
        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        if (resultComponent.getChildren().findAny().isEmpty()) {
            loadResourceString(element, "text", context.getMessageGroup(), resultComponent::setText);
        } else {
            loadSubComponents();
        }
        loadResourceString(element, "title", context.getMessageGroup(), resultComponent::setTitle);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadWhiteSpace(resultComponent, element);
        componentLoader().loadBadge(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }
}
