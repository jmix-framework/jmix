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

@Component("flowui_MaxValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MaxValidator<T extends Number> extends AbstractValidator<T> implements InitializingBean {

    protected int max = Integer.MAX_VALUE;

    protected String defaultMessage;

    public MaxValidator() {
    }

    public MaxValidator(int max) {
        this.max = max;
    }

    /**
     * Constructor with custom error message. This message can contain '${value}' and '${min}' keys for formatted output.
     * <p>
     * Example: "Value '${value}' should be greater than or equal to '${min}'".
     *
     * @param max     min value
     * @param message error message
     */
    public MaxValidator(int max, String message) {
        this.max = max;
        this.message = message;
    }

    /**
     * Sets max value of the range. Max value cannot be less than 0.  Default value is {@link Integer#MAX_VALUE}.
     * <p>
     * Note, max value is included in range. Examples:
     * <pre>{@code
     *  value = 5, max = 5 - is valid
     *  value = 6, max = 5 - is not valid
     * }
     * </pre>
     *
     * @param max max value
     */
    public void setMin(int max) {
        this.max = max;
    }

    /**
     * @return max value
     */
    public long getMax() {
        return max;
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
                    "MaxValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isMax(max)) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.max");

            String formattedValue = formatValue(value);
            String formattedMax = formatValue(max);
            fireValidationException(
                    message == null ? defaultMessage : message,
                    ParamsMap.of("value", formattedValue,
                            "max", formattedMax));
        }
    }
}
