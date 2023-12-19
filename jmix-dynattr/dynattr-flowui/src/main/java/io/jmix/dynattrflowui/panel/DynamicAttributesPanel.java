/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dynattrflowui.panel;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.dynattr.model.Categorized;
import io.jmix.dynattr.model.Category;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.data.value.ContainerValueSourceProvider;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewValidation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicAttributesPanel extends Composite<VerticalLayout> implements HasSize {

    public static final String NAME = "dynamicAttributesPanel";

    public static final String DEFAULT_FIELD_WIDTH = null;

    protected final UiComponentsGenerator uiComponentsGenerator;
    protected final UiComponents uiComponents;
    protected final Messages messages;
    protected final DynAttrMetadata dynAttrMetadata;
    protected final ViewValidation validate;
    protected InstanceContainer<?> instanceContainer;
    protected String fieldWidth = DEFAULT_FIELD_WIDTH;
    protected VerticalLayout rootPanel;
    protected VerticalLayout categoryFieldBox;
    protected H3 categoryFieldLabel;
    protected JmixComboBox<Category> categoryField;
    protected JmixFormLayout propertiesForm;

    public DynamicAttributesPanel(UiComponentsGenerator uiComponentsGenerator,
                                  UiComponents uiComponents,
                                  Messages messages,
                                  DynAttrMetadata dynAttrMetadata,
                                  ViewValidation validate) {
        this.uiComponentsGenerator = uiComponentsGenerator;
        this.uiComponents = uiComponents;
        this.messages = messages;
        this.dynAttrMetadata = dynAttrMetadata;
        this.validate = validate;

        rootPanel = uiComponents.create(VerticalLayout.class);
        rootPanel.setPadding(false);
        rootPanel.setSpacing(true);
        rootPanel.setWidth("100%");

        categoryFieldBox = uiComponents.create(VerticalLayout.class);
        categoryFieldBox.setPadding(false);
        categoryFieldBox.setMargin(false);
        categoryFieldBox.setWidth("100%");
        categoryFieldBox.setSpacing(true);

        categoryFieldLabel = uiComponents.create(H3.class);
        categoryFieldLabel.setText(messages.getMessage(getClass(), "category"));

        //noinspection unchecked
        categoryField = uiComponents.create(JmixComboBox.class);
        categoryField.setWidth(fieldWidth);
        categoryField.addValueChangeListener(e -> initPropertiesForm());
        categoryFieldBox.add(categoryFieldLabel, categoryField);
        categoryFieldBox.expand(categoryField);

        propertiesForm = uiComponents.create(JmixFormLayout.class);
        propertiesForm.setWidth("100%");
        rootPanel.add(categoryFieldBox, propertiesForm);
        rootPanel.expand(propertiesForm);
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout content = super.initContent();
        content.setMargin(false);
        content.setPadding(false);
        content.setId("dynAttrPanelLayout");
        content.add(rootPanel);
        return content;
    }

    protected void initPropertiesForm() {
        propertiesForm.removeAll();

        Map<AttributeDefinition, Component> fields = new HashMap<>();
        for (AttributeDefinition attribute : getAttributesByCategory()) {
            Component resultComponent = generateFieldComponent(attribute);
            fields.put(attribute, resultComponent);
        }

        addFieldsToForm(propertiesForm, fields);
    }

    protected void addFieldsToForm(FormLayout newPropertiesForm, Map<AttributeDefinition, Component> fields) {
        if (fields.keySet().stream().anyMatch(attr -> attr.getConfiguration().getColumnNumber() != null
                && attr.getConfiguration().getRowNumber() != null)) {
            List<AttributeDefinition> attributesToAdd = fields.keySet().stream()
                    .filter(attr -> attr.getConfiguration().getColumnNumber() != null
                            && attr.getConfiguration().getRowNumber() != null)
                    .toList();

            int maxColumnIndex = attributesToAdd.stream()
                    .mapToInt(attr -> attr.getConfiguration().getColumnNumber())
                    .max()
                    .orElse(0);
            newPropertiesForm.setResponsiveSteps(Stream.of(maxColumnIndex)
                    .map(i -> new FormLayout.ResponsiveStep("0px", i))
                    .toList());
            for (int i = 0; i <= maxColumnIndex; i++) {
                int columnIndex = i;
                List<AttributeDefinition> columnAttributes = attributesToAdd.stream()
                        .filter(attr -> attr.getConfiguration().getColumnNumber() != null && attr.getConfiguration().getRowNumber() != null)
                        .filter(attr -> columnIndex == attr.getConfiguration().getColumnNumber())
                        .sorted(Comparator.comparing(attr -> attr.getConfiguration().getRowNumber()))
                        .toList();
                int currentRowNumber = 0;
                for (AttributeDefinition attr : columnAttributes) {
                    //noinspection DataFlowIssue
                    while (attr.getConfiguration().getRowNumber() > currentRowNumber) {
                        //add empty row
                        newPropertiesForm.add(createEmptyComponent());
                        currentRowNumber++;
                    }

                    newPropertiesForm.add(fields.get(attr));
                    currentRowNumber++;
                }
            }
        } else {
            List<Component> sortedAttributeFields = fields.entrySet().stream()
                    .sorted(Comparator.comparingInt(e -> e.getKey().getOrderNo()))
                    .map(Map.Entry::getValue)
                    .toList();
            for (Component field : sortedAttributeFields) {
                newPropertiesForm.add(field);
            }
        }
    }

    private Component createEmptyComponent() {
        Text component = uiComponents.create(Text.class);
        component.setText("\u2060");
        return component;
    }

    protected Collection<AttributeDefinition> getAttributesByCategory() {
        Category category = categoryField.getValue();
        if (category != null) {
            for (CategoryDefinition categoryDefinition : dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass())) {
                if (category.equals(categoryDefinition.getSource())) {
                    return categoryDefinition.getAttributeDefinitions();
                }
            }
        }
        return Collections.emptyList();
    }

    protected Component generateFieldComponent(AttributeDefinition attribute) {
        MetaProperty metaProperty = attribute.getMetaProperty();
        ValueSource<?> valueSource = new ContainerValueSource<>(instanceContainer, metaProperty.getName());

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(instanceContainer.getEntityMetaClass(), metaProperty.getName());
        componentContext.setValueSource(valueSource);

        Component resultComponent = uiComponentsGenerator.generate(componentContext);

        setWidth(resultComponent, attribute);

        return resultComponent;
    }

    protected void setWidth(Component component, AttributeDefinition attribute) {
        String formWidth = attribute.getConfiguration().getFormWidth();
        if (!Strings.isNullOrEmpty(formWidth) && component instanceof HasSize) {
            ((HasSize) component).setWidth(formWidth);
        } else {
            ((HasSize) component).setWidth(fieldWidth);
        }
    }

    protected void initCategoryField(InstanceContainer<?> instanceContainer) {
        if (!Categorized.class.isAssignableFrom(instanceContainer.getEntityMetaClass().getJavaClass())
                || instanceContainer.getEntityMetaClass().getPropertyPath("category") == null) {
            throw new DevelopmentException("Entity must implement 'io.jmix.dynattr.model.Categorized' and contain " +
                    "'category' attribute in order to use DynamicAttributesPanel.");
        }

        categoryField.setItems(getCategoriesOptionsList());
        categoryField.setValueSource(new ContainerValueSource<>(instanceContainer, "category"));
    }

    @Nullable
    protected Category getDefaultCategory() {
        for (CategoryDefinition category : getCategoryDefinitions()) {
            if (category != null && category.isDefault()) {
                return (Category) category.getSource();
            }
        }
        return null;
    }

    protected Collection<CategoryDefinition> getCategoryDefinitions() {
        return dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass());
    }

    protected List<Category> getCategoriesOptionsList() {
        Collection<CategoryDefinition> options = getCategoryDefinitions();
        return options.stream().
                map(definition -> (Category) definition.getSource())
                .collect(Collectors.toList());
    }

    protected void onInstanceContainerItemChangeEvent(InstanceContainer.ItemChangeEvent<?> event) {
        if (event.getItem() instanceof Categorized
                && ((Categorized) event.getItem()).getCategory() == null) {
            ((Categorized) event.getItem()).setCategory(getDefaultCategory());
        }
        if (event.getItem() == null) {
            propertiesForm.removeAll();
        }
    }

    /**
     * Defines InstanceContainer for DynamicAttributesPanel.
     *
     * @param container {@link InstanceContainer} object with editing entity
     */
    public void setInstanceContainer(InstanceContainer<Object> container) {
        this.instanceContainer = container;
        propertiesForm.setValueSourceProvider(new ContainerValueSourceProvider<>(instanceContainer));
        initCategoryField(instanceContainer);
        initPropertiesForm();
        instanceContainer.addItemChangeListener(this::onInstanceContainerItemChangeEvent);
        if (instanceContainer instanceof HasLoader) {
            DataLoader loader = ((HasLoader) instanceContainer).getLoader();
            if (loader != null) {
                loader.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
            }
        }
    }

    /**
     * Sets the width of the fields. This parameter is used if some dynamic attribute does not have own width value.
     *
     * @param fieldWidth width of the fields
     */
    public void setFieldWidth(String fieldWidth) {
        this.fieldWidth = fieldWidth;
    }
}