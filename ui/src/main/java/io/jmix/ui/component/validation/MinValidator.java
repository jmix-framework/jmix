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
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.number.NumberConstraint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.ui.component.validation.ValidatorHelper.getNumberConstraint;

/**
 * Min validator checks that value must be greater than or equal to the specified minimum.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' and '$min' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *    &lt;bean id="MinValidator" class="io.jmix.ui.component.validation.MinValidator" scope="prototype"/&gt;
 *    </pre>
*
 * @param <T> BigDecimal, BigInteger, Long, Integer
 */
@Component(MinValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MinValidator<T extends Number> extends AbstractValidator<T> {

    public static final String NAME = "ui_MinValidator";

    protected long min;

    public MinValidator(long min) {
        this.min = min;
    }

    /**
     * Constructor with custom error message. This message can contain '$value' and '$min' keys for formatted output.
     * <p>
     * Example: "Value '$value' should be greater than or equal to '$min'".
     *
     * @param min     min value
     * @param message error message
     */
    public MinValidator(long min, String message) {
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

    /**
     * Sets min value.
     *
     * @param min min value
     */
    public void setMin(long min) {
        this.min = min;
    }

    /**
     * @return min value
     */
    public long getMin() {
        return min;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = getNumberConstraint(value);
        if (constraint == null
                || value instanceof Double
                || value instanceof Float) {
            throw new IllegalArgumentException("MinValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isMin(min)) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.min");
            }

            String formattedValue = formatValue(value);
            String formattedMin = formatValue(min);
            throw new ValidationException(getTemplateErrorMessage(message, ParamsMap.of("value", formattedValue, "min", formattedMin)));
        }
    }
}
