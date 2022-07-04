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

package io.jmix.flowui.component.validation;

import io.jmix.core.common.util.ParamsMap;
import io.jmix.flowui.component.validation.number.NumberConstraint;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import static io.jmix.flowui.component.validation.ValidatorHelper.getNumberConstraint;

@Component("flowui_NegativeValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NegativeValidator<T extends Number> extends AbstractValidator<T> implements InitializingBean {

    protected String defaultMessage;

    public NegativeValidator() {
    }

    /**
     * Constructor for custom error message. This message can contain '${value}' key for formatted output.
     * <p>
     * Example: "Value '${value}' should be less than 0".
     *
     * @param message error message
     */
    public NegativeValidator(String message) {
        this.message = message;
    }

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = getNumberConstraint(value);
        if (constraint == null) {
            throw new IllegalArgumentException(
                    "NegativeValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isNegative()) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.negative");

            String formattedValue = formatValue(value);
            fireValidationException(
                    message == null ? defaultMessage : message,
                    ParamsMap.of("value", formattedValue));
        }
    }
}
