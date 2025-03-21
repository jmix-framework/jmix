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
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.component.SupportsStatusChangeHandler.StatusContext;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.ConversionException;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.exception.ComponentValidationException;
import io.jmix.flowui.exception.ValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.flowui.component.SupportsValidation.PROPERTY_ERROR_MESSAGE;
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
    protected UiComponentProperties uiComponentProperties;

    protected List<Validator<? super T>> validators;
    protected Datatype<T> datatype;

    protected Function<V, T> toModelConverter;

    protected Consumer<StatusContext<C>> statusChangeHandler;
    protected String errorMessage;

    protected String requiredMessage;
    protected boolean explicitlyInvalid = false;
    protected boolean conversionInvalid = false;
    protected boolean immediateRequiredValidationEnabled = true;

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

    @Autowired
    public void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        this.uiComponentProperties = uiComponentProperties;
        immediateRequiredValidationEnabled = uiComponentProperties.isImmediateRequiredValidationEnabled();
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
        if (component.isReadOnly()
                || !isComponentVisible(component)
                || !isComponentEnabled(component)) {
            return;
        }

        if (isInvalidInternal()) {
            setInvalidInternal(false);
            setErrorMessage(null);
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
                setErrorMessage(e.getMessage());
                throw new ComponentValidationException(e.getMessage(), component);
            }
        }

        if (isEmptyAndRequired()) {
            setComponentRequiredErrorState();
            throw new ComponentValidationException(getRequiredErrorMessage(), component);
        }

        // Use model value from provided converter if it's possible. Typed value from
        // the component can be obsolete in validation time.
        T value = modelValue != null ? modelValue : getComponentValue();

        //If a component has an input value on the client side and doesn't have the value on the server side
        // then the client value is unparseable
        if (value == null && component.getElement().getProperty("_hasInputValue", false)) {
            setInvalidInternal(true);
            String validationMessage = messages.getMessage("validation.unparseableValue");
            setErrorMessage(validationMessage);
            throw new ComponentValidationException(validationMessage, component);
        }

        if (CollectionUtils.isNotEmpty(validators)) {
            for (Validator<? super T> validator : validators) {
                try {
                    validator.accept(value);
                } catch (ValidationException e) {
                    setInvalidInternal(true);
                    setErrorMessage(e.getDetailsMessage());
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

        if (isInvalid() && isEmptyAndRequired()) {
            setComponentRequiredErrorState();
        }
    }

    @Nullable
    public Datatype<T> getDatatype() {
        return datatype;
    }

    public void setDatatype(@Nullable Datatype<T> datatype) {
        this.datatype = datatype;
    }

    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;

        if (statusChangeHandler != null) {
            statusChangeHandler.accept(new StatusContext<>(component, errorMessage));
        } else if (component instanceof HasValidation
                && uiComponentProperties.isShowErrorMessageBelowField()) {
            component.getElement().setProperty(PROPERTY_ERROR_MESSAGE, Strings.nullToEmpty(errorMessage));
        }
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<C>> statusChangeHandler) {
        if (this.statusChangeHandler != statusChangeHandler) {
            String currentErrorMessage = getErrorMessage();
            // Clear previous error message renderer
            setErrorMessage(null);

            this.statusChangeHandler = statusChangeHandler;

            // Set the error message to the current error message renderer
            setErrorMessage(currentErrorMessage);
        }
    }

    public boolean isInvalid() {
        return isInvalidInternal();
    }

    public void setInvalid(boolean invalid) {
        explicitlyInvalid = invalid;

        updateInvalidState();
    }

    public void setConversionInvalid(boolean conversionInvalid) {
        this.conversionInvalid = conversionInvalid;

        if (explicitlyInvalid || conversionInvalid || isEmptyAndRequired()) {
            updateInvalidState();
        } else {
            setInvalidInternal(false);
        }
    }

    public void updateRequiredState() {
        if (immediateRequiredValidationEnabled) {
            updateInvalidState();
        }
    }

    public void updateInvalidState() {
        boolean invalid = explicitlyInvalid || conversionInvalid || !validatorsPassed();

        setInvalidInternal(invalid);
    }

    protected boolean isInvalidInternal() {
        return component.getElement().getProperty(PROPERTY_INVALID, false);
    }

    protected void setInvalidInternal(boolean invalid) {
        component.getElement().setProperty(PROPERTY_INVALID, invalid);
        // It seems some components loose synchronization with client-side after attaching/reattaching
        // with "invalid" state. The execution of JS code helps to update "invalid" state.
        // This is temporal solution, wait for Vaadin issue: https://github.com/vaadin/flow/issues/18180
        // and Jmix issue: https://github.com/jmix-framework/jmix/issues/3557
        component.getElement().executeJs("this.invalid = $0", invalid);
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
        setErrorMessage(errorMessage);
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

    protected boolean isEmptyAndRequired() {
        return component.isEmpty() && component.isRequiredIndicatorVisible();
    }
}
