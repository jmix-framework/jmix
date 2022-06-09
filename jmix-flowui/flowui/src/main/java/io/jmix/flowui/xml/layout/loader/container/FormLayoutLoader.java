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
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.SupportsValueSource;
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
    protected MessageTools messageTools;

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
            if (isChildElementIgnored(subElement)) {
                continue;
            }

            ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
            componentLoader.initComponent();
            componentLoader.loadComponent();
            Component child = componentLoader.getResultComponent();

            Optional<Integer> colspan = loadInteger(subElement, "colspan");

            String label = getLabel(child);
            if (label == null) {
                label = generatePropertyLabel(child);
                setLabel(child, label);
            }

            if (labelsPosition == LabelsPosition.ASIDE) {
                FormLayout.FormItem formItem = resultComponent.addFormItem(child, label);
                setLabel(child, null);
                formItem.setVisible(child.isVisible());
                colspan.ifPresent(it -> resultComponent.setColspan(formItem, it));
            } else {
                resultComponent.add(child);
                colspan.ifPresent(it -> resultComponent.setColspan(child, it));
            }
        }
    }

    @Nullable
    protected String getLabel(Component component) {
        return component instanceof HasLabel ?
                ((HasLabel) component).getLabel()
                : null;
    }

    protected void setLabel(Component component, @Nullable String label) {
        if (component instanceof HasLabel) {
            ((HasLabel) component).setLabel(label);
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
    protected String generatePropertyLabel(Component component) {
        if (!(component instanceof SupportsValueSource)
                || !(((SupportsValueSource<?>) component).getValueSource() instanceof EntityValueSource)) {
            return null;
        }

        MetaPropertyPath mpp =
                ((EntityValueSource<?, ?>) ((SupportsValueSource<?>) component).getValueSource()).getMetaPropertyPath();
        MetaClass propertyMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(mpp);
        String propertyName = mpp.getMetaProperty().getName();

        return getMessageTools().getPropertyCaption(propertyMetaClass, propertyName);
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

    protected MetadataTools getMetadataTools() {
        if (metaDataTools == null) {
            metaDataTools = applicationContext.getBean(MetadataTools.class);
        }
        return metaDataTools;
    }

    protected MessageTools getMessageTools() {
        if (messageTools == null) {
            messageTools = applicationContext.getBean(MessageTools.class);
        }

        return messageTools;
    }
}
