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

import static io.jmix.ui.components.validation.ValidatorHelper.getNumberConstraint;

/**
 * Max validator checks that value must be less than or equal to the specified maximum.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' and '$max' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *    &lt;bean id="cuba_MaxValidator" class="io.jmix.ui.components.validation.MaxValidator" scope="prototype"/&gt;
 *    </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> BigDecimal, BigInteger, Long, Integer
 */
@Component(MaxValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MaxValidator<T extends Number> extends AbstractValidator<T> {

    public static final String NAME = "cuba_MaxValidator";

    protected long max;

    public MaxValidator(long max) {
        this.max = max;
    }

    /**
     * Constructor for custom error message. This message can contain '$value' and '$max' keys for formatted output.
     * <p>
     * Example: "Value '$value' should be less than or equal to '$max'".
     *
     * @param max     max value
     * @param message error message
     */
    public MaxValidator(long max, String message) {
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
    public void setMax(long max) {
        this.max = max;
    }

    /**
     * @return max value
     */
    public long getMax() {
        return max;
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
            throw new IllegalArgumentException("MaxValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isMax(max)) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.max");
            }

            String formattedValue = formatValue(value);
            String formattedMax = formatValue(max);
            throw new ValidationException(getTemplateErrorMessage(message, ParamsMap.of("value", formattedValue, "max", formattedMax)));
        }
    }
}
