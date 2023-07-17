/*
 * Copyright 2020 Haulmont.
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

package io.jmix.flowui.app.jmxconsole;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.core.Messages;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@org.springframework.stereotype.Component("ui_AttributeComponentProvider")
public class AttributeComponentProvider {

    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Actions actions;

    protected String type;
    protected Object value;
    protected boolean requestFocus;
    protected boolean isFixedSize;

    public AttributeComponentProvider withType(String type) {
        this.type = type;
        return this;
    }

    public AttributeComponentProvider withValue(Object value) {
        this.value = value;
        return this;
    }

    public AttributeComponentProvider requestFocus(boolean requestFocus) {
        this.requestFocus = requestFocus;
        return this;
    }

    public AttributeComponentProvider withFixedSize(boolean isFixedSize) {
        this.isFixedSize = isFixedSize;
        return this;
    }

    public AbstractField build() {
        AbstractField field;

        if (AttributeHelper.isBoolean(type)) {
            field = uiComponents.create(JmixSelect.class);
            ((JmixSelect) field).setItems(Boolean.TRUE, Boolean.FALSE);
        } else if (AttributeHelper.isArrayOrCollection(type)) {
            field = uiComponents.create(JmixValuePicker.class);
            MultiValueSelectAction<String> multiValueSelectAction = actions.create(MultiValueSelectAction.ID);
            multiValueSelectAction.setJavaClass(String.class);
            multiValueSelectAction.setTarget((JmixValuePicker) field);
            ((JmixValuePicker) field).addAction(multiValueSelectAction);
        } else if (AttributeHelper.isDate(type)) {
            field = uiComponents.create(DatePicker.class);
        } else {
            field = uiComponents.create(TextField.class);
        }
        setValue(field, value);

        return field;
    }

    protected void setValue(AbstractField field, Object value) {
        if (value != null) {
            if (field instanceof TextField) {
                field.setValue(value.toString());
            } else {
                field.setValue(value);
            }
        }
    }

    public Object getFieldConvertedValue(AbstractField field, boolean allowNull) {
        if (field instanceof TextField) {
            String strValue = field.getValue().toString();
            if (allowNull && StringUtils.isBlank(strValue)) {
                return null;
            } else {
                return AttributeHelper.convert(type, strValue);
            }
        } else if (field instanceof DatePicker) {
            LocalDate localDate = (LocalDate) field.getValue();
            return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        } else if (field instanceof JmixValuePicker<?>) {
            return ((List) field.getValue()).toArray(new String[0]);
        }
        return field.getValue();
    }
}
