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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "int", javaClass = Integer.class, defaultForClass = true, value = "core_IntegerDatatype")
@io.jmix.core.metamodel.annotation.NumberFormat(
        pattern = "0"
)
public class IntegerDatatype extends NumberDatatype implements Datatype<Integer> {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @Override
    public String format(Object value) {
        return value == null ? "" : createFormat().format(value);
    }

    @Override
    public String format(Object value, Locale locale) {
        if (value == null)
            return "";

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null)
            return format(value);

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        NumberFormat format = new DecimalFormat(formatStrings.getIntegerFormat(), formatSymbols);
        return format.format(value);
    }

    @Override
    public Integer parse(String value) throws ParseException {
        if (StringUtils.isBlank(value))
            return null;

        return parse(value, createFormat()).intValue();
    }

    @Override
    public Integer parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value))
            return null;

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null)
            return parse(value);

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        NumberFormat format = new DecimalFormat(formatStrings.getIntegerFormat(), formatSymbols);
        return parse(value, format).intValue();
    }

    @Override
    protected Number parse(String value, NumberFormat format) throws ParseException {
        format.setParseIntegerOnly(true);

        Number result = super.parse(value, format);
        if (!hasValidIntegerRange(result)) {
            throw new ParseException(messages.formatMessage(
                    "", "datatype.integerOutOfRange.message", value), 0);
        }
        return result;
    }

    protected boolean hasValidIntegerRange(Number result) throws ParseException {
        if (result instanceof Long) {
            Long longResult = (Long) result;

            if (longResult > Integer.MAX_VALUE || longResult < Integer.MIN_VALUE) {
                return false;
            }
        } else {
            Double doubleResult = (Double) result;
            if (doubleResult > Integer.MAX_VALUE || doubleResult < Integer.MIN_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}