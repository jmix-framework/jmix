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

package io.jmix.ui.components.impl;

import com.vaadin.v7.ui.components.calendar.CalendarComponentEvents;
import io.jmix.core.Messages;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.Entity;
import io.jmix.core.security.UserSessionSource;
import io.jmix.ui.components.Calendar;
import io.jmix.ui.components.calendar.*;
import io.jmix.ui.components.data.calendar.EntityCalendarEventProvider;
import io.jmix.ui.widgets.CubaCalendar;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WebCalendar extends WebAbstractComponent<CubaCalendar>
        implements Calendar, InitializingBean {

    protected final String TIME_FORMAT_12H = "12H";
    protected final String TIME_FORMAT_24H = "24H";

    protected CalendarEventProvider calendarEventProvider;
    protected boolean navigationButtonsVisible = false;

    public WebCalendar() {
        component = createComponent();
    }

    protected CubaCalendar createComponent() {
        return new CubaCalendar();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
        initDefaultEventProvider(component);
    }

    protected void initComponent(CubaCalendar component) {
        Messages messages = beanLocator.get(Messages.NAME);
        String[] monthNamesShort = new String[12];
        monthNamesShort[0] = messages.getMessage("calendar.januaryCaption");
        monthNamesShort[1] = messages.getMessage("calendar.februaryCaption");
        monthNamesShort[2] = messages.getMessage("calendar.marchCaption");
        monthNamesShort[3] = messages.getMessage("calendar.aprilCaption");
        monthNamesShort[4] = messages.getMessage("calendar.mayCaption");
        monthNamesShort[5] = messages.getMessage("calendar.juneCaption");
        monthNamesShort[6] = messages.getMessage("calendar.julyCaption");
        monthNamesShort[7] = messages.getMessage("calendar.augustCaption");
        monthNamesShort[8] = messages.getMessage("calendar.septemberCaption");
        monthNamesShort[9] = messages.getMessage("calendar.octoberCaption");
        monthNamesShort[10] = messages.getMessage("calendar.novemberCaption");
        monthNamesShort[11] = messages.getMessage("calendar.decemberCaption");
        component.setMonthNamesShort(monthNamesShort);

        String[] dayNamesShort = new String[7];
        dayNamesShort[0] = messages.getMessage("calendar.sundayCaption");
        dayNamesShort[1] = messages.getMessage("calendar.mondayCaption");
        dayNamesShort[2] = messages.getMessage("calendar.tuesdayCaption");
        dayNamesShort[3] = messages.getMessage("calendar.wednesdayCaption");
        dayNamesShort[4] = messages.getMessage("calendar.thursdayCaption");
        dayNamesShort[5] = messages.getMessage("calendar.fridayCaption");
        dayNamesShort[6] = messages.getMessage("calendar.saturdayCaption");
        component.setDayNamesShort(dayNamesShort);

        if (TIME_FORMAT_12H.equals(messages.getMessage("calendar.timeFormat"))) {
            setTimeFormat(TimeFormat.FORMAT_12H);
        } else if (TIME_FORMAT_24H.equals(messages.getMessage("calendar.timeFormat"))) {
            setTimeFormat(TimeFormat.FORMAT_24H);
        } else {
            throw new IllegalStateException(
                    String.format("Can't set time format '%s'", messages.getMessage("calendar.timeFormat")));
        }

        UserSessionSource userSessionSource = beanLocator.get(UserSessionSource.NAME);
        TimeZone userTimeZone = userSessionSource.getUserSession().getTimeZone();
        if (userTimeZone != null) {
            setTimeZone(userTimeZone);
        }

        setNavigationButtonsStyle(navigationButtonsVisible);
    }

    protected void initDefaultEventProvider(CubaCalendar component) {
        calendarEventProvider = new ListCalendarEventProvider();

        component.setEventProvider(new CalendarEventProviderWrapper(calendarEventProvider));
    }

    @Override
    public void setStartDate(Date date) {
        component.setStartDate(date);
    }

    @Override
    public Date getStartDate() {
        return component.getStartDate();
    }

    @Override
    public void setEndDate(Date date) {
        component.setEndDate(date);
    }

    @Override
    public Date getEndDate() {
        return component.getEndDate();
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        component.setTimeZone(zone);
    }

    @Override
    public TimeZone getTimeZone() {
        return component.getTimeZone();
    }

    /*
    TODO: legacy-ui
    Override
    public void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setEventProvider(null);
        } else {
            CollectionDsHelper.autoRefreshInvalid(datasource, true);
            setEventProvider(new com.haulmont.cuba.web.gui.components.calendar.EntityCalendarEventProvider(datasource));
        }
    }

    @Nullable
    @Override
    public CollectionDatasource getDatasource() {
        return (calendarEventProvider instanceof com.haulmont.cuba.web.gui.components.calendar.EntityCalendarEventProvider)
                ? ((com.haulmont.cuba.web.gui.components.calendar.EntityCalendarEventProvider) calendarEventProvider)
                .getDatasource()
                : null;
    }*/

    @Override
    public void setTimeFormat(TimeFormat format) {
        if (format == TimeFormat.FORMAT_12H) {
            component.setTimeFormat(com.vaadin.v7.ui.Calendar.TimeFormat.Format12H);
        } else {
            component.setTimeFormat(com.vaadin.v7.ui.Calendar.TimeFormat.Format24H);
        }
    }

    @Override
    public TimeFormat getTimeFormat() {
        if (component.getTimeFormat() == com.vaadin.v7.ui.Calendar.TimeFormat.Format12H) {
            return TimeFormat.FORMAT_12H;
        } else {
            return TimeFormat.FORMAT_24H;
        }
    }

    @Override
    public void setFirstVisibleDayOfWeek(int firstDay) {
        component.setFirstVisibleDayOfWeek(firstDay);
    }

    @Override
    public int getFirstVisibleDayOfWeek() {
        return component.getFirstVisibleDayOfWeek();
    }

    @Override
    public void setLastVisibleDayOfWeek(int lastDay) {
        component.setLastVisibleDayOfWeek(lastDay);
    }

    @Override
    public int getLastVisibleDayOfWeek() {
        return component.getLastVisibleDayOfWeek();
    }

    @Override
    public void setFirstVisibleHourOfDay(int firstHour) {
        component.setFirstVisibleHourOfDay(firstHour);
    }

    @Override
    public int getFirstVisibleHourOfDay() {
        return component.getFirstVisibleHourOfDay();
    }

    @Override
    public void setLastVisibleHourOfDay(int lastHour) {
        component.setLastVisibleHourOfDay(lastHour);
    }

    @Override
    public int getLastVisibleHourOfDay() {
        return component.getLastVisibleHourOfDay();
    }

    @Override
    public void setFirstDayOfWeek(Integer dayOfWeek) {
        component.setFirstDayOfWeek(dayOfWeek);
    }

    @Override
    public void setWeeklyCaptionFormat(String dateFormatPattern) {
        component.setWeeklyCaptionFormat(dateFormatPattern);
    }

    @Override
    public String getWeeklyCaptionFormat() {
        return component.getWeeklyCaptionFormat();
    }

    @Override
    public void setEventProvider(CalendarEventProvider calendarEventProvider) {
        if (this.calendarEventProvider instanceof EntityCalendarEventProvider) {
            ((EntityCalendarEventProvider ) this.calendarEventProvider).unbind();
        }

        this.calendarEventProvider = calendarEventProvider;
        if (calendarEventProvider != null) {
            component.setEventProvider(new CalendarEventProviderWrapper(calendarEventProvider));
        } else {
            component.setEventProvider(new CalendarEventProviderWrapper(new ListCalendarEventProvider()));
        }
    }

    @Override
    public Subscription addDateClickListener(Consumer<CalendarDateClickEvent> listener) {
        component.setHandler(this::onDateClick);

        return getEventHub().subscribe(CalendarDateClickEvent.class, listener);
    }

    protected void onDateClick(CalendarComponentEvents.DateClickEvent event) {
        CalendarDateClickEvent calendarDateClickEvent =
                new CalendarDateClickEvent(WebCalendar.this, event.getDate());
        publish(CalendarDateClickEvent.class, calendarDateClickEvent);
    }

    @Override
    public void removeDateClickListener(Consumer<CalendarDateClickEvent> listener) {
        unsubscribe(CalendarDateClickEvent.class, listener);

        if (!hasSubscriptions(CalendarDateClickEvent.class)) {
            component.setHandler((CalendarComponentEvents.DateClickHandler) null);
        }
    }

    @Override
    public Subscription addEventClickListener(Consumer<CalendarEventClickEvent> listener) {
        component.setHandler(this::onEventClick);

        return getEventHub().subscribe(CalendarEventClickEvent.class, listener);
    }

    protected void onEventClick(CalendarComponentEvents.EventClick event) {
        com.vaadin.v7.ui.components.calendar.event.CalendarEvent calendarEvent = event.getCalendarEvent();
        if (calendarEvent instanceof CalendarEventWrapper) {
            CalendarEvent calendarEventWrapper = ((CalendarEventWrapper) calendarEvent).getCalendarEvent();
            Entity entity = null;
            if (calendarEventWrapper instanceof EntityCalendarEvent) {
                entity = ((EntityCalendarEvent) calendarEventWrapper).getEntity();
            }

            CalendarEventClickEvent calendarEventClickEvent = new CalendarEventClickEvent(
                    WebCalendar.this,
                    calendarEventWrapper,
                    entity);
            publish(CalendarEventClickEvent.class, calendarEventClickEvent);
        }
    }

    @Override
    public void removeEventClickListener(Consumer<CalendarEventClickEvent> listener) {
        unsubscribe(CalendarEventClickEvent.class, listener);

        if (!hasSubscriptions(CalendarEventClickEvent.class)) {
            component.setHandler((CalendarComponentEvents.EventClickHandler) null);
        }
    }

    @Override
    public Subscription addEventResizeListener(Consumer<CalendarEventResizeEvent> listener) {
        component.setHandler(this::onEventResize);

        return getEventHub().subscribe(CalendarEventResizeEvent.class, listener);
    }

    protected void onEventResize(CalendarComponentEvents.EventResize event) {
        com.vaadin.v7.ui.components.calendar.event.CalendarEvent calendarEvent = event.getCalendarEvent();
        if (calendarEvent instanceof CalendarEventWrapper) {
            CalendarEvent calendarEventWrapper = ((CalendarEventWrapper) calendarEvent).getCalendarEvent();
            Entity entity = null;
            if (calendarEventWrapper instanceof EntityCalendarEvent) {
                entity = ((EntityCalendarEvent) calendarEventWrapper).getEntity();
            }

            CalendarEventResizeEvent calendarEventResizeEvent = new CalendarEventResizeEvent(
                    WebCalendar.this,
                    ((CalendarEventWrapper) calendarEvent).getCalendarEvent(),
                    event.getNewStart(),
                    event.getNewEnd(),
                    entity);
            publish(CalendarEventResizeEvent.class, calendarEventResizeEvent);
        }
    }

    @Override
    public void removeEventResizeListener(Consumer<CalendarEventResizeEvent> listener) {
        unsubscribe(CalendarEventResizeEvent.class, listener);

        if (!hasSubscriptions(CalendarEventResizeEvent.class)) {
            component.setHandler((CalendarComponentEvents.EventResizeHandler) null);
        }
    }

    @Override
    public Subscription addEventMoveListener(Consumer<CalendarEventMoveEvent> listener) {
        component.setHandler(this::onEventMove);

        return getEventHub().subscribe(CalendarEventMoveEvent.class, listener);
    }

    protected void onEventMove(CalendarComponentEvents.MoveEvent event) {
        com.vaadin.v7.ui.components.calendar.event.CalendarEvent calendarEvent = event.getCalendarEvent();
        CalendarEvent calendarEventWrapper = ((CalendarEventWrapper) calendarEvent).getCalendarEvent();

        Entity entity = null;
        if (calendarEventWrapper instanceof EntityCalendarEvent) {
            entity = ((EntityCalendarEvent) calendarEventWrapper).getEntity();
        }

        CalendarEventMoveEvent calendarEventMoveEvent = new CalendarEventMoveEvent(
                WebCalendar.this,
                calendarEventWrapper,
                event.getNewStart(),
                entity);
        publish(CalendarEventMoveEvent.class, calendarEventMoveEvent);
    }

    @Override
    public void removeEventMoveListener(Consumer<CalendarEventMoveEvent> listener) {
        unsubscribe(CalendarEventMoveEvent.class, listener);

        if (!hasSubscriptions(CalendarEventMoveEvent.class)) {
            component.setHandler((CalendarComponentEvents.EventMoveHandler) null);
        }
    }

    @Override
    public Subscription addWeekClickListener(Consumer<CalendarWeekClickEvent> listener) {
        component.setHandler(this::onWeekClick);

        return getEventHub().subscribe(CalendarWeekClickEvent.class, listener);
    }

    protected void onWeekClick(CalendarComponentEvents.WeekClick event) {
        CalendarWeekClickEvent calendarWeekClickEvent = new CalendarWeekClickEvent(
                WebCalendar.this,
                event.getWeek(),
                event.getYear());
        publish(CalendarWeekClickEvent.class, calendarWeekClickEvent);
    }

    @Override
    public void removeWeekClickListener(Consumer<CalendarWeekClickEvent> listener) {
        unsubscribe(CalendarWeekClickEvent.class, listener);

        if (!hasSubscriptions(CalendarWeekClickEvent.class)) {
            component.setHandler((CalendarComponentEvents.WeekClickHandler) null);
        }
    }

    @Override
    public Subscription addForwardClickListener(Consumer<CalendarForwardClickEvent> listener) {
        component.setHandler(this::onForward);

        return getEventHub().subscribe(CalendarForwardClickEvent.class, listener);
    }

    protected void onForward(CalendarComponentEvents.ForwardEvent event) {
        CalendarForwardClickEvent calendarForwardClickEvent =
                new CalendarForwardClickEvent(WebCalendar.this);
        publish(CalendarForwardClickEvent.class, calendarForwardClickEvent);
    }

    @Override
    public void removeForwardClickListener(Consumer<CalendarForwardClickEvent> listener) {
        unsubscribe(CalendarForwardClickEvent.class, listener);

        if (!hasSubscriptions(CalendarForwardClickEvent.class)) {
            component.setHandler((CalendarComponentEvents.ForwardHandler) null);
        }
    }

    @Override
    public Subscription addBackwardClickListener(Consumer<CalendarBackwardClickEvent> listener) {
        component.setHandler(this::onBackward);

        return getEventHub().subscribe(CalendarBackwardClickEvent.class, listener);
    }

    protected void onBackward(CalendarComponentEvents.BackwardEvent event) {
        CalendarBackwardClickEvent calendarBackwardClickEvent = new CalendarBackwardClickEvent(this);
        publish(CalendarBackwardClickEvent.class, calendarBackwardClickEvent);
    }

    @Override
    public void removeBackwardClickListener(Consumer<CalendarBackwardClickEvent> listener) {
        unsubscribe(CalendarBackwardClickEvent.class, listener);

        if (!hasSubscriptions(CalendarBackwardClickEvent.class)) {
            component.setHandler((CalendarComponentEvents.BackwardHandler) null);
        }
    }

    @Override
    public Subscription addRangeSelectListener(Consumer<CalendarRangeSelectEvent> listener) {
        component.setHandler(this::onRangeSelect);

        return getEventHub().subscribe(CalendarRangeSelectEvent.class, listener);
    }

    protected void onRangeSelect(CalendarComponentEvents.RangeSelectEvent event) {
        CalendarRangeSelectEvent calendarRangeSelectEvent = new CalendarRangeSelectEvent(
                this,
                event.getStart(),
                event.getEnd());
        publish(CalendarRangeSelectEvent.class, calendarRangeSelectEvent);
    }

    @Override
    public void removeRangeSelectListener(Consumer<CalendarRangeSelectEvent> listener) {
        unsubscribe(CalendarRangeSelectEvent.class, listener);

        if (!hasSubscriptions(CalendarRangeSelectEvent.class)) {
            component.setHandler((CalendarComponentEvents.RangeSelectHandler) null);
        }
    }

    @Override
    public CalendarEventProvider getEventProvider() {
        return ((CalendarEventProviderWrapper) component.getEventProvider()).getCalendarEventProvider();
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);
        setNavigationButtonsStyle(navigationButtonsVisible);
    }

    @Override
    public void setNavigationButtonsVisible(boolean value) {
        navigationButtonsVisible = value;
        setNavigationButtonsStyle(value);
    }

    @Override
    public boolean isNavigationButtonsVisible() {
        return navigationButtonsVisible;
    }

    protected void setNavigationButtonsStyle(boolean value) {
        if (!value) {
            addStyleName("navbuttons-disabled");
        } else {
            removeStyleName("navbuttons-disabled");
        }
    }

    @Override
    public Map<DayOfWeek, String> getDayNames() {
        List<String> days = Arrays.asList(component.getDayNamesShort().clone());

        int shift = Math.abs(component.getFirstDayOfWeek() - java.util.Calendar.MONDAY) + 1;
        Collections.rotate(days, -shift);

        return days.stream().collect(Collectors.toMap(
                (String d) -> DayOfWeek.of(days.indexOf(d) + 1),
                d -> d
        ));
    }

    @Override
    public void setDayNames(Map<DayOfWeek, String> dayNames) {
        Preconditions.checkNotNullArgument(dayNames);

        if (!CollectionUtils.isEqualCollection(Arrays.asList(DayOfWeek.values()), dayNames.keySet())) {
            throw new IllegalArgumentException("Day names map doesn't contain all required values");
        }

        List<String> daysList = Arrays.stream(DayOfWeek.values())
                .map(dayNames::get)
                .collect(Collectors.toList());

        int shift = Math.abs(component.getFirstDayOfWeek() - java.util.Calendar.MONDAY) + 1;
        Collections.rotate(daysList, shift);

        String[] days = new String[7];
        component.setDayNamesShort(daysList.toArray(days));
    }

    @Override
    public Map<Month, String> getMonthNames() {
        List<String> months = Arrays.asList(component.getMonthNamesShort());

        return months.stream().collect(Collectors.toMap(
                (String m) -> Month.of(months.indexOf(m) + 1),
                m -> m
        ));
    }

    @Override
    public void setMonthNames(Map<Month, String> monthNames) {
        Preconditions.checkNotNullArgument(monthNames);

        if (!CollectionUtils.isEqualCollection(Arrays.asList(Month.values()), monthNames.keySet())) {
            throw new IllegalArgumentException("Month names map doesn't contain all required values");
        }

        String[] months = Arrays.stream(Month.values())
                .map(monthNames::get)
                .toArray(String[]::new);

        component.setMonthNamesShort(months);
    }
}
