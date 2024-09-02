/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.kit.component.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

public class CalendarDuration implements Serializable {

    protected int years;
    protected int months;
    protected int weeks;
    protected int days;
    protected int hours;
    protected int minutes;
    protected long seconds;
    protected long milliseconds;

    private CalendarDuration(int years, int months, int weeks, int days, int hours, int minutes,
                             long seconds, long milliseconds) {
        this.years = years;
        this.months = months;
        this.weeks = weeks;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    public static CalendarDuration ofYears(int years) {
        return create(years, 0, 0, 0, 0, 0, 0, 0);
    }

    public static CalendarDuration ofMonths(int months) {
        return create(0, months, 0, 0, 0, 0, 0, 0);
    }

    public static CalendarDuration ofWeeks(int weeks) {
        return create(0, 0, weeks, 0, 0, 0, 0, 0);
    }

    public static CalendarDuration ofDays(int days) {
        return create(0, 0, 0, days, 0, 0, 0, 0);
    }

    public static CalendarDuration ofHours(int hours) {
        return create(0, 0, 0, 0, hours, 0, 0, 0);
    }

    public static CalendarDuration ofMinutes(int minutes) {
        return create(0, 0, 0, 0, 0, minutes, 0, 0);
    }

    public static CalendarDuration ofSeconds(long seconds) {
        return create(0, 0, 0, 0, 0, 0, seconds, 0);
    }

    public static CalendarDuration ofMilliseconds(long ms) {
        return create(0, 0, 0, 0, 0, 0, 0, ms);
    }

    public static CalendarDuration ofDuration(Duration duration) {
        long durationMs = duration.toMillis();

        int dayMs = 86400000;
        int days = (int) (durationMs / dayMs);

        durationMs = durationMs - (long) days * dayMs;

        int hourMs = 3600000;
        int hours = (int) (durationMs / hourMs);

        durationMs = durationMs - (long) hours * hourMs;

        int minuteMs = 60000;
        int minutes = (int) (durationMs / minuteMs);

        durationMs = durationMs - (long) minutes * minuteMs;

        long secondsMs = 1000;
        long seconds = durationMs / secondsMs;

        durationMs = durationMs - seconds * secondsMs;

        long milliseconds = durationMs;

        return create(0, 0, 0, days, hours, minutes, seconds, milliseconds);
    }

    private static CalendarDuration create(int years, int months, int weeks, int days, int hours, int minutes,
                                           long seconds, long milliseconds) {
        return new CalendarDuration(years, months, weeks, days, hours, minutes, seconds, milliseconds);
    }

    public CalendarDuration plusYears(int years) {
        int newYears = this.years + years;
        return create(newYears, months, weeks, days, hours, minutes, seconds, milliseconds);
    }

    public CalendarDuration minusYears(int years) {
        return plusYears(-years);
    }

    public CalendarDuration plusMonths(int months) {
        int newMonths = this.months + months;
        return create(years, newMonths, weeks, days, hours, minutes, seconds, milliseconds);
    }

    public CalendarDuration minusMonths(int months) {
        return plusMonths(-months);
    }

    public CalendarDuration plusWeeks(int weeks) {
        int newWeeks = this.weeks + weeks;
        return create(years, months, newWeeks, days, hours, minutes, seconds, milliseconds);
    }

    public CalendarDuration minusWeeks(int weeks) {
        return plusWeeks(-weeks);
    }

    public CalendarDuration plusDays(int days) {
        int newDays = this.days + days;
        return create(years, months, weeks, newDays, hours, minutes, seconds, milliseconds);
    }

    public CalendarDuration minusDays(int days) {
        return plusDays(-days);
    }

    public CalendarDuration plusHours(int hours) {
        int newHours = this.hours + hours;
        return create(years, months, weeks, days, newHours, minutes, seconds, milliseconds);
    }

    public CalendarDuration minusHours(int hours) {
        return plusHours(-hours);
    }

    public CalendarDuration plusMinutes(int minutes) {
        int newMinutes = this.minutes + minutes;
        return create(years, months, weeks, days, hours, newMinutes, seconds, milliseconds);
    }

    public CalendarDuration minusMinutes(int minutes) {
        return plusMinutes(-minutes);
    }

    public CalendarDuration plusSeconds(long seconds) {
        long newSeconds = this.seconds + seconds;
        return create(years, months, weeks, days, hours, minutes, newSeconds, milliseconds);
    }

    public CalendarDuration minusSeconds(long seconds) {
        return plusSeconds(-seconds);
    }

    public CalendarDuration plusMilliseconds(long milliseconds) {
        long newMilliseconds = this.milliseconds + milliseconds;
        return create(years, months, weeks, days, hours, minutes, seconds, newMilliseconds);
    }

    public CalendarDuration minusMilliseconds(long milliseconds) {
        return plusMilliseconds(-milliseconds);
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getWeeks() {
        return weeks;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof CalendarDuration cObj)
                && cObj.years == years
                && cObj.months == months
                && cObj.weeks == weeks
                && cObj.days == days
                && cObj.hours == hours
                && cObj.minutes == minutes
                && cObj.seconds == seconds
                && cObj.milliseconds == milliseconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, weeks, days, hours, minutes, seconds, milliseconds);
    }
}
