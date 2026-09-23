/*
 * Copyright 2026 Haulmont.
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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.SliderValueBinding;
import org.jspecify.annotations.Nullable;

/**
 * @param <C> component type
 * @param <T> value source value type
 * @param <V> component value type
 */
public abstract class AbstractSliderDelegate<C extends AbstractField<?, V>, T extends Number, V extends Number>
        extends AbstractFieldDelegate<C, T, V> {

    protected Registration maxValidatorRegistration;
    protected Registration minValidatorRegistration;

    protected boolean valueSet = false;

    public AbstractSliderDelegate(C component) {
        super(component);
    }

    public void setMax(@Nullable T max) {
        if (maxValidatorRegistration != null) {
            maxValidatorRegistration.remove();
        }
        if (max != null) {
            maxValidatorRegistration = addValidator(getMaxValidator(max));
        }
    }

    protected abstract Validator<T> getMaxValidator(T max);

    public void setMin(@Nullable T min) {
        if (minValidatorRegistration != null) {
            minValidatorRegistration.remove();
        }
        if (min != null) {
            minValidatorRegistration = addValidator(getMinValidator(min));
        }
    }

    protected abstract Validator<T> getMinValidator(T min);

    /**
     * Checks whether the component has no value.
     * <p>
     * A slider always shows a number: an absent value is replaced with the minimum value, hence
     * the component value cannot indicate the empty state. A component bound to a value source is
     * empty when the value source value is absent, because the substituted minimum value is not
     * propagated back, see {@link SliderValueBinding}. An unbound component is empty until a
     * value is set to it, either by the user or programmatically.
     *
     * @return {@code true} if the component has no value, {@code false} otherwise
     */
    public boolean isEmpty() {
        ValueSource<T> valueSource = getValueSource();

        return valueSource != null
                ? valueSource.getValue() == null
                : !valueSet;
    }

    /**
     * {@inheritDoc}
     * <p>
     * An empty component shows the minimum value as a substitute for an absent one. That
     * substitute is not the value of the component and is not passed to validators.
     */
    @Nullable
    @Override
    protected T getComponentValue() {
        return isEmpty() ? null : super.getComponentValue();
    }

    /**
     * Clears the value of the component. A bound component clears its value source as well,
     * instead of writing the minimum value substituted for the absent one.
     *
     * @param clearComponentValueAction action that clears the component value
     */
    public void clearValue(Runnable clearComponentValueAction) {
        if (valueBinding instanceof SliderValueBinding) {
            ((SliderValueBinding<T>) valueBinding).clearValue(clearComponentValueAction);
        } else {
            clearComponentValueAction.run();
        }

        setValueSet(false);
    }

    /**
     * Handles a value that has been set to the component: makes sure the value reaches the value
     * source and records that the component has a value.
     * <p>
     * The binding cannot propagate a value the component already showed, because setting it fires
     * no value change event.
     *
     * @see #isEmpty()
     */
    public void onValueSet() {
        if (valueBinding instanceof SliderValueBinding) {
            ((SliderValueBinding<T>) valueBinding).writeComponentValueToSource();
        }

        setValueSet(true);
    }

    /**
     * Records whether a value has been set to the component. Affects an unbound component only,
     * because for a bound one the value source is the source of truth.
     *
     * @param valueSet whether a value has been set to the component
     * @see #isEmpty()
     */
    protected void setValueSet(boolean valueSet) {
        if (this.valueSet == valueSet) {
            return;
        }

        this.valueSet = valueSet;

        updateStateOnEmptyStateChange();
    }

    /**
     * Updates the validation state after the empty state of the component may have changed
     * without a value change event.
     * <p>
     * Entering the required error state respects
     * {@link io.jmix.flowui.UiComponentProperties#isImmediateRequiredValidationEnabled()},
     * while leaving it does not, the same way {@link #setConversionInvalid(boolean)} does it.
     * Otherwise a required error raised by an explicit validation would never be cleared when
     * the setting is off.
     *
     * @see #isEmpty()
     */
    protected void updateStateOnEmptyStateChange() {
        if (isEmptyAndRequired()) {
            updateRequiredState();
        } else {
            // The value passed to validators changes together with the empty state, so the
            // validation result can change even though the component value did not.
            updateInvalidState();
        }
    }

    @Override
    protected AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource) {
        //noinspection unchecked
        SliderValueBinding<T> valueBinding =
                applicationContext.getBean(SliderValueBinding.class, valueSource, component);
        // The empty state of a bound component follows the value source, so it must be
        // re-evaluated after every value source value the binding sets to the component.
        valueBinding.setComponentValueSetHandler(this::updateStateOnEmptyStateChange);

        return valueBinding;
    }
}
