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
 * Digits validator checks that value must be a number within accepted range.
 * <p>
 * For error message it uses template string and it is possible to use '${value}', '${integer}' and '${fraction}' keys
 * for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_DigitsValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected DigitsValidator digitsValidator(int integer, int fraction) {
 *          return new CustomDigitsValidator(integer, fraction);
 *     }
 * </pre>
 *
 * @param <T> BigDecimal, BigInteger, Long, Integer and String that represents BigDecimal value with current locale
 */
@StudioElement(
        caption = "DigitsValidator",
        xmlElement = "digits",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.MaskedField",
                "io.jmix.ui.component.TextArea", "io.jmix.ui.component.TextField"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_DigitsValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DigitsValidator<T> extends AbstractValidator<T> {

    protected int integer;
    protected int fraction;

    /**
     * Constructor with default error message.
     *
     * @param integer  maximum number of integral digits
     * @param fraction maximum number of fractional digits
     */
    public DigitsValidator(int integer, int fraction) {
        this.integer = integer;
        this.fraction = fraction;
    }

    /**
     * Constructor with custom error message. This message can contain '${value}', '${integer}' and '${fraction}' keys
     * for formatted output.
     * <p>
     * Example: "Value '${value}' is out of bounds ('${integer}' digits is expected in integer part and '${fraction}' in
     * fractional part)".
     *
     * @param integer  maximum number of integral digits
     * @param fraction maximum number of fractional digits
     * @param message  error message
     */
    public DigitsValidator(int integer, int fraction, String message) {
        this.integer = integer;
        this.fraction = fraction;
        this.message = message;
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
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setStringSubstitutor(StringSubstitutor substitutor) {
        this.substitutor = substitutor;
    }

    /**
     * Sets maximum value inclusive.
     *
     * @param integer maximum number of integral digits
     */
    @StudioProperty(required = true)
    public void setInteger(int integer) {
        this.integer = integer;
    }

    /**
     * Sets maximum value inclusive.
     *
     * @param fraction maximum number of fractional digits
     */
    @StudioProperty(required = true)
    public void setFraction(int fraction) {
        this.fraction = fraction;
    }

    /**
     * @return maximum number of integral digits
     */
    public int getInteger() {
        return integer;
    }

    /**
     * @return maximum number of fractional digits
     */
    public int getFraction() {
        return fraction;
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
            throw new IllegalArgumentException("DigitsValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isDigits(integer, fraction)) {
            fireValidationException(value);
        }
    }

    protected void fireValidationException(T value) {
        String message = getMessage();
        if (message == null) {
            message = messages.getMessage("validation.constraints.digits");
        }

        String formattedValue = formatValue(value);
        String formattedMessage = getTemplateErrorMessage(message,
                ParamsMap.of("value", formattedValue,
                        "integer", integer,
                        "fraction", fraction));

        throw new ValidationException(formattedMessage);
    }
}
