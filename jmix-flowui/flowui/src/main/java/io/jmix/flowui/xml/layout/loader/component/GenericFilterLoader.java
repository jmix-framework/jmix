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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.SupportsResponsiveSteps;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class GenericFilterLoader extends AbstractComponentLoader<GenericFilter> {

    @Override
    protected GenericFilter createComponent() {
        return factory.create(GenericFilter.class);
    }

    @Override
    public void loadComponent() {
        loadResourceString(element, "summaryText", context.getMessageGroup(), resultComponent::setSummaryText);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);

        getLoaderSupport().loadBoolean(element, "opened", resultComponent::setOpened);
        getLoaderSupport().loadBoolean(element, "autoApply", resultComponent::setAutoApply);


        loadDataLoader(resultComponent, element);
        loadResponsiveSteps(resultComponent, element);
    }

    protected void loadDataLoader(GenericFilter component, Element element) {
        loadString(element, "dataLoader",
                (dataLoaderId) -> {
                    ViewData screenData = ViewControllerUtils.getViewData(getComponentContext().getView());
                    DataLoader dataLoader = screenData.getLoader(dataLoaderId);
                    component.setDataLoader(dataLoader);
                });
    }

    protected void loadResponsiveSteps(SupportsResponsiveSteps resultComponent, Element element) {
        Element responsiveSteps = element.element("responsiveSteps");
        if (responsiveSteps == null) {
            return;
        }

        List<Element> responsiveStepList = responsiveSteps.elements("responsiveStep");
        if (responsiveStepList.isEmpty()) {
            throw new GuiDevelopmentException(responsiveSteps.getName() + "can't be empty", context);
        }

        List<SupportsResponsiveSteps.ResponsiveStep> pendingSetResponsiveSteps = new ArrayList<>();
        for (Element subElement : responsiveStepList) {
            pendingSetResponsiveSteps.add(loadResponsiveStep(subElement));
        }

        resultComponent.setResponsiveSteps(pendingSetResponsiveSteps);
    }

    protected SupportsResponsiveSteps.ResponsiveStep loadResponsiveStep(Element element) {
        String minWidth = loadString(element, "minWidth")
                .orElseThrow(() -> new GuiDevelopmentException("'minWidth' can't be empty", context));
        Integer columns = loadInteger(element, "columns")
                .orElse(1);
        SupportsResponsiveSteps.ResponsiveStep.LabelsPosition labelsPosition = loadEnum(element, SupportsResponsiveSteps.ResponsiveStep.LabelsPosition.class, "labelsPosition")
                .orElse(null);

        return new SupportsResponsiveSteps.ResponsiveStep(minWidth, columns, labelsPosition);
    }
}
