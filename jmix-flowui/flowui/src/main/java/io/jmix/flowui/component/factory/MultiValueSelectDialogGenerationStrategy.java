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

package io.jmix.flowui.component.factory;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.app.multivaluepicker.MultiValueSelectView.MultiValueSelectContext;
import io.jmix.flowui.app.multivaluepicker.MultiValueSelectDialog;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.items.EnumDataProvider;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import org.springframework.lang.Nullable;
import java.sql.Time;
import java.time.*;
import java.util.Date;
import java.util.Objects;

import static io.jmix.core.FetchPlan.INSTANCE_NAME;

@org.springframework.stereotype.Component("flowui_MultiValueSelectDialogGenerationStrategy")
public class MultiValueSelectDialogGenerationStrategy<E> extends AbstractComponentGenerationStrategy implements Ordered {

    protected DataComponents dataComponents;

    public MultiValueSelectDialogGenerationStrategy(UiComponents uiComponents,
                                                    Metadata metadata,
                                                    MetadataTools metadataTools,
                                                    Actions actions,
                                                    DatatypeRegistry datatypeRegistry,
                                                    Messages messages,
                                                    EntityFieldCreationSupport entityFieldCreationSupport) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);
    }

    @Autowired
    public void setDataComponents(DataComponents dataComponents) {
        this.dataComponents = dataComponents;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() != null &&
                MultiValueSelectDialog.class.isAssignableFrom(context.getTargetClass())
                && context instanceof MultiValueSelectContext) {
            return createComponentInternal(context);
        }

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        MultiValueSelectContext generationContext = (MultiValueSelectContext) context;
        if (generationContext.getItems() != null) {
            return createFieldWithItems(generationContext);
        } else if (!Strings.isNullOrEmpty(generationContext.getEntityName())) {
            return createEntityField(generationContext);
        } else if (generationContext.getEnumClass() != null) {
            return createEnumField(generationContext);
        } else if (generationContext.getJavaClass() != null) {
            return createDatatypeField(generationContext);
        } else {
            throw new IllegalStateException("Cannot create a component. " +
                    "Not enough information to infer its type");
        }
    }

    @SuppressWarnings("unchecked")
    protected Component createComboBox(MultiValueSelectContext<E> context) {
        JmixComboBox<E> comboBox = uiComponents.create(JmixComboBox.class);

        if (context.getItemLabelGenerator() != null) {
            comboBox.setItemLabelGenerator(context.getItemLabelGenerator());
        }

        comboBox.addValueChangeListener(e -> {
            E selectedValue = e.getValue();
            if (selectedValue != null && !context.getValueExistsHandler().test(selectedValue)) {
                context.getAddItemToLayoutHandler().accept(selectedValue);
            }
            comboBox.setValue(null);
        });

        return comboBox;
    }

    @Nullable
    protected Component createEntityField(MultiValueSelectContext<E> context) {
        MetaClass metaClass = metadata.getClass(Objects.requireNonNull(context.getEntityName()));

        return context.isUseComboBox()
                ? createEntityComboBox(context, metaClass)
                : createEntityPicker(context, metaClass);
    }

    @SuppressWarnings("unchecked")
    protected Component createEntityComboBox(MultiValueSelectContext<E> context, MetaClass metaClass) {
        EntityComboBox<E> entityComboBox = uiComponents.create(EntityComboBox.class);

        CollectionContainer<E> container = dataComponents.createCollectionContainer(metaClass.getJavaClass());
        CollectionLoader<E> loader = dataComponents.createCollectionLoader();

        loader.setQuery("select e from " + metaClass.getName() + " e");
        loader.setFetchPlan(INSTANCE_NAME);
        loader.setContainer(container);
        loader.load();

        entityComboBox.setItems(container);
        if (context.getItemLabelGenerator() != null) {
            entityComboBox.setItemLabelGenerator(context.getItemLabelGenerator());
        }

        entityComboBox.addValueChangeListener(e -> {
            E selectedEntity = e.getValue();
            if (selectedEntity != null && !context.getValueExistsHandler().test(selectedEntity)) {
                context.getAddItemToLayoutHandler().accept(selectedEntity);
            }
            entityComboBox.setValue(null);
        });

        return entityComboBox;
    }

    @SuppressWarnings("unchecked")
    public Component createEntityPicker(MultiValueSelectContext<E> context, MetaClass metaClass) {
        EntityPicker<E> entityPicker = uiComponents.create(EntityPicker.class);
        entityPicker.setMetaClass(metaClass);

        EntityLookupAction<E> lookupAction = (EntityLookupAction<E>) actions.create(EntityLookupAction.ID);

        lookupAction.addActionPerformedListener(context.getEntityPickerActionPerformedEventHandler());
        lookupAction.setTarget(entityPicker);
        entityPicker.addAction(lookupAction);

        return entityPicker;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component createEnumField(MultiValueSelectContext<E> context) {
        JmixComboBox enumField = (JmixComboBox) createComboBox(context);
        enumField.setItems(new EnumDataProvider<>(Objects.requireNonNull(context.getEnumClass())));

        return enumField;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component createFieldWithItems(MultiValueSelectContext<E> context) {
        JmixComboBox comboBox = (JmixComboBox) createComboBox(context);
        comboBox.setItems(context.getItems());

        return comboBox;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Component createDatatypeField(MultiValueSelectContext<E> context) {
        Class<?> type = context.getJavaClass();
        Datatype<E> datatype = (Datatype<E>) datatypeRegistry.get(Objects.requireNonNull(type));

        if (type.equals(java.sql.Date.class)
                || type.equals(Date.class)
                || type.equals(LocalDate.class)) {

            return createDatePicker(datatype);
        } else if (type.equals(Time.class)
                || type.equals(LocalTime.class)
                || type.equals(OffsetTime.class)) {

            return createTimePicker(datatype);
        } else if (type.equals(LocalDateTime.class)
                || type.equals(OffsetDateTime.class)) {

            return createDateTimePicker(datatype);
        } else {

            return createTextField(context, datatype);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDatePicker(Datatype<E> datatype) {
        TypedDatePicker datePicker = uiComponents.create(TypedDatePicker.class);
        datePicker.setDatatype(datatype);

        return datePicker;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTimePicker(Datatype<E> datatype) {
        TypedTimePicker timePicker = uiComponents.create(TypedTimePicker.class);
        timePicker.setDatatype(datatype);

        return timePicker;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateTimePicker(Datatype<E> datatype) {
        TypedDateTimePicker dateTimePicker = uiComponents.create(TypedDateTimePicker.class);
        dateTimePicker.setDatatype(datatype);

        return dateTimePicker;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTextField(MultiValueSelectContext<E> context, Datatype<E> datatype) {
        TypedTextField typedTextField = uiComponents.create(TypedTextField.class);
        typedTextField.setDatatype(datatype);

        if (!context.isReadOnly()) {
            typedTextField.addKeyPressListener(Key.ENTER,
                    e -> context.getAddValueInternalHandler().accept(typedTextField));
        }

        return typedTextField;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 400;
    }
}
