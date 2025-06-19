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

package io.jmix.flowui.component;

import com.vaadin.flow.component.HasElement;
import org.springframework.lang.Nullable;

/**
 * Interface that defines behavior for components that can be marked as required.
 * Provides methods to handle the required state and required messages for validation purposes.
 */
public interface HasRequired extends HasElement {

    String PROPERTY_REQUIRED = "required";

    /**
     * Determines if the component is marked as required.
     *
     * @return {@code true} if the component is required, {@code false} otherwise
     */
    default boolean isRequired() {
        return getElement().getProperty(PROPERTY_REQUIRED, false);
    }

    /**
     * Sets the required property for the component. When a component is marked as required,
     * it indicates that a value must be provided for the component in order to pass validation.
     *
     * @param required a boolean value indicating whether the component is marked as required.
     *                 {@code true} to mark the component as required, {@code false} otherwise.
     */
    default void setRequired(boolean required) {
        getElement().setProperty(PROPERTY_REQUIRED, required);
    }

    /**
     * Returns the custom validation message to be displayed if the component is marked as required
     * and the validation fails due to a missing value.
     *
     * @return the required message set for the component, or {@code null} if no message is defined
     */
    @Nullable
    String getRequiredMessage();

    /**
     * Sets a message that will be displayed or logged when a required field or input is not provided.
     *
     * @param requiredMessage the message to be set for required fields.
     *                        If {@code null}, the default message or no message will be used.
     */
    void setRequiredMessage(@Nullable String requiredMessage);
}
