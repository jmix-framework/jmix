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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.details.Details;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

public abstract class AbstractDetailsLoader<C extends Details> extends AbstractContainerLoader<C> {

    @Override
    public void initComponent() {
        super.initComponent();

        createContent(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadResourceString(element, "summaryText", context.getMessageGroup(), resultComponent::setSummaryText);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        getLoaderSupport().loadBoolean(element, "opened", resultComponent::setOpened);

        loadSubComponents();
    }

    protected void createContent(Details resultComponent, Element element) {
        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : element.elements()) {
            if (!isChildElementIgnored(subElement)) {
                ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
                componentLoader.initComponent();
                pendingLoadComponents.add(componentLoader);

                resultComponent.addContent(componentLoader.getResultComponent());
            }
        }
    }
}
