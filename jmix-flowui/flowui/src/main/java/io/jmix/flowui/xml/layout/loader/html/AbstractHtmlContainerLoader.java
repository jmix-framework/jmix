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

import com.google.common.base.Strings;
import com.vaadin.flow.component.HtmlContainer;
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding;
import io.jmix.flowui.xml.layout.loader.container.AbstractContainerLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

public abstract class AbstractHtmlContainerLoader<T extends HtmlContainer> extends AbstractContainerLoader<T> {

    protected DataLoaderSupport dataLoaderSupport;
    protected HtmlContainerReadonlyDataBinding htmlComponentReadonlyDataBinding;

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
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadThemeList(resultComponent, element);

        loadData(resultComponent, element);
    }

    protected void loadData(T resultComponent, Element element) {
        String property = element.attributeValue("property");
        if (!Strings.isNullOrEmpty(property)) {
            getDataLoaderSupport().loadContainer(element, property)
                    .ifPresent(container -> getHtmlDataBinding().bind(resultComponent, container, property));
        }
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    protected HtmlContainerReadonlyDataBinding getHtmlDataBinding() {
        if (htmlComponentReadonlyDataBinding == null) {
            htmlComponentReadonlyDataBinding = applicationContext.getBean(HtmlContainerReadonlyDataBinding.class);
        }
        return htmlComponentReadonlyDataBinding;
    }
}
