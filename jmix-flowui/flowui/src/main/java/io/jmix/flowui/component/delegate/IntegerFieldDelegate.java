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

import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.component.validation.MaxValidator;
import io.jmix.flowui.component.validation.MinValidator;
import io.jmix.flowui.component.validation.Validator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_IntegerFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IntegerFieldDelegate extends AbstractNumberFieldDelegate<JmixIntegerField, Integer, Integer> {

    public IntegerFieldDelegate(JmixIntegerField component) {
        super(component);
    }

    @Override
    protected Validator<Integer> getMaxValidator(Integer max) {
        //noinspection unchecked
        return applicationContext.getBean(MaxValidator.class, max);
    }

    @Override
    protected Validator<Integer> getMinValidator(Integer min) {
        //noinspection unchecked
        return applicationContext.getBean(MinValidator.class, min);
    }
}
