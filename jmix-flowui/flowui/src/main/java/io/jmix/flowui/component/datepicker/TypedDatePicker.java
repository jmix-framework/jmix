package io.jmix.flowui.component.datepicker;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.DatePickerDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.time.LocalDate;

public class TypedDatePicker<V extends Comparable<V>> extends DatePicker
        implements SupportsValueSource<V>,
        SupportsTypedValue<TypedDatePicker<V>, ComponentValueChangeEvent<DatePicker, LocalDate>, V, LocalDate>,
        SupportsDatatype<V>, SupportsValidation<V>, HasRequired, InitializingBean, ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected DateTimeTransformations dateTimeTransformations;
    protected Messages messages;

    protected DatePickerDelegate<V> fieldDelegate;

    protected V internalValue;

    /**
     * Component manually handles Vaadin value change event: when programmatically sets value
     * (see {@link #setValueInternal(LocalDate)}) and client-side sets value
     * (see {@link #onValueChange(ComponentValueChangeEvent)}). Therefore, any Vaadin value change listener has a
     * wrapper and disabled for handling event.
     */
    protected boolean isVaadinValueChangeEnabled = false;

    @Override
    public void afterPropertiesSet() {
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
        fieldDelegate.setToModelConverter(this::convertToModel);

        setI18n(new DatePickerI18n()
                .setDateFormat(messages.getMessage("dateFormat")));

        attachValueChangeListener();
    }

    @SuppressWarnings("unchecked")
    protected DatePickerDelegate<V> createFieldDelegate() {
        return applicationContext.getBean(DatePickerDelegate.class, this);
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
    public void setValue(@Nullable LocalDate value) {
        setValueInternal(value);
    }

    protected void setValueInternal(@Nullable LocalDate value) {
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
            ComponentEventListener<TypedValueChangeEvent<TypedDatePicker<V>, V>> listener) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<DatePicker, LocalDate>> listener) {
        ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> listenerWrapper = event -> {
            if (isVaadinValueChangeEnabled) {
                listener.valueChanged(event);
            }
        };
        return super.addValueChangeListener(listenerWrapper);
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
    public ValueSource<V> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        fieldDelegate.setValueSource(valueSource);
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
    public void setMin(LocalDate min) {
        super.setMin(min);

        fieldDelegate.setMin(min);
    }

    @Override
    public void setMax(LocalDate max) {
        super.setMax(max);

        fieldDelegate.setMax(max);
    }

    protected void fireAllValueChangeEvents(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        fireDatePickerValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        TypedValueChangeEvent<TypedDatePicker<V>, V> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected void fireDatePickerValueChangeEvent(@Nullable V oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<DatePicker, LocalDate> event = new ComponentValueChangeEvent<>(
                this, this, convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<TypedDatePicker<V>, LocalDate>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(this, ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener);
    }

    protected void onValueChange(ComponentValueChangeEvent<TypedDatePicker<V>, LocalDate> event) {
        if (event.isFromClient()) {
            LocalDate presValue = event.getValue();

            V value = convertToModel(presValue);

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                fireAllValueChangeEvents(value, oldValue, true);
            }
        }
    }

    @Nullable
    protected V convertToModel(@Nullable LocalDate presentationValue) {
        if (presentationValue == null) {
            return null;
        }

        return (V) dateTimeTransformations.transformToType(presentationValue, fieldDelegate.getValueType(), null);
    }

    @Nullable
    protected LocalDate convertToPresentation(@Nullable V modelValue) {
        if (modelValue == null) {
            return null;
        }

        return (LocalDate) dateTimeTransformations.transformToType(modelValue, LocalDate.class, null);
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(value, oldValue);
    }
}
