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

package io.jmix.fullcalendarflowui.component;

import com.vaadin.flow.component.UI;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static java.util.Objects.requireNonNullElseGet;

/**
 * The internationalization properties for {@link FullCalendar} component.
 */
public class FullCalendarI18n implements Serializable {

    public enum Direction {
        LTR, RTL
    }

    protected Direction direction;
    protected DayOfWeek firstDayOfWeek;
    protected Integer dayOfYear;

    protected String weekTextLong;
    protected String allDayText;
    protected String moreLinkText;
    protected String noEventsText;

    protected String closeHint;
    protected String eventHint;
    protected String timeHint;
    protected String navLinkHint;
    protected String moreLinkHint;

    public FullCalendarI18n() {
    }

    private FullCalendarI18n(FullCalendarI18n i18n) {
        direction = i18n.direction;
        firstDayOfWeek = i18n.firstDayOfWeek;
        dayOfYear = i18n.dayOfYear;
        weekTextLong = i18n.weekTextLong;
        allDayText = i18n.allDayText;
        moreLinkText = i18n.moreLinkText;
        noEventsText = i18n.noEventsText;
        closeHint = i18n.closeHint;
        eventHint = i18n.eventHint;
        timeHint = i18n.timeHint;
        navLinkHint = i18n.navLinkHint;
        moreLinkHint = i18n.moreLinkHint;
    }

    /**
     * @return the direction in which text should be written and read or {@code null} if not set
     */
    @Nullable
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction in which text should be written and read.
     * <p>
     * {@link FullCalendar} also respects the direction of
     * {@link UI#setDirection(com.vaadin.flow.component.Direction)}.
     *
     * @param direction the direction to set
     */
    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    /**
     * Sets a direction. See {@link #setDirection(Direction)}.
     *
     * @param direction the direction to set
     * @return current instance of i18n
     */
    public FullCalendarI18n withDirection(@Nullable Direction direction) {
        setDirection(direction);
        return this;
    }

    /**
     * @return the first day of week or {@code null} if not set
     */
    @Nullable
    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Sets the first day of week. The default value is taken from locale.
     * <p>
     * The order of days is the following: 0 - Sunday, 1 - Monday, etc.
     *
     * @param dayOfWeek the first day of week
     */
    public void setFirstDayOfWeek(@Nullable DayOfWeek dayOfWeek) {
        this.firstDayOfWeek = dayOfWeek;
    }

    /**
     * Sets the first day of week. See {@link #setFirstDayOfWeek(DayOfWeek)}.
     *
     * @param dayOfWeek the first day of week
     * @return current instance of i18n
     */
    public FullCalendarI18n withFirstDayOfWeek(@Nullable DayOfWeek dayOfWeek) {
        setFirstDayOfWeek(dayOfWeek);
        return this;
    }

    /**
     * @return count of the days or {@code null} if not set
     */
    @Nullable
    public Integer getDayOfYear() {
        return dayOfYear;
    }

    /**
     * The rule how to define the first week of the year. For instance, if the first day of the week is {@code 1} and
     * the day of year equals to {@code 4}, it means that the week that contains {@code Jan 4th} is the first week of
     * the year.
     * <p>
     * The default value is taken from locale.
     *
     * @param dayOfYear count of the days
     */
    public void setDayOfYear(@Nullable Integer dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    /**
     * Sets the count of days that should be at least presented in the week to consider it as a first week of the year.
     * See {@link #setDayOfYear(Integer)}.
     *
     * @param dayOfYear count of the days
     * @return current instance of i18n
     */
    public FullCalendarI18n withDayOfYear(@Nullable Integer dayOfYear) {
        setDayOfYear(dayOfYear);
        return this;
    }

    /**
     * @return the long name of the week or {@code null} if not set
     */
    @Nullable
    public String getWeekTextLong() {
        return weekTextLong;
    }

    /**
     * Sets the long name of the week.
     *
     * @param weekTextLong the long name of the week
     */
    public void setWeekTextLong(@Nullable String weekTextLong) {
        this.weekTextLong = weekTextLong;
    }

    /**
     * Sets the long name of the week. See {@link #setWeekTextLong(String)}.
     *
     * @param weekTextLong the long name of the week
     * @return current instance of i18n
     */
    public FullCalendarI18n withWeekTextLong(String weekTextLong) {
        setWeekTextLong(weekTextLong);
        return this;
    }

    /**
     * @return the all-day text or {@code null} if not set
     */
    @Nullable
    public String getAllDayText() {
        return allDayText;
    }

    /**
     * Sets the all-day text. The text is visible in time-grid display modes.
     *
     * @param allDayText the all-day text
     */
    public void setAllDayText(@Nullable String allDayText) {
        this.allDayText = allDayText;
    }

    /**
     * Sets the all-day text. See {@link #setAllDayText(String)}.
     *
     * @param allDayText the all-day text
     * @return current instance of i18n
     */
    public FullCalendarI18n withAllDayText(@Nullable String allDayText) {
        setAllDayText(allDayText);
        return this;
    }

    /**
     * @return the "more" link text or {@code null} if not set
     */
    @Nullable
    public String getMoreLinkText() {
        return moreLinkText;
    }

    /**
     * Sets the "more" link text. The provided text can be a
     * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Template_literals">
     * JavaScript Template string</a>. This template takes the {@code count} parameter, for instance:
     * <pre>
     * setMoreLinkText("+${count} event(s)");
     * </pre>
     * It also takes a string definition of JavaScript function that takes {@code count} as a parameter. For instance:
     * <pre>
     * setMoreLinkText(
     *     """
     *     function (count) {
     *         return `See +${count} event` + (count === 1 ? '' : 's');
     *     }
     *     """)
     * </pre>
     *
     * @param moreLinkText the "more" link text
     */
    public void setMoreLinkText(@Nullable String moreLinkText) {
        this.moreLinkText = moreLinkText;
    }

    /**
     * Sets the "more" link text. See {@link #setMoreLinkText(String)}.
     *
     * @param moreLinkText the "more" link text
     * @return current instance of i18n
     */
    public FullCalendarI18n withMoreLinkText(@Nullable String moreLinkText) {
        setMoreLinkText(moreLinkText);
        return this;
    }

    /**
     * @return text that is shown when no events are displayed or {@code null} if not set
     */
    @Nullable
    public String getNoEventsText() {
        return noEventsText;
    }

    /**
     * Sets text that will be shown when no events are displayed in list display modes,
     * e.g {@link CalendarDisplayModes#LIST_DAY}.
     *
     * @param noEventsText the text to set
     */
    public void setNoEventsText(@Nullable String noEventsText) {
        this.noEventsText = noEventsText;
    }

    /**
     * Sets text that will be shown when no events are displayed. See {@link #setNoEventsText(String)}.
     *
     * @param noEventsText the text to set
     * @return current instance of i18n
     */
    public FullCalendarI18n withNoEventsText(@Nullable String noEventsText) {
        setNoEventsText(noEventsText);
        return this;
    }

    /**
     * @return the hint for close button or {@code null} if not set
     */
    @Nullable
    public String getCloseHint() {
        return closeHint;
    }

    /**
     * Sets the hint for the close button in the popover when you click the "more" link.
     *
     * @param closeHint the close hint text
     */
    public void setCloseHint(@Nullable String closeHint) {
        this.closeHint = closeHint;
    }

    /**
     * Sets the hint for close button. See {@link #setCloseHint(String)}.
     *
     * @param closeHint the close hint text
     * @return current instance of i18n
     */
    public FullCalendarI18n withCloseHint(@Nullable String closeHint) {
        setCloseHint(closeHint);
        return this;
    }

    /**
     * @return the event hint
     */
    @Nullable
    public String getEventHint() {
        return eventHint;
    }

    /**
     * Sets for list display modes' non-visible table header the name of the column with event names.
     *
     * @param eventHint the event hint text
     */
    public void setEventHint(@Nullable String eventHint) {
        this.eventHint = eventHint;
    }

    /**
     * Sets for list display modes' non-visible table header the name of the column with event names.
     * See {@link #setEventHint(String)}.
     *
     * @param eventHint the even hint text
     * @return current instance of i18n
     */
    public FullCalendarI18n withEventHint(@Nullable String eventHint) {
        setEventHint(eventHint);
        return this;
    }

    /**
     * @return the time hint text or {@code null} if not set
     */
    @Nullable
    public String getTimeHint() {
        return timeHint;
    }

    /**
     * Sets for list display modes' non-visible table header the name of the column with days and times.
     *
     * @param timeHint the time hint text
     */
    public void setTimeHint(@Nullable String timeHint) {
        this.timeHint = timeHint;
    }

    /**
     * Sets the time hint text. See {@link #setTimeHint(String)}.
     *
     * @param timeHint the time hint text
     * @return current instance of i18n
     */
    public FullCalendarI18n withTimeHint(@Nullable String timeHint) {
        setTimeHint(timeHint);
        return this;
    }

    /**
     * @return the navigation link hint or {@code null} if not set
     */
    @Nullable
    public String getNavLinkHint() {
        return navLinkHint;
    }

    /**
     * Sets the hint text of navigation links. The provided text can be a
     * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Template_literals">
     * JavaScript Template string</a>. This template takes the {@code date} parameter that has String type,
     * for instance:
     * <pre>
     *  setNavLinkHint("Navigate to ${date}");
     *  </pre>
     * It also takes a string definition of JavaScript function that takes the {@code date} as a parameter with
     * the String type. For instance:
     * <pre>
     *  setNavLinkHint(
     *      """
     *      function (date) {
     *          return `Navigate to ${date}`;
     *      }
     *      """)
     *  </pre>
     *
     * @param navLinkHint the navigation link hint
     */
    public void setNavLinkHint(@Nullable String navLinkHint) {
        this.navLinkHint = navLinkHint;
    }

    /**
     * Sets the hint text of navigation links. See {@link #setNavLinkHint(String)}.
     *
     * @param navLinkHint the navigation link hint
     * @return current instance of i18n
     */
    public FullCalendarI18n withNavLinkHint(@Nullable String navLinkHint) {
        setNavLinkHint(navLinkHint);
        return this;
    }

    /**
     * @return the "more" link hint text or {@code null} if not set
     */
    public String getMoreLinkHint() {
        return moreLinkHint;
    }

    /**
     * Sets the "more" link hint. The provided text can be a
     * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Template_literals">
     * JavaScript Template string</a>. This template takes the {@code count} parameter that has Number type,
     * for instance:
     * <pre>
     *  setMoreLinkHint("Show ${count} more event${count === 1 ? '' : 's'}");
     *  </pre>
     * It also takes a string definition of JavaScript function that takes {@code count} as a parameter. For instance:
     * <pre>
     *  setMoreLinkHint(
     *      """
     *      function (count) {
     *          return `Show ${count} more event${count === 1 ? '' : 's'}`;
     *      }
     *      """)
     *  </pre>
     *
     * @param moreLinkHint the "more" link hint text
     */
    public void setMoreLinkHint(@Nullable String moreLinkHint) {
        this.moreLinkHint = moreLinkHint;
    }

    /**
     * Sets the "more" link hint text. See {@link #setMoreLinkHint(String)}.
     *
     * @param moreLinkHint the "more" link hint text
     * @return current instance of i18n
     */
    public FullCalendarI18n withMoreLinkHint(@Nullable String moreLinkHint) {
        setMoreLinkHint(moreLinkHint);
        return this;
    }

    /**
     * Creates new i18n object from current instance and copies non-null properties from the provided object to
     * the new one.
     *
     * @param i18n object to copy non-null properties
     * @return new i18n object
     */
    public FullCalendarI18n combine(@Nullable FullCalendarI18n i18n) {
        FullCalendarI18n result = new FullCalendarI18n(this);
        if (i18n == null) {
            return result;
        }

        result.setDirection(requireNonNullElseGet(i18n.getDirection(), result::getDirection));
        result.setFirstDayOfWeek(requireNonNullElseGet(i18n.getFirstDayOfWeek(), result::getFirstDayOfWeek));
        result.setDayOfYear(requireNonNullElseGet(i18n.getDayOfYear(), result::getDayOfYear));
        result.setWeekTextLong(requireNonNullElseGet(i18n.getWeekTextLong(), result::getWeekTextLong));
        result.setAllDayText(requireNonNullElseGet(i18n.getAllDayText(), result::getAllDayText));
        result.setMoreLinkText(requireNonNullElseGet(i18n.getMoreLinkText(), result::getMoreLinkText));
        result.setNoEventsText(requireNonNullElseGet(i18n.getNoEventsText(), result::getNoEventsText));
        result.setCloseHint(requireNonNullElseGet(i18n.getCloseHint(), result::getCloseHint));
        result.setEventHint(requireNonNullElseGet(i18n.getEventHint(), result::getEventHint));
        result.setTimeHint(requireNonNullElseGet(i18n.getTimeHint(), result::getTimeHint));
        result.setNavLinkHint(requireNonNullElseGet(i18n.getNavLinkHint(), result::getNavLinkHint));
        result.setMoreLinkHint(requireNonNullElseGet(i18n.getMoreLinkHint(), result::getMoreLinkHint));
        return result;
    }
}
