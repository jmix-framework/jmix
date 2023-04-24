/*
 * Copyright 2023 Haulmont.
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

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "float", javaClass = Float.class, defaultForClass = true, value = "core_FloatDatatype")
@io.jmix.core.metamodel.annotation.NumberFormat(
        pattern = "0.###",
        decimalSeparator = ".",
        groupingSeparator = ""
)
public class FloatDatatype extends NumberDatatype implements Datatype<Float> {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @Override
    public String format(@Nullable Object value) {
        return value == null ? "" : createFormat().format(value);
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

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        NumberFormat format = new DecimalFormat(formatStrings.getDoubleFormat(), formatSymbols);
        return format.format(value);
    }

    @Override
    public Float parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return parse(value, createFormat()).floatValue();
    }

    @Override
    public Float parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        NumberFormat format = new DecimalFormat(formatStrings.getDoubleFormat(), formatSymbols);
        return parse(value, format).floatValue();
    }

    @Override
    protected Number parse(String value, NumberFormat format) throws ParseException {
        Number result = super.parse(value, format);
        if (!hasValidFloatRange(result)) {
            throw new ParseException(String.format("Float range exceeded: \"%s\"", value), 0);
        }

        return result;
    }

    protected boolean hasValidFloatRange(Number result) {
        if (result instanceof Long) {
            Long longResult = (Long) result;

            if (longResult > Float.MAX_VALUE || longResult < Float.MIN_VALUE) {
                return false;
            }
        } else {
            Double doubleResult = (Double) result;

            if (doubleResult > Float.MAX_VALUE || doubleResult < Float.MIN_VALUE) {
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
