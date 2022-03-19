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

package io.jmix.ui.component.validation;


import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.number.NumberConstraint;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.substitutor.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import static io.jmix.ui.component.validation.ValidatorHelper.getNumberConstraint;

/**
 * DecimalMax validator checks that value must be less than or equal to the specified maximum.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' and '${max}' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_DecimalMaxValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected DecimalMaxValidator decimalMaxValidator(BigDecimal max) {
 *          return new CustomDecimalMaxValidator(max);
 *     }
 * </pre>
 *
 * @param <T> BigDecimal, BigInteger, Long, Integer and String that represents BigDecimal value with current locale
 */
@StudioElement(
        caption = "DecimalMaxValidator",
        xmlElement = "decimalMax",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.MaskedField",
                "io.jmix.ui.component.TextArea", "io.jmix.ui.component.TextField"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_DecimalMaxValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DecimalMaxValidator<T> extends AbstractValidator<T> {

    protected BigDecimal max;
    protected boolean inclusive = true;

    /**
     * Constructor with default error message.
     *
     * @param max max value
     */
    public DecimalMaxValidator(BigDecimal max) {
        this.max = max;
    }

    /**
     * Constructor with custom error message. This message can contain '${value}', and '${max}' keys for formatted output.
     * <p>
     * Example: "Value '${value}' should be less than or equal to '${max}'".
     *
     * @param max     max value
     * @param message error message
     */
    public DecimalMaxValidator(BigDecimal max, String message) {
        this.max = max;
        this.message = message;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    protected void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setStringSubstitutor(StringSubstitutor substitutor) {
        this.substitutor = substitutor;
    }

    /**
     * Sets max value.
     *
     * @param max max value
     */
    @StudioProperty(name = "value", required = true)
    public void setMax(BigDecimal max) {
        this.max = max;
    }

    /**
     * @return max value
     */
    public BigDecimal getMax() {
        return max;
    }

    /**
     * Sets max value and inclusive option.
     *
     * @param max       max value
     * @param inclusive inclusive option
     */
    public void setMax(BigDecimal max, boolean inclusive) {
        this.max = max;
        this.inclusive = inclusive;
    }

    /**
     * Set to true if the value must be less than or equal to the specified maximum. Default value is true.
     *
     * @param inclusive inclusive option
     */
    @StudioProperty
    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    /**
     * @return true if the value must be less than or equal to the specified maximum
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
                Datatype datatype = datatypeRegistry.get(BigDecimal.class);
                Locale locale = currentAuthentication.getLocale();
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
            throw new IllegalArgumentException("DecimalMaxValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDecimalMax(max, inclusive)) {
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
