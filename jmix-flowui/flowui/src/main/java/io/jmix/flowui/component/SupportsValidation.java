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
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;

import javax.annotation.Nullable;

public interface SupportsValidation<V> extends HasValidation, HasElement {

    String PROPERTY_ERROR_MESSAGE = "errorMessage";

    Registration addValidator(Validator<? super V> validator);

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
     *     <li>Invalid - false and validation passed - false = field is valid</li>
     *     <li>Invalid - false and validation passed - true  = field is valid</li>
     *     <li>Invalid - true  and validation passed - false = field is invalid</li>
     *     <li>Invalid - true  and validation passed - true  = field is valid</li>
     * </ul>
     *
     * @param invalid whether field should be invalid
     */
    @Override
    void setInvalid(boolean invalid);
}
