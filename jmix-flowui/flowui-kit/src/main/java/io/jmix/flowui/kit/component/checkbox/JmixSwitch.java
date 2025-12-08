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

package io.jmix.flowui.kit.component.checkbox;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Validator;

import java.util.Optional;

/**
 * JmixSwitch is an input field representing a binary choice.
 */
@Tag("jmix-switch")
@JsModule("./src/checkbox/jmix-switch.js")
public class JmixSwitch extends AbstractSinglePropertyField<JmixSwitch, Boolean>
        implements ClickNotifier<JmixSwitch>, Focusable<JmixSwitch>, HasAriaLabel,
        HasValidationProperties, HasValidator<Boolean>,
        InputField<AbstractField.ComponentValueChangeEvent<JmixSwitch, Boolean>, Boolean> {

    protected NativeLabel labelElement;

    protected ValidationController<JmixSwitch, Boolean> validationController = new ValidationController<>(
            this);

    public JmixSwitch() {
        super("checked", false, false);

        getElement().addPropertyChangeListener("checked", "checked-changed",
                __ -> {
                });
        // Initialize property value unless it has already been set from a
        // template
        if (getElement().getProperty("checked") == null) {
            setPresentationValue(false);
        }

        // Initialize custom label
        labelElement = new NativeLabel();
        labelElement.getElement().setAttribute("slot", "label");

        addValueChangeListener(e -> validate());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    /**
     * Sets whether the user is required to select the switch. When required,
     * an indicator appears next to the label and the field invalidates if the
     * switch is first selected and then deselected.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required {@code true} to make the field required, {@code false}
     *                 otherwise
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to select the switch.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Get the current label text.
     *
     * @return the current label text
     */
    @Override
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Set the current label text of this switch.
     *
     * @param label the label text to set
     */
    @Override
    public void setLabel(String label) {
        if (getElement().equals(labelElement.getElement().getParent())) {
            getElement().removeChild(labelElement.getElement());
        }
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Replaces the label content with the given label component.
     *
     * @param component the component to be added to the label.
     * @since 23.1
     */
    public void setLabelComponent(Component component) {
        setLabel("");
        getElement().appendChild(labelElement.getElement());
        labelElement.removeAll();
        labelElement.add(component);
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabelledBy(String ariaLabelledBy) {
        getElement().setProperty("accessibleNameRef", ariaLabelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    /**
     * Set the switch to be input focused when the page loads.
     *
     * @param autofocus the boolean value to set
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Get the state for the auto-focus property of the switch.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code autofocus} property from the switch
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    @Override
    public Validator<Boolean> getDefaultValidator() {
        return (value, context) -> {
            boolean fromComponent = context == null;

            // Do the required check only if the validator is called from the
            // component, and not from Binder. Binder has its own implementation
            // of required validation.
            boolean isRequired = fromComponent && isRequiredIndicatorVisible();
            return ValidationUtil.validateRequiredConstraint(
                    Strings.nullToEmpty(getErrorMessage()),
                    isRequired, getValue(), getEmptyValue()
            );
        };
    }

    protected void validate() {
        validationController.validate(getValue());
    }
}
