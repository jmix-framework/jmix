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

/**
 * NotNull validator checks that value is not null.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_NotNullValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected NotNullValidator notNullValidator() {
 *          return new CustomNotNullValidator();
 *     }
 * </pre>
 *
 * @param <T> value type
 */
@StudioElement(
        caption = "NotNullValidator",
        xmlElement = "notNull",
        unsupportedTarget = {"io.jmix.ui.component.CheckBox", "io.jmix.ui.component.ColorPicker",
                "io.jmix.ui.component.CurrencyField", "io.jmix.ui.component.DatePicker",
                "io.jmix.ui.component.SingleFileUploadField", "io.jmix.ui.component.Slider",
                "io.jmix.searchui.component.SearchField"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_NotNullValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotNullValidator<T> extends AbstractValidator<T> {

    public NotNullValidator() {
    }

    /**
     * Constructor for custom error message.
     *
     * @param message error message
     */
    public NotNullValidator(String message) {
        this.message = message;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(T value) throws ValidationException {
        if (value == null) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.notNull");
            }

            throw new ValidationException(message);
        }
    }
}
