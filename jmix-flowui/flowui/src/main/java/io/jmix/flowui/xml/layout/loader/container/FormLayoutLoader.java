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
import com.vaadin.flow.component.HasSize;
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
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormLayoutLoader extends AbstractComponentLoader<FormLayout> {

    protected MetadataTools metaDataTools;
    protected MessageTools messageTools;

    @Override
    protected FormLayout createComponent() {
        return factory.create(FormLayout.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

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
            }
            setLabel(child, null);
            setWidthFull(child);

            FormLayout.FormItem formItem = resultComponent.addFormItem(child, label);
            formItem.setVisible(child.isVisible());
            formItem.setEnabled(child.getElement().isEnabled());
            colspan.ifPresent(it -> resultComponent.setColspan(formItem, it));
        }
    }

    @Nullable
    protected String getLabel(Component component) {
        return component instanceof HasLabel hasLabelComponent
                ? hasLabelComponent.getLabel()
                : null;
    }

    protected void setLabel(Component component, @Nullable String label) {
        if (component instanceof HasLabel hasLabelComponent) {
            hasLabelComponent.setLabel(label);
        }
    }

    protected void setWidthFull(Component component) {
        if (component instanceof HasSize hasSizeComponent) {
            hasSizeComponent.setWidthFull();
        }
    }

    protected void loadResponsiveSteps(FormLayout resultComponent, Element element) {
        Element responsiveSteps = element.element("responsiveSteps");
        if (responsiveSteps == null) {
            resultComponent.setResponsiveSteps(getDefaultResponsiveSteps(element));
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

    protected List<ResponsiveStep> getDefaultResponsiveSteps(Element element) {
        LabelsPosition labelsPosition = loadEnum(element, LabelsPosition.class, "labelsPosition")
                .orElse(LabelsPosition.TOP);

        return List.of(
                new ResponsiveStep("0", 1, labelsPosition),
                new ResponsiveStep("40em", 2, labelsPosition)
        );
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
        if (!(component instanceof SupportsValueSource<?> supportsValueSourceComponent)
                || !(supportsValueSourceComponent.getValueSource() instanceof EntityValueSource<?, ?> entityValueSource)) {
            return null;
        }

        MetaPropertyPath mpp = entityValueSource.getMetaPropertyPath();
        MetaClass propertyMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(mpp);
        String propertyName = mpp.getMetaProperty().getName();

        return getMessageTools().getPropertyCaption(propertyMetaClass, propertyName);
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
