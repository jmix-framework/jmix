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

import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

public class FullCalendarI18n implements Serializable {

    public enum Direction {
        LTR, RTL
    }

    protected Direction direction;
    protected Integer dayOfWeek;
    protected Integer dayOfYear;

    protected String weekText;
    protected String weekTextLong;
    protected String allDayText;
    protected String moreLinkText;
    protected String noEventsText;

    protected String closeHint;
    protected String eventHint;
    protected String timeHint;
    protected String navLinkHint;
    protected String moreLinkHint;

    protected String dayPopoverFormat;
    protected String dayHeaderFormat;
    protected String weekNumberFormat;
    protected String slotLabelFormat;
    protected String eventTimeFormat;
    protected String monthStartFormat;

    public FullCalendarI18n() {
    }

    private FullCalendarI18n(FullCalendarI18n i18n) {
        direction = i18n.direction;
        dayOfWeek = i18n.dayOfWeek;
        dayOfYear = i18n.dayOfYear;
        weekText = i18n.weekText;
        weekTextLong = i18n.weekTextLong;
        allDayText = i18n.allDayText;
        moreLinkText = i18n.moreLinkText;
        noEventsText = i18n.noEventsText;
        closeHint = i18n.closeHint;
        eventHint = i18n.eventHint;
        timeHint = i18n.timeHint;
        navLinkHint = i18n.navLinkHint;
        moreLinkHint = i18n.moreLinkHint;
        dayPopoverFormat = i18n.dayPopoverFormat;
        dayHeaderFormat = i18n.dayHeaderFormat;
        weekNumberFormat = i18n.weekNumberFormat;
        slotLabelFormat = i18n.slotLabelFormat;
        eventTimeFormat = i18n.eventTimeFormat;
        monthStartFormat = i18n.monthStartFormat;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    public FullCalendarI18n withDirection(@Nullable Direction direction) {
        setDirection(direction);
        return this;
    }

    @Nullable
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(@Nullable Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public FullCalendarI18n withDayOfWeek(@Nullable Integer dayOfWeek) {
        setDayOfWeek(dayOfWeek);
        return this;
    }

    @Nullable
    public Integer getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(@Nullable Integer dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public FullCalendarI18n withDayOfYear(@Nullable Integer dayOfYear) {
        setDayOfYear(dayOfYear);
        return this;
    }

    @Nullable
    public String getWeekText() {
        return weekText;
    }

    public void setWeekText(@Nullable String weekText) {
        this.weekText = weekText;
    }

    public FullCalendarI18n withWeekText(@Nullable String weekText) {
        setWeekText(weekText);
        return this;
    }

    @Nullable
    public String getWeekTextLong() {
        return weekTextLong;
    }

    public void setWeekTextLong(@Nullable String weekTextLong) {
        this.weekTextLong = weekTextLong;
    }

    public FullCalendarI18n withWeekTextLong(String weekTextLong) {
        setWeekTextLong(weekTextLong);
        return this;
    }

    @Nullable
    public String getAllDayText() {
        return allDayText;
    }

    public void setAllDayText(@Nullable String allDayText) {
        this.allDayText = allDayText;
    }

    public FullCalendarI18n withAllDayText(@Nullable String allDayText) {
        setAllDayText(allDayText);
        return this;
    }

    @Nullable
    public String getMoreLinkText() {
        return moreLinkText;
    }

    public void setMoreLinkText(@Nullable String moreLinkText) {
        this.moreLinkText = moreLinkText;
    }

    public FullCalendarI18n withMoreLinkText(@Nullable String moreLinkText) {
        setMoreLinkText(moreLinkText);
        return this;
    }

    @Nullable
    public String getNoEventsText() {
        return noEventsText;
    }

    public void setNoEventsText(@Nullable String noEventsText) {
        this.noEventsText = noEventsText;
    }

    public FullCalendarI18n withNoEventsText(@Nullable String noEventsText) {
        setNoEventsText(noEventsText);
        return this;
    }

    @Nullable
    public String getCloseHint() {
        return closeHint;
    }

    public void setCloseHint(@Nullable String closeHint) {
        this.closeHint = closeHint;
    }

    public FullCalendarI18n withCloseHint(@Nullable String closeHint) {
        setCloseHint(closeHint);
        return this;
    }

    @Nullable
    public String getEventHint() {
        return eventHint;
    }

    public void setEventHint(@Nullable String eventHint) {
        this.eventHint = eventHint;
    }

    public FullCalendarI18n withEventHint(@Nullable String eventHint) {
        setEventHint(eventHint);
        return this;
    }

    @Nullable
    public String getTimeHint() {
        return timeHint;
    }

    public void setTimeHint(@Nullable String timeHint) {
        this.timeHint = timeHint;
    }

    public FullCalendarI18n withTimeHint(@Nullable String timeHint) {
        setTimeHint(timeHint);
        return this;
    }

    @Nullable
    public String getNavLinkHint() {
        return navLinkHint;
    }

    public void setNavLinkHint(@Nullable String navLinkHint) {
        this.navLinkHint = navLinkHint;
    }

    public FullCalendarI18n withNavLinkHint(@Nullable String navLinkHint) {
        setNavLinkHint(navLinkHint);
        return this;
    }

    public String getMoreLinkHint() {
        return moreLinkHint;
    }

    public void setMoreLinkHint(@Nullable String moreLinkHint) {
        this.moreLinkHint = moreLinkHint;
    }

    public FullCalendarI18n withMoreLinkHint(@Nullable String moreLinkHint) {
        setMoreLinkHint(moreLinkHint);
        return this;
    }

    @Nullable
    public String getDayPopoverFormat() {
        return dayPopoverFormat;
    }

    public void setDayPopoverFormat(@Nullable String dayPopoverFormat) {
        this.dayPopoverFormat = dayPopoverFormat;
    }

    public FullCalendarI18n withDayPopoverFormat(@Nullable String dayPopoverFormat) {
        setDayPopoverFormat(dayPopoverFormat);
        return this;
    }

    @Nullable
    public String getDayHeaderFormat() {
        return dayHeaderFormat;
    }

    public void setDayHeaderFormat(@Nullable String dayHeaderFormat) {
        this.dayHeaderFormat = dayHeaderFormat;
    }

    public FullCalendarI18n withDayHeaderFormat(@Nullable String dayHeaderFormat) {
        setDayHeaderFormat(dayHeaderFormat);
        return this;
    }

    @Nullable
    public String getWeekNumberFormat() {
        return weekNumberFormat;
    }

    /**
     * Note that it override the {@link #setWeekText(String)} value.
     *
     * @param weekNumberFormat
     */
    public void setWeekNumberFormat(@Nullable String weekNumberFormat) {
        this.weekNumberFormat = weekNumberFormat;
    }

    public FullCalendarI18n withWeekNumberFormat(@Nullable String weekNumberFormat) {
        setWeekNumberFormat(weekNumberFormat);
        return this;
    }

    @Nullable
    public String getSlotLabelFormat() {
        return slotLabelFormat;
    }

    public void setSlotLabelFormat(@Nullable String slotLabelFormat) {
        this.slotLabelFormat = slotLabelFormat;
    }

    public FullCalendarI18n withSlotLabelFormat(@Nullable String slotLabelFormat) {
        setSlotLabelFormat(slotLabelFormat);
        return this;
    }

    @Nullable
    public String getEventTimeFormat() {
        return eventTimeFormat;
    }

    public void setEventTimeFormat(@Nullable String eventTimeFormat) {
        this.eventTimeFormat = eventTimeFormat;
    }

    public FullCalendarI18n withEventTimeFormat(@Nullable String eventTimeFormat) {
        setEventTimeFormat(eventTimeFormat);
        return this;
    }

    @Nullable
    public String getMonthStartFormat() {
        return monthStartFormat;
    }

    public void setMonthStartFormat(@Nullable String monthStartFormat) {
        this.monthStartFormat = monthStartFormat;
    }

    public FullCalendarI18n withMonthStartFormat(@Nullable String monthStartFormat) {
        setMonthStartFormat(monthStartFormat);
        return this;
    }

    public FullCalendarI18n combine(@Nullable FullCalendarI18n i18n) {
        FullCalendarI18n result = new FullCalendarI18n(this);
        if (i18n == null) {
            return result;
        }
        setProperty(i18n.getDirection(), "direction", result);
        setProperty(i18n.getDayOfWeek(), "dayOfWeek", result);
        setProperty(i18n.getDayOfYear(), "dayOfYear", result);
        setProperty(i18n.getWeekText(), "weekText", result);
        setProperty(i18n.getWeekTextLong(), "weekTextLong", result);
        setProperty(i18n.getAllDayText(), "allDayText", result);
        setProperty(i18n.getMoreLinkText(), "moreLinkText", result);
        setProperty(i18n.getNoEventsText(), "noEventsText", result);
        setProperty(i18n.getCloseHint(), "closeHint", result);
        setProperty(i18n.getEventHint(), "eventHint", result);
        setProperty(i18n.getTimeHint(), "timeHint", result);
        setProperty(i18n.getNavLinkHint(), "navLinkHint", result);
        setProperty(i18n.getMoreLinkHint(), "moreLinkHint", result);
        setProperty(i18n.getDayPopoverFormat(), "dayPopoverFormat", result);
        setProperty(i18n.getDayHeaderFormat(), "dayHeaderFormat", result);
        setProperty(i18n.getWeekNumberFormat(), "weekNumberFormat", result);
        setProperty(i18n.getSlotLabelFormat(), "slotLabelFormat", result);
        setProperty(i18n.getEventTimeFormat(), "eventTimeFormat", result);
        setProperty(i18n.getMonthStartFormat(), "monthStartFormat", result);

        return result;
    }

    protected void setProperty(@Nullable Object value, String property, FullCalendarI18n i18n) {
        if (value != null) {
            Method method = ReflectionUtils.findMethod(FullCalendarI18n.class, property, value.getClass());
            if (method == null) {
                throw new IllegalStateException("Cannot find setter method for property: " + property);
            }
            ReflectionUtils.invokeMethod(method, i18n, value);
        }
    }

    /*todo rp specific options for Views*/
    /*public class DayGrid implements Serializable {
        protected String dayGridDayDayHeaderFormat;
        protected String dayGridWeekDayHeaderFormat;
        protected String dayGridMonthDayHeaderFormat;
        protected String dayGridYearDayHeaderFormat;

        protected String dayGridDayWeekNumberFormat;
        protected String dayGridWeekWeekNumberFormat;
        protected String dayGridMonthWeekNumberFormat;
        protected String dayGridYearWeekNumberFormat;

        protected String dayGridDayEventTimeFormat;
        protected String dayGridWeekEventTimeFormat;
        protected String dayGridMonthEventTimeFormat;
        protected String dayGridYearEventTimeFormat;
    }

    public class TimeGrid implements Serializable {
        protected String timeGridDayDayHeaderFormat;
        protected String timeGridWeekDayHeaderFormat;

        protected String timeGridDayWeekNumberFormat;
        protected String timeGridWeekWeekNumberFormat;

        protected String timeGridDaySlotLabelFormat;
        protected String timeGridWeekSlotLabelFormat;

        protected String timeGridDayEventTimeFormat;
        protected String timeGridWeekEventTimeFormat;
    }*/
}
