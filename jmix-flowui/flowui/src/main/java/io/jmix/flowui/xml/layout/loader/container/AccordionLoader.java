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

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import io.jmix.flowui.component.accordion.JmixAccordion;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

public class AccordionLoader extends AbstractContainerLoader<JmixAccordion> {

    @Override
    protected JmixAccordion createComponent() {
        return factory.create(JmixAccordion.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createContent(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadSubComponents();
    }

    protected void createContent(Accordion resultComponent, Element element) {
        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : element.elements("accordionPanel")) {
            ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
            componentLoader.initComponent();
            pendingLoadComponents.add(componentLoader);

            resultComponent.add((AccordionPanel) componentLoader.getResultComponent());
        }
    }
}
