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

import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.flowui.component.virtuallist.ComponentTemplateValueProvider;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

import java.util.List;

public class VirtualListLoader extends AbstractComponentLoader<JmixVirtualList<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixVirtualList<?> createComponent() {
        return factory.create(JmixVirtualList.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadItems(resultComponent, element);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        loadRenderer();
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    protected void loadRenderer() {
        Element componentRenderer = element.element("componentRenderer");
        if (componentRenderer != null) {
            List<Element> rootElements = componentRenderer.elements();
            if (rootElements.size() != 1) {
                throw new GuiDevelopmentException("Component renderer for virtual list must have single root component",
                        context);
            }
            ComponentTemplateValueProvider<?> valueProvider = getComponentTemplateValueProvider(rootElements.get(0));
            //noinspection rawtypes,unchecked
            resultComponent.setRenderer(new ComponentRenderer(valueProvider));
        }
    }

    protected ComponentTemplateValueProvider<?> getComponentTemplateValueProvider(Element rootElement) {
        return applicationContext.getBean(ComponentTemplateValueProvider.class, rootElement, context);
    }
}
