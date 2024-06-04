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
package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.Messages;
import io.jmix.core.UuidProvider;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.Locale;
import java.util.UUID;

@DatatypeDef(id = "uuid", javaClass = UUID.class, defaultForClass = true, value = "core_UuidDatatype")
public class UuidDatatype implements Datatype<UUID> {

    protected Messages messages;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public String format(Object value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public String format(Object value, Locale locale) {
        return format(value);
    }

    @Override
    public UUID parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        } else {
            try {
                return UuidProvider.fromString(value.trim());
            } catch (Exception e) {
                throw new ParseException(messages.formatMessage(
                        "", "datatype.unparseableUuid.message", value.trim()), 0);
            }
        }
    }

    @Override
    public UUID parse(String value, Locale locale) throws ParseException {
        return parse(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}