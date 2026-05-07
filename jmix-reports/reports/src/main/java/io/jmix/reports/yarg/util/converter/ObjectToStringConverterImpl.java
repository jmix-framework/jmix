/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.util.converter;

import io.jmix.reports.yarg.exception.ReportingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class ObjectToStringConverterImpl extends AbstractObjectToStringConverter {

    protected SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    protected DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    protected DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected DateTimeFormatter localTimeFormat = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        if (paramValue == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return (String) paramValue;
        } else if (Date.class.isAssignableFrom(parameterClass)) {
            return dateTimeFormat.format(paramValue);
        } else if (LocalDate.class.isAssignableFrom(parameterClass)) {
            return localDateFormat.format((LocalDate) paramValue);
        } else if (LocalDateTime.class.isAssignableFrom(parameterClass)) {
            return localDateTimeFormat.format((LocalDateTime) paramValue);
        } else if (LocalTime.class.isAssignableFrom(parameterClass)) {
            return localTimeFormat.format((LocalTime) paramValue);
        }

        return String.valueOf(paramValue);
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        if (paramValueStr == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return paramValueStr;
        } else if (java.sql.Date.class.isAssignableFrom(parameterClass)) {
            Date date = parseDate(paramValueStr);
            return new java.sql.Date(date.getTime());
        } else if (java.sql.Timestamp.class.isAssignableFrom(parameterClass)) {
            Date date = parseDate(paramValueStr);
            return new java.sql.Timestamp(date.getTime());
        } else if (Date.class.isAssignableFrom(parameterClass)) {
            return parseDate(paramValueStr);
        } else if (LocalDate.class.isAssignableFrom(parameterClass)) {
            return parseLocalDate(paramValueStr);
        } else if (LocalDateTime.class.isAssignableFrom(parameterClass)) {
            return parseLocalDateTime(paramValueStr);
        } else if (LocalTime.class.isAssignableFrom(parameterClass)) {
            return parseLocalTime(paramValueStr);
        } else {
            return convertFromStringUnresolved(parameterClass, paramValueStr);
        }
    }

    private LocalDate parseLocalDate(String paramValueStr) {
        try {
            return LocalDate.parse(paramValueStr, localDateFormat);
        } catch (DateTimeParseException e) {
            throw new ReportingException(
                    String.format("Couldn't read LocalDate from value [%s]. Date format should be [%s].",
                            paramValueStr, "dd/MM/yyyy"));
        }
    }

    private LocalDateTime parseLocalDateTime(String paramValueStr) {
        try {
            return LocalDateTime.parse(paramValueStr, localDateTimeFormat);
        } catch (DateTimeParseException e) {
            throw new ReportingException(
                    String.format("Couldn't read LocalDateTime from value [%s]. Date format should be [%s].",
                            paramValueStr, "dd/MM/yyyy HH:mm"));
        }
    }

    private LocalTime parseLocalTime(String paramValueStr) {
        try {
            return LocalTime.parse(paramValueStr, localTimeFormat);
        } catch (DateTimeParseException e) {
            throw new ReportingException(
                    String.format("Couldn't read LocalTime from value [%s]. Date format should be [%s].",
                            paramValueStr, "HH:mm"));
        }
    }

    private Date parseDate(String paramValueStr) {
        try {
            return dateTimeFormat.parse(paramValueStr);
        } catch (ParseException e) {
            try {
                return dateFormat.parse(paramValueStr);
            } catch (ParseException e1) {
                throw new ReportingException(
                        String.format("Couldn't read date from value [%s]. Date format should be [%s].",
                                paramValueStr, dateTimeFormat.toPattern()));
            }
        }
    }
}