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

package io.jmix.supersetflowui.component.loader;

import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.supersetflowui.SupersetGuestTokenProvider;
import io.jmix.supersetflowui.component.SupersetDashboard;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstrainsProvider;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Element;

import java.util.List;

public class SupersetDashboardLoader extends AbstractComponentLoader<SupersetDashboard> {

    @Override
    protected SupersetDashboard createComponent() {
        return factory.create(SupersetDashboard.class);
    }

    @Override
    public void loadComponent() {
        loadId(resultComponent, element);

        componentLoaderSupport.loadSizeAttributes(resultComponent, element);
        componentLoaderSupport.loadClassNames(resultComponent, element);

        loadString(element, "embeddedId", resultComponent::setEmbeddedId);
        loadString(element, "url", resultComponent::setUrl);
        loadBoolean(element, "titleVisible", resultComponent::setTitleVisible);
        loadBoolean(element, "chartControlsVisible", resultComponent::setChartControlsVisible);
        loadBoolean(element, "filtersExpanded", resultComponent::setFiltersExpanded);

        loadDatasetConstraints();
        loadGuestTokenProvider();
    }

    protected void loadDatasetConstraints() {
        Element datasetConstraintsElement = element.element("datasetConstraints");
        if (datasetConstraintsElement != null) {
            List<DatasetConstraint> datasetConstraints = loadDatasetConstraintsList(datasetConstraintsElement);
            if (CollectionUtils.isNotEmpty(datasetConstraints)) {
                resultComponent.setDatasetConstrainsProvider(() -> datasetConstraints);
            }
        } else {
            loadString(element, "datasetConstraintsProviderBean")
                    .ifPresent(beanName ->
                            resultComponent.setDatasetConstrainsProvider(
                                    (DatasetConstrainsProvider) applicationContext.getBean(beanName)));
        }
    }

    protected void loadGuestTokenProvider() {
        loadString(element, "guestTokenProviderBean", beanName -> {
            SupersetGuestTokenProvider bean = applicationContext.getBean(beanName, SupersetGuestTokenProvider.class);
            resultComponent.setGuestTokenProvider(bean);
        });
    }

    protected List<DatasetConstraint> loadDatasetConstraintsList(Element datasetConstraintsElement) {
        List<Element> datasetConstraint = datasetConstraintsElement.elements("datasetConstraint");
        return datasetConstraint.stream()
                .map(element ->
                        new DatasetConstraint(
                                Integer.parseInt(element.attributeValue("datasetId")),
                                element.getText().trim()))
                .toList();
    }
}
