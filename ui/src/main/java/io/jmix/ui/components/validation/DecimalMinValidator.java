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

import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import io.jmix.core.commons.util.ParamsMap;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
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
 * DecimalMin validator checks that value must be greater than or equal to the specified minimum.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' and '$min' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *   &lt;bean id="cuba_DecimalMinValidator" class="io.jmix.ui.components.validation.DecimalMinValidator" scope="prototype"/&gt;
 *   </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> BigDecimal, BigInteger, Long, Integer and String that represents BigDecimal value with current locale
 */
@Component(DecimalMinValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DecimalMinValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "cuba_DecimalMinValidator";

    protected BigDecimal min;
    protected boolean inclusive = true;

    /**
     * Constructor with default error message.
     *
     * @param min min value
     */
    public DecimalMinValidator(BigDecimal min) {
        this.min = min;
    }

    /**
     * Constructor with custom error message. This message can contain '$value', and '$min' keys for formatted output.
     * <p>
     * Example: "Value '$value' should be greater than or equal to '$min'".
     *
     * @param min     min value
     * @param message error message
     */
    public DecimalMinValidator(BigDecimal min, String message) {
        this.min = min;
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
     * Sets min value.
     *
     * @param min min value
     */
    public void setMin(BigDecimal min) {
        this.min = min;
    }

    /**
     * @return min value
     */
    public BigDecimal getMin() {
        return min;
    }

    /**
     * Sets min value and inclusive option.
     *
     * @param min       min value
     * @param inclusive inclusive option
     */
    public void setMin(BigDecimal min, boolean inclusive) {
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
                Datatype datatype = datatypeRegistry.getNN(BigDecimal.class);
                Locale locale = userSessionSource.getUserSession().getLocale();
                BigDecimal bigDecimal = (BigDecimal) datatype.parse((String) value, locale);
                if (bigDecimal == null) {
                    fireValidationException(value);
                }
                constraint = getNumberConstraint(bigDecimal);
            } catch (ParseException e) {
                throw new ValidationException(e.getLocalizedMessage());
            }
        }

        if (constraint == null
                || value instanceof Double
                || value instanceof Float) {
            throw new IllegalArgumentException("DecimalMinValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDecimalMin(min, inclusive)) {
            fireValidationException(value);
        }
    }

    protected String getDefaultMessage() {
        return inclusive ?
                messages.getMessage("validation.constraints.decimalMinInclusive")
                : messages.getMessage("validation.constraints.decimalMin");
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        String formattedValue = formatValue(value);
        String formattedMin = formatValue(min);

        String formattedMessage = getTemplateErrorMessage(
                message == null ? getDefaultMessage() : message,
                ParamsMap.of("value", formattedValue, "min", formattedMin));

        throw new ValidationException(formattedMessage);
    }
}
