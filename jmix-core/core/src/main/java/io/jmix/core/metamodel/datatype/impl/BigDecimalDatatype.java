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

import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.NumberFormat;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

@Internal
@DatatypeDef(id = "decimal", javaClass = BigDecimal.class, defaultForClass = true, value = "core_BigDecimalDatatype")
@NumberFormat(
        pattern = "0.####",
        decimalSeparator = ".",
        groupingSeparator = ""
)
public class BigDecimalDatatype extends NumberDatatype implements Datatype<BigDecimal> {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @Override
    protected java.text.NumberFormat createFormat() {
        java.text.NumberFormat format = super.createFormat();
        if (format instanceof DecimalFormat) {
            ((DecimalFormat) format).setParseBigDecimal(true);
        }
        return format;
    }

    @Override
    public String format(Object value) {
        return value == null ? "" : createFormat().format(value);
    }

    @Override
    public String format(Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return format(value);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        java.text.NumberFormat format = new DecimalFormat(formatStrings.getDecimalFormat(), formatSymbols);
        return format.format(value);
    }

    @Override
    public BigDecimal parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return (BigDecimal) parse(value, createFormat());
    }

    @Override
    public BigDecimal parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        DecimalFormat format = new DecimalFormat(formatStrings.getDecimalFormat(), formatSymbols);
        format.setParseBigDecimal(true);
        return (BigDecimal) parse(value, format);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}