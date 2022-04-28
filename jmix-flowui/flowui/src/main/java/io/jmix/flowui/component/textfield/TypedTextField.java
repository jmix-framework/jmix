package io.jmix.flowui.component.textfield;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.data.ConversionException;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.TextFieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Collectors;

public class TypedTextField<V> extends TextField
        implements SupportsValidation<V>, SupportsDatatype<V>,
        SupportsTypedValue<TypedTextField<V>, ComponentValueChangeEvent<TextField, String>, V, String>,
        SupportsValueSource<V>, HasRequired, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected TextFieldDelegate<V> fieldDelegate;

    protected V internalValue;

    /**
     * Component manually handles Vaadin value change event: when programmatically sets value
     * (see {@link #setValueInternal(String)}) and client-side sets value
     * (see {@link #onValueChange(ComponentValueChangeEvent)}). Therefore, any Vaadin value change listener has a
     * wrapper and disabled for handling event.
     */
    protected boolean isVaadinValueChangeEnabled = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        fieldDelegate.setToModelConverter(this::convertToModel);

        attachValueChangeListener();
    }

    @SuppressWarnings("unchecked")
    protected TextFieldDelegate<V> createFieldDelegate() {
        return applicationContext.getBean(TextFieldDelegate.class, this);
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

    @Override
    public Registration addValidator(Validator<? super V> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void validate() {
        isInvalid();
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
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
    public ValueSource<V> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        fieldDelegate.setValueSource(valueSource);
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
    public void setValue(String value) {
        setValueInternal(value);
    }

    protected void setValueInternal(String value) {
        try {
            V modelValue = convertToModel(value);

            super.setValue(value);

            V oldValue = internalValue;
            this.internalValue = modelValue;

            if (!fieldValueEquals(modelValue, oldValue)) {
                fireAllValueChangeEvents(modelValue, oldValue, false);
            }
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Cannot convert value to a model type");
        }
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        ValueChangeListener<ComponentValueChangeEvent<TextField, String>> listenerWrapper = event -> {
            if (isVaadinValueChangeEnabled) {
                listener.valueChanged(event);
            }
        };
        return super.addValueChangeListener(listenerWrapper);
    }

    @Override
    public Registration addTypedValueChangeListener(ComponentEventListener<TypedValueChangeEvent<TypedTextField<V>, V>> listener) {
        return getEventBus().addListener(TypedValueChangeEvent.class, (ComponentEventListener) listener);
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
    public void setMaxLength(int maxLength) {
        super.setMaxLength(maxLength);

        fieldDelegate.setMaxLength(maxLength);
    }

    @Override
    public void setMinLength(int minLength) {
        super.setMinLength(minLength);

        fieldDelegate.setMinLength(minLength);
    }

    @Override
    public void setPattern(String pattern) {
        super.setPattern(pattern);

        fieldDelegate.setPattern(pattern);
    }

    protected void fireAllValueChangeEvents(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        fireTextFieldValueChangeEvent(oldValue, isFromClient);
        fireTypedValueChangeEvent(value, oldValue, isFromClient);
    }

    protected void fireTypedValueChangeEvent(@Nullable V value, @Nullable V oldValue, boolean isFromClient) {
        TypedValueChangeEvent<TypedTextField<V>, V> event =
                new TypedValueChangeEvent<>(this, value, oldValue, isFromClient);

        getEventBus().fireEvent(event);
    }

    protected void fireTextFieldValueChangeEvent(@Nullable V oldValue, boolean isFromClient) {
        ComponentValueChangeEvent<TextField, String> event = new ComponentValueChangeEvent<>(
                this, this, convertToPresentation(oldValue), isFromClient);

        isVaadinValueChangeEnabled = true;
        fireEvent(event);
        isVaadinValueChangeEnabled = false;
    }

    protected void attachValueChangeListener() {
        ComponentEventListener<ComponentValueChangeEvent<TypedTextField<V>, String>> componentListener =
                this::onValueChange;

        ComponentUtil.addListener(this, ComponentValueChangeEvent.class,
                (ComponentEventListener) componentListener);
    }

    protected void onValueChange(ComponentValueChangeEvent<TypedTextField<V>, String> event) {
        if (event.isFromClient()) {
            String presValue = event.getValue();

            V value;
            try {
                value = convertToModel(presValue);

                setValue(convertToPresentation(value));
            } catch (ConversionException e) {
                setErrorMessage(e.getLocalizedMessage());
                setInvalid(true);
                return;
            }

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                fireAllValueChangeEvents(value, oldValue, true);
            }
        }
    }

    @Nullable
    protected V convertToModel(String presentationValue) throws ConversionException {
        if (fieldDelegate.getValueSource() instanceof EntityValueSource) {
            MetaPropertyPath propertyPath = ((EntityValueSource<?, V>) fieldDelegate.getValueSource()).getMetaPropertyPath();
            MetaProperty metaProperty = propertyPath.getMetaProperty();

            if (metaProperty.getRange().isDatatype()) {
                Datatype<V> datatype = metaProperty.getRange().asDatatype();
                try {
                    // todo rp empty to null for String type?
                    return datatype.parse(presentationValue, UI.getCurrent().getLocale());
                } catch (ParseException e) {
                    throw new ConversionException(e.getLocalizedMessage());
                }
            }
        }
        if (fieldDelegate.getDatatype() != null) {
            try {
                // todo rp empty to null for String type?
                return fieldDelegate.getDatatype().parse(presentationValue, UI.getCurrent().getLocale());
            } catch (ParseException e) {
                throw new ConversionException(e.getLocalizedMessage());
            }
        }
        return (V) presentationValue;
    }

    protected String convertToPresentation(@Nullable Object modelValue) {
        if (fieldDelegate.getValueSource() instanceof EntityValueSource) {
            MetaPropertyPath propertyPath = ((EntityValueSource<?, V>) fieldDelegate.getValueSource())
                    .getMetaPropertyPath();
            MetaProperty metaProperty = propertyPath.getMetaProperty();
            Range range = metaProperty.getRange();

            if (range.isDatatype()) {
                Datatype<V> propertyDataType = range.asDatatype();
                return Strings.nullToEmpty(propertyDataType.format(modelValue, UI.getCurrent().getLocale()));
            } else {
                setReadOnly(true);
                if (modelValue == null) {
                    return "";
                }

                if (applicationContext != null) {
                    if (range.isClass()) {
                        MetadataTools metadataTools = applicationContext.getBean(MetadataTools.class);
                        if (range.getCardinality().isMany()) {
                            return ((Collection<Object>) modelValue).stream()
                                    .map(metadataTools::getInstanceName)
                                    .collect(Collectors.joining(", "));
                        } else {
                            return metadataTools.getInstanceName(modelValue);
                        }
                    } else if (range.isEnum()) {
                        Messages messages = applicationContext.getBean(Messages.class);
                        return messages.getMessage((Enum<?>) modelValue);
                    }
                }
                return modelValue.toString();
            }
        }
        if (fieldDelegate.getDatatype() != null) {
            return Strings.nullToEmpty(fieldDelegate.getDatatype().format(modelValue, UI.getCurrent().getLocale()));
        }
        return Strings.nullToEmpty((String) modelValue);
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(value, oldValue);
    }
}
