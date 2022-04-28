package io.jmix.flowui.component.timepicker;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.TimePickerDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.time.*;

public class TypedTimePicker<V extends Comparable<V>> extends TimePicker implements SupportsValueSource<V>,
        SupportsTypedValue<TypedTimePicker<V>, ComponentValueChangeEvent<TimePicker, LocalTime>, V, LocalTime>,
        SupportsDatatype<V>, SupportsValidation<V>, HasRequired, HasZoneId, ApplicationContextAware,
        InitializingBean {

    protected ApplicationContext applicationContext;
    protected DateTimeTransformations dateTimeTransformations;
    protected Messages messages;

    protected TimePickerDelegate<V> fieldDelegate;

    protected V internalValue;

    protected ZoneId zoneId;

    /**
     * Component manually handles Vaadin value change event: when programmatically sets value
     * (see {@link #setValueInternal(LocalTime)}) and client-side sets value
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

    @SuppressWarnings("unchecked")
    protected TimePickerDelegate<V> createFieldDelegate() {
        return applicationContext.getBean(TimePickerDelegate.class, this);
    }

    protected void autowireDependencies() {
        dateTimeTransformations = applicationContext.getBean(DateTimeTransformations.class);
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        fieldDelegate.setToModelConverter(this::convertToModel);

        attachValueChangeListener();

        // todo rp bad API for time format
        //  see setI18n(JsonObject) and setLocale()
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
    public Datatype<V> getDatatype() {
        return fieldDelegate.getDatatype();
    }

    @Override
    public void setDatatype(@Nullable Datatype<V> datatype) {
        fieldDelegate.setDatatype(datatype);
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

    @Override
    public Registration addValidator(Validator<? super V> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
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
    public void setValue(LocalTime value) {
        setValueInternal(value);
    }

    protected void setValueInternal(@Nullable LocalTime value) {
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
            ComponentEventListener<TypedValueChangeEvent<TypedTimePicker<V>, V>> listener) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<TimePicker, LocalTime>> listener) {
        ValueChangeListener<ComponentValueChangeEvent<TimePicker, LocalTime>> listenerWrapper = event -> {
            if (isVaadinValueChangeEnabled) {
                listener.valueChanged(event);
            }
        };
        return super.addValueChangeListener(listenerWrapper);
    }

    @Override
    public void setMin(LocalTime min) {
        super.setMin(min);

        fieldDelegate.setMin(min);
    }

    @Override
    public void setMax(LocalTime max) {
        super.setMax(max);

        fieldDelegate.setMax(max);
    }

    @Override
    public void validate() {
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

    protected void fireAllValueChangeEvents(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        fireTimePickerValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        TypedValueChangeEvent<TypedTimePicker<V>, V> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected void fireTimePickerValueChangeEvent(@Nullable V oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<TimePicker, LocalTime> event = new ComponentValueChangeEvent<>(
                this, this, convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<TypedTimePicker<V>, LocalTime>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(this, ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener);
    }

    protected void onValueChange(ComponentValueChangeEvent<TypedTimePicker<V>, LocalTime> event) {
        if (event.isFromClient()) {
            LocalTime presValue = event.getValue();

            V value = convertToModel(presValue);

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                fireAllValueChangeEvents(value, oldValue, true);
            }
        }
    }

    @Nullable
    protected V convertToModel(@Nullable LocalTime presentationValue) {
        if (presentationValue == null) {
            return null;
        }

        Class<?> modelType = fieldDelegate.getValueType();

        if (isTimeTypeSupportsTimeZones(modelType)) {
            ZonedDateTime userZonedDateTime = convertTimeToZDT(presentationValue, getZoneIdInternal());
            return convertTimeFromZDT(userZonedDateTime, modelType);
        } else {
            return (V) dateTimeTransformations.transformFromLocalTime(presentationValue, modelType);
        }
    }

    @Nullable
    protected LocalTime convertToPresentation(@Nullable V modelValue) {
        if (modelValue == null) {
            return null;
        }

        if (isTimeTypeSupportsTimeZones(fieldDelegate.getValueType())) {
            ZonedDateTime zonedDateTime = convertTimeToZDT(modelValue, getZoneIdInternal());
            return zonedDateTime.toLocalTime();
        } else {
            return dateTimeTransformations.transformToLocalTime(modelValue);
        }
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(value, oldValue);
    }

    protected ZonedDateTime convertTimeToZDT(Object value, ZoneId fromZoneId) {
        if (LocalTime.class.equals(value.getClass())) {
            return LocalDate.now().atTime((LocalTime) value).atZone(fromZoneId);
        } else if (OffsetTime.class.equals(value.getClass())) {
            return ((OffsetTime) value).atDate(LocalDate.now()).atZoneSameInstant(fromZoneId);
        }
        throw new IllegalArgumentException(String.format("Cannot convert to '%s'. Unsupported time type '%s'",
                ZonedDateTime.class.getName(), value.getClass()));
    }

    @SuppressWarnings("unchecked")
    protected V convertTimeFromZDT(ZonedDateTime zonedDateTime, Class<?> modelType) {
        ZonedDateTime systemZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        if (OffsetTime.class.equals(modelType)) {
            return (V) OffsetTime.of(systemZonedDateTime.toLocalTime(), systemZonedDateTime.getOffset());
        }
        throw new IllegalArgumentException(String.format("Cannot convert from '%s'. Unsupported time type '%s'",
                ZonedDateTime.class.getName(), modelType));
    }

    protected ZoneId getZoneIdInternal() {
        return zoneId != null ? zoneId : ZoneId.systemDefault();
    }

    protected boolean isTimeTypeSupportsTimeZones(Class<?> javaType) {
        return OffsetTime.class.isAssignableFrom(javaType);
    }
}
