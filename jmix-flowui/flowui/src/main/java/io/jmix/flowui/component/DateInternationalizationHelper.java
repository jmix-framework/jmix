/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormatSymbols;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility class for internationalizing date-related information such as month names,
 * weekday names, short weekday names, and the first day of the week.
 */
public class DateInternationalizationHelper {

    /**
     * Returns a list of full month names for stand-alone use in the specified locale.
     *
     * @param locale the locale for which to retrieve month names
     * @return a list of full month names
     */
    public static List<String> getMonthNames(Locale locale) {
        return Arrays.stream(Month.values())
                .map(month -> StringUtils.capitalize(month.getDisplayName(TextStyle.FULL_STANDALONE, locale)))
                .toList();
    }

    /**
     * Returns a list of weekday names in the specified locale.
     *
     * @param locale the locale for which to retrieve weekday names
     * @return a list of weekday names
     */
    public static List<String> getWeekdayNames(Locale locale) {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        return Arrays.stream(dateFormatSymbols.getWeekdays())
                .filter(weekday -> !weekday.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of short weekday names in the specified locale.
     *
     * @param locale the locale for which to retrieve short weekday names
     * @return a list of short weekday names
     */
    public static List<String> getShortWeekdayNames(Locale locale) {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        return Arrays.stream(dateFormatSymbols.getShortWeekdays())
                .filter(shortWeekday -> !shortWeekday.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns the first day of the week in the specified locale.
     *
     * @param locale the locale for which to retrieve the first day of the week
     * @return the first day of the week as an integer (e.g., Calendar.SUNDAY)
     */
    public static int getFirstDayOfWeek(Locale locale) {
        Calendar calendar = Calendar.getInstance(locale);
        return calendar.getFirstDayOfWeek();
    }
}
