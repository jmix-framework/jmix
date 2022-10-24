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

package io.jmix.flowui.component.datetimepicker;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.DateTimePickerDelegate;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.component.HasRequired;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TypedDateTimePicker<V extends Comparable> extends DateTimePicker
        implements SupportsValueSource<V>,
        SupportsTypedValue<TypedDateTimePicker<V>,
                        ComponentValueChangeEvent<DateTimePicker, LocalDateTime>, V, LocalDateTime>, HasZoneId,
        SupportsDatatype<V>, SupportsValidation<V>, HasRequired, InitializingBean, ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected DateTimeTransformations dateTimeTransformations;
    protected Messages messages;

    protected DateTimePickerDelegate<V> fieldDelegate;

    protected V internalValue;

    protected ZoneId zoneId;

    /**
     * Component manually handles Vaadin value change event: when programmatically sets value
     * (see {@link #setValueInternal(LocalDateTime)}) and client-side sets value
     * (see {@link #onValueChange(ComponentValueChangeEvent)}). Therefore, any Vaadin value change listener has a
     * wrapper and disabled for handling event.
     */
    protected boolean isVaadinValueChangeEnabled = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected void autowireDependencies() {
        dateTimeTransformations = applicationContext.getBean(DateTimeTransformations.class);
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();

        // todo rp date format only for DatePicker
        setDatePickerI18n(new DatePicker.DatePickerI18n()
                .setDateFormat(messages.getMessage("dateFormat")));

        attachValueChangeListener();
    }

    @SuppressWarnings("unchecked")
    protected DateTimePickerDelegate<V> createFieldDelegate() {
        return applicationContext.getBean(DateTimePickerDelegate.class, this);
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Nullable
    @Override
    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public void setZoneId(@Nullable ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Nullable
    @Override
    public Datatype<V> getDatatype() {
        return fieldDelegate.getDatatype();
    }

    @Override
    public void setDatatype(@Nullable Datatype<V> datatype) {
        fieldDelegate.setDatatype(datatype);
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
    public Registration addValidator(Validator<? super V> validator) {
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
        // Method is called from constructor so delegate can be null
        if (fieldDelegate != null) {
            fieldDelegate.setInvalid(invalid);
        } else {
            super.setInvalid(invalid);
        }
    }

    @Override
    public void setMin(LocalDateTime min) {
        super.setMin(min);

        fieldDelegate.setMin(min);
    }

    @Override
    public void setMax(LocalDateTime max) {
        super.setMax(max);

        fieldDelegate.setMax(max);
    }

    @Nullable
    @Override
    public V getTypedValue() {
        return internalValue;
    }

    @Override
    public void setTypedValue(@Nullable V value) {
        setValueInternal(convertToPresentation(value));
    }

    @Override
    public void setValue(LocalDateTime value) {
        setValueInternal(value);
    }

    protected void setValueInternal(@Nullable LocalDateTime value) {
        V modelValue = convertToModel(value);

        super.setValue(value);

        V oldValue = internalValue;
        this.internalValue = modelValue;

        if (!fieldValueEquals(modelValue, oldValue)) {
            fireAllValueChangeEvents(modelValue, oldValue, false);
        }
    }

    @Override
    public Registration addTypedValueChangeListener(
            ComponentEventListener<TypedValueChangeEvent<TypedDateTimePicker<V>, V>> listener) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listenerWrapper = event -> {
            if (isVaadinValueChangeEnabled) {
                listener.valueChanged(event);
            }
        };
        return super.addValueChangeListener(listenerWrapper);
    }

    protected void fireAllValueChangeEvents(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        fireDateTimePickerValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        TypedValueChangeEvent<TypedDateTimePicker<V>, V> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected void fireDateTimePickerValueChangeEvent(@Nullable V oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<DateTimePicker, LocalDateTime> event = new ComponentValueChangeEvent<>(
                this, this, convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<TypedDateTimePicker<V>, LocalDateTime>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(this, ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener);
    }

    protected void onValueChange(ComponentValueChangeEvent<TypedDateTimePicker<V>, LocalDateTime> event) {
        if (event.isFromClient()) {
            LocalDateTime presValue = event.getValue();

            V value = convertToModel(presValue);

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                fireAllValueChangeEvents(value, oldValue, true);
            }
        }
    }

    @Nullable
    protected V convertToModel(@Nullable LocalDateTime presentationValue) {
        if (presentationValue == null) {
            return null;
        }

        Class<?> valueType = fieldDelegate.getValueType();

        ZonedDateTime zonedDateTime = presentationValue.atZone(getZoneIdInternal());

        return (V) dateTimeTransformations.transformFromZDT(zonedDateTime, valueType);
    }

    @Nullable
    protected LocalDateTime convertToPresentation(@Nullable V modelValue) {
        if (modelValue == null) {
            return null;
        }

        Class<?> valueType = fieldDelegate.getValueType();

        ZonedDateTime zonedDateTime = dateTimeTransformations.transformToZDT(modelValue);
        if (dateTimeTransformations.isDateTypeSupportsTimeZones(valueType)) {
            zonedDateTime = zonedDateTime.withZoneSameInstant(getZoneIdInternal());
        }
        return zonedDateTime.toLocalDateTime();
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(value, oldValue);
    }

    protected ZoneId getZoneIdInternal() {
        return zoneId != null ? zoneId : ZoneId.systemDefault();
    }
}
