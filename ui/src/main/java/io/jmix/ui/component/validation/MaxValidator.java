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
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.substitutor.StringSubstitutor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.ui.component.validation.ValidatorHelper.getNumberConstraint;

/**
 * Max validator checks that value must be less than or equal to the specified maximum.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' and '${max}' keys for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_MaxValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected MaxValidator maxValidator(long max) {
 *          return new CustomMaxValidator(max);
 *     }
 * </pre>
*
 * @param <T> BigDecimal, BigInteger, Long, Integer
 */
@StudioElement(
        caption = "MaxValidator",
        xmlElement = "max",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.MaskedField",
                "io.jmix.ui.component.TextArea", "io.jmix.ui.component.TextField"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_MaxValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MaxValidator<T extends Number> extends AbstractValidator<T> {

    protected long max;

    public MaxValidator(long max) {
        this.max = max;
    }

    /**
     * Constructor for custom error message. This message can contain '${value}' and '${max}' keys for formatted output.
     * <p>
     * Example: "Value '${value}' should be less than or equal to '${max}'".
     *
     * @param max     max value
     * @param message error message
     */
    public MaxValidator(long max, String message) {
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
