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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * NotEmpty validator checks that value is not null and not empty.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' keys for formatted output.
 * <p>
 * Note, that size validator for Collection doesn't use key 'value' for output error message.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_NotEmptyValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected NotEmptyValidator notEmptyValidator() {
 *          return new CustomNotEmptyValidator();
 *     }
 * </pre>
 *
 * @param <T> Collection or String
 */
@StudioElement(
        caption = "NotEmptyValidator",
        xmlElement = "notEmpty",
        target = {"io.jmix.ui.component.OptionsField", "io.jmix.ui.component.TextInputField",
                "io.jmix.ui.component.SourceCodeEditor", "io.jmix.ui.component.TagField",
                "io.jmix.ui.component.ValuesPicker"},
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_NotEmptyValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotEmptyValidator<T> extends AbstractValidator<T> {

    public NotEmptyValidator() {
    }

    /**
     * Constructor for custom error message.
     *
     * @param message error message
     */
    public NotEmptyValidator(String message) {
        this.message = message;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // null value is not valid
        if (value == null
                || (value instanceof Collection && ((Collection) value).isEmpty())
                || (value instanceof String) && ((String) value).isEmpty()) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.notEmpty");
            }

            throw new ValidationException(message);
        }
    }
}
