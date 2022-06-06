/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.data.HasValue;
import com.vaadin.data.ValueProvider;
import com.vaadin.event.Action;
import com.vaadin.event.SerializableEventListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import com.vaadin.util.ReflectTools;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

import static io.jmix.ui.widget.JmixPickerField.FieldValueChangeListener.FIELD_VALUE_CHANGE_METHOD;

public class JmixPickerField<T> extends com.vaadin.ui.CustomField<T> implements Action.Container {

    protected static final String PRIMARY_STYLENAME = "jmix-pickerfield";
    protected static final String LAYOUT_STYLENAME = "jmix-pickerfield-layout";
    protected static final String TEXT_FIELD_STYLENAME = "jmix-pickerfield-text";
    protected static final String BUTTON_STYLENAME = "jmix-pickerfield-button";

    protected T internalValue;

    protected AbstractComponent field;
    protected ValueProvider<T, String> textFieldValueProvider;

    protected IconGenerator<T> iconGenerator;

    protected List<Button> buttons = new ArrayList<>(4);
    protected JmixCssActionsLayout container;

    // CAUTION used only for Safari layout, is null for another browsers
    // Fixes cuba-platform/cuba#1107
    protected CssLayout inputWrapper;

    protected boolean fieldReadOnly = true;

    protected boolean suppressTextChangeListener = false;

    protected Registration fieldValueChangeListener;

    public JmixPickerField() {
        init();
        initField();
        initLayout();
    }

    protected void init() {
        setPrimaryStyleName(PRIMARY_STYLENAME);
        setSizeUndefined();
    }

    @Override
    protected Component initContent() {
        return container;
    }

    protected void initLayout() {
        container = new JmixCssActionsLayout();
        container.setPrimaryStyleName(LAYOUT_STYLENAME);

        container.setWidth(100, Unit.PERCENTAGE);
        field.setWidth(100, Unit.PERCENTAGE);

        Page current = Page.getCurrent();
        if (current != null) {
            WebBrowser browser = current.getWebBrowser();
            if (browser != null
                    && browser.isSafari()) {
                inputWrapper = new CssLayout();
                inputWrapper.setWidth(100, Unit.PERCENTAGE);
                inputWrapper.setPrimaryStyleName("safari-input-wrap");
                inputWrapper.addComponent(field);

                container.addComponent(inputWrapper);
            } else {
                container.addComponent(field);
            }
        } else {
            container.addComponent(field);
        }

        setFocusDelegate((Focusable) field);
    }

    protected void initField() {
        JmixTextField field = new JmixTextField();
        field.setStyleName(TEXT_FIELD_STYLENAME);
        field.setReadOnlyFocusable(true);
        field.setValueChangeMode(ValueChangeMode.BLUR);

        field.setReadOnly(fieldReadOnly);

        this.field = field;

        field.addValueChangeListener(this::onFieldValueChange);
    }

    @Override
    protected boolean isDifferentValue(T newValue) {
        Object value = getValue();
        return value != newValue;
    }

    protected void onFieldValueChange(ValueChangeEvent<?> event) {
        markAsDirty();
    }

    public AbstractComponent getField() {
        return field;
    }

    protected void updateTextRepresentation() {
        JmixTextField textField = (JmixTextField) field;

        suppressTextChangeListener = true;

        String value = getStringRepresentation();
        textField.setValue(value);

        suppressTextChangeListener = false;
    }

    protected String getStringRepresentation() {
        if (textFieldValueProvider != null) {
            return textFieldValueProvider.apply(getValue());
        }

        T value = getValue();
        return value != null
                ? String.valueOf(value)
                : getEmptyStringRepresentation();
    }

    protected String getEmptyStringRepresentation() {
        return "";
    }

    @Override
    protected void doSetValue(T value) {
        internalValue = value;
        updateTextRepresentation();
        updateIcon(value);
    }

    @Nullable
    @Override
    public T getValue() {
        return internalValue;
    }

    @Nullable
    public IconGenerator<T> getIconGenerator() {
        return iconGenerator;
    }

    public void setIconGenerator(@Nullable IconGenerator<T> iconGenerator) {
        if (this.iconGenerator != iconGenerator) {
            this.iconGenerator = iconGenerator;

            updateIcon(internalValue);
        }
    }

    protected void updateIcon(T value) {
        Resource iconResource = iconGenerator != null ? iconGenerator.apply(value) : null;
        getField().setIcon(iconResource);
    }

    public boolean isFieldReadOnly() {
        return fieldReadOnly;
    }

    public void setFieldReadOnly(boolean fieldReadOnly) {
        this.fieldReadOnly = fieldReadOnly;

        updateFieldReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        updateFieldReadOnly();
        updateFieldReadOnlyFocusable();
    }

    protected void updateFieldReadOnly() {
        ((HasValue) getField()).setReadOnly(isReadOnly() || fieldReadOnly);
    }

    protected void updateFieldReadOnlyFocusable() {
        ((JmixTextField) getField()).setReadOnlyFocusable(!isReadOnly() && fieldReadOnly);
    }

    @Nullable
    public String getPlaceholder() {
        return ((JmixTextField) getField()).getPlaceholder();
    }

    public void setPlaceholder(@Nullable String placeholder) {
        ((JmixTextField) getField()).setPlaceholder(placeholder);
    }

    @Override
    public void attach() {
        suppressTextChangeListener = true;

        super.attach();

        suppressTextChangeListener = false;

        // update text representation manually
        if (field instanceof TextField) {
            updateTextRepresentation();
        }
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);

        if (container != null) {
            if (width < 0) {
                container.setWidthUndefined();
                field.setWidthUndefined();
                if (inputWrapper != null) {
                    inputWrapper.setWidthUndefined();
                }
            } else {
                container.setWidth(100, Unit.PERCENTAGE);
                field.setWidth(100, Unit.PERCENTAGE);
                if (inputWrapper != null) {
                    inputWrapper.setWidth(100, Unit.PERCENTAGE);
                }
            }
        }
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);

        if (container != null) {
            if (height < 0) {
                container.setHeightUndefined();
                field.setHeightUndefined();
                if (inputWrapper != null) {
                    inputWrapper.setHeightUndefined();
                }
            } else {
                container.setHeight(100, Unit.PERCENTAGE);
                field.setHeight(100, Unit.PERCENTAGE);
                if (inputWrapper != null) {
                    inputWrapper.setHeight(100, Unit.PERCENTAGE);
                }
            }
        }
    }

    public List<Button> getButtons() {
        return Collections.unmodifiableList(buttons);
    }

    public void addButton(Button button, int index) {
        button.setTabIndex(-1);
        button.setStyleName(BUTTON_STYLENAME);

        buttons.add(index, button);
        container.addComponent(button, index + 1); // 0 - field
    }

    public void removeButton(Button button) {
        buttons.remove(button);
        container.removeComponent(button);
    }

    public Registration addFieldListener(FieldValueChangeListener<T> listener) {
        if (fieldValueChangeListener == null) {
            fieldValueChangeListener = ((JmixTextField) field).addValueChangeListener(event -> {
                String text = event.getValue();

                if (!suppressTextChangeListener &&
                        !Objects.equals(getStringRepresentation(), text)) {
                    suppressTextChangeListener = true;

                    FieldValueChangeEvent<T> e = new FieldValueChangeEvent<>(JmixPickerField.this, text, getValue());
                    fireEvent(e);

                    suppressTextChangeListener = false;

                    // update text representation manually
                    if (field instanceof TextField) {
                        updateTextRepresentation();
                    }
                }
            });
        }

        addListener(FieldValueChangeEvent.class, listener, FIELD_VALUE_CHANGE_METHOD);

        return fieldValueChangeListener;
    }

    @Override
    public void focus() {
        ((Focusable) field).focus();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        ((Focusable) field).setTabIndex(tabIndex);
    }

    @Override
    public int getTabIndex() {
        return ((Focusable) field).getTabIndex();
    }

    @Override
    public void addActionHandler(Action.Handler actionHandler) {
        container.addActionHandler(actionHandler);
    }

    @Override
    public void removeActionHandler(Action.Handler actionHandler) {
        container.removeActionHandler(actionHandler);
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null;
    }

    public ValueProvider<T, String> getTextFieldValueProvider() {
        return textFieldValueProvider;
    }

    public void setTextFieldValueProvider(ValueProvider<T, String> textFieldValueProvider) {
        this.textFieldValueProvider = textFieldValueProvider;
    }

    public interface FieldValueChangeListener<V> extends SerializableEventListener {
        Method FIELD_VALUE_CHANGE_METHOD = ReflectTools
                .findMethod(FieldValueChangeListener.class, "valueChange", FieldValueChangeEvent.class);

        void valueChange(FieldValueChangeEvent<V> event);
    }

    public static class FieldValueChangeEvent<V> extends EventObject {
        protected final String text;
        protected final V prevValue;

        public FieldValueChangeEvent(JmixPickerField<V> source, String text, @Nullable V prevValue) {
            super(source);
            this.text = text;
            this.prevValue = prevValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public JmixPickerField<V> getSource() {
            return (JmixPickerField<V>) super.getSource();
        }

        public String getText() {
            return text;
        }

        @Nullable
        public V getPrevValue() {
            return prevValue;
        }
    }
}
