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
import io.jmix.ui.substitutor.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.jmix.ui.component.validation.ValidatorHelper.getNumberConstraint;

/**
 * Negative validator checks that value should be a strictly less than 0.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_NegativeValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected NegativeValidator negativeValidator() {
 *          return new CustomNegativeValidator();
 *     }
 * </pre>
 *
 * @param <T> BigDecimal, BigInteger, Long, Integer, Double, Float
 */
@StudioElement(
        caption = "NegativeValidator",
        xmlElement = "negative",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.MaskedField",
                "io.jmix.ui.component.TextArea", "io.jmix.ui.component.TextField"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_NegativeValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NegativeValidator<T extends Number> extends AbstractValidator<T> {

    public NegativeValidator() {
    }

    /**
     * Constructor for custom error message. This message can contain '${value}' key for formatted output.
     * <p>
     * Example: "Value '${value}' should be less than 0".
     *
     * @param message error message
     */
    public NegativeValidator(String message) {
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

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = getNumberConstraint(value);
        if (constraint == null) {
            throw new IllegalArgumentException("NegativeValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isNegative()) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.negative");
            }

            String formattedValue = formatValue(value);
            throw new ValidationException(getTemplateErrorMessage(message, ParamsMap.of("value", formattedValue)));
        }
    }
}
