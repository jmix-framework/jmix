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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import static io.jmix.ui.component.validation.ValidatorHelper.getNumberConstraint;

/**
 * DoubleMax validator checks that value must be greater than or equal to the specified maximum.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' and '${max}' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_DoubleMaxValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected DoubleMaxValidator doubleMaxValidator(Double max) {
 *          return new CustomDoubleMaxValidator(max);
 *     }
 * </pre>
*
 * @param <T> Double and String that represents Double value with current locale
 */
@StudioElement(
        caption = "DoubleMaxValidator",
        xmlElement = "doubleMax",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.MaskedField",
                "io.jmix.ui.component.TextArea", "io.jmix.ui.component.TextField"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_DoubleMaxValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DoubleMaxValidator<T> extends AbstractValidator<T> {

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
    @StudioProperty
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
                Datatype datatype = datatypeRegistry.get(Double.class);
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
