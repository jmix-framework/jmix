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
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class AbstractViewComponent<T extends com.vaadin.ui.Component, P, V>
        extends AbstractComponent<T> implements HasValue<V>, HasValueSource<V> {

    protected V internalValue;
    protected ValueBinding<V> valueBinding;

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

    protected void valueBindingConnected(ValueSource<V> valueSource) {
        // hook
    }

    protected void valueBindingActivated(ValueSource<V> valueSource) {
        // hook
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
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

    protected abstract void setValueToPresentation(@Nullable P value);

    @SuppressWarnings("unchecked")
    @Nullable
    protected P convertToPresentation(@Nullable V modelValue) throws ConversionException {
        return (P) modelValue;
    }

    protected boolean fieldValueEquals(@Nullable V value, @Nullable V oldValue) {
        return EntityValues.propertyValueEquals(oldValue, value);
    }
}
