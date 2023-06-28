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

package io.jmix.flowui.kit.component.valuepicker;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.*;
import io.jmix.flowui.kit.component.formatter.Formatter;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static com.vaadin.flow.data.value.ValueChangeMode.eventForMode;

@Tag("jmix-value-picker")
@JsModule("./src/value-picker/jmix-value-picker.js")
public abstract class ValuePickerBase<C extends ValuePickerBase<C, V>, V>
        extends AbstractField<C, V>
        implements SupportsFormatter<V>, SupportsUserAction<V>, HasValidationProperties,
        HasLabel, HasHelper, HasStyle, HasTheme, HasSize, HasPlaceholder,
        HasTitle, HasAutofocus, HasActions, Focusable<C>, HasTooltip, HasAriaLabel {

    protected static final String PROPERTY_VALUE = "value";
    protected static final String PROPERTY_ALLOW_CUSTOM_VALUE = "allowCustomValue";

    protected ValuePickerActionSupport actionsSupport;

    protected Formatter<? super V> formatter;

    protected ValuePickerBase() {
        super(null);

        initComponent();

        addValueChangeListener(e -> validate());
    }

    protected void initComponent() {
        setAllowCustomValue(false);
        initFieldValuePropertyChangeListener();
    }

    protected void initFieldValuePropertyChangeListener() {
        String propChangeEvent = eventForMode(ValueChangeMode.ON_CHANGE, PROPERTY_VALUE + "-changed");
        getElement().addPropertyChangeListener(PROPERTY_VALUE, propChangeEvent, this::handlePropertyChange);
    }

    protected void handlePropertyChange(PropertyChangeEvent event) {
        if (!isAllowCustomValueBoolean()) {
            return;
        }

        String text = (String) event.getValue();
        V value = getValue();

        if (Strings.isNullOrEmpty(text)
                || Objects.equals(text, formatValue(value))) {
            return;
        }

        setPresentationValue(value);

        //noinspection unchecked
        CustomValueSetEvent<C, V> valueChangeEvent = new CustomValueSetEvent<>(((C) this), text);
        fireEvent(valueChangeEvent);
    }

    @Override
    public void setValue(@Nullable V value) {
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        setModelValue(value, true);
        setPresentationValue(value);
    }

    protected void validate() {
        boolean isRequired = this.isRequiredIndicatorVisible();
        boolean isInvalid = ValidationUtil
                .checkRequired(isRequired, getValue(), getEmptyValue())
                .isError();

        setInvalid(isInvalid);
    }

    @Override
    protected void setPresentationValue(@Nullable V newPresentationValue) {
        String newValue = formatValue(newPresentationValue);
        getElement().setProperty(PROPERTY_VALUE, newValue);
    }

    protected String formatValue(@Nullable V value) {
        String formattedValue = formatter != null
                ? formatter.apply(value)
                : applyDefaultValueFormat(value);

        return nullToEmpty(formattedValue);
    }

    protected String applyDefaultValueFormat(@Nullable V value) {
        return value != null ? String.valueOf(value) : "";
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Formatter<V> getFormatter() {
        return (Formatter<V>) formatter;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super V> formatter) {
        this.formatter = formatter;
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id).orElse(null);
    }

    protected boolean isAllowCustomValueBoolean() {
        return this.getElement().getProperty(PROPERTY_ALLOW_CUSTOM_VALUE, false);
    }

    protected void setAllowCustomValue(boolean allowCustomValue) {
        this.getElement().setProperty(PROPERTY_ALLOW_CUSTOM_VALUE, allowCustomValue);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    @Override
    public void setAriaLabelledBy(String labelledBy) {
        getElement().setProperty("accessibleNameRef", labelledBy);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<C, V>> listener) {
        return getEventBus().addListener(CustomValueSetEvent.class, (ComponentEventListener) listener);
    }

    protected ValuePickerActionSupport getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected ValuePickerActionSupport createActionsSupport() {
        return new ValuePickerActionSupport(this);
    }
}
