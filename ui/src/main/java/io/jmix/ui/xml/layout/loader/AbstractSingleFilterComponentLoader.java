/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.SingleFilterComponent;
import io.jmix.ui.component.SupportsCaptionPosition;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.List;

@SuppressWarnings("rawtypes")
public abstract class AbstractSingleFilterComponentLoader<C extends SingleFilterComponent>
        extends AbstractComponentLoader<C> {

    @Override
    public void loadComponent() {
        loadAttributesBeforeValueComponent();

        loadDataLoader(resultComponent, element);
        loadBoolean(element, "autoApply", resultComponent::setAutoApply);
        loadValueComponent(resultComponent, element);

        loadBoolean(element, "captionVisible", resultComponent::setCaptionVisible);
        loadCaption(resultComponent, element);
        loadEnum(element, SupportsCaptionPosition.CaptionPosition.class, "captionPosition",
                resultComponent::setCaptionPosition);
        loadString(element, "captionWidth", resultComponent::setCaptionWidth);

        loadContextHelp(resultComponent, element);
        loadRequired(resultComponent, element);
        loadEditable(resultComponent, element);
        loadTabIndex(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);
    }

    protected void loadAttributesBeforeValueComponent() {
        assignFrame(resultComponent);
        assignXmlDescriptor(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadHtmlSanitizerEnabled(resultComponent, element);

        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);
        loadCss(resultComponent, element);
        loadAlign(resultComponent, element);
        loadResponsive(resultComponent, element);
    }

    protected void loadDataLoader(C resultComponent, Element element) {
        String dataLoaderId = element.attributeValue("dataLoader");
        if (StringUtils.isNotBlank(dataLoaderId)) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            DataLoader dataLoader = screenData.getLoader(dataLoaderId);

            resultComponent.setDataLoader(dataLoader);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadValueComponent(C resultComponent, Element element) {
        Component valueComponent;

        if (!element.elements().isEmpty()) {
            valueComponent = createValueComponent(element.elements());
        } else {
            valueComponent = generateValueComponent();
        }

        if (!(valueComponent instanceof HasValue)) {
            throw new GuiDevelopmentException("Value component of the PropertyFilter must implement HasValue",
                    getComponentContext().getCurrentFrameId());
        }

        resultComponent.setValueComponent((HasValue) valueComponent);
    }

    protected Component createValueComponent(List<Element> elements) {
        Element valueComponentElement = elements.get(0);
        ComponentLoader<?> valueComponentLoader = getLayoutLoader().createComponent(valueComponentElement);
        valueComponentLoader.loadComponent();
        return valueComponentLoader.getResultComponent();
    }

    protected abstract HasValue<?> generateValueComponent();

    protected SingleFilterSupport getSingleFilterSupport() {
        return applicationContext.getBean(SingleFilterSupport.class);
    }
}
