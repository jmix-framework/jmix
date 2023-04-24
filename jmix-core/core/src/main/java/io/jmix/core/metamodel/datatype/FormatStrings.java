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

package io.jmix.core.metamodel.datatype;

import java.text.DecimalFormatSymbols;

/**
 * Localized format strings container. <br>
 * An instance of this class can be acquired through {@link FormatStringsRegistry}.
 */
public class FormatStrings {

    private DecimalFormatSymbols formatSymbols;
    private String integerFormat;
    private String doubleFormat;
    private String decimalFormat;
    private String dateFormat;
    private String offsetDateTimeFormat;
    private String dateTimeFormat;
    private String timeFormat;
    private String offsetTimeFormat;
    private String trueString;
    private String falseString;

    public FormatStrings(char decimalSeparator, char groupingSeparator,
                         String integerFormat, String doubleFormat, String decimalFormat,
                         String dateFormat, String dateTimeFormat, String offsetDateTimeFormat,
                         String timeFormat, String offsetTimeFormat,
                         String trueString, String falseString) {
        formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator(decimalSeparator);
        formatSymbols.setGroupingSeparator(groupingSeparator);
        this.integerFormat = integerFormat;
        this.doubleFormat = doubleFormat;
        this.decimalFormat = decimalFormat;
        this.dateFormat = dateFormat;
        this.dateTimeFormat = dateTimeFormat;
        this.offsetDateTimeFormat = offsetDateTimeFormat;
        this.timeFormat = timeFormat;
        this.offsetTimeFormat = offsetTimeFormat;
        this.trueString = trueString;
        this.falseString = falseString;
    }

    public DecimalFormatSymbols getFormatSymbols() {
        return (DecimalFormatSymbols) formatSymbols.clone();
    }

    public String getIntegerFormat() {
        return integerFormat;
    }

    public String getDoubleFormat() {
        return doubleFormat;
    }

    public String getDecimalFormat() {
        return decimalFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public String getOffsetDateTimeFormat() {
        return offsetDateTimeFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getOffsetTimeFormat() {
        return offsetTimeFormat;
    }

    public String getTrueString() {
        return trueString;
    }

    public String getFalseString() {
        return falseString;
    }
}