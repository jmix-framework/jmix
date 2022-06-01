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

package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * A field that is used to enter secret text information like passwords. The entered text is not displayed on the screen.
 */
@StudioComponent(
        caption = "PasswordField",
        category = "Components",
        xmlElement = "passwordField",
        icon = "io/jmix/ui/icon/component/passwordField.svg",
        canvasBehaviour = CanvasBehaviour.INPUT_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/password-field.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, options = "string"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface PasswordField
        extends TextInputField<String>,
        TextInputField.MaxLengthLimited,
        HasInputPrompt,
        TextInputField.HtmlNameSupported {

    String NAME = "passwordField";

    /**
     * Return autocomplete attribute value to specify saving it in browser.
     */
    boolean isAutocomplete();

    /**
     * Set autocomplete attribute value to specify saving it in browser.
     * False value disables saving passwords in browser.
     */
    @StudioProperty(defaultValue = "false")
    void setAutocomplete(Boolean autocomplete);

    /**
     * Sets CapsLockIndicator component, that will be indicate when caps lock key is active.
     *
     * @param capsLockIndicator capsLockIndicator component
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF, options = {"io.jmix.ui.component.CapsLockIndicator"})
    void setCapsLockIndicator(@Nullable CapsLockIndicator capsLockIndicator);

    /**
     * @return capsLockIndicator component
     */
    @Nullable
    CapsLockIndicator getCapsLockIndicator();
}