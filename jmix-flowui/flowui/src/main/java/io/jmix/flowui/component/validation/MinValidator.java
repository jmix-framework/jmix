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

@Component("flowui_MinValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MinValidator<T extends Number> extends AbstractValidator<T> implements InitializingBean {

    protected int min;

    protected String defaultMessage;

    public MinValidator() {
    }

    public MinValidator(int min) {
        this.min = min;
    }

    /**
     * Constructor with custom error message. This message can contain '${value}' and '${min}' keys for formatted output.
     * <p>
     * Example: "Value '${value}' should be greater than or equal to '${min}'".
     *
     * @param min     min value
     * @param message error message
     */
    public MinValidator(int min, String message) {
        this.min = min;
        this.message = message;
    }

    /**
     * Sets min value of the range. Min value cannot be less than 0. Default value is 0.
     * <p>
     * Note, min value is included in range. Examples:
     * <pre>{@code
     *  value = 0, min = 0 - is valid
     *  value = 1, min = 2 - is not valid
     * }
     * </pre>
     *
     * @param min min value
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * @return min value
     */
    public long getMin() {
        return min;
    }

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = getNumberConstraint(value);
        if (constraint == null
                || value instanceof Double
                || value instanceof Float) {
            throw new IllegalArgumentException(
                    "MinValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isMin(min)) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.min");

            String formattedValue = formatValue(value);
            String formattedMin = formatValue(min);
            fireValidationException(
                    message == null ? defaultMessage : message,
                    ParamsMap.of("value", formattedValue,
                            "min", formattedMin));
        }
    }
}
