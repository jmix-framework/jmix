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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("flowui_NotBlankValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotBlankValidator extends AbstractValidator<String> implements InitializingBean {

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

    @Override
    public void accept(@Nullable String value) throws ValidationException {
        if (StringUtils.isBlank(value)) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.notBlank");

            fireValidationException(message == null ? defaultMessage : message);
        }
    }
}
