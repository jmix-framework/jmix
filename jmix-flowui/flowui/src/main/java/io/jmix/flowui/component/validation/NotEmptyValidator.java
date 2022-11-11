/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.validation;

import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;

@Component("flowui_NotEmptyValidator")
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

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // null value is not valid
        if (value == null
                || (value instanceof Collection && ((Collection<?>) value).isEmpty())
                || (value instanceof String) && ((String) value).isEmpty()) {

            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.notEmpty");

            fireValidationException(message == null ? defaultMessage : message);
        }
    }
}
