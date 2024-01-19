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

package io.jmix.flowui.xml.layout.loader.html;

import com.vaadin.flow.component.html.NativeDetails;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.List;

public class NativeDetailsLoader extends AbstractHtmlComponentLoader<NativeDetails> {

    protected ComponentLoader<?> pendingLoadContent;

    @Override
    protected NativeDetails createComponent() {
        return factory.create(NativeDetails.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createContent(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadBoolean(element, "open", resultComponent::setOpen);
        loadResourceString(element, "summaryText", context.getMessageGroup(), resultComponent::setSummaryText);

        componentLoader().loadClickNotifierAttributes(resultComponent, element);

        loadContent();
    }

    protected void createContent(NativeDetails resultComponent, Element element) {
        LayoutLoader loader = getLayoutLoader();
        List<Element> elements = element.elements();

        if (elements.size() != 1) {
            throw new GuiDevelopmentException(
                    String.format("%s must contain only one children", NativeDetails.class.getSimpleName()),
                    context, "Component ID", resultComponent.getId()
            );
        }

        ComponentLoader<?> contentLoader = loader.createComponentLoader(elements.get(0));
        contentLoader.initComponent();
        pendingLoadContent = contentLoader;

        resultComponent.setContent(contentLoader.getResultComponent());
    }

    protected void loadContent() {
        pendingLoadContent.loadComponent();
        pendingLoadContent = null;
    }
}
