/*
 * Copyright 2022 Haulmont.
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

import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.data.DataAwareComponentsTools;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("flowui_DateTimePickerDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateTimePickerDelegate<V extends Comparable>
        extends AbstractDateTimeFieldDelegate<TypedDateTimePicker<V>, V, LocalDateTime> {

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public DateTimePickerDelegate(TypedDateTimePicker<V> component) {
        super(component);
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<V> createValueBinding(ValueSource<V> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }

    @Override
    protected void setupProperties(ValueSource<V> valueSource) {
        if (valueSource instanceof EntityValueSource<?,?> entityValueSource) {
            dataAwareComponentsTools.setupZoneId(component, entityValueSource);
            dataAwareComponentsTools.setupRange(component, entityValueSource);
        }
    }
}
