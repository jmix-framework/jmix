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
package com.haulmont.cuba.web.gui.components;

import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.ValueBinding;
import io.jmix.ui.component.data.value.ValueBinder;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.component.impl.UiTestIdsSupport;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.widget.compatibility.JmixValueChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class WebAbstractField<T extends com.vaadin.v7.ui.AbstractField, V>
        extends AbstractComponent<T> implements Field<V> {

    protected static final int VALIDATORS_LIST_INITIAL_CAPACITY = 4;

    protected List<Validator<V>> validators; // lazily initialized list

    protected boolean editable = true;

    protected V internalValue;
    protected ValueBinding<V> valueBinding;

    protected Subscription parentEditableChangeListener;

    protected UiTestIdsSupport uiTestIdsSupport;

    @Autowired
    public void setUiTestIdsSupport(UiTestIdsSupport uiTestIdsSupport) {
        this.uiTestIdsSupport = uiTestIdsSupport;
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return valueBinding != null ? valueBinding.getSource() : null;
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        if (this.valueBinding != null) {
            valueBinding.unbind();

            this.valueBinding = null;
        }

        if (valueSource != null) {
            ValueBinder binder = applicationContext.getBean(ValueBinder.class);

            this.valueBinding = binder.bind(this, valueSource);

            valueBindingConnected(valueSource);

            this.valueBinding.activate();

            valueBindingActivated(valueSource);

            setUiTestId(valueSource);
        }
    }

    protected void setUiTestId(ValueSource<V> valueSource) {
        AppUI ui = AppUI.getCurrent();

        if (ui != null && ui.isTestMode()
                && getComponent().getJTestId() == null) {

            String testId = uiTestIdsSupport.getInferredTestId(valueSource);
            if (testId != null) {
                getComponent().setJTestId(testId);
            }
        }
    }

    protected void valueBindingActivated(ValueSource<V> valueSource) {
        // hook
    }

    protected void valueBindingConnected(ValueSource<V> valueSource) {
        // hook
    }

    protected void initFieldConverter() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public boolean isRequired() {
        return component.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        component.setRequired(required);
    }

    @Override
    public void setRequiredMessage(@Nullable String msg) {
        component.setRequiredError(msg);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return component.getRequiredError();
    }

    @Nullable
    @Override
    public V getValue() {
        //noinspection unchecked
        return (V) component.getValue();
    }

    @Override
    public void setValue(@Nullable V value) {
        setValueToPresentation(convertToPresentation(value));
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

    protected void setValueToPresentation(@Nullable Object value) {
        if (hasValidationError()) {
            setValidationError(null);
        }

        component.setValue(value);
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

    protected void attachListener(T component) {
        component.addValueChangeListener(event -> {
            Object value = event.getProperty().getValue();
            componentValueChanged(value, event instanceof JmixValueChangeEvent
                    && ((JmixValueChangeEvent) event).isUserOriginated());
        });
    }

    protected void componentValueChanged(Object newComponentValue, boolean userOriginated) {
        V value = convertToModel(newComponentValue);
        V oldValue = internalValue;
        internalValue = value;

        if (!fieldValueEquals(value, oldValue)) {
            if (hasValidationError()) {
                setValidationError(null);
            }

            ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, userOriginated);
            publish(ValueChangeEvent.class, event);
        }
    }

    @SuppressWarnings("unchecked")
    protected V convertToModel(Object componentRawValue) {
        return (V) componentRawValue;
    }

    @Nullable
    protected Object convertToPresentation(@Nullable V modelValue) {
        return modelValue;
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(oldValue, value);
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

        V value = getValue();
        if (isEmpty(value) && isRequired()) {
            String requiredMessage = getRequiredMessage();
            if (requiredMessage == null) {
                Messages messages = applicationContext.getBean(Messages.class);
                requiredMessage = messages.getMessage("validationFail.defaultRequiredMessage");
            }
            throw new RequiredValueMissingException(requiredMessage, this);
        }

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

    protected void commit() {
        component.commit();
    }

    protected void discard() {
        component.discard();
    }

    protected boolean isBuffered() {
        return component.isBuffered();
    }

    protected void setBuffered(boolean buffered) {
        component.setBuffered(buffered);
    }

    protected boolean isModified() {
        return component.isModified();
    }

    protected boolean isEmpty(@Nullable Object value) {
        return value == null;
    }
}
