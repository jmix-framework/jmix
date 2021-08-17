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
 * Double validator checks that value must be greater than or equal to the specified minimum.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' and '${min}' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_DoubleMinValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected DoubleMinValidator doubleMinValidator(Double min) {
 *          return new CustomDoubleMinValidator(min);
 *     }
 * </pre>
*
 * @param <T> Double and String that represents BigDouble value with current locale
 */
@StudioElement(
        caption = "DoubleMinValidator",
        xmlElement = "doubleMin",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.MaskedField",
                "io.jmix.ui.component.TextArea", "io.jmix.ui.component.TextField"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_DoubleMinValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DoubleMinValidator<T> extends AbstractValidator<T> {

    protected Double min;
    protected boolean inclusive = true;

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
     * Sets min value.
     *
     * @param min min value
     */
    @StudioProperty(name = "value", required = true)
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * @return min value
     */
    public Double getMin() {
        return min;
    }

    /**
     * Sets min value and inclusive option.
     *
     * @param min       min value
     * @param inclusive inclusive option
     */
    public void setMin(Double min, boolean inclusive) {
        this.min = min;
        this.inclusive = inclusive;
    }

    /**
     * Set to true if the value must be greater than or equal to the specified minimum. Default value is true.
     *
     * @param inclusive inclusive option
     */
    @StudioProperty
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
            throw new IllegalArgumentException("DoubleMinValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDoubleMin(min, inclusive)) {
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
