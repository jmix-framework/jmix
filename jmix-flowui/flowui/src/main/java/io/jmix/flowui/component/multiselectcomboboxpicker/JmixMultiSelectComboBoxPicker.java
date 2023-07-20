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

package io.jmix.flowui.component.multiselectcomboboxpicker;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.delegate.EntityCollectionFieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.JmixValuePickerActionSupport;
import io.jmix.flowui.data.*;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.multiselectcomboboxpicker.MultiSelectComboBoxPicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.FetchCallbackAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;

public class JmixMultiSelectComboBoxPicker<V> extends MultiSelectComboBoxPicker<V>
        implements EntityMultiPickerComponent<V>, SupportsValueSource<Collection<V>>, SupportsValidation<Collection<V>>,
        SupportsTypedValue<JmixMultiSelectComboBoxPicker<V>, AbstractField.ComponentValueChangeEvent<MultiSelectComboBox<V>, Set<V>>, Collection<V>, Set<V>>,
        SupportsDataProvider<V>, SupportsItemsEnum<V>, SupportsFilterableItemsContainer<V>, HasRequired,
        SupportsItemsFetchCallback<V, String>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected EntityCollectionFieldDelegate<JmixMultiSelectComboBoxPicker<V>, V, V> fieldDelegate;
    protected DataViewDelegate<JmixMultiSelectComboBoxPicker<V>, V> dataViewDelegate;

    protected Collection<V> internalValue;

    /**
     * Component manually handles Vaadin value change event: when programmatically sets value
     * (see {@link #setValueInternal(Collection, Set, boolean)}) and client-side sets value
     * (see {@link #onValueChange(ComponentValueChangeEvent)}). Therefore, any Vaadin value change listener has a
     * wrapper and disabled for handling event.
     */
    protected boolean isVaadinValueChangeEnabled = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();

        fieldDelegate.addValueBindingChangeListener(event ->
                dataViewDelegate.valueBindingChanged(event));

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);

        attachValueChangeListener();
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);

        fieldDelegate.updateInvalidState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        fieldDelegate.updateInvalidState();
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Override
    public Registration addValidator(Validator<? super Collection<V>> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    protected void validate() {
        fieldDelegate.updateInvalidState();
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        ComboBox.ItemFilter<V> itemFilter = (item, filterText) ->
                generateLabel(item).toLowerCase(getLocale())
                        .contains(filterText.toLowerCase(getLocale()));

        setItems(container, itemFilter);
    }

    @Override
    public void setItems(CollectionContainer<V> container,
                         SerializableBiPredicate<V, String> itemFilter) {
        ContainerDataProvider<V> dataProvider = new ContainerDataProvider<>(container);
        setItems(dataProvider, filterText ->
                item -> itemFilter.test(item, filterText));
    }

    @Override
    public ComboBoxListDataView<V> setItems(ComboBox.ItemFilter<V> itemFilter,
                                            ListDataProvider<V> listDataProvider) {
        bindDataProvider(listDataProvider);
        return super.setItems(itemFilter, listDataProvider);
    }

    @Override
    public ComboBoxListDataView<V> setItems(ListDataProvider<V> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ComboBoxLazyDataView<V> setItems(BackEndDataProvider<V, String> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ComboBoxDataView<V> setItems(DataProvider<V, String> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ComboBoxDataView<V> setItems(InMemoryDataProvider<V> inMemoryDataProvider, SerializableFunction<String,
            SerializablePredicate<V>> filterConverter) {
        bindDataProvider(inMemoryDataProvider);
        return super.setItems(inMemoryDataProvider, filterConverter);
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    @Override
    public void setItemsFetchCallback(FetchCallback<V, String> fetchCallback) {
        setItems(new FetchCallbackAdapter<>(fetchCallback));
    }

    @Override
    public void setValueFromClient(@Nullable Collection<V> value) {
        Set<V> convertedValue = fieldDelegate.convertToPresentation(value);
        setModelValue(convertedValue, true);
        setPresentationValue(convertedValue);
    }

    @Nullable
    @Override
    public ValueSource<Collection<V>> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Collection<V>> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        return fieldDelegate.getMetaClass();
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        fieldDelegate.setMetaClass(metaClass);
    }

    @Nullable
    @Override
    public Collection<V> getTypedValue() {
        return internalValue;
    }

    @Override
    public void setTypedValue(@Nullable Collection<V> value) {
        setValueInternal(value, fieldDelegate.convertToPresentation(value), false);
    }

    @Override
    public void setValue(@Nullable Set<V> value) {
        setValueInternal(null, value, false);
    }

    @Override
    public void setValue(Collection<V> vs) {
        setTypedValue(vs);
    }

    protected void setValueInternal(@Nullable Collection<V> modelValue, @Nullable Set<V> presentationValue,
                                    boolean fromClient) {
        try {
            if (modelValue == null && presentationValue != null) {
                modelValue = convertToModel(presentationValue);
            }

            super.setValue(presentationValue);

            Collection<V> oldValue = internalValue;
            this.internalValue = modelValue;

            if (!fieldValueEquals(modelValue, oldValue)) {
                fireAllValueChangeEvents(modelValue, oldValue, fromClient);
            }
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Cannot convert value to a model type");
        }
    }

    protected Collection<V> convertToModel(Set<V> presentationValue) {
        if (getDataProvider() != null && getDataProvider().isInMemory()) {
            return fieldDelegate.convertToModel(presentationValue, getDataProvider().fetch(new Query<>()));
        } else {
            return fieldDelegate.convertToModel(presentationValue);
        }
    }

    @Override
    public void select(V... items) {
        Set<V> value = new LinkedHashSet<>(List.of(items));
        select(value);
    }

    @Override
    public void deselect(V... items) {
        Set<V> value = new LinkedHashSet<>(List.of(items));
        deselect(value);
    }

    @Override
    public void select(Iterable<V> items) {
        Set<V> itemsToSelect;

        if (items instanceof Set) {
            itemsToSelect = (Set<V>) items;
        } else {
            itemsToSelect = new LinkedHashSet<>();
            Preconditions.checkNotNullArgument(itemsToSelect);

            items.forEach(itemsToSelect::add);
        }

        super.updateSelection(itemsToSelect, Collections.emptySet());

        internalValue = convertToModel(super.getSelectedItems());
    }

    @Override
    public void deselect(Iterable<V> items) {
        Set<V> itemsToDeselect;

        if (items instanceof Set) {
            itemsToDeselect = (Set<V>) items;
        } else {
            itemsToDeselect = new LinkedHashSet<>();
            Preconditions.checkNotNullArgument(itemsToDeselect);

            items.forEach(itemsToDeselect::add);
        }

        super.updateSelection(Collections.emptySet(), itemsToDeselect);

        internalValue = convertToModel(super.getSelectedItems());
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<MultiSelectComboBox<V>, Set<V>>> listener
    ) {
        ValueChangeListener<ComponentValueChangeEvent<MultiSelectComboBox<V>, Set<V>>> listenerWrapper = event -> {
            if (isVaadinValueChangeEnabled) {
                listener.valueChanged(event);
            }
        };

        return super.addValueChangeListener(listenerWrapper);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTypedValueChangeListener(
            ComponentEventListener<TypedValueChangeEvent<JmixMultiSelectComboBoxPicker<V>, Collection<V>>> listener
    ) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<V>, Set<V>>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(
                this,
                ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener
        );
    }

    protected void onValueChange(ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<V>, Set<V>> event) {
        if (event.isFromClient()) {
            Set<V> presValue = event.getValue();

            Collection<V> value;
            try {
                value = convertToModel(presValue);

                setValueInternal(value, fieldDelegate.convertToPresentation(value), true);
            } catch (ConversionException e) {
                setErrorMessage(e.getLocalizedMessage());
                setInvalid(true);
                return;
            }
        }

        // update invalid state
        isInvalid();
    }

    protected void fireMultiSelectComboBoxValueChangeEvent(@Nullable Collection<V> oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<V>, Set<V>> event = new ComponentValueChangeEvent<>(
                this, this, fieldDelegate.convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    protected void fireAllValueChangeEvents(@Nullable Collection<V> value, @Nullable Collection<V> oldValue,
                                            boolean isFromClient) {
        fireMultiSelectComboBoxValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable Collection<V> value, @Nullable Collection<V> oldValue,
                                             boolean isFromClient) {
        TypedValueChangeEvent<JmixMultiSelectComboBoxPicker<V>, Collection<V>> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected boolean fieldValueEquals(@Nullable Collection<V> value, @Nullable Collection<V> oldValue) {
        return value == null && oldValue == null || fieldDelegate.equalCollections(value, oldValue);
    }

    @SuppressWarnings("unchecked")
    protected EntityCollectionFieldDelegate<JmixMultiSelectComboBoxPicker<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(EntityCollectionFieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected DataViewDelegate<JmixMultiSelectComboBoxPicker<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }

    @Override
    protected ValuePickerActionSupport createActionsSupport() {
        return new JmixValuePickerActionSupport(this);
    }
}
