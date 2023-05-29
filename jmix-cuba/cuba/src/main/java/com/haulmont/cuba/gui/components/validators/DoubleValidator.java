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
import io.jmix.ui.component.validation.DoubleMaxValidator;
import io.jmix.ui.component.validation.DoubleMinValidator;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Objects;

/**
 * @deprecated Use {@link DoubleMinValidator} or {@link DoubleMaxValidator} instead
 */
@Deprecated
public class DoubleValidator implements Field.Validator {

    protected String message;
    protected String messagesPack;
    protected String onlyPositive;
    protected Messages messages = AppBeans.get(Messages.class);
    protected MessageTools messageTools = AppBeans.get(MessageTools.class);

    public DoubleValidator(Element element, String messagesPack) {
        message = element.attributeValue("message");
        onlyPositive = element.attributeValue("onlyPositive");
        this.messagesPack = messagesPack;
    }

    public DoubleValidator(String message) {
        this.message = message;
    }

    public DoubleValidator() {
        this.message = messages.getMessage("validation.invalidNumber");
    } // todo vm mainmessage

    private boolean checkDoubleOnPositive(Double value) {
        return !Objects.equals("true", onlyPositive) || value >= 0;
    }

    private boolean checkBigDecimalOnPositive(BigDecimal value) {
        return !Objects.equals("true", onlyPositive) || value.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null) {
            return;
        }

        boolean result;
        if (value instanceof String) {
            try {
                Datatype<Double> datatype = Datatypes.getNN(Double.class);
                CurrentAuthentication currentAuthentication = AppBeans.get(CurrentAuthentication.class);
                Double num = datatype.parse((String) value, currentAuthentication.getLocale());
                result = checkDoubleOnPositive(num);
            } catch (ParseException e) {
                result = false;
            }
        } else {
            result = (value instanceof Double && checkDoubleOnPositive((Double) value)) || (value instanceof BigDecimal && checkBigDecimalOnPositive((BigDecimal) value));
        }

        if (!result) {
            String msg = message != null ? messageTools.loadString(messagesPack, message) : "Invalid value '%s'";
            throw new ValidationException(String.format(msg, value));
        }
    }
}