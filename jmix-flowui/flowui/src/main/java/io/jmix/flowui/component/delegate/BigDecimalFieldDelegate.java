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

import io.jmix.flowui.component.textfield.JmixBigDecimalField;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import io.jmix.flowui.exception.ComponentValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component("flowui_BigDecimalFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BigDecimalFieldDelegate extends AbstractFieldDelegate<JmixBigDecimalField, BigDecimal, BigDecimal> {

    public BigDecimalFieldDelegate(JmixBigDecimalField component) {
        super(component);
    }

    @Override
    protected void initComponent(JmixBigDecimalField component) {
        super.initComponent(component);

        addValidator(this::unparsableValueValidator);
    }

    protected void unparsableValueValidator(@Nullable BigDecimal value) {
        // BigDecimalField doesn't send 'unparsable-change' event.
        // Reflect its default validator's behavior to check unparsable value.
        if (Objects.equals(value, component.getEmptyValue())
                // BigDecimalField.getInputElementValue()
                && (component.getElement().getProperty("value", false))) {
            String validationMessage = messages.getMessage("validation.unparseableValue");
            throw new ComponentValidationException(validationMessage, component);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<BigDecimal> createValueBinding(ValueSource<BigDecimal> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
