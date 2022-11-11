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

package io.jmix.securityflowui.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs all existing {@link PasswordValidator}s and returns the list of validation errors.
 *
 * @see #validate(Object, String)
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Component("sec_PasswordValidation")
public class PasswordValidation {

    protected List<PasswordValidator> passwordValidators;

    @Autowired(required = false)
    public void setPasswordValidators(List<PasswordValidator> passwordValidators) {
        this.passwordValidators = passwordValidators;
    }

    /**
     * Runs all existing {@link PasswordValidator}s.
     *
     * @param user user entity
     * @param password password to validate
     * @return list of validation error messages, or empty list if there were no errors
     */
    public List<String> validate(Object user, String password) {
        List<String> errors = new ArrayList<>();
        if (passwordValidators != null) {
            for (PasswordValidator passwordValidator : passwordValidators) {
                try {
                    passwordValidator.validate(new PasswordValidationContext(user, password));
                } catch (PasswordValidationException e) {
                    errors.add(e.getMessage());
                }
            }
        }
        return errors;
    }
}
