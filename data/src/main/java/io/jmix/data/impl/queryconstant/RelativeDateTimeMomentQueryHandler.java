/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.impl.queryconstant;

import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.DataProperties;
import io.jmix.data.impl.QueryConstantHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("data_RelativeDateTimeMomentQueryConstantHandler")
@Scope("prototype")
public class RelativeDateTimeMomentQueryHandler implements QueryConstantHandler {

    private static final String CONSTANT_PATTERN = "%s";

    private final CurrentAuthentication currentAuthentication;
    private final TimeSource timeSource;
    private final DataProperties dataProperties;

    public RelativeDateTimeMomentQueryHandler(CurrentAuthentication currentAuthentication, TimeSource timeSource, DataProperties dataProperties) {
        this.currentAuthentication = currentAuthentication;
        this.timeSource = timeSource;
        this.dataProperties = dataProperties;
    }

    @Override
    public String expandConstant(String queryString) {
        for (RelativeDateTimeMoment moment : RelativeDateTimeMoment.values()) {
            Pattern pattern = Pattern.compile(String.format(CONSTANT_PATTERN, moment.name()), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(queryString);
            if (matcher.find()) {
                queryString = matcher.replaceAll(getValueForConstant(moment));
            }
        }
        return queryString;
    }

    private String getValueForConstant(RelativeDateTimeMoment moment) {
        String result = "'%s'";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        ZoneId userTimeZone = getUserZoneId();
        LocalDate nowDate = LocalDate.now(userTimeZone);
        LocalDateTime nowDateTime = LocalDateTime.now(userTimeZone);
        Locale locale = Locale.getDefault();
        if (dataProperties.isUseUserLocaleForRelativeDateTimeMoments()) {
            if (currentAuthentication.isSet()) {
                locale = currentAuthentication.getLocale();
            }
        }
        int currentYear = nowDate.getYear();
        switch (moment) {
            case FIRST_DAY_OF_CURRENT_YEAR:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate.withDayOfYear(1), LocalTime.MIDNIGHT)).format(dateTimeFormatter));
            case LAST_DAY_OF_CURRENT_YEAR:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of((nowDate.withDayOfYear(nowDate.lengthOfYear())), LocalTime.MAX)).format(dateTimeFormatter));
            case FIRST_DAY_OF_CURRENT_MONTH:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(LocalDate.of(currentYear, nowDate.getMonth(), 1), LocalTime.MIDNIGHT)).format(dateTimeFormatter));
            case LAST_DAY_OF_CURRENT_MONTH:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(LocalDate.of(currentYear, nowDate.getMonth(), nowDate.getMonth().length(nowDate.isLeapYear())), LocalTime.MAX)).format(dateTimeFormatter));
            case FIRST_DAY_OF_CURRENT_WEEK:
                TemporalField fieldISO = WeekFields.of(locale).dayOfWeek();
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate.with(fieldISO, 1), LocalTime.MIDNIGHT)).format(dateTimeFormatter));
            case LAST_DAY_OF_CURRENT_WEEK:
                fieldISO = WeekFields.of(locale).dayOfWeek();
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate.with(fieldISO, 7), LocalTime.MAX)).format(dateTimeFormatter));
            case START_OF_CURRENT_DAY:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate, LocalTime.MIDNIGHT)).format(dateTimeFormatter));
            case END_OF_CURRENT_DAY:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate, LocalTime.MAX)).format(dateTimeFormatter));
            case START_OF_YESTERDAY:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate, LocalTime.MIDNIGHT).minusDays(1)).format(dateTimeFormatter));
            case START_OF_TOMORROW:
                return String.format(result, convertToApplicationTimeZone(LocalDateTime.of(nowDate, LocalTime.MIDNIGHT).plusDays(1)).format(dateTimeFormatter));
            case START_OF_CURRENT_HOUR:
                return String.format(result, convertToApplicationTimeZone(nowDateTime.withMinute(0).withSecond(0).withNano(0)).format(dateTimeFormatter));
            case END_OF_CURRENT_HOUR:
                return String.format(result, convertToApplicationTimeZone(nowDateTime.withMinute(59).withSecond(59).withNano(999_999_999)).format(dateTimeFormatter));
            case START_OF_CURRENT_MINUTE:
                return String.format(result, convertToApplicationTimeZone(nowDateTime.withSecond(0).withNano(0)).format(dateTimeFormatter));
            case END_OF_CURRENT_MINUTE:
                return String.format(result, convertToApplicationTimeZone(nowDateTime.withSecond(59).withNano(999_999_999)).format(dateTimeFormatter));
        }
        return moment.name();
    }

    private LocalDateTime convertToApplicationTimeZone(LocalDateTime localDateTime) {
        ZoneId applicationZoneId = timeSource.now().getZone();
        ZoneId userTimeZone = getUserZoneId();
        return ZonedDateTime.of(localDateTime, userTimeZone).withZoneSameInstant(applicationZoneId).toLocalDateTime();
    }

    @NonNull
    private ZoneId getUserZoneId() {
        ZoneId userTimeZone = ZoneId.systemDefault();
        if (currentAuthentication.isSet()) {
            userTimeZone = ZoneId.of(currentAuthentication.getTimeZone().getID());
        }
        return userTimeZone;
    }

}
