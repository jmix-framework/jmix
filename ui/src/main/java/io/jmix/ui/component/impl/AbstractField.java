/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractComponent;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.validation.Validator;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Vaadin 8 based input components.
 *
 * @param <T> type of underlying Vaadin component
 * @param <P> type of presentation value
 * @param <V> type of model value
 */
public abstract class AbstractField<T extends com.vaadin.ui.Component & com.vaadin.data.HasValue<P>, P, V>
        extends AbstractValueComponent<T, P, V> implements Field<V> {

    protected static final int VALIDATORS_LIST_INITIAL_CAPACITY = 2;
    protected List<Validator<V>> validators; // lazily initialized list

    protected boolean editable = true;

    protected Subscription parentEditableChangeListener;

    @Override
    public boolean isRequired() {
        return component.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequired(boolean required) {
        component.setRequiredIndicatorVisible(required);

        setupComponentErrorProvider(required, component);
    }

    protected void setupComponentErrorProvider(boolean required, T component) {
        AbstractComponent abstractComponent = (AbstractComponent) component;
        if (required) {
            abstractComponent.setComponentErrorProvider(this::getErrorMessage);
        } else {
            abstractComponent.setComponentErrorProvider(null);
        }
    }

    @Nullable
    protected ErrorMessage getErrorMessage() {
        return (isEditableWithParent() && isRequired() && isEmpty())
                ? new UserError(getRequiredMessage())
                : null;
    }

    @Override
    public void setRequiredMessage(@Nullable String msg) {
        ((AbstractComponent) component).setRequiredError(msg);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return ((AbstractComponent) component).getRequiredError();
    }

    @Override
    public void setParent(@Nullable Component parent) {
        if (this.parent instanceof EditableChangeNotifier
                && parentEditableChangeListener != null) {
            parentEditableChangeListener.remove();
            parentEditableChangeListener = null;
        }

        super.setParent(parent);

        if (parent instanceof EditableChangeNotifier) {
            parentEditableChangeListener = ((EditableChangeNotifier) parent).addEditableChangeListener(event -> {
                boolean parentEditable = event.getSource().isEditable();
                boolean finalEditable = parentEditable && editable;
                setEditableToComponent(finalEditable);
            });

            Editable parentEditable = (Editable) parent;
            if (!parentEditable.isEditable()) {
                setEditableToComponent(false);
            }
        }
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        if (this.editable == editable) {
            return;
        }

        this.editable = editable;

        boolean parentEditable = true;
        if (parent instanceof ChildEditableController) {
            parentEditable = ((ChildEditableController) parent).isEditable();
        }
        boolean finalEditable = parentEditable && editable;

        setEditableToComponent(finalEditable);
    }

    protected void setEditableToComponent(boolean editable) {
        component.setReadOnly(!editable);
    }

    protected void attachValueChangeListener(T component) {
        component.addValueChangeListener(event ->
                componentValueChanged(event.getOldValue(), event.getValue(), event.isUserOriginated())
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addValidator(Validator<? super V> validator) {
        if (validators == null) {
            validators = new ArrayList<>(VALIDATORS_LIST_INITIAL_CAPACITY);
        }

        if (!validators.contains(validator)) {
            validators.add((Validator<V>) validator);
        }
    }

    @Override
    public void removeValidator(Validator<V> validator) {
        if (validators != null) {
            validators.remove(validator);
        }
    }

    @Override
    public Collection<Validator<V>> getValidators() {
        if (validators == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(validators);
    }

    @Override
    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    @Override
    public void validate() throws ValidationException {
        if (hasValidationError()) {
            setValidationError(null);
        }

        if (!isVisibleRecursive() || !isEditableWithParent() || !isEnabledRecursive()) {
            return;
        }

        try {
            // if we cannot convert current presentation value into model - UI value is invalid
            convertToModel(component.getValue());
        } catch (ConversionException ce) {
            LoggerFactory.getLogger(getClass()).trace("Unable to convert presentation value to model", ce);

            setValidationError(ce.getLocalizedMessage());

            throw new ValidationException(ce.getLocalizedMessage());
        }

        if (isEmpty() && isRequired()) {
            String requiredMessage = getRequiredMessage();
            if (requiredMessage == null) {
                requiredMessage = getDefaultRequiredMessage();
            }
            throw new RequiredValueMissingException(requiredMessage, this);
        }

        V value = getValue();
        triggerValidators(value);
    }

    protected String getDefaultRequiredMessage() {
        Messages messages = applicationContext.getBean(Messages.class);
        String caption = getCaption();
        return Strings.isNullOrEmpty(caption)
                ? messages.getMessage("validationFail.defaultRequiredMessage")
                : messages.formatMessage("", "validationFail.defaultDetailedRequiredMessage", caption);
    }

    protected void triggerValidators(@Nullable V value) throws ValidationFailedException {
        if (validators != null) {
            try {
                for (Validator<V> validator : validators) {
                    validator.accept(value);
                }
            } catch (ValidationException e) {
                setValidationError(e.getDetailsMessage());

                throw new ValidationFailedException(e.getDetailsMessage(), this, e);
            }
        }
    }

    @Nullable
    protected String getDatatypeConversionErrorMsg(@Nullable Datatype<V> datatype) {
        if (datatype == null) {
            return null;
        }

        DatatypeRegistry datatypeRegistry = applicationContext.getBean(DatatypeRegistry.class);

        String datatypeId = datatypeRegistry
                .getIdOptional(datatype)
                .orElse(datatypeRegistry
                        .getIdByJavaClassOptional(datatype.getJavaClass())
                        .orElse(null)
                );

        if (Strings.isNullOrEmpty(datatypeId)) {
            return null;
        }

        String msgKey = String.format("databinding.conversion.error.%s", datatypeId);

        String msg = applicationContext.getBean(Messages.class)
                .getMessage(msgKey);

        if (msgKey.equals(msg)) {
            msg = applicationContext.getBean(Messages.class)
                    .getMessage("databinding.conversion.error.defaultMessage");
        }

        return msg;
    }
}
