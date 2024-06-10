/*
 * Copyright 2024 Haulmont.
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

import com.vaadin.flow.dom.Element;
import io.jmix.core.Messages;
import io.jmix.flowui.exception.ValidationException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.Nullable;

public class NumberParseValidation<T> implements Validator<T> {

    protected Messages messages;
    protected String messageKey;
    protected Element element;

    public NumberParseValidation(Messages messages, String messageKey, Element element) {
        this.messages = messages;
        this.messageKey = messageKey;
        this.element = element;
    }

    @Override
    public void accept(@Nullable T value) {
        String valueProperty = element.getProperty("value");
        if (value == null && Strings.isNotEmpty(valueProperty)) {
            throw new ValidationException(messages.formatMessage("", messageKey, valueProperty));
        }
    }
}
