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

import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Regexp validator checks that String value is matched with specified regular expression.
 * <p>
 * The regular expression follows the Java regular expression conventions.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("flowui_RegexpValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected RegexpValidator regexpValidator(String regexp) {
 *          return new CustomRegexpValidator(regexp);
 *     }
 * </pre>
 *
 * @see Pattern
 */
@Component("flowui_RegexpValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RegexpValidator extends AbstractValidator<String> implements InitializingBean {

    protected Pattern pattern;

    public RegexpValidator(String regexp) {
        Preconditions.checkNotNullArgument(regexp);

        this.pattern = Pattern.compile(regexp);
    }

    /**
     * Constructor for regexp value and custom error message. This message can contain '${value}' key for formatted output.
     * Example: "Invalid value '${value}'".
     *
     * @param regexp  regular expression
     * @param message error message
     */
    public RegexpValidator(String regexp, String message) {
        Preconditions.checkNotNullArgument(regexp);

        this.message = message;
        this.pattern = Pattern.compile(regexp);
    }

    /**
     * Sets regexp pattern value.
     *
     * @param regexp a regexp pattern value
     */
    public void setRegexp(String regexp) {
        this.pattern = Pattern.compile(regexp);
    }

    /**
     * @return a regexp pattern value
     */
    public String getRegexp() {
        return pattern.pattern();
    }

    @Override
    public void accept(@Nullable String value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        if (!pattern.matcher((value)).matches()) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.regexp");

            fireValidationException(
                    message == null ? defaultMessage : message,
                    ParamsMap.of("value", value));
        }
    }
}
