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

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class PropertyFilterLoader extends AbstractComponentLoader<PropertyFilter> {

    public static final String OPERATION_BASE_MESSAGE_KEY = "io.jmix.core.querycondition/propertyfilter.";

    private UiProperties uiProperties;

    @Override
    public void createComponent() {
        resultComponent = factory.create(PropertyFilter.NAME);
        loadId(resultComponent, element);
        uiProperties = applicationContext.getBean(UiProperties.class);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);
        loadWidth(resultComponent, element);
        loadProperty();
        loadParameterName();
        loadOperation();

        loadCaptionPosition();
        loadCaptionWidth();
        loadOperationCaptionVisible();

        if (Strings.isNullOrEmpty(resultComponent.getId()) &&
                Strings.isNullOrEmpty(resultComponent.getParameterName())) {
            throw new DevelopmentException("Either id or parameterName should be defined for propertyFilter");
        }

        loadDataLoader();
        loadCaption();
        loadValueComponent();
        loadAutoApply();
    }

    @Override
    protected void loadId(Component component, Element element) {
        super.loadId(component, element);
    }

    protected void loadDataLoader() {
        String dataLoaderId = element.attributeValue("dataLoader");
        if (StringUtils.isNotBlank(dataLoaderId)) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            DataLoader dataLoader = screenData.getLoader(dataLoaderId);

            Condition rootCondition = dataLoader.getCondition();
            if (rootCondition == null) {
                rootCondition = new LogicalCondition(LogicalCondition.Type.AND);
                dataLoader.setCondition(rootCondition);
            }

            if (rootCondition instanceof LogicalCondition) {
                ((LogicalCondition) rootCondition).add(resultComponent.getPropertyCondition());
            }

            resultComponent.setDataLoader(dataLoader);
        }

        getComponentContext().addPostInitTask((context1, window) -> {
            resultComponent.createLayout();
        });
    }

    protected void loadProperty() {
        String property = element.attributeValue("property");
        resultComponent.setProperty(property);
    }

    protected void loadParameterName() {
        String parameterName = element.attributeValue("parameterName");
        if (Strings.isNullOrEmpty(parameterName)) {
            parameterName = PropertyConditionUtils.generateParameterName(resultComponent.getPropertyCondition());
        }
        resultComponent.setParameterName(parameterName);
    }

    protected void loadOperation() {
        String operation = element.attributeValue("operation");
        resultComponent.setOperation(operation);
    }

    protected void loadOperationCaptionVisible() {
        String operationCaptionVisible = element.attributeValue("operationCaptionVisible");
        if (!Strings.isNullOrEmpty(operationCaptionVisible)) {
            resultComponent.setOperationCaptionVisible(Boolean.parseBoolean(operationCaptionVisible));
        }
    }

    private void loadCaptionPosition() {
        String captionPosition = element.attributeValue("captionPosition");
        if (!Strings.isNullOrEmpty(captionPosition)) {
            resultComponent.setCaptionPosition(PropertyFilter.CaptionPosition.valueOf(captionPosition));
        }
    }

    private void loadCaptionWidth() {
        String captionWidth = element.attributeValue("captionWidth");
        resultComponent.setCaptionWidth(captionWidth);
    }

    private void loadCaption() {
        loadCaption(resultComponent, element);
        if (resultComponent.getCaption() == null) {
            String caption = getDefaultCaption();
            resultComponent.setCaption(caption);
        }
    }

    private void loadAutoApply() {
        String autoApply = element.attributeValue("autoApply");
        if (!Strings.isNullOrEmpty(autoApply)) {
            resultComponent.setAutoApply("true".equals(autoApply));
        } else {
            resultComponent.setAutoApply(uiProperties.isPropertyFilterAutoApply());
        }
    }

    /**
     * Default caption consist of the related entity property caption and the operation caption (if the operation
     * caption is configured to be visible), e.g. "Last name contains".
     */
    public String getDefaultCaption() {
        MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();
        MetaPropertyPath mpp = metaClass.getPropertyPath(resultComponent.getProperty());
        if (mpp == null) {
            return resultComponent.getProperty();
        } else {
            MetaProperty[] metaProperties = mpp.getMetaProperties();
            StringBuilder sb = new StringBuilder();

            MetaPropertyPath parentMpp = null;
            MetaClass tempMetaClass;

            for (int i = 0; i < metaProperties.length; i++) {
                if (i == 0) {
                    parentMpp = new MetaPropertyPath(metaClass, metaProperties[i]);
                    tempMetaClass = metaClass;
                } else {
                    parentMpp = new MetaPropertyPath(parentMpp, metaProperties[i]);
                    tempMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(parentMpp);
                }

                sb.append(getMessageTools().getPropertyCaption(tempMetaClass, metaProperties[i].getName()));
                if (i < metaProperties.length - 1) {
                    sb.append(".");
                }
            }
            if (resultComponent.isOperationCaptionVisible()) {
                sb.append(" ").append(getOperationCaption(resultComponent.getOperation()));
            }
            return sb.toString();
        }
    }

    public MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }

    private String getOperationCaption(String operation) {
        return getMessages().getMessage(OPERATION_BASE_MESSAGE_KEY + operation);
    }

    private void loadValueComponent() {
        if (!element.elements().isEmpty()) {
            Element valueComponentElement = this.element.elements().get(0);
            ComponentLoader valueComponentLoader = getLayoutLoader().createComponent(valueComponentElement);
            valueComponentLoader.loadComponent();
            Component valueComponent = valueComponentLoader.getResultComponent();
            if (!(valueComponent instanceof HasValue)) {
                throw new GuiDevelopmentException("Value component of the PropertyFilter must implement HasValue",
                        getComponentContext().getCurrentFrameId());
            }
            resultComponent.setValueComponent((HasValue) valueComponent);
        }
    }

}
