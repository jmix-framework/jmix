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

package io.jmix.jmxconsole;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.core.Messages;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Provides components for attribute fields.
 */
@org.springframework.stereotype.Component("jmxcon_AttributeComponentProvider")
public class AttributeComponentProvider {

    protected Messages messages;
    protected UiComponents uiComponents;
    protected Actions actions;

    public AttributeComponentProvider(Messages messages, UiComponents uiComponents, Actions actions) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.actions = actions;
    }

    /**
     * Builder class for creating attribute components.
     */
    public class ComponentBuilder {
        private String type;
        private Object value;
        private String width;
        private String maxWidth;

        public ComponentBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public ComponentBuilder withValue(Object value) {
            this.value = value;
            return this;
        }

        public ComponentBuilder withWidth(String width) {
            this.width = width;
            return this;
        }

        public ComponentBuilder withMaxWidth(String maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public AbstractField<?, ?> build() {
            return AttributeComponentProvider.this.build(this);
        }
    }

    public ComponentBuilder builder() {
        return new ComponentBuilder();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected AbstractField<?, ?> build(ComponentBuilder componentBuilder) {
        AbstractField field;

        if (AttributeHelper.isBoolean(componentBuilder.type)) {
            JmixSelect select = uiComponents.create(JmixSelect.class);
            select.setItems(Boolean.TRUE, Boolean.FALSE);
            field = select;
        } else if (AttributeHelper.isArrayOrCollection(componentBuilder.type)) {
            JmixMultiValuePicker multiValuePicker = uiComponents.create(JmixMultiValuePicker.class);
            MultiValueSelectAction<String> multiValueSelectAction = actions.create(MultiValueSelectAction.ID);
            multiValueSelectAction.setJavaClass(String.class);
            multiValueSelectAction.setTarget(multiValuePicker);
            multiValuePicker.addAction(multiValueSelectAction);
            field = multiValuePicker;
        } else if (AttributeHelper.isDate(componentBuilder.type)) {
            field = uiComponents.create(DatePicker.class);
        } else {
            field = uiComponents.create(TextField.class);
        }

        if (!Strings.isNullOrEmpty(componentBuilder.width)) {
            ((HasSize) field).setWidth(componentBuilder.width);
        }
        if (!Strings.isNullOrEmpty(componentBuilder.maxWidth)) {
            ((HasSize) field).setMaxWidth(componentBuilder.maxWidth);
        }

        if (field instanceof TextField) {
            componentBuilder.value = componentBuilder.value != null ? String.valueOf(componentBuilder.value) : "";
        }

        UiComponentUtils.setValue(field, componentBuilder.value);

        return field;
    }

    @Nullable
    public Object getFieldConvertedValue(HasValueAndElement<?, ?> field, String type, boolean allowNull) {
        Object value = UiComponentUtils.getValue((HasValue<?, ?>) field);
        if (value == null && allowNull) {
            return null;
        }

        if (field instanceof TextField) {
            String strValue = value.toString();
            if (allowNull && StringUtils.isBlank(strValue)) {
                return null;
            } else {
                return AttributeHelper.convert(type, strValue);
            }
        } else if (field instanceof DatePicker) {
            return value;
        } else if (field instanceof JmixMultiValuePicker<?>) {
            return ((List) value).toArray(new String[0]);
        }

        return value;
    }
}