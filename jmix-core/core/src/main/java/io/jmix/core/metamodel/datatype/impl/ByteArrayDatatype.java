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

import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Locale;

@DatatypeDef(id = "byteArray", javaClass = byte[].class, defaultForClass = true, value = "core_ByteArrayDatatype")
public class ByteArrayDatatype implements Datatype<byte[]> {

    @Override
    public String format(Object value) {
        if (value == null)
            return "";

        return new String(Base64.getEncoder().encode((byte[]) value), StandardCharsets.UTF_8);
    }

    @Override
    public String format(Object value, Locale locale) {
        if (value == null)
            return "";

        return format(value);
    }

    @Override
    public byte[] parse(String value) throws ParseException {
        if (value == null || value.length() == 0)
            return null;

        return Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] parse(String value, Locale locale) throws ParseException {
        return parse(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}