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
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.meta.StudioElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * NotBlank validator checks that value contains at least one non-whitespace character.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_NotBlankValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected NotBlankValidator notBlankValidator() {
 *          return new CustomNotBlankValidator();
 *     }
 * </pre>
 */
@StudioElement(
        caption = "NotBlankValidator",
        xmlElement = "notBlank",
        target = {"io.jmix.ui.component.ComboBox", "io.jmix.ui.component.TextInputField",
                "io.jmix.ui.component.SourceCodeEditor"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_NotBlankValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotBlankValidator extends AbstractValidator<String> {

    public NotBlankValidator() {
    }

    /**
     * Constructor for custom error message.
     *
     * @param message error message
     */
    public NotBlankValidator(String message) {
        this.message = message;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(String value) throws ValidationException {
        if (StringUtils.isBlank(value)) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.notBlank");
            }

            throw new ValidationException(message);
        }
    }
}
