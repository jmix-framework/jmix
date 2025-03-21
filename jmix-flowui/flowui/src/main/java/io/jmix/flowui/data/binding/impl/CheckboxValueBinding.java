/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.HasValue;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.data.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component("flowui_CheckboxValueBinding")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CheckboxValueBinding<V> extends AbstractValueBinding<V> {

    protected UiComponentProperties uiComponentProperties;

    public CheckboxValueBinding(ValueSource<V> valueSource, HasValue<?, V> component) {
        super(valueSource, component);
    }

    @Autowired
    public void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        this.uiComponentProperties = uiComponentProperties;
    }

    @Override
    protected void initRequired(HasValue<?, V> component, MetaPropertyPath metaPropertyPath) {
        if (!uiComponentProperties.isCheckboxRequiredStateInitializationEnabled()) {
            return;
        }

        super.initRequired(component, metaPropertyPath);
    }

    @Nullable
    @Override
    protected V getComponentValue() {
        return component instanceof SupportsTypedValue
                ? ((SupportsTypedValue<?, ?, V, ?>) component).getTypedValue()
                : component.getValue();
    }

    @Override
    protected void setComponentValue(@Nullable V value) {
        if (component instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, V, ?>) component).setTypedValue(value);
        } else {
            component.setValue(value);
        }
    }
}
