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
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.component.validation.number.NumberConstraint;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import static io.jmix.flowui.component.validation.ValidatorHelper.getNumberConstraint;

@Component("flowui_DoubleMinValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DoubleMinValidator<T> extends AbstractValidator<T> implements InitializingBean {

    protected Double min;
    protected boolean inclusive = true;

    public DoubleMinValidator() {
    }

    /**
     * Constructor with default error message.
     *
     * @param min min value
     */
    public DoubleMinValidator(Double min) {
        this.min = min;
    }

    /**
     * Constructor with custom error message. This message can contain '${value}', and '${min}' keys for formatted output.
     * <p>
     * Example: "Value '${value}' should be greater than or equal to '${min}'".
     *
     * @param min     min value
     * @param message error message
     */
    public DoubleMinValidator(Double min, String message) {
        this.min = min;
        this.message = message;
    }

    /**
     * Sets min value.
     *
     * @param min min value
     */
    public void setMin(@Nullable Double min) {
        this.min = min;
    }

    /**
     * @return min value
     */
    @Nullable
    public Double getMin() {
        return min;
    }

    /**
     * Sets min value and inclusive option.
     *
     * @param min       min value
     * @param inclusive inclusive option
     */
    public void setMin(@Nullable Double min, boolean inclusive) {
        this.min = min;
        this.inclusive = inclusive;
    }

    /**
     * Set to true if the value must be greater than or equal to the specified minimum. Default value is true.
     *
     * @param inclusive inclusive option
     */
    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    /**
     * @return true if the value must be greater than or equal to the specified minimum
     */
    public boolean isInclusive() {
        return inclusive;
    }

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = null;

        if (value instanceof Number) {
            constraint = getNumberConstraint((Number) value);
        } else if (value instanceof String) {
            try {
                Datatype<?> datatype = datatypeRegistry.get(Double.class);
                Locale locale = currentAuthentication.getLocale();
                Double num = (Double) datatype.parse((String) value, locale);
                if (num == null) {
                    fireValidationException(value);
                }
                constraint = getNumberConstraint(num);
            } catch (ParseException e) {
                throw new ValidationException(e.getLocalizedMessage());
            }
        }

        if (constraint == null
                || value instanceof BigDecimal
                || value instanceof Float) {
            throw new IllegalArgumentException(
                    "DoubleMinValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDoubleMin(min, inclusive)) {
            fireValidationException(value);
        }
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        this.defaultMessage = inclusive ?
                messages.getMessage("validation.constraints.decimalMinInclusive")
                : messages.getMessage("validation.constraints.decimalMin");

        String formattedValue = formatValue(value);
        String formattedMin = formatValue(min);

        fireValidationException(
                message == null ? defaultMessage : message,
                ParamsMap.of("value", formattedValue, "min", formattedMin));
    }
}
