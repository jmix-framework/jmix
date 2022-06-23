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

package io.jmix.flowui.component.listbox;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.delegate.CollectionFieldDelegate;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.data.*;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public class JmixMultiSelectListBox<V> extends MultiSelectListBox<V> implements SupportsValueSource<Collection<V>>,
        SupportsTypedValue<JmixMultiSelectListBox<V>, ComponentValueChangeEvent<MultiSelectListBox<V>, Set<V>>, Collection<V>, Set<V>>,
        SupportsDataProvider<V>, SupportsItemsContainer<V>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected CollectionFieldDelegate<JmixMultiSelectListBox<V>, V, V> fieldDelegate;
    protected DataViewDelegate<JmixMultiSelectListBox<V>, V> dataViewDelegate;

    protected Collection<V> internalValue;

    /**
     * Component manually handles Vaadin value change event: when programmatically sets value
     * (see {@link #setValueInternal(Collection, Set)}) and client-side sets value
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

        setItemLabelGenerator(fieldDelegate::applyDefaultCollectionItemFormat);

        attachValueChangeListener();
    }

    @Nullable
    @Override
    public Collection<V> getTypedValue() {
        return internalValue;
    }

    @Override
    public void setTypedValue(@Nullable Collection<V> value) {
        setValueInternal(value, fieldDelegate.convertToPresentation(value));
    }

    @Override
    public void setValue(Set<V> value) {
        setValueInternal(null, value);
    }

    protected void setValueInternal(@Nullable Collection<V> modelValue, Set<V> presentationValue) {
        try {
            if (modelValue == null) {
                modelValue = fieldDelegate.convertToModel(presentationValue, getDataProvider().fetch(new Query<>()));
            }

            super.setValue(presentationValue);

            Collection<V> oldValue = internalValue;
            this.internalValue = modelValue;

            if (!fieldValueEquals(modelValue, oldValue)) {
                fireAllValueChangeEvents(modelValue, oldValue, false);
            }
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Cannot convert value to a model type");
        }
    }

    @Override
    public Registration addTypedValueChangeListener(ComponentEventListener<TypedValueChangeEvent<JmixMultiSelectListBox<V>, Collection<V>>> listener) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        setItems(new ContainerDataProvider<>(container));
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

    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<JmixMultiSelectListBox<V>, Set<V>>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(this, ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener);
    }

    protected void onValueChange(ComponentValueChangeEvent<JmixMultiSelectListBox<V>, Set<V>> event) {
        if (event.isFromClient()) {
            Set<V> presValue = event.getValue();

            Collection<V> value;
            try {
                value = fieldDelegate.convertToModel(presValue, getDataProvider().fetch(new Query<>()));

                setValue(fieldDelegate.convertToPresentation(value));
            } catch (ConversionException e) {
                return;
            }

            Collection<V> oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                fireAllValueChangeEvents(value, oldValue, true);
            }
        }
    }

    protected void fireMultiSelectListBoxValueChangeEvent(@Nullable Collection<V> oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<JmixMultiSelectListBox<V>, Set<V>> event = new ComponentValueChangeEvent<>(
                this, this, fieldDelegate.convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    protected void fireAllValueChangeEvents(@Nullable Collection<V> value, @Nullable Collection<V> oldValue, boolean isFromClient) {
        fireMultiSelectListBoxValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable Collection<V> value, @Nullable Collection<V> oldValue, boolean isFromClient) {
        TypedValueChangeEvent<JmixMultiSelectListBox<V>, Collection<V>> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected boolean fieldValueEquals(@Nullable Collection<V> value, @Nullable Collection<V> oldValue) {
        return fieldDelegate.equalCollections(value, oldValue);
    }

    protected CollectionFieldDelegate<JmixMultiSelectListBox<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(CollectionFieldDelegate.class, this);
    }

    protected DataViewDelegate<JmixMultiSelectListBox<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }

}
