/*
 * Copyright 2024 Haulmont.
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
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_FileFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileFieldDelegate<C extends AbstractField<?, V>, T, V> extends AbstractFieldDelegate<C, T, V> {

    public FileFieldDelegate(C component) {
        super(component);
    }

    @Override
    protected AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }

    @Override
    protected void setInvalidInternal(boolean invalid) {
        super.setInvalidInternal(invalid);
    }

    @Override
    public void updateInvalidState() {
        boolean invalid = !component.isReadOnly() && component.isEnabled() &&
                (explicitlyInvalid || conversionInvalid || !validatorsPassed());

        setInvalidInternal(invalid);
    }
}
