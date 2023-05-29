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

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Field;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.*;
import org.dom4j.Element;

import java.text.ParseException;
import java.util.Objects;

/**
 * @deprecated Use {@link MaxValidator}, {@link MinValidator}, {@link NegativeOrZeroValidator},
 * {@link NegativeValidator}, {@link PositiveOrZeroValidator} or {@link PositiveValidator} instead
 */
@Deprecated
public class IntegerValidator implements Field.Validator {

    protected String message;
    protected String messagesPack;
    protected String onlyPositive;
    protected Messages messages = AppBeans.get(Messages.class);
    protected MessageTools messageTools = AppBeans.get(MessageTools.class);

    public IntegerValidator(Element element, String messagesPack) {
        message = element.attributeValue("message");
        onlyPositive = element.attributeValue("onlyPositive");
        this.messagesPack = messagesPack;
    }

    public IntegerValidator(String message) {
        this.message = message;
    }

    public IntegerValidator() {
        this.message = messages.getMessage("validation.invalidNumber");
    }

    private boolean checkIntegerOnPositive(Integer value) {
        return !Objects.equals("true", onlyPositive) || value >= 0;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null) {
            return;
        }

        boolean result;
        if (value instanceof String) {
            try {
                Datatype<Integer> datatype = Datatypes.getNN(Integer.class);
                CurrentAuthentication currentAuthentication = AppBeans.get(CurrentAuthentication.class);
                Integer num = datatype.parse((String) value, currentAuthentication.getLocale());
                result = checkIntegerOnPositive(num);
            } catch (ParseException e) {
                result = false;
            }
        } else {
            result = value instanceof Integer && checkIntegerOnPositive((Integer) value);
        }
        if (!result) {
            String msg = message != null ? messageTools.loadString(messagesPack, message) : "Invalid value '%s'";
            throw new ValidationException(String.format(msg, value));
        }
    }
}