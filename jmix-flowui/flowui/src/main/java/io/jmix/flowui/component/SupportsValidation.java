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

import com.google.common.base.Strings;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.lang.Nullable;

/**
 * An interface that provides validation capabilities for components. It defines methods
 * for adding validators, executing validation, and managing error messages and invalid states.
 *
 * @param <V> the type of the value that the component handles
 */
public interface SupportsValidation<V> extends HasValidationProperties {

    String PROPERTY_ERROR_MESSAGE = "errorMessage";

    /**
     * Adds a validator to the list of validators for the component. The provided validator
     * will be used to validate the value of the component during validation execution.
     *
     * @param validator the validator to be added; should not be {@code null}
     * @return a {@link Registration} object that allows removing the added validator
     */
    Registration addValidator(Validator<? super V> validator);

    /**
     * Executes all registered validators against the current value of the component.
     * If any of the validators fail, a {@link ValidationException} is thrown to indicate
     * the failure. The exact behavior and order of validation depend on the validators added.
     *
     * @throws ValidationException if validation fails for one or more validators
     */
    void executeValidators() throws ValidationException;

    @Nullable
    @Override
    default String getErrorMessage() {
        return getElement().getProperty(PROPERTY_ERROR_MESSAGE);
    }

    @Override
    default void setErrorMessage(@Nullable String errorMessage) {
        getElement().setProperty(PROPERTY_ERROR_MESSAGE, Strings.nullToEmpty(errorMessage));
    }

    /**
     * Sets invalid state to the field considering result of field validation.
     * <ul>
     *     <li>Invalid - false and validation passed - false = field is invalid</li>
     *     <li>Invalid - false and validation passed - true  = field is valid</li>
     *     <li>Invalid - true  and validation passed - false = field is invalid</li>
     *     <li>Invalid - true  and validation passed - true  = field is invalid</li>
     * </ul>
     *
     * @param invalid whether field should be invalid
     */
    @Override
    void setInvalid(boolean invalid);
}
