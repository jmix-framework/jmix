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
package com.haulmont.cuba.gui.components.validators;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Field;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.ValidationException;
import org.dom4j.Element;
import org.hibernate.validator.constraints.Email;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Use {@link io.jmix.ui.component.validation.EmailValidator} instead
 */
@Deprecated
public class EmailValidator implements Field.Validator {

    protected String message;
    protected String messagesPack;
    protected Messages messages;
    protected MessageTools messageTools;

    protected Validator validator;

    public EmailValidator() {
        messages = AppBeans.get(Messages.class);
        messageTools = AppBeans.get(MessageTools.class);
        validator = AppBeans.get(Validator.class);
    }

    public EmailValidator(Element element, String messagesPack) {
        this();
        message = element.attributeValue("message");
        this.messagesPack = messagesPack;
    }

    /**
     * INTERNAL. Used in tests.
     */
    @Internal
    protected EmailValidator(Messages messages, MessageTools messageTools, Validator validator) {
        this.messages = messages;
        this.messageTools = messageTools;
        this.validator = validator;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null) {
            return;
        }

        List<String> emails = collectEmails((String) value);
        if (emails.isEmpty()) {
            return;
        }

        for (String email : emails) {
            boolean valid = validator.validateValue(EmailValidationPojo.class, "email", email).isEmpty();
            if (!valid) {
                String msg = message != null ?
                        messageTools.loadString(messagesPack, message)
                        : null;

                if (msg == null) {
                    msg = messages.getMessage("validation.invalidEmail"); //todo vm mainmessages
                }

                throw new ValidationException(String.format(msg, value));
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
