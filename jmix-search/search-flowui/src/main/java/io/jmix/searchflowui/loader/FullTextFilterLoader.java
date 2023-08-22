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

package io.jmix.searchflowui.loader;

import com.vaadin.flow.component.Component;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.component.AbstractSingleFilterComponentLoader;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchflowui.component.FullTextFilter;
import io.jmix.searchflowui.utils.FullTextFilterUtils;
import org.dom4j.Element;

import java.util.List;

public class FullTextFilterLoader extends AbstractSingleFilterComponentLoader<FullTextFilter> {

    @Override
    protected FullTextFilter createComponent() {
        return resultComponent = factory.create(FullTextFilter.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadSearchStrategy(resultComponent, element);
        loadString(element, "defaultValue", resultComponent::setValue);
    }

    protected void loadSearchStrategy(FullTextFilter resultComponent, Element element) {
        loadString(element, "searchStrategy")
                .ifPresent(strategyName -> {
                    SearchStrategy strategy = applicationContext.getBean(SearchStrategyManager.class).getSearchStrategyByName(strategyName);
                    resultComponent.setSearchStrategy(strategy);
                });
    }

    @Override
    protected Component generateValueComponent() {
        return applicationContext.getBean(UiComponents.class).create(TypedTextField.class);
    }

    @Override
    protected Element getValueComponentElement(List<Element> elements) {
        if (elements.size() > 2) {
            throw new GuiDevelopmentException("Only one value component can be defined", context);
        }

        return elements.stream()
                .filter(this::isValueComponent)
                .findAny()
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("Unknown value component for %s", resultComponent.getClass().getSimpleName()),
                        context)
                );
    }

    @Override
    protected boolean isValueComponent(Element subElement) {
        return !"tooltip".equals(subElement.getName());
    }

    protected PropertyFilterSupport getPropertyFilterSupport() {
        return applicationContext.getBean(PropertyFilterSupport.class);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }

    @Override
    protected void loadAttributesBeforeValueComponent() {
        super.loadAttributesBeforeValueComponent();
        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(FullTextFilterUtils.generateParameterName()));
    }
}
