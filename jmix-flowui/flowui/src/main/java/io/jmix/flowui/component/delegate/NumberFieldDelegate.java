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

import io.jmix.flowui.component.textfield.JmixNumberField;
import io.jmix.flowui.component.validation.DoubleMaxValidator;
import io.jmix.flowui.component.validation.DoubleMinValidator;
import io.jmix.flowui.component.validation.Validator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_NumberFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NumberFieldDelegate extends AbstractNumberFieldDelegate<JmixNumberField, Double, Double> {

    public NumberFieldDelegate(JmixNumberField component) {
        super(component);
    }

    @Override
    protected Validator<Double> getMaxValidator(Double max) {
        //noinspection unchecked
        return applicationContext.getBean(DoubleMaxValidator.class, max);
    }

    @Override
    protected Validator<Double> getMinValidator(Double min) {
        //noinspection unchecked
        return applicationContext.getBean(DoubleMinValidator.class, min);
    }
}
