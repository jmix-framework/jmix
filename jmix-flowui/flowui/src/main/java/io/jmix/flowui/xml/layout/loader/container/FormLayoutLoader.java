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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.shared.SlotUtils;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.value.ContainerValueSourceProvider;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FormLayoutLoader extends AbstractComponentLoader<FormLayout> {

    protected MetadataTools metaDataTools;
    protected MessageTools messageTools;

    protected LabelsPosition defaultLabelPosition = LabelsPosition.TOP;

    @Override
    protected JmixFormLayout createComponent() {
        return factory.create(JmixFormLayout.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClickNotifierAttributes(resultComponent, element);

        loadData(resultComponent, element);
        loadLabelPosition(element);
        loadSubComponents();
    }

    protected void loadData(FormLayout resultComponent, Element element) {
        String containerId = element.attributeValue("dataContainer");
        if (!Strings.isNullOrEmpty(containerId)) {

            InstanceContainer<?> container = getComponentContext().getViewData().getContainer(containerId);
            //noinspection unchecked
            ((JmixFormLayout)resultComponent).setValueSourceProvider(new ContainerValueSourceProvider(container));
        }
    }

    protected void loadLabelPosition(Element element) {
        loadEnum(element, LabelsPosition.class, "labelsPosition")
                .ifPresent(labelsPosition -> defaultLabelPosition = labelsPosition);
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

            String label = getLabel(child);
            if (label == null) {
                label = generatePropertyLabel(child);
                setLabel(child, label);
            }
            setWidthFull(child);

            resultComponent.add(child);
            loadInteger(subElement, "colspan")
                    .ifPresent(it -> resultComponent.setColspan(child, it));
        }
    }

    @Nullable
    protected String getLabel(Component component) {
        if (component instanceof FormLayout.FormItem formItemComponent) {
            NativeLabel label = (NativeLabel) SlotUtils.getChildInSlot(formItemComponent, "label");
            return label == null ? null : label.getText();
        } else if (component instanceof HasLabel hasLabelComponent) {
            return hasLabelComponent.getLabel();
        }

        return null;
    }

    protected void setLabel(Component component, @Nullable String label) {
        if (component instanceof FormLayout.FormItem formItemComponent) {
            SlotUtils.addToSlot(formItemComponent, "label", new NativeLabel(label));
        } else if (component instanceof HasLabel hasLabelComponent) {
            hasLabelComponent.setLabel(label);
        }
    }

    protected void setWidthFull(Component component) {
        if (component instanceof FormLayout.FormItem formItemComponent) {
            formItemComponent.getChildren()
                    .findAny()
                    .ifPresent(this::setWidthFull);
        } else if (component instanceof HasSize hasSizeComponent) {
            hasSizeComponent.setWidthFull();
        }
    }

    protected void loadResponsiveSteps(FormLayout resultComponent, Element element) {
        Element responsiveSteps = element.element("responsiveSteps");
        if (responsiveSteps == null) {
            resultComponent.setResponsiveSteps(getDefaultResponsiveSteps());
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

    protected List<ResponsiveStep> getDefaultResponsiveSteps() {
        return List.of(
                new ResponsiveStep("0", 1, defaultLabelPosition),
                new ResponsiveStep("40em", 2, defaultLabelPosition)
        );
    }

    protected ResponsiveStep loadResponsiveStep(Element element) {
        String minWidth = loadString(element, "minWidth")
                .orElse(null);
        Integer columns = loadInteger(element, "columns")
                .orElse(1);
        LabelsPosition labelsPosition = loadEnum(element, LabelsPosition.class, "labelsPosition")
                .orElse(defaultLabelPosition);

        return new ResponsiveStep(minWidth, columns, labelsPosition);
    }

    public static class FormItemLoader extends AbstractComponentLoader<FormLayout.FormItem> {

        protected ComponentLoader<?> pendingLoadComponent;

        @Override
        protected FormLayout.FormItem createComponent() {
            return new FormLayout.FormItem();
        }

        @Override
        public void initComponent() {
            super.initComponent();
            createSubComponent(resultComponent, element);
        }

        @Override
        public void loadComponent() {
            loadLabel(resultComponent, element);
            componentLoader().loadEnabled(resultComponent, element);
            componentLoader().loadClassNames(resultComponent, element);
            componentLoader().loadClickNotifierAttributes(resultComponent, element);

            loadSubComponent();
        }

        protected void loadLabel(FormLayout.FormItem resultComponent, Element element) {
            loadResourceString(element, "label", context.getMessageGroup())
                    .ifPresent(label -> SlotUtils.addToSlot(resultComponent, "label", new NativeLabel(label)));
        }

        protected void createSubComponent(FormLayout.FormItem resultComponent, Element element) {
            LayoutLoader loader = getLayoutLoader();

            List<Element> childElements = element.elements();
            if (childElements.size() != 1) {
                String message = String.format("%s should have only one child component",
                        FormLayout.FormItem.class.getSimpleName());

                throw new GuiDevelopmentException(message, context,
                        "Component ID", resultComponent.getId().orElse("null"));
            }

            ComponentLoader<?> componentLoader = loader.createComponentLoader(childElements.get(0));
            componentLoader.initComponent();
            pendingLoadComponent = componentLoader;

            resultComponent.add(componentLoader.getResultComponent());
        }

        protected void loadSubComponent() {
            pendingLoadComponent.loadComponent();
            pendingLoadComponent = null;
        }
    }

    @Nullable
    protected String generatePropertyLabel(Component component) {
        if (component instanceof FormLayout.FormItem formItemComponent) {
            // There should only be one child at this point
            component = formItemComponent.getChildren()
                    .findAny()
                    .orElse(null);
        }

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
