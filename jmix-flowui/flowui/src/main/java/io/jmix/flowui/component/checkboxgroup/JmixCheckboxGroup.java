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

package io.jmix.flowui.component.checkboxgroup;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupDataView;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.delegate.CollectionFieldDelegate;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.*;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class JmixCheckboxGroup<V> extends CheckboxGroup<V>
        implements SupportsTypedValue<JmixCheckboxGroup<V>,
        ComponentValueChangeEvent<CheckboxGroup<V>, Set<V>>, Collection<V>, Set<V>>, SupportsValueSource<Collection<V>>,
        SupportsDataProvider<V>, SupportsItemsContainer<V>, SupportsItemsEnum<V>, SupportsValidation<Collection<V>>,
        SupportsStatusChangeHandler<JmixCheckboxGroup<V>>, HasRequired, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected CollectionFieldDelegate<JmixCheckboxGroup<V>, V, V> fieldDelegate;
    protected DataViewDelegate<JmixCheckboxGroup<V>, V> dataViewDelegate;

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
        isInvalid();
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<JmixCheckboxGroup<V>>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        dataViewDelegate.setItems(container);
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public CheckboxGroupDataView<V> setItems(DataProvider<V, Void> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public CheckboxGroupDataView<V> setItems(InMemoryDataProvider<V> inMemoryDataProvider) {
        bindDataProvider(inMemoryDataProvider);
        return super.setItems(inMemoryDataProvider);
    }

    @Override
    public CheckboxGroupListDataView<V> setItems(ListDataProvider<V> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    @Nullable
    @Override
    public DataProvider<V, ?> getDataProvider() {
        return dataViewDelegate.getDataProvider();
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
    public Collection<V> getTypedValue() {
        return internalValue;
    }

    @Override
    public void setTypedValue(@Nullable Collection<V> value) {
        setValueInternal(value, fieldDelegate.convertToPresentation(value), false);
    }

    @Override
    public void setValue(Set<V> value) {
        setValueInternal(null, value, false);
    }

    protected void setValueInternal(@Nullable Collection<V> modelValue, Set<V> presentationValue, boolean fromClient) {
        try {
            if (modelValue == null) {
                modelValue = fieldDelegate.convertToModel(presentationValue, getDataProvider().fetch(new Query<>()));
            }

            super.setValue(presentationValue);

            Collection<V> oldValue = internalValue;
            internalValue = modelValue;

            if (!fieldValueEquals(modelValue, oldValue)) {
                fireAllValueChangeEvents(modelValue, oldValue, fromClient);
            }
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Cannot convert value to a model type");
        }
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<CheckboxGroup<V>, Set<V>>> listener) {
        ValueChangeListener<ComponentValueChangeEvent<CheckboxGroup<V>, Set<V>>> listenerWrapper = event -> {
            if (isVaadinValueChangeEnabled) {
                listener.valueChanged(event);
            }
        };
        return super.addValueChangeListener(listenerWrapper);
    }

    @Override
    public Registration addTypedValueChangeListener(ComponentEventListener<TypedValueChangeEvent<JmixCheckboxGroup<V>, Collection<V>>> listener) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
    }

    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<JmixCheckboxGroup<V>, Set<V>>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(this, ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener);
    }

    protected void onValueChange(ComponentValueChangeEvent<JmixCheckboxGroup<V>, Set<V>> event) {
        if (event.isFromClient()) {
            Set<V> presValue = event.getValue();

            Collection<V> value = fieldDelegate.convertToModel(presValue, getDataProvider().fetch(new Query<>()));
            setValueInternal(value, fieldDelegate.convertToPresentation(value), true);
        }

        // update invalid state
        validate();
    }

    protected void fireCheckboxGroupValueChangeEvent(@Nullable Collection<V> oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<JmixCheckboxGroup<V>, Set<V>> event = new ComponentValueChangeEvent<>(
                this, this, fieldDelegate.convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    protected void fireAllValueChangeEvents(@Nullable Collection<V> value, @Nullable Collection<V> oldValue, boolean isFromClient) {
        fireCheckboxGroupValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable Collection<V> value, @Nullable Collection<V> oldValue, boolean isFromClient) {
        TypedValueChangeEvent<JmixCheckboxGroup<V>, Collection<V>> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected boolean fieldValueEquals(@Nullable Collection<V> value, @Nullable Collection<V> oldValue) {
        return fieldDelegate.equalCollections(value, oldValue);
    }

    @SuppressWarnings("unchecked")
    protected CollectionFieldDelegate<JmixCheckboxGroup<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(CollectionFieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected DataViewDelegate<JmixCheckboxGroup<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }
}
