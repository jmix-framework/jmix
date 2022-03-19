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

package io.jmix.ui.app.jmxconsole.screen.inspect.attribute;

import io.jmix.core.Messages;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.jmxconsole.AttributeHelper;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@org.springframework.stereotype.Component("ui_AttributeComponentProvider")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AttributeComponentProvider {
    protected CheckBox checkBox;
    protected TextField<String> textField;
    protected DateField<Date> dateField;
    protected BoxLayout layout;
    protected String type;

    protected Frame frame;
    protected Object value;

    protected boolean requestFocus;
    protected boolean isFixedSize;

    @Autowired
    protected Messages messages;

    @Autowired
    protected UiComponents uiComponents;

    protected ThemeConstants themeConstants;

    @Autowired
    public void setThemeConstants(ThemeConstantsManager themeConstantsManager) {
        this.themeConstants = themeConstantsManager.getConstants();
    }

    public AttributeComponentProvider withFrame(Frame frame) {
        this.frame = frame;
        return this;
    }

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

    public AttributeComponentProvider build() {
        if (AttributeHelper.isBoolean(type)) {
            checkBox = uiComponents.create(CheckBox.class);
            checkBox.setFrame(frame);
            if (requestFocus) {
                checkBox.focus();
            }
            if (value != null) {
                checkBox.setValue((Boolean) value);
            }

        } else if (AttributeHelper.isArrayOrCollection(type)) {
            initArrayLayout(value, isFixedSize, AttributeHelper.isObjectArrayOrCollection(type));
        } else if (AttributeHelper.isDate(type)) {
            dateField = uiComponents.create(DateField.class);
            dateField.setWidth(themeConstants.get("jmix.ui.jmxconsole.AttributeComponentProvider.dateField.width"));
            dateField.setFrame(frame);
            if (value != null) {
                dateField.setValue((Date) value);
            }
        } else {
            textField = uiComponents.create(TextField.NAME);
            textField.setWidth(themeConstants.get("jmix.ui.jmxconsole.AttributeComponentProvider.textField.width"));
            textField.setFrame(frame);

            if (requestFocus) {
                textField.focus();
            }
            if (value != null) {
                textField.setValue(value.toString());
            }
        }
        return this;
    }

    protected void initArrayLayout(Object value, boolean isFixedSize, boolean isReadOnly) {
        layout = uiComponents.create(VBoxLayout.class);
        layout.setSpacing(true);
        layout.setWidth(themeConstants.get("jmix.ui.jmxconsole.AttributeComponentProvider.arrayLayout.width"));
        if (isFixedSize) {
            layout.setHeight(themeConstants.get("jmix.ui.jmxconsole.AttributeComponentProvider.arrayLayout.height"));
        }

        Button btnAdd = uiComponents.create(Button.class);
        btnAdd.setIconFromSet(JmixIcon.PLUS_CIRCLE);
        btnAdd.setDescription(messages.getMessage(getClass(), "editAttribute.array.btnAdd"));
        layout.add(btnAdd);

        ScrollBoxLayout scrollBoxLayout = uiComponents.create(ScrollBoxLayout.class);
        scrollBoxLayout.setWidthFull();
        scrollBoxLayout.setSpacing(true);

        layout.add(scrollBoxLayout);
        layout.expand(scrollBoxLayout);

        AbstractAction addRowAction = new BaseAction("addRow")
                .withCaption("")
                .withHandler(actionPerformedEvent ->
                        addRow(null, scrollBoxLayout, false)
                );

        addRowAction.setEnabled(!isReadOnly);
        btnAdd.setAction(addRowAction);

        if (value != null) {
            List values = objectToStringArray(value);
            values.forEach(obj -> addRow(obj, scrollBoxLayout, isReadOnly));
        }
    }

    protected void addRow(Object value, ComponentContainer parent, boolean isReadOnly) {
        BoxLayout row = uiComponents.create(HBoxLayout.class);
        row.setSpacing(true);
        row.setWidthFull();

        TextField valueField = uiComponents.create(TextField.class);
        valueField.setValue(value);
        valueField.setEditable(!isReadOnly);
        row.add(valueField);
        row.expand(valueField);

        Button btnRemove = uiComponents.create(Button.class);
        btnRemove.setIconFromSet(JmixIcon.TIMES);
        btnRemove.setDescription(messages.getMessage(getClass(), "editAttribute.array.btnRemove"));

        Action removeRowAction = new BaseAction("removeRow")
                .withCaption("")
                .withHandler(actionPerformedEvent ->
                        parent.remove(row)
                );

        removeRowAction.setEnabled(!isReadOnly);

        btnRemove.setAction(removeRowAction);
        row.add(btnRemove);

        parent.add(row);
    }

    protected List objectToStringArray(Object value) {
        if (value instanceof Collection) {
            return new ArrayList((Collection) value);
        }
        int length = Array.getLength(value);
        List<Object> output = IntStream.range(0, length)
                .mapToObj(i -> Array.get(value, i))
                .collect(Collectors.toList());
        return output;
    }

    public Component getComponent() {
        if (checkBox != null) {
            return checkBox;
        }
        if (dateField != null) {
            return dateField;
        }
        if (textField != null) {
            return textField;
        }
        return layout;
    }

    public Object getAttributeValue(boolean allowNull) {
        if (checkBox != null) {
            Boolean value = checkBox.getValue();
            return BooleanUtils.isTrue(value);
        } else if (dateField != null) {
            return dateField.getValue();
        } else if (textField != null) {
            String strValue = textField.getValue();
            if (allowNull && StringUtils.isBlank(strValue)) {
                return null;
            } else {
                if (strValue == null)
                    strValue = "";
                return AttributeHelper.convert(type, strValue);
            }
        } else if (layout != null) {
            if (AttributeHelper.isList(type)) {
                return getValuesFromArrayLayout(layout);
            } else if (AttributeHelper.isArray(type)) {
                Class clazz = AttributeHelper.getArrayType(type);
                if (clazz != null) {
                    List<String> strValues = getValuesFromArrayLayout(layout);
                    Object array = Array.newInstance(clazz, strValues.size());
                    IntStream.range(0, strValues.size())
                            .forEach(i -> Array.set(array, i, AttributeHelper.convert(clazz.getName(), strValues.get(i))));
                    return array;
                }
            }
        }
        return null;
    }

    protected List<String> getValuesFromArrayLayout(BoxLayout layout) {
        return layout.getComponents()
                .stream()
                .filter(component -> component instanceof TextField)
                .map(component -> (String)((TextField<?>) component).getValue())
                .collect(Collectors.toList());
    }
}