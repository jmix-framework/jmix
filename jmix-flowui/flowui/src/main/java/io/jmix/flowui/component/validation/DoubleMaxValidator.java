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

import org.springframework.lang.Nullable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import static io.jmix.flowui.component.validation.ValidatorHelper.getNumberConstraint;

@Component("flowui_DoubleMaxValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DoubleMaxValidator<T> extends AbstractValidator<T> implements InitializingBean {

    protected Double max = Double.MAX_VALUE;
    protected boolean inclusive = true;

    public DoubleMaxValidator() {
    }

    public DoubleMaxValidator(Double max) {
        this.max = max;
    }

    /**
     * Constructor with custom error message. This message can contain '${value}', and '${max}' keys for formatted output.
     * <p>
     * Example: "Value '${value}' should be greater than or equal to '${max}'".
     *
     * @param max     max value
     * @param message error message
     */
    public DoubleMaxValidator(Double max, String message) {
        this.max = max;
        this.message = message;
    }

    /**
     * Sets max value.
     *
     * @param max max value
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * @return max value
     */
    public Double getMax() {
        return max;
    }

    /**
     * Sets max value and inclusive option.
     *
     * @param max       max value
     * @param inclusive inclusive option
     */
    public void setMax(Double max, boolean inclusive) {
        this.max = max;
        this.inclusive = inclusive;
    }

    /**
     * Set to true if the value must be greater than or equal to the specified maximum. Default value is true.
     *
     * @param inclusive inclusive option
     */
    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    /**
     * @return true if the value must be greater than or equal to the specified maximum
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
                    "DoubleMaxValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDoubleMax(max, inclusive)) {
            fireValidationException(value);
        }
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        this.defaultMessage = inclusive ?
                messages.getMessage("validation.constraints.decimalMaxInclusive")
                : messages.getMessage("validation.constraints.decimalMax");

        String formattedValue = formatValue(value);
        String formattedMax = formatValue(max);

        fireValidationException(
                message == null ? defaultMessage : message,
                ParamsMap.of("value", formattedValue, "max", formattedMax));
    }
}
