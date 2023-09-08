/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.filter.condition;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.genericfilter.FilterMetadataTools;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ViewController("flowui_PropertyFilterCondition.detail")
@ViewDescriptor("property-filter-condition-detail-view.xml")
@EditedEntityContainer("filterConditionDc")
@DialogMode(width = "32em", resizable = true)
public class PropertyFilterConditionDetailView extends FilterConditionDetailView<PropertyFilterCondition> {

    @ViewComponent
    protected InstanceContainer<PropertyFilterCondition> filterConditionDc;

    @ViewComponent
    protected JmixSelect<String> propertyField;
    @ViewComponent
    protected JmixSelect<PropertyFilter.Operation> operationField;
    @ViewComponent
    protected HorizontalLayout defaultValueBox;

    @Autowired
    protected FilterMetadataTools filterMetadataTools;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MessageBundle messageBundle;

    @SuppressWarnings("rawtypes")
    protected HasValueAndElement defaultValueField;

    @Override
    public InstanceContainer<PropertyFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Subscribe
    protected void onReady(@SuppressWarnings("unused") ReadyEvent event) {
        initPropertyField();
        initOperationField();
        initDefaultValueField();
    }

    protected void initPropertyField() {
        if (currentConfiguration != null) {
            List<String> properties = filterMetadataTools.getPropertyPaths(currentConfiguration.getOwner()).stream()
                    .map(MetaPropertyPath::toPathString)
                    .collect(Collectors.toList());

            Optional<String> previousValue = propertyField.getOptionalValue();
            propertyField.setItems(properties);

            previousValue.ifPresent(propertyField::setValue);
        }
    }

    @SuppressWarnings("unchecked")
    protected void initOperationField() {
        List<PropertyFilter.Operation> operations;

        MetaClass filterMetaClass = getFilterMetaClass();
        PropertyFilterCondition editedEntity = getEditedEntity();

        if (filterMetaClass != null && editedEntity.getProperty() != null) {
            EnumSet<PropertyFilter.Operation> availableOperations =
                    propertyFilterSupport.getAvailableOperations(filterMetaClass, editedEntity.getProperty());

            operations = new ArrayList<>(availableOperations);
        } else {
            operations = Collections.EMPTY_LIST;
        }

        Optional<PropertyFilter.Operation> previousValue = operationField.getOptionalValue();
        operationField.setItems(operations);

        previousValue.ifPresent(operationField::setValue);
    }

    @Nullable
    protected MetaClass getFilterMetaClass() {
        return currentConfiguration != null
                ? currentConfiguration.getOwner().getDataLoader().getContainer().getEntityMetaClass()
                : null;
    }

    @SuppressWarnings({"unchecked"})
    protected void initDefaultValueField() {
        String property = getEditedEntity().getProperty();
        PropertyFilter.Operation operation = getEditedEntity().getOperation();
        MetaClass filterMetaClass = getFilterMetaClass();

        if (filterMetaClass != null && property != null && operation != null) {
            defaultValueField = singleFilterSupport.generateValueComponent(filterMetaClass, property, operation);

            FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
            if (valueComponent != null && valueComponent.getDefaultValue() != null) {
                String modelDefaultValue = valueComponent.getDefaultValue();
                MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(filterMetaClass,
                        property);

                if (metaPropertyPath != null) {
                    Object defaultValue = propertyFilterSupport.parseDefaultValue(metaPropertyPath.getMetaProperty(),
                            operation.getType(), modelDefaultValue);

                    UiComponentUtils.setValue(defaultValueField, defaultValue);
                }
            }
        } else {
            defaultValueField = uiComponents.create(TypedTextField.class);
            defaultValueField.setEnabled(false);
        }

        defaultValueBox.removeAll();
        defaultValueBox.add((Component) defaultValueField);

        if (defaultValueField instanceof HasSize) {
            ((HasSize) defaultValueField).setWidthFull();
        }

        if (defaultValueField instanceof HasLabel) {
            String label = messageBundle.getMessage("propertyFilterConditionDetailView.defaultValue");
            ((HasLabel) defaultValueField).setLabel(label);
        }
    }

    @Subscribe("propertyField")
    public void onPropertyFieldComponentValueChange(ComponentValueChangeEvent<JmixSelect<String>, String> event) {
        String property = event.getValue();
        MetaClass filterMetaClass = getFilterMetaClass();

        if (!Strings.isNullOrEmpty(property) && event.isFromClient() && filterMetaClass != null) {
            String parameterName = PropertyConditionUtils.generateParameterName(property);
            getEditedEntity().setParameterName(parameterName);

            EnumSet<PropertyFilter.Operation> availableOperations =
                    propertyFilterSupport.getAvailableOperations(filterMetaClass, property);

            PropertyFilter.Operation previousValue = operationField.getValue();
            operationField.setItems(availableOperations);

            if (availableOperations.contains(previousValue)) {
                operationField.setValue(previousValue);
            }

            resetDefaultValue();
            initDefaultValueField();
        }

        operationField.setEnabled(!Strings.isNullOrEmpty(property));
    }

    @Subscribe("operationField")
    public void onOperationFieldComponentValueChange(ComponentValueChangeEvent<JmixSelect<PropertyFilter.Operation>,
            PropertyFilter.Operation> event) {
        PropertyFilter.Operation operation = event.getValue();

        if (operation != null && event.isFromClient()) {
            resetDefaultValue();
            initDefaultValueField();
        }
    }

    @Install(to = "propertyField", subject = "itemLabelGenerator")
    protected String propertyFieldItemLabelGenerator(String property) {
        //can't be null because the field was filled using the metaclass
        MetaClass filterMetaClass = Objects.requireNonNull(getFilterMetaClass());
        return propertyFilterSupport.getPropertyFilterCaption(filterMetaClass, property);
    }

    @Install(to = "operationField", subject = "itemLabelGenerator")
    protected String operationFieldItemLabelGenerator(PropertyFilter.Operation operation) {
        return propertyFilterSupport.getOperationText(operation);
    }

    protected void resetDefaultValue() {
        FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
        if (valueComponent != null) {
            valueComponent.setDefaultValue(null);
        }
    }

    @Subscribe
    protected void onBeforeSave(BeforeSaveEvent event) {
        String property = getEditedEntity().getProperty();
        PropertyFilter.Operation operation = getEditedEntity().getOperation();
        MetaClass filterMetaClass = getFilterMetaClass();

        if (defaultValueField != null
                && property != null
                && operation != null
                && filterMetaClass != null) {

            MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(filterMetaClass, property);

            String modelDefaultValue = null;
            if (metaPropertyPath != null) {
                Object value = defaultValueField instanceof SupportsTypedValue ?
                        ((SupportsTypedValue<?, ?, ?, ?>) defaultValueField).getTypedValue() :
                        defaultValueField.getValue();

                modelDefaultValue = propertyFilterSupport.formatDefaultValue(metaPropertyPath.getMetaProperty(),
                        operation.getType(), value);
            }

            FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
            valueComponent.setDefaultValue(modelDefaultValue);
            valueComponent.setComponentName(singleFilterSupport.getValueComponentName(defaultValueField));

            String label = getEditedEntity().getLabel();
            String localizedLabel;
            if (!Strings.isNullOrEmpty(label)) {
                localizedLabel = label;
            } else {
                boolean operationLabelVisible = getEditedEntity().getOperationTextVisible()
                        && !getEditedEntity().getOperationEditable();

                localizedLabel = propertyFilterSupport.getPropertyFilterCaption(
                        filterMetaClass, property, operation, operationLabelVisible
                );
            }

            getEditedEntity().setLocalizedLabel(localizedLabel);
        }
    }
}
