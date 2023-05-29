/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.core.metamodel.datatype;

import io.jmix.core.metamodel.datatype.impl.DateTimeDatatype;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.time.*;
import java.util.Date;
import java.util.TimeZone;

/**
 * Convenience bean for locale-dependent conversion of some widely used data types to and from strings.
 * <p>
 * For locale-independent conversion use {@link Datatype} methods directly.
 */
@Component("core_DatatypeFormatter")
public class DatatypeFormatter {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    /**
     * Format Date (date without time) using {@code dateFormat} string specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatDate(@Nullable Date value) {
        return datatypeRegistry.get(java.sql.Date.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format Date (time without date) using {@code timeFormat} string specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatTime(@Nullable Date value) {
        return datatypeRegistry.get(Time.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format Date (date and time) using {@code dateTimeFormat} string specified in the main message group.
     * <p>Takes into account time zone if it is set for the current user session.</p>
     * @return string representation or empty string if the value is null
     */
    public String formatDateTime(@Nullable Date value) {
        TimeZone timeZone = currentAuthentication.getTimeZone();
        Datatype<Date> datatype = datatypeRegistry.get(Date.class);
        if (datatype instanceof DateTimeDatatype) {
            return ((DateTimeDatatype) datatype).format(value, currentAuthentication.getLocale(), timeZone);
        }
        return datatype.format(value, currentAuthentication.getLocale());
    }

    /**
     * Format LocalDate (date without time and without a time-zone) using {@code dateFormat} string specified in the
     * main message group.
     *
     * @return string representation or empty string if the value is null
     */
    public String formatLocalDate(@Nullable LocalDate value) {
        return datatypeRegistry.get(LocalDate.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format LocalDateTime (date and time without a time-zone) using {@code dateTimeFormat} string specified in the
     * main message group.
     *
     * @return string representation or empty string if the value is null
     */
    public String formatLocalDateTime(@Nullable LocalDateTime value) {
        return datatypeRegistry.get(LocalDateTime.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format LocalTime (time without date and without a time-zone) using {@code timeFormat} string specified in the
     * main message group.
     *
     * @return string representation or empty string if the value is null
     */
    public String formatLocalTime(@Nullable LocalTime value) {
        return datatypeRegistry.get(LocalTime.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format OffsetDateTime (date and time with an offset from UTC/Greenwich) using {@code offsetDateTimeFormat} string
     * specified in the main message group.
     *
     * @return string representation or empty string if the value is null
     */
    public String formatOffsetDateTime(@Nullable OffsetDateTime value) {
        return datatypeRegistry.get(OffsetDateTime.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format OffsetTime (time with an offset from UTC/Greenwich) using {@code offsetTimeFormat} string specified in the
     * main message group.
     *
     * @return string representation or empty string if the value is null
     */
    public String formatOffsetTime(@Nullable OffsetTime value) {
        return datatypeRegistry.get(OffsetTime.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format Double using {@code doubleFormat} string specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatDouble(@Nullable Double value) {
        return datatypeRegistry.get(Double.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format BigDecimal using {@code decimalFormat} string specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatBigDecimal(@Nullable BigDecimal value) {
        return datatypeRegistry.get(BigDecimal.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format Boolean using {@code trueString} and {@code falseString} strings specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatBoolean(@Nullable Boolean value) {
        return datatypeRegistry.get(Boolean.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format Integer using {@code integerFormat} string specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatInteger(@Nullable Integer value) {
        return datatypeRegistry.get(Integer.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Format Long using {@code integerFormat} string specified in the main message group.
     * @return string representation or empty string if the value is null
     */
    public String formatLong(@Nullable Long value) {
        return datatypeRegistry.get(Long.class).format(value, currentAuthentication.getLocale());
    }

    /**
     * Parse Date (date without time) using {@code dateFormat} string specified in the main message group.
     * @return Date value or null if a blank string is provided
     */
    @Nullable
    public Date parseDate(String str) throws ParseException {
        return datatypeRegistry.get(java.sql.Date.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse Date (time without date) using {@code timeFormat} string specified in the main message group.
     * @return Date value or null if a blank string is provided
     */
    @Nullable
    public Date parseTime(String str) throws ParseException {
        return datatypeRegistry.get(Time.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse Date (date and time) using {@code dateTimeFormat} string specified in the main message group.
     * <p>Takes into account time zone if it is set for the current user session.</p>
     * @return Date value or null if a blank string is provided
     */
    @Nullable
    public Date parseDateTime(String str) throws ParseException {
        TimeZone timeZone = currentAuthentication.getTimeZone();
        Datatype<Date> datatype = datatypeRegistry.get(Date.class);
        if (datatype instanceof DateTimeDatatype) {
            return ((DateTimeDatatype) datatype).parse(str, currentAuthentication.getLocale(), timeZone);
        }
        return datatype.parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse LocalDate (date without time and without a time-zone) using {@code dateFormat} string specified in the main
     * message group.
     *
     * @return LocalDate value or null if a blank string is provided
     */
    @Nullable
    public LocalDate parseLocalDate(String str) throws ParseException {
        return datatypeRegistry.get(LocalDate.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse LocalTime (time without date and without a time-zone) using {@code timeFormat} string specified in the main
     * message group.
     *
     * @return LocalTime value or null if a blank string is provided
     */
    @Nullable
    public LocalTime parseLocalTime(String str) throws ParseException {
        return datatypeRegistry.get(LocalTime.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse LocalDateTime (date and time without a time-zone) using {@code dateTimeFormat} string specified in the main
     * message group.
     *
     * @return LocalDateTime value or null if a blank string is provided
     */
    @Nullable
    public LocalDateTime parseLocalDateTime(String str) throws ParseException {
        return datatypeRegistry.get(LocalDateTime.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse OffsetDateTime (date and time with an offset from UTC/Greenwich) using {@code offsetDateTimeFormat} string
     * specified in the main message group.
     *
     * @return OffsetDateTime value or null if a blank string is provided
     */
    @Nullable
    public OffsetDateTime parseOffsetDateTime(String str) throws ParseException {
        return datatypeRegistry.get(OffsetDateTime.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse OffsetTime (time without date and with an offset from UTC/Greenwich) using {@code offsetTimeFormat} string
     * specified in the main message group.
     *
     * @return OffsetTime value or null if a blank string is provided
     */
    @Nullable
    public OffsetTime parseOffsetTime(String str) throws ParseException {
        return datatypeRegistry.get(OffsetTime.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse Double using {@code doubleFormat} string specified in the main message group.
     * @return Double value or null if a blank string is provided
     */
    @Nullable
    public Double parseDouble(String str) throws ParseException {
        return datatypeRegistry.get(Double.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse BigDecimal using {@code decimalFormat} string specified in the main message group.
     * @return BigDecimal value or null if a blank string is provided
     */
    @Nullable
    public BigDecimal parseBigDecimal(String str) throws ParseException {
        return datatypeRegistry.get(BigDecimal.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse Boolean using {@code trueString} and {@code falseString} strings specified in the main message group.
     * @return Boolean value or null if a blank string is provided
     */
    @Nullable
    public Boolean parseBoolean(String str) throws ParseException {
        return datatypeRegistry.get(Boolean.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse Integer using {@code integerFormat} string specified in the main message group.
     * @return Integer value or null if a blank string is provided
     */
    @Nullable
    public Integer parseInteger(String str) throws ParseException {
        return datatypeRegistry.get(Integer.class).parse(str, currentAuthentication.getLocale());
    }

    /**
     * Parse Long using {@code integerFormat} string specified in the main message group.
     * @return Long value or null if a blank string is provided
     */
    @Nullable
    public Long parseLong(String str) throws ParseException {
        return datatypeRegistry.get(Long.class).parse(str, currentAuthentication.getLocale());
    }
}