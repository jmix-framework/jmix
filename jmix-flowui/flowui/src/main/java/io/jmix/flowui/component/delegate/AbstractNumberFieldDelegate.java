/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.component.textfield.AbstractNumberField;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import org.springframework.lang.Nullable;

public abstract class AbstractNumberFieldDelegate<C extends AbstractNumberField<?, V>, T extends Number, V extends Number>
        extends AbstractFieldDelegate<C, T, V> {

    protected Registration maxValidatorRegistration;
    protected Registration minValidatorRegistration;

    public AbstractNumberFieldDelegate(C component) {
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

    @Override
    protected AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource) {
        //noinspection unchecked
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
