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
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.Nullable;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "boolean", javaClass = Boolean.class, defaultForClass = true, value = "core_BooleanDatatype")
public class BooleanDatatype implements Datatype<Boolean> {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @Override
    public String format(@Nullable Object value) {
        return value == null ? "" : Boolean.toString((Boolean) value);
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return format(value);
        }

        return (boolean) value ? formatStrings.getTrueString() : formatStrings.getFalseString();
    }

    @Nullable
    protected Boolean parse(@Nullable String value, String trueString, String falseString) throws ParseException {
        if (!StringUtils.isBlank(value)) {
            if (trueString.equalsIgnoreCase(value)) {
                return Boolean.TRUE;
            }
            if (falseString.equalsIgnoreCase(value)) {
                return Boolean.FALSE;
            }
            throw new ParseException(String.format("Can't parse '%s'", value), 0);
        }
        return null;
    }

    @Override
    public Boolean parse(@Nullable String value) throws ParseException {
        return parse(value, "true", "false");
    }

    @Override
    public Boolean parse(@Nullable String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        return parse(value, formatStrings.getTrueString(), formatStrings.getFalseString());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}