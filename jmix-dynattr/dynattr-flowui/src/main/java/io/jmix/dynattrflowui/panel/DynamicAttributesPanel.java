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

package io.jmix.dynattrflowui.panel;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.data.value.ContainerValueSourceProvider;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewValidation;
import org.hibernate.validator.internal.metadata.facets.Validatable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DynamicAttributesPanel extends Composite<VerticalLayout> implements HasSize, HasComponents {

    public static final String NAME = "dynamicAttributesPanel";

    protected final UiComponentsGenerator uiComponentsGenerator;
    protected final UiComponents uiComponents;
    protected final Messages messages;
    protected final DynAttrMetadata dynAttrMetadata;
    protected final ViewValidation viewValidation;
    protected InstanceContainer<?> instanceContainer;
    protected VerticalLayout rootLayout;
    protected VerticalLayout categoryFieldBox;
    protected H3 categoryFieldLabel;
    protected JmixComboBox<Category> categoryField;
    protected JmixFormLayout dynamicAttributesForm;

    protected List<Component> dynAttrFormFields = new ArrayList<>();

    public DynamicAttributesPanel(UiComponentsGenerator uiComponentsGenerator,
                                  UiComponents uiComponents,
                                  Messages messages,
                                  DynAttrMetadata dynAttrMetadata,
                                  ViewValidation viewValidation) {
        this.uiComponentsGenerator = uiComponentsGenerator;
        this.uiComponents = uiComponents;
        this.messages = messages;
        this.dynAttrMetadata = dynAttrMetadata;
        this.viewValidation = viewValidation;


    protected VerticalLayout initDynAttrContent() {
        rootLayout = uiComponents.create(VerticalLayout.class);
        rootLayout.setPadding(false);
        rootLayout.setSpacing(true);
        rootLayout.setWidth("100%");

        categoryFieldBox = uiComponents.create(VerticalLayout.class);
        categoryFieldBox.setPadding(false);
        categoryFieldBox.setMargin(false);
        categoryFieldBox.setWidth("100%");
        categoryFieldBox.setSpacing(true);


        categoryFieldLabel = uiComponents.create(H3.class);
        categoryFieldLabel.setText(messages.getMessage(getClass(), "category"));

        //noinspection unchecked
        categoryField = uiComponents.create(JmixComboBox.class);
        categoryField.addValueChangeListener(e -> initPropertiesForm());
        categoryFieldBox.add(categoryFieldLabel, categoryField);
        categoryFieldBox.expand(categoryField);

        dynamicAttributesForm = uiComponents.create(JmixFormLayout.class);
        dynamicAttributesForm.setWidth("100%");
        rootLayout.add(categoryFieldBox, dynamicAttributesForm);
        rootLayout.expand(dynamicAttributesForm);

        return rootLayout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        View<?> view = UiComponentUtils.findView(this);
        if(view instanceof StandardDetailView<?> detailView) {
            ViewControllerUtils.addValidationEventListener(detailView, this::onValidation);
        }

        super.onAttach(attachEvent);
    }

    protected void onValidation(StandardDetailView.ValidationEvent validationEvent) {
        validationEvent.addErrors(viewValidation.validateUiComponents(propertiesForm));
    }


    @Override
    protected VerticalLayout initContent() {
        VerticalLayout content = super.initContent();
        content.setMargin(false);
        content.setPadding(false);
        content.setWidth("100%");
        content.setId("dynAttrPanelLayout");

        VerticalLayout dynamicAttributeContent = initDynAttrContent();

        content.add(dynamicAttributeContent);

        return content;
    }

    protected void initPropertiesForm() {
        dynamicAttributesForm.removeAll();
        dynAttrFormFields.clear();

        Map<AttributeDefinition, Component> fields = new HashMap<>();
        for (AttributeDefinition attribute : getAttributesByCategory()) {
            Component resultComponent = generateFieldComponent(attribute);
            fields.put(attribute, resultComponent);
            dynAttrFormFields.add(resultComponent);
        }

        addFieldsToForm(dynamicAttributesForm, fields);
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

    protected Component createEmptyComponent() {
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

        return uiComponentsGenerator.generate(componentContext);
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

    protected Collection<CategoryDefinition> getCategoryDefinitions() {
        return dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass());
    }

    protected List<Category> getCategoriesOptionsList() {
        Collection<CategoryDefinition> options = getCategoryDefinitions();
        return options.stream().
                map(definition -> (Category) definition.getSource())
                .toList();
    }

    protected void onInstanceContainerItemChangeEvent(InstanceContainer.ItemChangeEvent<?> event) {
        if (event.getItem() instanceof Categorized
                && ((Categorized) event.getItem()).getCategory() == null) {
            ((Categorized) event.getItem()).setCategory(getDefaultCategory());
        }
        if (event.getItem() == null) {
            dynamicAttributesForm.removeAll();
        }
    }

    protected void doOnDynAttrFields(Consumer<Component> consumer) {
        List<Component> allElements = new ArrayList<>(dynAttrFormFields);
        allElements.add(categoryField);
        allElements.forEach(consumer);
    }

    /**
     * Sets the instance container for DynamicAttributePanel component,
     * which provides the data for the dynamic attributes form.
     *
     * @param container The instance container providing data for the dynamic attributes form
     */
    public void setInstanceContainer(InstanceContainer<Object> container) {
        this.instanceContainer = container;
        dynamicAttributesForm.setValueSourceProvider(new ContainerValueSourceProvider<>(instanceContainer));
        initCategoryField(instanceContainer);
        initPropertiesForm();
        instanceContainer.addItemChangeListener(this::onInstanceContainerItemChangeEvent);
        if (instanceContainer instanceof HasLoader hasLoader) {
            DataLoader loader = hasLoader.getLoader();
            if (loader != null) {
                loader.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
            }
        }
    }

    /**
     * Retrieves the default category from the category list for settle entity.
     *
     * @return The default category, or null if no default category is found
     */
    @Nullable
    public Category getDefaultCategory() {
        for (CategoryDefinition category : getCategoryDefinitions()) {
            if (category != null && category.isDefault()) {
                return (Category) category.getSource();
            }
        }
        return null;
    }

    /**
     * Sets the visibility of the category field.
     *
     * @param visible true to make the category field visible, false otherwise
     */
    public void setCategoryFieldVisible(boolean visible) {
        categoryFieldBox.setVisible(visible);
    }

    /**
     * Checks is the dynamic attributes panel is valid by validating internal components and fields.
     *
     * @return true if the dynamic attributes panel is valid, false otherwise
     */
    public boolean isValid() {
        Collection<Component> components = UiComponentUtils.getOwnComponents(dynamicAttributesForm);
        for (Component component : components) {
            if (component instanceof Validatable) {
                ValidationErrors validationErrors = validate.validateUiComponents(component);
                if (!validationErrors.isEmpty())
                    return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the root layout of category field layout and form layout of the dynamic attribute fields.
     *
     * @return The root layout
     */
    public VerticalLayout getRootLayout() {
        return rootLayout;
    }

    /**
     * Sets the visibility of the dynamic attributes panel.
     *
     * @param visible true to make the dynamic attributes panel visible, false otherwise
     */
    public void setVisibility(boolean visible) {
        getContent().setVisible(visible);
    }

    /**
     * Retrieves the category field of the dynamic attributes panel component.
     *
     * @return The category field
     */
    public JmixComboBox<Category> getCategoryField() {
        return categoryField;
    }

    /**
     * Retrieves the dynamic attributes form layout where dynamic attribute fields placed.
     *
     * @return The dynamic attributes form layout
     */
    public JmixFormLayout getDynAttrForm() {
        return dynamicAttributesForm;
    }

    /**
     * Retrieves the list of fields in the dynamic attribute panel's form.
     *
     * @return The list of dynamic attributes form fields
     */
    public List<Component> getDynAttrFormFields() {
        return dynAttrFormFields;
    }
}