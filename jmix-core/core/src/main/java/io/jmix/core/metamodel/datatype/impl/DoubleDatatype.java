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

import io.jmix.core.CoreProperties;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "double", javaClass = Double.class, defaultForClass = true, value = "core_DoubleDatatype")
@io.jmix.core.metamodel.annotation.NumberFormat(
        pattern = "0.###",
        decimalSeparator = ".",
        groupingSeparator = ""
)
public class DoubleDatatype extends NumberDatatype implements Datatype<Double> {

    private static final BigDecimal DOUBLE_MIN_VALUE = BigDecimal.valueOf(-Double.MAX_VALUE);
    private static final BigDecimal DOUBLE_MAX_VALUE = BigDecimal.valueOf(Double.MAX_VALUE);

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;
    @Autowired
    protected CoreProperties coreProperties;

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
        NumberFormat format = new DecimalFormat(formatStrings.getDoubleFormat(), formatSymbols);
        return format.format(value);
    }

    @Override
    public Double parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return parse(value, createFormat()).doubleValue();
    }

    @Override
    public Double parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        DecimalFormat format = new DecimalFormat(formatStrings.getDoubleFormat(), formatSymbols);
        format.setParseBigDecimal(true);
        return parse(value, format).doubleValue();
    }

    protected Number parse(String value, NumberFormat format) throws ParseException {
        BigDecimal result = (BigDecimal) super.parse(value, format);
        if (coreProperties.isRoundDecimalValueByFormat()) {
            int maximumFractionDigits = format.getMaximumFractionDigits();
            RoundingMode roundingMode = format.getRoundingMode();
            result = result.setScale(maximumFractionDigits, roundingMode);
        }

        if (!isInDoubleRange(result)) {
            throw new ParseException(String.format("The value is out of Double datatype range: \"%s\"", value), 0);
        }

        return result;
    }

    @Override
    protected java.text.NumberFormat createFormat() {
        java.text.NumberFormat format = super.createFormat();
        if (format instanceof DecimalFormat) {
            ((DecimalFormat) format).setParseBigDecimal(true);
        }
        return format;
    }

    protected boolean isInDoubleRange(BigDecimal result) {
        return result.compareTo(DOUBLE_MAX_VALUE) <= 0 && result.compareTo(DOUBLE_MIN_VALUE) >= 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}