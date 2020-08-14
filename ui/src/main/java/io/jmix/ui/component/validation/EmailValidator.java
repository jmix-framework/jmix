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


import io.jmix.core.BeanValidation;
import io.jmix.core.Messages;
import io.jmix.ui.component.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.Validator;
import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

/**
 * Email validator checks that String value is email or contains multiple emails separated by a semicolon or comma.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="ui_EmailValidator" class="io.jmix.ui.component.validation.EmailValidator" scope="prototype"/&gt;
 *     </pre>
*/
@Component(EmailValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailValidator extends AbstractValidator<String> {

    public static final String NAME = "ui_EmailValidator";

    protected Validator validator;

    public EmailValidator() {
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    protected void setValidator(BeanValidation beanValidation) {
        validator = beanValidation.getValidator();
    }

    @Override
    public void accept(String value) {
        if (value == null) {
            return;
        }

        List<String> emails = collectEmails(value);
        if (emails.isEmpty()) {
            return;
        }

        for (String email : emails) {
            boolean valid = validator.validateValue(EmailValidationPojo.class, "email", email).isEmpty();
            if (!valid) {
                if (message == null) {
                    message = messages.getMessage("validation.invalidEmail");
                }

                throw new ValidationException(String.format(message, value));
            }
        }
    }

    protected List<String> collectEmails(String emailString) {
        List<String> emails = new ArrayList<>();

        int sepIdx = getSepIdx(emailString);
        while (sepIdx > 0) {
            String email = emailString.substring(0, sepIdx).trim();
            emails.add(preventEmpty(email));

            emailString = emailString.substring(sepIdx + 1);
            sepIdx = getSepIdx(emailString);
        }

        emails.add(emailString.trim());

        return emails;
    }

    protected String preventEmpty(String s) {
        // make validator fall on blank emails for rejecting trailing separators
        return s.isEmpty() ? " " : s;
    }

    protected int getSepIdx(String emailString) {
        int semicolonIdx = emailString.indexOf(';');
        int commaIdx = emailString.indexOf(',');

        if (semicolonIdx < 0 && commaIdx < 0) {
            return -1;
        }
        if (semicolonIdx >= 0 && commaIdx >= 0) {
            return Math.min(semicolonIdx, commaIdx);
        }

        return semicolonIdx != -1 ? semicolonIdx : commaIdx;
    }

    protected static class EmailValidationPojo {

        @Email
        private final String email;

        public EmailValidationPojo(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }
}
