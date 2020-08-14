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

import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.common.util.Preconditions;

import io.jmix.core.Messages;
import io.jmix.ui.component.ValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.regex.Pattern;

/**
 * Regexp validator checks that String value is matched with specified regular expression.
 * <p>
 * The regular expression follows the Java regular expression conventions.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="ui_RegexpValidator" class="io.jmix.ui.component.validation.RegexpValidator" scope="prototype"/&gt;
 *     </pre>
*
 * @see Pattern
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(RegexpValidator.NAME)
public class RegexpValidator extends AbstractValidator<String> {

    public static final String NAME = "ui_RegexpValidator";

    protected Pattern pattern;

    public RegexpValidator(String regexp) {
        Preconditions.checkNotNullArgument(regexp);

        this.pattern = Pattern.compile(regexp);
    }

    /**
     * Constructor for regexp value and custom error message. This message can contain '$value' key for formatted output.
     * Example: "Invalid value '$value'".
     *
     * @param regexp  regular expression
     * @param message error message
     */
    public RegexpValidator(String regexp, String message) {
        Preconditions.checkNotNullArgument(regexp);

        this.message = message;
        this.pattern = Pattern.compile(regexp);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(String value) throws ValidationException {
        if (value == null) {
            return;
        }

        if (!pattern.matcher((value)).matches()) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.regexp");
            }

            throw new ValidationException(getTemplateErrorMessage(message, ParamsMap.of("value", value)));
        }
    }
}
