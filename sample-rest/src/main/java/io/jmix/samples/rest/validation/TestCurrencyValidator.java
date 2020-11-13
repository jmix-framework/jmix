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

package io.jmix.samples.rest.validation;

import io.jmix.core.EntityStates;
import io.jmix.samples.rest.entity.driver.Currency;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TestCurrencyValidator implements ConstraintValidator<TestCurrencyClassConstraint, Currency> {
    @Inject
    protected EntityStates entityStates;

    @Override
    public boolean isValid(Currency currency, ConstraintValidatorContext context) {
        if (entityStates.isLoaded(currency, "code")) {
            if ("BAN".equals(currency.getCode())) {
                return false;
            }
        }
        return true;
    }
}
