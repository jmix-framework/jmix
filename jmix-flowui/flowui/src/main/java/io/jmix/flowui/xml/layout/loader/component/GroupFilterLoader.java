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

import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

public class GroupFilterLoader extends AbstractComponentLoader<GroupFilter> {

    @Override
    protected GroupFilter createComponent() {
        return factory.create(GroupFilter.class);
    }

    @Override
    public void loadComponent() {
        loadResourceString(element, "summaryText", context.getMessageGroup(), resultComponent::setSummaryText);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadResponsiveSteps(resultComponent, element);

        getLoaderSupport().loadBoolean(element, "autoApply", resultComponent::setAutoApply);

        loadEnum(element, LogicalFilterComponent.Operation.class, "operation", resultComponent::setOperation);

        loadDataLoader(resultComponent, element);

        loadFilterComponents(resultComponent, element);
    }

    protected void loadDataLoader(GroupFilter resultComponent, Element element) {
        loadString(element, "dataLoader")
                .ifPresent(dataLoaderId -> {
                    DataLoader dataLoader = getComponentContext().getViewData().getLoader(dataLoaderId);
                    getComponentContext().addInitTask((context, view) ->
                            resultComponent.setDataLoader(dataLoader)
                    );
                });
    }

    protected void loadFilterComponents(GroupFilter resultComponent, Element element) {
        for (Element filterElement : element.elements()) {
            if (!isFilterElement(filterElement)) {
                continue;
            }

            FilterComponent filterComponent = loadFilterComponent(resultComponent, filterElement);
            resultComponent.add(filterComponent);
        }
    }

    protected FilterComponent loadFilterComponent(GroupFilter resultComponent, Element filterElement) {
        //noinspection DuplicatedCode
        ComponentLoader<?> filterComponentLoader = getLayoutLoader().createComponentLoader(filterElement);
        filterComponentLoader.initComponent();

        FilterComponent filterResultComponent = (FilterComponent) filterComponentLoader.getResultComponent();

        filterResultComponent.setConditionModificationDelegated(true);
        filterResultComponent.setDataLoader(resultComponent.getDataLoader());

        filterComponentLoader.loadComponent();

        return filterResultComponent;
    }

    protected boolean isFilterElement(Element element) {
        return !"responsiveSteps".equals(element.getName());
    }
}
