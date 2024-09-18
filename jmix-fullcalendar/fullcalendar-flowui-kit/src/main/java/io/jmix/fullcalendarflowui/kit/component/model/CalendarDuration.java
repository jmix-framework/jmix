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

import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

/**
 * A time based object. Is used to pass time value, e.g. to properties
 * ({@link JmixFullCalendar#setSlotDuration(CalendarDuration)} or to navigation methods
 * ({@link JmixFullCalendar#navigateToNext(CalendarDuration)}).
 * <p>
 * Note that each property in this object does not include the units of the other properties.
 * For instance, if the duration is {@code 6} months, the year value will be {@code 0}, not {@code 0.5}.
 * And vice-versa, if the duration is {@code 1} year, the month value will be {@code 0}, not {@code 12}.
 *
 * @see <a href="https://fullcalendar.io/docs/duration-object">FullCalendar docs :: duration-object</a>
 */
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

    /**
     * Creates a duration object from years.
     *
     * @param years years to pass
     * @return new duration object
     */
    public static CalendarDuration ofYears(int years) {
        return create(years, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a duration object from months.
     *
     * @param months months to pass
     * @return new duration object
     */
    public static CalendarDuration ofMonths(int months) {
        return create(0, months, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a duration object from weeks.
     *
     * @param weeks weeks to pass
     * @return new duration object
     */
    public static CalendarDuration ofWeeks(int weeks) {
        return create(0, 0, weeks, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a duration object from days.
     *
     * @param days days to pass
     * @return new duration object
     */
    public static CalendarDuration ofDays(int days) {
        return create(0, 0, 0, days, 0, 0, 0, 0);
    }

    /**
     * Creates a duration object from hours.
     *
     * @param hours hours to pass
     * @return new duration object
     */
    public static CalendarDuration ofHours(int hours) {
        return create(0, 0, 0, 0, hours, 0, 0, 0);
    }

    /**
     * Creates a duration object from minutes.
     *
     * @param minutes minutes to pass
     * @return new duration object
     */
    public static CalendarDuration ofMinutes(int minutes) {
        return create(0, 0, 0, 0, 0, minutes, 0, 0);
    }

    /**
     * Creates a duration object from seconds.
     *
     * @param seconds seconds to pass
     * @return new duration object
     */
    public static CalendarDuration ofSeconds(long seconds) {
        return create(0, 0, 0, 0, 0, 0, seconds, 0);
    }

    /**
     * Creates a duration object from milliseconds.
     *
     * @param ms milliseconds to pass
     * @return new duration object
     */
    public static CalendarDuration ofMilliseconds(long ms) {
        return create(0, 0, 0, 0, 0, 0, 0, ms);
    }

    /**
     * Creates a new duration object from the provided {@link java.time.Duration}. The new object will contain
     * the extracted days, along with the remaining hours, seconds, and milliseconds after the calculation.
     *
     * @param duration duration to pass
     * @return new duration object
     */
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

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of years added.
     *
     * @param years years to add
     * @return a {@link CalendarDuration} based on this duration with the years added
     */
    public CalendarDuration plusYears(int years) {
        int newYears = this.years + years;
        return create(newYears, months, weeks, days, hours, minutes, seconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of years subtracted.
     *
     * @param years years to subtract
     * @return a {@link CalendarDuration} based on this duration with the years subtracted
     */
    public CalendarDuration minusYears(int years) {
        return plusYears(-years);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of months added.
     *
     * @param months months to add
     * @return a {@link CalendarDuration} based on this duration with the months added
     */
    public CalendarDuration plusMonths(int months) {
        int newMonths = this.months + months;
        return create(years, newMonths, weeks, days, hours, minutes, seconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of months subtracted.
     *
     * @param months months to subtract
     * @return a {@link CalendarDuration} based on this duration with the months subtracted
     */
    public CalendarDuration minusMonths(int months) {
        return plusMonths(-months);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of weeks added.
     *
     * @param weeks weeks to add
     * @return a {@link CalendarDuration} based on this duration with the weeks added
     */
    public CalendarDuration plusWeeks(int weeks) {
        int newWeeks = this.weeks + weeks;
        return create(years, months, newWeeks, days, hours, minutes, seconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of weeks subtracted.
     *
     * @param weeks weeks to subtract
     * @return a {@link CalendarDuration} based on this duration with the weeks subtracted
     */
    public CalendarDuration minusWeeks(int weeks) {
        return plusWeeks(-weeks);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of days added.
     *
     * @param days days to add
     * @return a {@link CalendarDuration} based on this duration with the days added
     */
    public CalendarDuration plusDays(int days) {
        int newDays = this.days + days;
        return create(years, months, weeks, newDays, hours, minutes, seconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of days subtracted.
     *
     * @param days days to subtract
     * @return a {@link CalendarDuration} based on this duration with the days subtracted
     */
    public CalendarDuration minusDays(int days) {
        return plusDays(-days);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of hours added.
     *
     * @param hours hours to add
     * @return a {@link CalendarDuration} based on this duration with the hours added
     */
    public CalendarDuration plusHours(int hours) {
        int newHours = this.hours + hours;
        return create(years, months, weeks, days, newHours, minutes, seconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of hours subtracted.
     *
     * @param hours hours to subtract
     * @return a {@link CalendarDuration} based on this duration with the hours subtracted
     */
    public CalendarDuration minusHours(int hours) {
        return plusHours(-hours);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of minutes added.
     *
     * @param minutes minutes to add
     * @return a {@link CalendarDuration} based on this duration with the minutes added
     */
    public CalendarDuration plusMinutes(int minutes) {
        int newMinutes = this.minutes + minutes;
        return create(years, months, weeks, days, hours, newMinutes, seconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of minutes subtracted.
     *
     * @param minutes minutes to subtract
     * @return a {@link CalendarDuration} based on this duration with the minutes subtracted
     */
    public CalendarDuration minusMinutes(int minutes) {
        return plusMinutes(-minutes);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of seconds added.
     *
     * @param seconds seconds to add
     * @return a {@link CalendarDuration} based on this duration with the seconds added
     */
    public CalendarDuration plusSeconds(long seconds) {
        long newSeconds = this.seconds + seconds;
        return create(years, months, weeks, days, hours, minutes, newSeconds, milliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of seconds subtracted.
     *
     * @param seconds seconds to subtract
     * @return a {@link CalendarDuration} based on this duration with the seconds subtracted
     */
    public CalendarDuration minusSeconds(long seconds) {
        return plusSeconds(-seconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of milliseconds added.
     *
     * @param milliseconds milliseconds to add
     * @return a {@link CalendarDuration} based on this duration with the milliseconds added
     */
    public CalendarDuration plusMilliseconds(long milliseconds) {
        long newMilliseconds = this.milliseconds + milliseconds;
        return create(years, months, weeks, days, hours, minutes, seconds, newMilliseconds);
    }

    /**
     * Returns a copy of this {@link CalendarDuration} with the specified number of milliseconds subtracted.
     *
     * @param milliseconds milliseconds to subtract
     * @return a {@link CalendarDuration} based on this duration with the milliseconds subtracted
     */
    public CalendarDuration minusMilliseconds(long milliseconds) {
        return plusMilliseconds(-milliseconds);
    }

    /**
     * @return years field
     */
    public int getYears() {
        return years;
    }

    /**
     * @return months field
     */
    public int getMonths() {
        return months;
    }

    /**
     * @return weeks field
     */
    public int getWeeks() {
        return weeks;
    }

    /**
     * @return days field
     */
    public int getDays() {
        return days;
    }

    /**
     * @return hours field
     */
    public int getHours() {
        return hours;
    }

    /**
     * @return minutes field
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @return seconds field
     */
    public long getSeconds() {
        return seconds;
    }

    /**
     * @return milliseconds field
     */
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
