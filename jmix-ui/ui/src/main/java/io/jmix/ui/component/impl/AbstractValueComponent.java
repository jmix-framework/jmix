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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.data.*;
import io.jmix.ui.component.data.meta.ValueBinding;
import io.jmix.ui.component.data.value.ValueBinder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class AbstractValueComponent<T extends com.vaadin.ui.Component & com.vaadin.data.HasValue<P>, P, V>
        extends AbstractComponent<T> implements HasValue<V>, HasValueSource<V> {

    protected V internalValue;
    protected ValueBinding<V> valueBinding;

    protected UiTestIdsSupport uiTestIdsSupport;

    @Autowired
    public void setUiTestIdsSupport(UiTestIdsSupport uiTestIdsSupport) {
        this.uiTestIdsSupport = uiTestIdsSupport;
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

    protected void valueBindingConnected(ValueSource<V> valueSource) {
        // hook
    }

    protected void valueBindingActivated(@SuppressWarnings("unused") ValueSource<V> valueSource) {
        // hook
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return valueBinding != null ? valueBinding.getSource() : null;
    }

    @Nullable
    @Override
    public V getValue() {
        return internalValue;
    }

    @Override
    public void setValue(@Nullable V value) {
        setValueToPresentation(convertToPresentation(value));

        V oldValue = internalValue;
        this.internalValue = value;

        if (!fieldValueEquals(value, oldValue)) {
            ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, false);
            publish(ValueChangeEvent.class, event);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    protected void setValueToPresentation(@Nullable P value) {
        if (hasValidationError()) {
            setValidationError(null);
        }

        component.setValue(value);
    }

    protected void componentValueChanged(P prevComponentValue, P newComponentValue, boolean isUserOriginated) {
        if (isUserOriginated) {
            V value;

            try {
                value = convertToModel(newComponentValue);
                P presentationValue = convertToPresentation(value);

                // always update presentation value after change
                // for instance: "1000" entered by user could be "1 000" in case of integer formatting
                setValueToPresentation(presentationValue);
            } catch (ConversionException ce) {
                LoggerFactory.getLogger(getClass()).trace("Unable to convert presentation value to model", ce);

                setValidationError(ce.getLocalizedMessage());

                // notification displaying: cuba-platform/cuba#1877

                return;
            }

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, isUserOriginated);
                publish(ValueChangeEvent.class, event);
            }
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected V convertToModel(@Nullable P componentRawValue) throws ConversionException {
        return (V) componentRawValue;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected P convertToPresentation(@Nullable V modelValue) throws ConversionException {
        return (P) modelValue;
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(oldValue, value);
    }

    protected void commit() {
        if (valueBinding != null) {
            valueBinding.write();
        }
    }

    protected void discard() {
        if (valueBinding != null) {
            valueBinding.discard();
        }
    }

    protected boolean isBuffered() {
        return valueBinding != null
                && valueBinding.isBuffered();
    }

    protected void setBuffered(boolean buffered) {
        if (valueBinding != null) {
            valueBinding.setBuffered(buffered);
        }
    }

    protected boolean isModified() {
        return valueBinding != null
                && valueBinding.isModified();
    }
}
