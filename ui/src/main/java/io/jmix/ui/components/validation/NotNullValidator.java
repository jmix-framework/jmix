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

/**
 * NotNull validator checks that value is not null.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="cuba_NotNullValidator" class="io.jmix.ui.components.validation.NotNullValidator" scope="prototype"/&gt;
 *     </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> value type
 */
@Component(NotNullValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotNullValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "cuba_NotNullValidator";

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

    @Inject
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
