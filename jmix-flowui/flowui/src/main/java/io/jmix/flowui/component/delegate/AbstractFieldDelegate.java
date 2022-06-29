package io.jmix.flowui.component.delegate;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.data.ConversionException;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ComponentValidationException;
import io.jmix.flowui.exception.ValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.jmix.flowui.component.UiComponentUtils.isComponentEnabled;
import static io.jmix.flowui.component.UiComponentUtils.isComponentVisible;

/**
 * @param <C> component type
 * @param <T> value source value type
 * @param <V> component value type
 */
public abstract class AbstractFieldDelegate<C extends AbstractField<?, V>, T, V>
        extends AbstractValueComponentDelegate<C, T, V>
        implements ApplicationContextAware {

    public static final String PROPERTY_INVALID = "invalid";

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected MetadataTools metadataTools;

    protected List<Validator<? super T>> validators;
    protected Datatype<T> datatype;

    protected Function<V, T> toModelConverter;

    protected String requiredMessage;
    protected boolean explicitlyInvalid = true;

    public AbstractFieldDelegate(C component) {
        super(component);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    public void setToModelConverter(@Nullable Function<V, T> toModelConverter) {
        this.toModelConverter = toModelConverter;
    }

    public String applyDefaultValueFormat(@Nullable Object value) {
        if (valueBinding != null
                && valueBinding.getValueSource() instanceof EntityValueSource) {
            EntityValueSource<?, V> entityValueSource = (EntityValueSource<?, V>) valueBinding.getValueSource();
            return metadataTools.format(value, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(value);
    }

    public Registration addValidator(Validator<? super T> validator) {
        if (validators == null) {
            validators = new ArrayList<>();
        }
        validators.add(validator);
        return () -> validators.remove(validator);
    }

    public void executeValidators() throws ValidationException {
        if (isInvalidInternal()) {
            setInvalidInternal(false);
            setComponentErrorMessage(null);
        }

        if (component.isReadOnly()
                || !isComponentVisible(component)
                || !isComponentEnabled(component)) {
            return;
        }

        T modelValue = null;
        if (toModelConverter != null
                && component instanceof SupportsTypedValue) {
            try {
                modelValue = toModelConverter.apply(component.getValue());
            } catch (ConversionException e) {
                LoggerFactory.getLogger(component.getClass())
                        .trace("Unable to convert presentation value to model", e);

                setInvalidInternal(true);
                setComponentErrorMessage(e.getMessage());
                throw new ComponentValidationException(e.getMessage(), component);
            }
        }

        if (component.isEmpty() && component.isRequiredIndicatorVisible()) {
            setComponentRequiredErrorState();
            throw new ComponentValidationException(getRequiredErrorMessage(), component);
        }

        // Use model value from provided converter if it's possible. Typed value from
        // the component can be obsolete in validation time.
        T value = modelValue != null ? modelValue : getComponentValue();

        if (CollectionUtils.isNotEmpty(validators)) {
            for (Validator<? super T> validator : validators) {
                try {
                    validator.accept(value);
                } catch (ValidationException e) {
                    setInvalidInternal(true);
                    setComponentErrorMessage(e.getDetailsMessage());
                    throw new ComponentValidationException(e.getDetailsMessage(), component, e);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    @Nullable
    protected T getComponentValue() {
        if (component instanceof SupportsTypedValue) {
            return ((SupportsTypedValue<?, ?, T, ?>) component).getTypedValue();
        } else {
            return (T) component.getValue();
        }
    }

    @Nullable
    public String getRequiredMessage() {
        return requiredMessage;
    }

    public void setRequiredMessage(@Nullable String requiredMessage) {
        this.requiredMessage = requiredMessage;
    }

    @Nullable
    public Datatype<T> getDatatype() {
        return datatype;
    }

    public void setDatatype(@Nullable Datatype<T> datatype) {
        this.datatype = datatype;
    }

    protected void setComponentErrorMessage(@Nullable String errorMessage) {
        if (component instanceof HasValidation) {
            ((HasValidation) component).setErrorMessage(errorMessage);
        }
    }

    @Nullable
    protected String getComponentErrorMessage() {
        return component instanceof HasValidation
                ? ((HasValidation) component).getErrorMessage()
                : null;
    }

    public boolean isInvalid() {
        updateInvalidState();

        return isInvalidInternal();
    }

    public void setInvalid(boolean invalid) {
        explicitlyInvalid = invalid;

        updateInvalidState();
    }

    protected void updateInvalidState() {
        boolean invalid = explicitlyInvalid && !validatorsPassed();

        setInvalidInternal(invalid);
    }

    protected boolean isInvalidInternal() {
        return component.getElement().getProperty(PROPERTY_INVALID, false);
    }

    protected void setInvalidInternal(boolean invalid) {
        component.getElement().setProperty(PROPERTY_INVALID, invalid);
    }

    protected boolean validatorsPassed() {
        try {
            executeValidators();
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    protected void setComponentRequiredErrorState() {
        // Error message is placed under the component
        // so there is no need to get a label from the field
        String errorMessage = getRequiredMessage();
        if (Strings.isNullOrEmpty(errorMessage)) {
            errorMessage = getMessage("validationFail.defaultRequiredMessage");
        }
        setInvalidInternal(true);
        setComponentErrorMessage(errorMessage);
    }

    protected String getRequiredErrorMessage() {
        String requiredMessage = getRequiredMessage();
        if (!Strings.isNullOrEmpty(requiredMessage)) {
            return requiredMessage;
        }

        String label;
        if (component instanceof HasLabel) {
            label = ((HasLabel) component).getLabel();
        } else {
            label = component.getElement()
                    .getProperty(ElementConstants.LABEL_PROPERTY_NAME, null);
        }

        return Strings.isNullOrEmpty(label)
                ? getMessage("validationFail.defaultRequiredMessage")
                : formatMessage("validationFail.defaultRequiredMessageWithFieldLabel", label);
    }

    protected String getMessage(String key) {
        return messages.getMessage(key);
    }

    protected String formatMessage(String key, Object... params) {
        return messages.formatMessage("", key, params);
    }
}
