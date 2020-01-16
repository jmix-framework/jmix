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

package io.jmix.ui.components.validation;

import io.jmix.core.commons.util.ParamsMap;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import io.jmix.core.security.UserSessionSource;
import io.jmix.ui.components.ValidationException;
import io.jmix.ui.components.validation.numbers.NumberConstraint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import static io.jmix.ui.components.validation.ValidatorHelper.getNumberConstraint;

/**
 * DoubleMax validator checks that value must be greater than or equal to the specified maximum.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' and '$max' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *   &lt;bean id="cuba_DoubleMaxValidator" class="io.jmix.ui.components.validation.DoubleMaxValidator" scope="prototype"/&gt;
 *   </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> Double and String that represents Double value with current locale
 */
@Component(DoubleMaxValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DoubleMaxValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "cuba_DoubleMaxValidator";

    protected Double max;
    protected boolean inclusive = true;

    /**
     * Constructor with default error message.
     *
     * @param max max value
     */
    public DoubleMaxValidator(Double max) {
        this.max = max;
    }

    /**
     * Constructor with custom error message. This message can contain '$value', and '$max' keys for formatted output.
     * <p>
     * Example: "Value '$value' should be greater than or equal to '$max'".
     *
     * @param max     max value
     * @param message error message
     */
    public DoubleMaxValidator(Double max, String message) {
        this.max = max;
        this.message = message;
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Inject
    protected void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Inject
    protected void setUserSessionSource(UserSessionSource userSessionSource) {
        this.userSessionSource = userSessionSource;
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
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = null;

        if (value instanceof Number) {
            constraint = getNumberConstraint((Number) value);
        } else if (value instanceof String) {
            try {
                Datatype datatype = datatypeRegistry.getNN(Double.class);
                Locale locale = userSessionSource.getUserSession().getLocale();
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
            throw new IllegalArgumentException("DoubleMaxValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDoubleMax(max, inclusive)) {
            fireValidationException(value);
        }
    }

    protected String getDefaultMessage() {
        return inclusive ?
                messages.getMessage("validation.constraints.decimalMaxInclusive")
                : messages.getMessage("validation.constraints.decimalMax");
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        String formattedValue = formatValue(value);
        String formattedMax = formatValue(max);

        String formattedMessage = getTemplateErrorMessage(
                message == null ? getDefaultMessage() : message,
                ParamsMap.of("value", formattedValue, "max", formattedMax));

        throw new ValidationException(formattedMessage);
    }
}
