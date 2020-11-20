/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.PropertyFilter.Operation;
import io.jmix.ui.component.SupportsCaptionPosition.CaptionPosition;
import io.jmix.ui.component.UiComponentsGenerator;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import static io.jmix.core.querycondition.PropertyConditionUtils.generateParameterName;

public class PropertyFilterLoader extends AbstractComponentLoader<PropertyFilter> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(PropertyFilter.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadVisible(resultComponent, element);
        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);
        loadCss(resultComponent, element);

        loadString(element, "property", resultComponent::setProperty);
        loadEnum(element, Operation.class, "operation", resultComponent::setOperation);
        loadBoolean(element, "operationEditable", resultComponent::setOperationEditable);

        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(generateParameterName(resultComponent.getProperty())));

        loadEnum(element, CaptionPosition.class, "captionPosition", resultComponent::setCaptionPosition);
        loadString(element, "captionWidth", resultComponent::setCaptionWidth);

        loadDataLoader(resultComponent, element);
        loadValueComponent(resultComponent, element);

        loadCaption(resultComponent, element);

        resultComponent.setAutoApply(loadBoolean(element, "autoApply")
                .orElse(getUiProperties().isPropertyFilterAutoApply()));
    }

    protected void loadDataLoader(PropertyFilter<?> resultComponent, Element element) {
        String dataLoaderId = element.attributeValue("dataLoader");
        if (StringUtils.isNotBlank(dataLoaderId)) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            DataLoader dataLoader = screenData.getLoader(dataLoaderId);

            resultComponent.setDataLoader(dataLoader);
        }
    }

    @Override
    protected void loadCaption(Component.HasCaption component, Element element) {
        super.loadCaption(component, element);

        if (component.getCaption() == null) {
            String caption = getDefaultCaption();
            component.setCaption(caption);
        }
    }

    protected String getDefaultCaption() {
        MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();
        return getPropertyFilterSupport().getPropertyFilterCaption(
                metaClass, resultComponent.getProperty(), resultComponent.getOperation(),
                isOperationCaptionVisible() && !resultComponent.isOperationEditable());
    }

    protected boolean isOperationCaptionVisible() {
        return loadBoolean(element, "operationCaptionVisible").orElse(true);
    }

    protected void loadValueComponent(PropertyFilter<?> resultComponent, Element element) {
        Component valueComponent;

        if (!element.elements().isEmpty()) {
            Element valueComponentElement = element.elements().get(0);

            ComponentLoader<?> valueComponentLoader = getLayoutLoader().createComponent(valueComponentElement);
            valueComponentLoader.loadComponent();
            valueComponent = valueComponentLoader.getResultComponent();
        } else {
            MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();
            valueComponent = getPropertyFilterSupport()
                    .generateValueField(metaClass, resultComponent.getProperty(), resultComponent.getOperation());
        }

        if (!(valueComponent instanceof HasValue)) {
            throw new GuiDevelopmentException("Value component of the PropertyFilter must implement HasValue",
                    getComponentContext().getCurrentFrameId());
        }

        resultComponent.setValueComponent((HasValue) valueComponent);
    }

    protected UiComponentsGenerator getUiComponentsGenerator() {
        return (UiComponentsGenerator) applicationContext.getBean(UiComponentsGenerator.NAME);
    }

    protected PropertyFilterSupport getPropertyFilterSupport() {
        return applicationContext.getBean(PropertyFilterSupport.class);
    }

    protected UiProperties getUiProperties() {
        return applicationContext.getBean(UiProperties.class);
    }
}
