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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormLayoutLoader extends AbstractComponentLoader<FormLayout> {

    protected MetadataTools metaDataTools;
    protected LabelsPosition labelsPosition = LabelsPosition.TOP;

    @Override
    protected FormLayout createComponent() {
        return factory.create(FormLayout.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        loadLabelPosition(resultComponent, element);

        loadSubComponents();
    }

    protected void loadSubComponents() {
        loadResponsiveSteps(resultComponent, element);

        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : element.elements()) {
            if (!isChildElementIgnored(subElement)) {
                ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
                componentLoader.initComponent();
                componentLoader.loadComponent();
                Component child = componentLoader.getResultComponent();

                String label = loadResourceString(subElement, "label", context.getMessageGroup())
                        .orElse(loadPropertyLabel(subElement));
                Optional<Integer> colspan = loadInteger(subElement, "colspan");

                //TODO: kremnevda, two labels 08.06.2022
                if (labelsPosition == LabelsPosition.ASIDE) {
                    FormLayout.FormItem formItem = resultComponent.addFormItem(child, label);
                    formItem.setVisible(child.isVisible());
                    colspan.ifPresent(it -> resultComponent.setColspan(formItem, it));
                } else {
                    //TODO: kremnevda, check no hasLabeled components 08.06.2022
                    // see UIComponentUtils -> setLabel(Component, label) [hasLabel or reflection]
                    if (child instanceof HasLabel) {
                        ((HasLabel) child).setLabel(label);
                    }
                    resultComponent.add(child);
                    colspan.ifPresent(it -> resultComponent.setColspan(child, it));
                }
            }
        }
    }

    protected void loadResponsiveSteps(FormLayout resultComponent, Element element) {
        Element responsiveSteps = element.element("responsiveSteps");
        if (responsiveSteps == null) {
            return;
        }
        List<Element> responsiveStepList = responsiveSteps.elements("responsiveStep");

        if (responsiveStepList.isEmpty()) {
            throw new GuiDevelopmentException(responsiveSteps.getName() + "can't be empty", context);
        }

        List<ResponsiveStep> pendingSetResponsiveSteps = new ArrayList<>();
        for (Element subElement : responsiveStepList) {
            pendingSetResponsiveSteps.add(loadResponsiveStep(subElement));
        }

        resultComponent.setResponsiveSteps(pendingSetResponsiveSteps);
    }

    protected ResponsiveStep loadResponsiveStep(Element element) {
        String minWidth = loadString(element, "minWidth")
                .orElse(null);
        Integer columns = loadInteger(element, "columns")
                .orElse(1);
        LabelsPosition labelsPosition = loadEnum(element, LabelsPosition.class, "labelsPosition")
                .orElse(null);

        return new ResponsiveStep(minWidth, columns, labelsPosition);
    }

    @Nullable
    protected String loadPropertyLabel(Element element) {
        String property = loadString(element, "property").orElse(null);
        String dataContainer = loadString(this.element, "dataContainer").orElse(null);

        if (property == null || dataContainer == null) {
            return null;
        }

        MetaClass entityMetaClass = getComponentContext().getScreenData().getContainer(dataContainer).getEntityMetaClass();

        MetaPropertyPath metaPropertyPath = getMetaDataTools().resolveMetaPropertyPathOrNull(entityMetaClass, property);
        assert metaPropertyPath != null;
        return metaPropertyPath.getMetaProperty().getName();
    }

    protected void loadLabelPosition(FormLayout resultComponent, Element element) {
        loadEnum(element, LabelsPosition.class, "labelsPosition")
                .ifPresent(this::setLabelsPosition);
    }

    protected void setLabelsPosition(LabelsPosition labelsPosition) {
        this.labelsPosition = labelsPosition;
    }

    protected boolean isChildElementIgnored(Element subElement) {
        return "responsiveSteps".equalsIgnoreCase(subElement.getName());
    }

    protected MetadataTools getMetaDataTools() {
        if (metaDataTools == null) {
            metaDataTools = applicationContext.getBean(MetadataTools.class, context);
        }
        return metaDataTools;
    }
}
