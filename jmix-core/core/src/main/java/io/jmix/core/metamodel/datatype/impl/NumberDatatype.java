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

import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.ParameterizedDatatype;
import org.apache.commons.lang3.StringUtils;

import java.text.*;
import java.util.Map;

public abstract class NumberDatatype implements ParameterizedDatatype {

    protected String formatPattern;
    protected String decimalSeparator;
    protected String groupingSeparator;

    protected NumberDatatype(String formatPattern, String decimalSeparator, String groupingSeparator) {
        this.formatPattern = formatPattern;
        this.decimalSeparator = decimalSeparator;
        this.groupingSeparator = groupingSeparator;
    }

    protected NumberDatatype() {
        io.jmix.core.metamodel.annotation.NumberFormat numberFormat =
                getClass().getAnnotation(io.jmix.core.metamodel.annotation.NumberFormat.class);
        if (numberFormat != null) {
            formatPattern = numberFormat.pattern();
            decimalSeparator = numberFormat.decimalSeparator();
            groupingSeparator = numberFormat.groupingSeparator();
        }
    }

    @Override
    public Map<String, Object> getParameters() {
        return ParamsMap.of(
                "format", formatPattern,
                "decimalSeparator", decimalSeparator,
                "groupingSeparator", groupingSeparator);
    }

    /**
     * Creates non-localized format.
     */
    protected NumberFormat createFormat() {
        if (formatPattern != null) {
            DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();

            if (!StringUtils.isBlank(decimalSeparator))
                formatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));

            if (!StringUtils.isBlank(groupingSeparator))
                formatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));

            return new DecimalFormat(formatPattern, formatSymbols);
        } else {
            return NumberFormat.getNumberInstance();
        }
    }

    protected Number parse(String value, NumberFormat format) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Number res = format.parse(value.trim(), pos);
        if (pos.getIndex() != value.length()) {
            throw new ParseException(
                    String.format("Unparseable number: \"%s\"", value),
                    pos.getErrorIndex()
            );
        }
        return res;
    }
}