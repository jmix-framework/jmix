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

import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import io.jmix.ui.components.ValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;

/**
 * NotEmpty validator checks that value is not null and not empty.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' keys for formatted output.
 * <p>
 * Note, that size validator for Collection doesn't use key 'value' for output error message.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="cuba_NotEmptyValidator" class="io.jmix.ui.components.validation.NotEmptyValidator" scope="prototype"/&gt;
 *     </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> Collection or String
 */
@Component(NotEmptyValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotEmptyValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "cuba_NotEmptyValidator";

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

    @Inject
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
