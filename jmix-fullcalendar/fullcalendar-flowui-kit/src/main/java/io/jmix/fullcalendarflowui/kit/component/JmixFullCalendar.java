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

package io.jmix.fullcalendarflowui.kit.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import io.jmix.flowui.kit.meta.StudioIgnore;
import io.jmix.fullcalendarflowui.kit.component.event.dom.*;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import io.jmix.fullcalendarflowui.kit.component.model.AbstractCalendarDisplayModeProperties;
import io.jmix.fullcalendarflowui.kit.component.model.option.JmixFullCalendarOptions;
import io.jmix.fullcalendarflowui.kit.component.serialization.JmixFullCalendarDeserializer;
import io.jmix.fullcalendarflowui.kit.component.serialization.JmixFullCalendarSerializer;
import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Tag("jmix-full-calendar")
@NpmPackage(value = "@fullcalendar/core", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/interaction", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/daygrid", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/timegrid", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/list", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/multimonth", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/moment-timezone", version = "6.1.15")
@NpmPackage(value = "@fullcalendar/moment", version = "6.1.15")
@NpmPackage(value = "moment", version = "2.30.1")
@JsModule("./src/fullcalendar/jmix-full-calendar.js")
@CssImport("./src/fullcalendar/jmix-full-calendar.css")
public class JmixFullCalendar extends Component implements HasSize, HasStyle {

    protected JmixFullCalendarSerializer serializer;
    protected JmixFullCalendarDeserializer deserializer;
    protected JmixFullCalendarOptions options;

    protected JsonFactory jsonFactory;

    protected CalendarDisplayMode displayMode;
    protected LocalDate currentDate;

    protected Map<String, StateTree.ExecutionRegistration> itemsDataProvidersExecutionMap = new HashMap<>(2);
    protected Map<String, StateTree.ExecutionRegistration> callbackDataProvidersExecutionMap = new HashMap<>(2);
    protected StateTree.ExecutionRegistration synchronizeOptionsExecution;
    protected StateTree.ExecutionRegistration incrementalUpdateExecution;

    protected Registration datesSetDomRegistration;
    protected Registration moreLinkClickDomRegistration;
    protected Registration eventClickDomRegistration;
    protected Registration eventMouseEnterDomRegistration;
    protected Registration eventMouseLeaveDomRegistration;
    protected Registration eventDropDomRegistration;
    protected Registration eventResizeDomRegistration;
    protected Registration dateClickDomRegistration;
    protected Registration selectDomRegistration;
    protected Registration unselectDomRegistration;
    protected Registration dayNavigationLinkClickDomRegistration;
    protected Registration weekNavigationLinkClickDomRegistration;

    protected boolean initialized = false;

    public JmixFullCalendar() {
        serializer = createSerializer();
        deserializer = createDeserializer();
        options = createOptions();
        jsonFactory = createJsonFactory();

        attachCalendarOptionChangeListener();
        attachDatesSetDomEventListener();
        attachMoreLinkClickDomEventListener();
        attachDayNavigationLinkClickDomEventListener();
        attachWeekNavigationLinkClickDomEventListener();
    }

    /**
     * @return initial calendar display mode or {@code null} if not set
     */
    @Nullable
    public CalendarDisplayMode getInitialCalendarDisplayMode() {
        return options.getInitialDisplayMode().getValue();
    }

    /**
     * Sets initial calendar display mode that will be shown after attaching component to th UI.
     * <p>
     * The default value is {@link CalendarDisplayModes#DAY_GRID_MONTH}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param displayMode initial calendar display mode
     */
    @StudioIgnore
    public void setInitialCalendarDisplayMode(CalendarDisplayMode displayMode) {
        Objects.requireNonNull(displayMode);

        options.getInitialDisplayMode().setValue(displayMode);
    }

    /**
     * @return current calendar display mode
     */
    public CalendarDisplayMode getCurrentCalendarDisplayMode() {
        if (displayMode != null) {
            return displayMode;
        }
        CalendarDisplayMode initialDisplayMode = options.getInitialDisplayMode().getValue();
        if (initialDisplayMode != null) {
            return initialDisplayMode;
        }
        CalendarDisplayMode defaultDisplayMode = options.getInitialDisplayMode().getDefaultValue();
        return Objects.requireNonNull(defaultDisplayMode);
    }

    /**
     * Switches currently shown display mode to the provided one.
     *
     * @param displayMode calendar display mode to set
     */
    @StudioIgnore
    public void setCalendarDisplayMode(CalendarDisplayMode displayMode) {
        Objects.requireNonNull(displayMode);

        getElement().executeJs("this.calendar.changeView($0)", displayMode.getId());
    }

    /**
     * Adds custom display mode to the calendar. Then custom display mode can be shown by:
     * <ul>
     *     <li>
     *          {@link #setCalendarDisplayMode(CalendarDisplayMode)}
     *     </li>
     *     <li>
     *          {@link #setInitialCalendarDisplayMode(CalendarDisplayMode)}.
     *     </li>
     * </ul>
     * Note that it is initial option and dynamically changing/adding/removing custom display modes will not apply
     * after attaching component to the UI.
     *
     * @param displayMode custom calendar display mode to add
     */
    public void addCustomCalendarDisplayMode(CustomCalendarDisplayMode displayMode) {
        Objects.requireNonNull(displayMode);

        options.getDisplayModes().addCustomCalendarDisplayMode(displayMode);
    }

    /**
     * Removes custom calendar display mode.
     * <p>
     * Note that it is initial option and dynamically changing/adding/removing custom display modes will not apply
     * after attaching component to UI.
     *
     * @param customDisplayMode custom calendar display mode to remove
     */
    public void removeCustomCalendarDisplayMode(CustomCalendarDisplayMode customDisplayMode) {
        Objects.requireNonNull(customDisplayMode);

        options.getDisplayModes().removeCustomCalendarDisplayMode(customDisplayMode);
    }

    /**
     * Returns custom calendar display mode by its ID.
     *
     * @param id the ID of custom calendar display mode
     * @return custom calendar display mode or {@code null} if there is no display mode with the provided ID
     */
    @Nullable
    public CustomCalendarDisplayMode getCustomCalendarDisplayMode(String id) {
        return options.getDisplayModes().getCustomCalendarDisplayMode(id);
    }

    /**
     * @return list of custom calendar display modes added to the component
     */
    public List<CustomCalendarDisplayMode> getCustomCalendarDisplayModes() {
        return options.getDisplayModes().getCustomCalendarDisplayModes();
    }

    /**
     * Returns properties for the specific calendar display mode. Almost all calendar display modes have
     * a specific set of properties. Moreover, the properties object can override some properties
     * from the component.
     *
     * @param displayMode calendar display mode to get properties
     * @param <T>         type of display mode properties
     * @return properties object that correspond to the provided calendar display mode
     */
    public <T extends AbstractCalendarDisplayModeProperties> T getCalendarDisplayModeProperties(
            CalendarDisplayModes displayMode) {
        return options.getDisplayModes().getCalendarDisplayModeProperties(displayMode);
    }

    /**
     * @return {@code true} if week numbers are visible
     */
    public boolean isWeekNumbersVisible() {
        return options.getWeekNumbers().getNotNullValue();
    }

    /**
     * Sets whether week numbers should be displayed on the calendar.
     * <p>
     * The default value is {@code false}.
     *
     * @param weekNumbersVisible whether to show week numbers
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        options.getWeekNumbers().setValue(weekNumbersVisible);
    }

    /**
     * @return the start date of valid range or {@code null} if not set
     */
    @Nullable
    public LocalDate getValidRangeStart() {
        return options.getValidRange().getStart();
    }

    /**
     * Sets the start date of valid range.
     * <p>
     * See for more information {@link #setValidRange(LocalDate, LocalDate)}.
     *
     * @param start the start date or {@code null} to reset value
     */
    public void setValidRangeStart(@Nullable LocalDate start) {
        options.getValidRange().setStart(start);
    }

    /**
     * @return the end date of valid range or {@code null} if not set
     */
    @Nullable
    public LocalDate getValidRangeEnd() {
        return options.getValidRange().getEnd();
    }

    /**
     * Sets the end date of valid range.
     * <p>
     * See for more information {@link #setValidRange(LocalDate, LocalDate)}.
     *
     * @param end the end date or {@code null} to reset value
     */
    public void setValidRangeEnd(@Nullable LocalDate end) {
        options.getValidRange().setEnd(end);
    }


    /**
     * Sets the date range where the user can navigate and where events can be displayed.
     * <p>
     * Dates outside the valid range will be grayed-out. The user will not be able to drag or resize
     * events into these areas.
     * <p>
     * The end date is exclusive. For example, if valid range is {@code 2024-09-01} to {@code 2024-09-10},
     * the September 10th day is not included in the range and will be disabled.
     * <p>
     * Navigation methods won't navigate to invalid range.
     *
     * @param start start date of valid range or {@code null} to reset value
     * @param end   end date of valid range or {@code null} to reset value
     */
    public void setValidRange(@Nullable LocalDate start, @Nullable LocalDate end) {
        options.getValidRange().setRange(start, end);
    }

    /**
     * @return start date of visible range or {@code null} if not set
     */
    @Nullable
    public LocalDate getVisibleRangeStart() {
        return options.getVisibleRange().getStart();
    }

    /**
     * @return end date of visible range or {@code null} if not set
     */
    @Nullable
    public LocalDate getVisibleRangeEnd() {
        return options.getVisibleRange().getEnd();
    }

    /**
     * The visible date range is applied together with generic calendar display modes
     * {@link GenericCalendarDisplayModes}. For instance, you can set {@link GenericCalendarDisplayModes#DAY_GRID},
     * visible range from {@code 2024-09-01} to {@code 2024-09-03} and component will show two days.
     * <p>
     * Note the end date is exclusive.
     *
     * @param start start date of visible range
     * @param end   end date of visible range
     */
    public void setVisibleRange(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        options.getVisibleRange().setRange(start, end);
    }

    /**
     * @return component's timezone or {@code null} if not set
     */
    @Nullable
    public TimeZone getTimeZone() {
        return options.getTimeZone().getValue();
    }

    /**
     * Sets the timezone to the component.
     * <p>
     * The default value is user's timezone. If user does not have a timezone, the system default will be used.
     *
     * @param timeZone timezone to set
     */
    public void setTimeZone(@Nullable TimeZone timeZone) {
        options.getTimeZone().setValue(timeZone != null ? timeZone : TimeZone.getDefault());
    }

    /**
     * Returns a date for the current date of the calendar.
     * <p>
     * For month view, it will always be a date between the first and last day of the month. For week views,
     * it will always be a date between the first and last day of the week.
     *
     * @return the current date of the calendar
     */
    public LocalDate getDate() {
        return currentDate;
    }

    /**
     * Moves the calendar one step forward. For instance:
     * <ul>
     *     <li>
     *         For {@link CalendarDisplayModes#DAY_GRID_DAY} and other day display modes calendar will be moved
     *         one day forward.
     *     </li>
     *     <li>
     *         For {@link CalendarDisplayModes#DAY_GRID_WEEK} and other week display modes calendar will be moved
     *         one week forward.
     *     </li>
     *     <li>
     *         And so on.
     *     </li>
     * </ul>
     * The duration of {@link CustomCalendarDisplayMode} also will be respected.
     */
    public void navigateToNext() {
        getElement().callJsFunction("navigateToNext");
    }

    /**
     * Moves the calendar forward by the specified duration.
     *
     * @param duration the duration to forward to
     */
    public void navigateToNext(CalendarDuration duration) {
        getElement().callJsFunction("incrementDate", serializer.serializeCalendarDuration(duration));
    }

    /**
     * Moves the calendar one step back. For instance:
     * <ul>
     *     <li>
     *         For {@link CalendarDisplayModes#DAY_GRID_DAY} and other day display modes calendar will be moved
     *         one day back.
     *     </li>
     *     <li>
     *         For {@link CalendarDisplayModes#DAY_GRID_WEEK} and other week display modes calendar will be moved
     *         one week back.
     *     </li>
     *     <li>
     *         And so on.
     *     </li>
     * </ul>
     * The duration of {@link CustomCalendarDisplayMode} also will be respected.
     */
    public void navigateToPrevious() {
        getElement().callJsFunction("navigateToPrevious");
    }

    /**
     * Moves the calendar back by the specified duration.
     *
     * @param duration the duration to back to
     */
    public void navigateToPrevious(CalendarDuration duration) {
        getElement().callJsFunction("incrementDate",
                serializer.serializeCalendarDuration(inverseDuration(duration)));
    }

    /**
     * Moves calendar to the current date.
     */
    public void navigateToToday() {
        getElement().callJsFunction("navigateToToday");
    }

    /**
     * Moves calendar to the specified date.
     *
     * @param date the date to move calendar to
     */
    public void navigateToDate(LocalDate date) {
        Objects.requireNonNull(date);
        getElement().callJsFunction("navigateToDate", date.toString());
    }

    /**
     * Scrolls to the specified time in milliseconds.
     * <p>
     * Scrolling to a specific time works when the calendar display mode is either
     * {@link CalendarDisplayModes#TIME_GRID_DAY} or {@link CalendarDisplayModes#TIME_GRID_WEEK}.
     *
     * @param duration the time duration
     */
    public void scrollToTime(Long duration) {
        Objects.requireNonNull(duration);
        getElement().callJsFunction("scrollToTimeMs", duration.toString());
    }

    /**
     * Scrolls to the specified time.
     * <p>
     * Scrolling to a specific time works when the calendar display mode is either
     * {@link CalendarDisplayModes#TIME_GRID_DAY} or {@link CalendarDisplayModes#TIME_GRID_WEEK}.
     *
     * @param localTime the time to scroll
     */
    public void scrollToTime(LocalTime localTime) {
        Objects.requireNonNull(localTime);
        getElement().callJsFunction("scrollToTime", localTime.toString());
    }

    /**
     * Selects a time range that starts from specified time to next 24h.
     *
     * @param start date-time to select
     */
    public void select(LocalDateTime start) {
        select(start, false);
    }

    /**
     * Selects a time range that starts from specified time to next 24h. If all-day is {@code true} the all-day cell
     * will be selected.
     *
     * @param start  date-time to select
     * @param allDay whether to select all-day cell
     */
    public void select(LocalDateTime start, boolean allDay) {
        Objects.requireNonNull(start);
        getElement().callJsFunction("select", allDay, serializer.serializeDateTime(start));
    }

    /**
     * Selects the date-time range.
     *
     * @param start start date-time of selection
     * @param end   end date-time of selection
     */
    public void select(LocalDateTime start, LocalDateTime end) {
        select(start, end, false);
    }

    /**
     * Selects the date-time range. If all-day is {@code true} the all-day cell(s) will be selected.
     *
     * @param start  start date-time of selection
     * @param end    end date-time of selection
     * @param allDay whether to select all-day cell(s)
     */
    public void select(LocalDateTime start, LocalDateTime end, boolean allDay) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        getElement().callJsFunction("select",
                allDay,
                serializer.serializeDateTime(start),
                serializer.serializeDateTime(end));
    }

    /**
     * Selects the date range of all-day cells.
     *
     * @param start start date of selection
     * @param end   end date of selection
     */
    public void select(LocalDate start, LocalDate end) {
        select(start, end, true);
    }

    /**
     * Selects the date range. If all-day is {@code true} the all-day cell(s) will be selected.
     *
     * @param start  start date of selection
     * @param end    end date of selection
     * @param allDay whether to select all-day cell(s)
     */
    public void select(LocalDate start, LocalDate end, boolean allDay) {
        select(LocalDateTime.of(start, LocalTime.MIDNIGHT), LocalDateTime.of(end, LocalTime.MIDNIGHT), allDay);
    }

    /**
     * Clears the current selection.
     */
    public void unselect() {
        getElement().executeJs("this.calendar.unselect()");
    }

    /**
     * @return {@code true} if navigation to date by day and week names is enabled
     */
    public boolean isNavigationLinksEnabled() {
        return options.getNavLinks().getNotNullValue();
    }

    /**
     * Enables navigation to date using day names and week names.
     * <p>
     * The default value is {@code false}.
     *
     * @param enabled whether to enable navigation
     */
    public void setNavigationLinksEnabled(boolean enabled) {
        options.getNavLinks().setValue(enabled);
    }

    /**
     * @return {@code true} if the component should limit the number of events by height of the day cell
     */
    public boolean isDefaultDayMaxEventRowsEnabled() {
        return options.getDayMaxEventRows().isDefaultEnabled();
    }

    /**
     * Limits the maximum number of stacked events within a given day.
     * <ul>
     *     <li>
     *         <strong>{@code true}</strong> - limits the number of events by height of the day cell.
     *     </li>
     *     <li>
     *         <strong>{@code false}</strong> - component displays all events as is.
     *     </li>
     * </ul>
     * This property is applied for day-grid display modes and in all-day cells.
     * <p>
     * The default value {@code false}.
     *
     * @param enabled whether to limit the number of events
     */
    public void setDefaultDayMaxEventRowsEnabled(boolean enabled) {
        options.getDayMaxEventRows().setEnabled(enabled);
    }

    /**
     * @return the maximum rows count of events including "more" row or {@code null} if not set
     */
    @Nullable
    public Integer getDayMaxEventRows() {
        return options.getDayMaxEventRows().getMax();
    }

    /**
     * Sets rows count of events in day-grid display modes and in "all-day" section in time-grid display modes.
     * <p>
     * Note that "more" row is included to count. For instance, if dayMaxEventRows = 1, only the "more" row
     * will be shown.
     * <p>
     * Takes precedence over the {@link #setDefaultDayMaxEventRowsEnabled(boolean)}.
     *
     * @param maxEventRows maximum value of event rows in cell
     */
    public void setDayMaxEventRows(@Nullable Integer maxEventRows) {
        options.getDayMaxEventRows().setMax(maxEventRows);
    }

    /**
     * @return {@code true} if the component should limit the number of events by height of the day cell
     */
    public boolean isDefaultDayMaxEventsEnabled() {
        return options.getDayMaxEvents().isDefaultEnabled();
    }

    /**
     * Limits the maximum number of stacked events within a given day.
     * <ul>
     *     <li>
     *         <strong>{@code true}</strong> - limits the number of events by height of the day cell.
     *     </li>
     *     <li>
     *         <strong>{@code false}</strong> - component displays all events as is.
     *     </li>
     * </ul>
     * This property is applied for day-grid display modes and in all-day cells.
     * <p>
     * The default value {@code false}.
     *
     * @param enabled whether to limit the number of events
     */
    public void setDefaultDayMaxEventsEnabled(boolean enabled) {
        options.getDayMaxEvents().setDefaultEnabled(enabled);
    }

    /**
     * @return the maximum rows count of events excluding "more" row or {@code null} if not set
     */
    @Nullable
    public Integer getDayMaxEvents() {
        return options.getDayMaxEvents().getMax();
    }

    /**
     * Sets rows count of events in day-grid display modes and in "all-day" section in time-grid display modes.
     * <p>
     * Note that "more" row <strong>is not included</strong> to count. For instance, if dayMaxEventRows = 1,
     * one event and the "more" row will be shown.
     * <p>
     * Takes precedence over the {@link #setDefaultDayMaxEventsEnabled(boolean)}.
     *
     * @param maxRows maximum value of event rows in cell excluding "more" row
     */
    public void setDayMaxEvents(@Nullable Integer maxRows) {
        options.getDayMaxEvents().setMax(maxRows);
    }

    /**
     * @return maximum number of events to stack or {@code null} if not set
     */
    @Nullable
    public Integer getEventMaxStack() {
        return options.getEventMaxStack().getValue();
    }

    /**
     * Sets the maximum number of events that stack top-to-bottom for time-grid display modes.
     *
     * @param eventMaxStack the maximum number of events that stack. The {@code null} or {@code -1}
     *                      values set default behaviour.
     */
    public void setEventMaxStack(@Nullable Integer eventMaxStack) {
        if (eventMaxStack != null && eventMaxStack < -1) {
            throw new IllegalArgumentException("Event max stack value must be >= -1");
        }
        options.getEventMaxStack().setValue(eventMaxStack);
    }

    /**
     * @return the calendar display mode that should be shown when "more" more link is clicked
     * or {@code null} if not set
     */
    @Nullable
    public CalendarDisplayMode getMoreLinkCalendarDisplayMode() {
        return options.getMoreLinkClick().getCalendarView();
    }

    /**
     * Sets the calendar display mode that should be shown when "more" link is clicked.
     *
     * @param displayMode the display mode that should be opened
     */
    @StudioIgnore
    public void setMoreLinkCalendarDisplayMode(@Nullable CalendarDisplayMode displayMode) {
        options.getMoreLinkClick().setCalendarView(displayMode);
    }

    /**
     * @return list of CSS class names added to "more" link
     */
    public List<String> getMoreLinkClassNames() {
        return options.getMoreLinkClassNames().getClassNames();
    }

    /**
     * Sets CSS class names that should be added to "more" link.
     *
     * @param classNames class names to set
     */
    public void setMoreLinkClassNames(@Nullable List<String> classNames) {
        options.getMoreLinkClassNames().setClassNames(classNames);
    }

    /**
     * Adds CSS class name to "more" link.
     *
     * @param className class name to add
     */
    public void addMoreLinkClassName(String className) {
        Objects.requireNonNull(className);

        options.getMoreLinkClassNames().addClassName(className);
    }

    /**
     * Adds class names to "more" link.
     *
     * @param classNames class names to add
     */
    public void addMoreLinkClassNames(String... classNames) {
        Objects.requireNonNull(classNames);

        options.getMoreLinkClassNames().addClassNames(classNames);
    }

    /**
     * Removes CSS class name from "more" link.
     *
     * @param className class name to remove
     */
    public void removeMoreLinkClassName(String className) {
        Objects.requireNonNull(className);

        options.getMoreLinkClassNames().removeClassName(className);
    }

    /**
     * Removes all CSS class names from "more" link.
     */
    public void removeAllMoreLinkClassName() {
        options.getMoreLinkClassNames().removeAllClassNames();
    }

    /**
     * @return {@code true} if event start time is editable
     */
    public boolean isEventStartEditable() {
        return options.getEventStartEditable().getNotNullValue();
    }

    /**
     * Enables to edit event start time using dragging.
     * <p>
     * Note, this property can be overridden on per-event by {@code CalendarEvent#getStartEditable()} property.
     * <p>
     * The default value is {@code false}.
     *
     * @param editable whether to enable editing event start time
     */
    public void setEventStartEditable(boolean editable) {
        options.getEventStartEditable().setValue(editable);
    }

    /**
     * @return {@code true} if event duration is editable
     */
    public boolean isEventDurationEditable() {
        return options.getEventDurationEditable().getNotNullValue();
    }

    /**
     * Enables to edit event duration using resizing.
     * <p>
     * Note, this property can be overridden on per-event by {@code durationEditable} property.
     * <p>
     * The default value is {@code false}.
     *
     * @param editable whether to edit event duration
     */
    public void setEventDurationEditable(boolean editable) {
        options.getEventDurationEditable().setValue(editable);
    }

    /**
     * @return {@code true} if an event is resizable from start date
     */
    public boolean isEventResizableFromStart() {
        return options.getEventResizableFromStart().getNotNullValue();
    }

    /**
     * Enables to resize an event from its starting date.
     * <p>
     * The default value is {@code false}.
     *
     * @param resizableFromStart whether to resize an event from start
     */
    public void setEventResizableFromStart(boolean resizableFromStart) {
        options.getEventResizableFromStart().setValue(resizableFromStart);
    }

    /**
     * @return drag minimum distance in pixels or {@code null} if not set
     */
    @Nullable
    public Integer getEventDragMinDistance() {
        return options.getEventDragMinDistance().getValue();
    }

    /**
     * Sets how many pixels the user’s mouse/touch must move before an event drag activates.
     * <p>
     * The default value is {@code 5}.
     *
     * @param minDistance minimum distance in pixels
     */
    public void setEventDragMinDistance(@Nullable Integer minDistance) {
        options.getEventDragMinDistance().setValue(minDistance);
    }

    /**
     * @return duration in milliseconds or {@code null} if not set
     */
    @Nullable
    public Integer getDragRevertDuration() {
        return options.getDragRevertDuration().getValue();
    }

    /**
     * Sets the time it takes for an event to revert to its original position after an unsuccessful drag.
     * <p>
     * The default value is {@code 500}.
     *
     * @param revertDuration duration in milliseconds
     */
    public void setDragRevertDuration(@Nullable Integer revertDuration) {
        options.getDragRevertDuration().setValue(revertDuration);
    }

    /**
     * @return {@code true} if component scrolls content during event drag-and-drop and date selecting
     */
    public boolean isDragScroll() {
        return options.getDragScroll().getNotNullValue();
    }

    /**
     * Enables to automatically scroll the scroll-containers during event drag-and-drop and date selecting.
     * <p>
     * The default value is {@code true}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param enabled whether to enable scrolling
     */
    public void setDragScroll(boolean enabled) {
        options.getDragScroll().setValue(enabled);
    }

    /**
     * @return snap duration or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getSnapDuration() {
        return options.getSnapDuration().getValue();
    }

    /**
     * Sets a value that determines the time interval at which a dragged event will snap to the time axis.
     * This also affects the granularity at which selections can be made.
     * <p>
     * The default value is taken from {@link #getSlotDuration()}.
     *
     * @param snapDuration duration to set
     */
    public void setSnapDuration(@Nullable CalendarDuration snapDuration) {
        options.getSnapDuration().setValue(snapDuration);
    }

    /**
     * @return {@code true} if the duration will remain roughly the same before and after
     * it is dragged to or from an all-day section
     */
    public boolean isAllMaintainDurationEnabled() {
        return options.getAllDayMaintainDuration().getNotNullValue();
    }

    /**
     * Determines how an event’s duration should be mutated when it is dragged from a timed section
     * to an all-day section and vice versa.
     * <ul>
     *     <li>
     *         <strong>{@code true}</strong> - the duration will remain roughly the same before and after
     *         it is dragged to or from an all-day section. "Roughly" because if an event has a duration
     *         with hourly precision, it will be rounded down to the nearest whole-day.
     *     </li>
     *     <li>
     *         <strong>{@code false}</strong> - the event’s duration will be reset to
     *         {@link #getDefaultAllDayEventDuration()} if it is being dropped in an all-day section or
     *         {@link #getDefaultTimedEventDuration()} if it is being dropped in a timed section
     *     </li>
     * </ul>
     * The default value is {@code false}.
     *
     * @param enabled whether to maintain duration
     */
    public void setAllDayMaintainDurationEnabled(boolean enabled) {
        options.getAllDayMaintainDuration().setValue(enabled);
    }

    /**
     * @return {@code true} if events can overlap other events during dragging and resizing
     */
    public boolean isEventOverlap() {
        return options.getEventOverlap().isEnabled();
    }

    /**
     * Allows events to overlap each other during dragging and resizing.
     * <p>
     * The default value is {@code true}.
     *
     * @param enabled whether to enable event overlap
     */
    public void setEventOverlap(boolean enabled) {
        options.getEventOverlap().setEnabled(enabled);
    }

    /**
     * @return event overlap JavaScript function or {@code null} if not set
     */
    @Nullable
    public JsFunction getEventOverlapJsFunction() {
        return options.getEventOverlap().getJsFunction();
    }

    /**
     * Sets JavaScript function definition that will be executed every time when an event is dragged/resized
     * to cell with other events. The function should return {@code true} if an event can overlap other event or
     * {@code false} otherwise.
     * <p>
     * For instance, function enables event overlapping if events are all-day:
     * <pre>{@code
     * calendar.setEventOverlapJsFunction(new JsFunction("""
     *         function(stillEvent, movingEvent) {
     *             return stillEvent.allDay && movingEvent.allDay;
     *         }
     *         """));
     * }</pre>
     * The {@code stillEvent} is the event underneath the moving event.
     * <p>
     * The {@code movingEvent} is the event that is being dragged or resized. Its start and end dates will remain
     * at their original values when the function is called.
     * <p>
     * The available properties of {@code movingEvent} and {@code stillEvent} you can find in
     * <a href="https://fullcalendar.io/docs/event-object">FullCalendar docs</a>
     * <p>
     * Note that JavaScript function takes precedence over the {@link #setEventOverlap(boolean)}. And
     * JavaScript function can be overridden by calendar event's "overlap" property value.
     *
     * @param jsFunction JavaScript function
     */
    public void setEventOverlapJsFunction(@Nullable JsFunction jsFunction) {
        options.getEventOverlap().setJsFunction(jsFunction);
    }

    /**
     * @return {@code true} if day or time selection is enabled
     */
    public boolean isSelectionEnabled() {
        return options.getSelectable().getNotNullValue();
    }

    /**
     * Allows the user to highlight multiple days or timeslots by clicking and dragging.
     * <p>
     * The default value is {@code false}.
     *
     * @param enabled whether to enable selection
     */
    public void setSelectionEnabled(boolean enabled) {
        options.getSelectable().setValue(enabled);
    }

    /**
     * @return {@code true} if component draws an event while the users is dragging
     */
    public boolean isSelectMirror() {
        return options.getSelectMirror().getNotNullValue();
    }

    /**
     * Enables the drawing of a placeholder event while the user is dragging.
     * <p>
     * This property applies to time-grid display modes.
     * <p>
     * The default value is {@code false}.
     *
     * @param selectMirror whether to draw a placeholder event
     */
    public void setSelectMirror(boolean selectMirror) {
        options.getSelectMirror().setValue(selectMirror);
    }

    /**
     * @return {@code true} if selection should be cleared when clicking on the page
     */
    public boolean isUnselectAuto() {
        return options.getUnselectAuto().getNotNullValue();
    }

    /**
     * Sets whether clicking elsewhere on the page will clear the current selection.
     * Works only if {@link #isSelectionEnabled()} is {@code true}.
     * <p>
     * The default value is {@code true}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param unselectAuto whether option is unselectAuto
     */
    public void setUnselectAuto(boolean unselectAuto) {
        options.getUnselectAuto().setValue(unselectAuto);
    }

    /**
     * @return the CSS selector or {@code null} if not set
     */
    @Nullable
    public String getUnselectCancelSelector() {
        return options.getUnselectCancel().getValue();
    }

    /**
     * Sets the CSS selector that will ignore the {@link #isUnselectAuto()} property. For instance:
     * <p>
     * {@code vaadin-form-layout .custom-text-input}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param cssSelector CSS selector, e.g. ".my-element"
     */
    public void setUnselectCancelSelector(@Nullable String cssSelector) {
        options.getUnselectCancel().setValue(cssSelector);
    }

    /**
     * @return {@code true} if selection of cells with events is enabled
     */
    public boolean isSelectOverlap() {
        return options.getSelectOverlap().isEnabled();
    }

    /**
     * Enables to select cells that contains events.
     * <p>
     * The default value is {@code true}.
     *
     * @param selectOverlap whether to enable selecting cells with events
     */
    public void setSelectOverlap(boolean selectOverlap) {
        options.getSelectOverlap().setEnabled(selectOverlap);
    }

    /**
     * @return select overlap function or {@code null} if not set
     */
    @Nullable
    public JsFunction getSelectOverlapJsFunction() {
        return options.getSelectOverlap().getJsFunction();
    }

    /**
     * Sets JavaScript function definition that will be executed every time when the user selection tries to occupy
     * cells with events. The function should return {@code true} if a selection can be performed for cell with the
     * provided event or {@code false} otherwise.
     * <p>
     * For instance, function enables selection if event is a background:
     * <pre>{@code
     * calendar.setSelectOverlapJsFunction(new JsFunction("""
     *         function (event) {
     *             return event.display === 'background';
     *         }
     *         """));
     * }</pre>
     * The {@code event} is the event in the cell.
     * <p>
     * The available properties of {@code event}  you can find in
     * <a href="https://fullcalendar.io/docs/event-object">FullCalendar docs</a>
     *
     * @param jsFunction JavaScript function
     */
    public void setSelectOverlapJsFunction(@Nullable JsFunction jsFunction) {
        options.getSelectOverlap().setJsFunction(jsFunction);
    }

    /**
     * @return select allow JavaScript function or {@code null} if not set
     */
    @Nullable
    public JsFunction getSelectAllowJsFunction() {
        return options.getSelectAllow().getValue();
    }

    /**
     * Sets JavaScript function definition that will be executed every time when the user drags selection to cells.
     * The function should return {@code true} if a selection can be performed for cell with the
     * provided date range or {@code false} otherwise.
     * <p>
     * For instance, the function enables selection for today's date and futures:
     * <pre>{@code
     * calendar.setSelectAllowJsFunction(new JsFunction("""
     *         function(selectionInfo) {
     *             const currentDate = new Date();
     *             currentDate.setHours(0, 0, 0, 0);
     *             return selectionInfo.start >= currentDate && currentDate < selectionInfo.end;
     *         }
     *         """)
     * }</pre>
     * The {@code selectionInfo} is object that contains information about date range.
     * <p>
     * The available properties of {@code selectionInfo}  you can find in
     * <a href="https://fullcalendar.io/docs/selectAllow">FullCalendar docs</a>
     * <p>
     *
     * @param jsFunction JavaScript function
     */
    public void setSelectAllowJsFunction(@Nullable JsFunction jsFunction) {
        options.getSelectAllow().setValue(jsFunction);
    }

    /**
     * @return the minimum distance the user’s mouse must travel after a mousedown, before a selection is allowed
     */
    public int getSelectMinDistance() {
        return options.getSelectMinDistance().getNotNullValue();
    }

    /**
     * Sets the minimum distance the user’s mouse must travel after a mousedown, before a selection is allowed.
     * A non-zero value is useful for differentiating a selection from a date click event.
     * <p>
     * This property is only applicable to mouse-related interaction. For touch interaction, see
     * {@link #setSelectLongPressDelay(Integer)}.
     * <p>
     * The default value is {@code 0}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param minDistance minimum distance in pixels
     */
    public void setSelectMinDistance(int minDistance) {
        options.getSelectMinDistance().setValue(minDistance);
    }

    /**
     * @return the day popover format or {@code null} if not set
     */
    @Nullable
    public String getDefaultDayPopoverFormat() {
        return options.getDayPopoverFormat().getValue();
    }

    /**
     * Sets the date format of title of the popover that is shown when "more" link is clicked. By default,
     * component sets localized format from messages when is created.
     * <p>
     * This property act as default format for all display modes and for {@link CustomCalendarDisplayMode} until specific
     * property won't be set for these display modes. However, all display-mode properties objects by default explicitly
     * specify the format, thus they override this property.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * you should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>. For instance,
     * the {@code "MMM D, YY"} produces {@code Sep 9, 24}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param format format to set
     */
    public void setDefaultDayPopoverFormat(@Nullable String format) {
        options.getDayPopoverFormat().setValue(format);
    }

    /**
     * @return the day header format or {@code null} if not set
     */
    @Nullable
    public String getDefaultDayHeaderFormat() {
        return options.getDayHeaderFormat().getValue();
    }

    /**
     * Sets the format of the text that will be displayed on the calendar’s column headings. By default,
     * component sets localized format from messages when is created.
     * <p>
     * This property act as default format for all display modes and for {@link CustomCalendarDisplayMode} until specific
     * property won't be set for these display modes. However, all display-mode properties objects by default explicitly
     * specify the format, thus they override this property.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * you should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>. For instance,
     * the {@code "dd"} produces {@code Mo}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param format format to set
     */
    public void setDefaultDayHeaderFormat(@Nullable String format) {
        options.getDayHeaderFormat().setValue(format);
    }

    /**
     * @return the format of the week number or {@code null} if not set
     */
    @Nullable
    public String getDefaultWeekNumberFormat() {
        return options.getWeekNumberFormat().getValue();
    }

    /**
     * Sets the format of the week number that will be displayed when {@link #isWeekNumbersVisible()} is {@code true}.
     * By default, component sets localized format from messages when is created.
     * <p>
     * This property act as default format for all display modes and for {@link CustomCalendarDisplayMode} until specific
     * property won't be set for these display modes. However, all display-mode properties objects by default explicitly
     * specify the format, thus they override this property.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * you should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>. For instance,
     * the {@code "[Week] w"} produces {@code Week 1} (1, 2, ... 52, 53).
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param format format to set
     */
    public void setDefaultWeekNumberFormat(@Nullable String format) {
        options.getWeekNumberFormat().setValue(format);
    }

    /**
     * @return the slot label format or {@code null} if not set
     */
    @Nullable
    public String getDefaultSlotLabelFormat() {
        return options.getSlotLabelFormat().getValue();
    }

    /**
     * Sets the format of the text that will be displayed within a time slot. By default, component sets
     * localized format from messages when is created.
     * <p>
     * This property act as default format for all display modes and for {@link CustomCalendarDisplayMode} until specific
     * property won't be set for these display modes. However, all display-mode properties objects by default explicitly
     * specify the format, thus they override this property.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * you should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>. For instance,
     * the {@code "ha"} produces {@code 1 am} (1, 2, ... 12 am/pm).
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param format format to set
     */
    public void setDefaultSlotLabelFormat(@Nullable String format) {
        options.getSlotLabelFormat().setValue(format);
    }

    /**
     * @return the event time format or {@code null} if not set
     */
    @Nullable
    public String getDefaultEventTimeFormat() {
        return options.getEventTimeFormat().getValue();
    }

    /**
     * Sets the format of the time-text that will be displayed on each event. By default, component sets
     * localized format from messages when is created.
     * <p>
     * This property act as default format for all display modes and for {@link CustomCalendarDisplayMode} until specific
     * property won't be set for these display modes. However, all display-mode properties objects by default explicitly
     * specify the format, thus they override this property.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * you should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>. For instance,
     * the {@code "HH:mm"} produces {@code 00:00} (01, 2, ... 23 : 01, 02 ... 59).
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param format format to set
     */
    public void setDefaultEventTimeFormat(@Nullable String format) {
        options.getEventTimeFormat().setValue(format);
    }

    /**
     * @return {@code true} if weekends are visible
     */
    public boolean isWeekendsVisible() {
        return options.getWeekends().getNotNullValue();
    }

    /**
     * Sets whether to include Saturday and Sunday columns in any of the calendar display modes.
     * <p>
     * The default value is {@code true}
     *
     * @param visible whether to show weekends
     */
    public void setWeekendsVisible(boolean visible) {
        options.getWeekends().setValue(visible);
    }

    /**
     * @return {@code true} if day headers are visible
     */
    public boolean isDayHeadersVisible() {
        return options.getDayHeaders().getNotNullValue();
    }

    /**
     * Sets whether the day headers should appear. It works for day-grid, time-grid and month display modes.
     * <p>
     * The default value is {@code true}.
     *
     * @param visible whether to show day headers
     */
    public void setDayHeadersVisible(boolean visible) {
        options.getDayHeaders().setValue(visible);
    }

    /**
     * @return the interval for displaying time slots or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getSlotDuration() {
        return options.getSlotDuration().getValue();
    }

    /**
     * Sets the interval at which time slots are displayed.
     * <p>
     * The default value is 30 minutes.
     *
     * @param duration the interval to set
     */
    public void setSlotDuration(@Nullable CalendarDuration duration) {
        options.getSlotDuration().setValue(duration);
    }

    /**
     * @return the frequency for labeling time slots with text or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getSlotLabelInterval() {
        return options.getSlotLabelInterval().getValue();
    }

    /**
     * Sets the frequency for labeling time slots with text.
     * <p>
     * If not specified, a reasonable value will be automatically computed based on {@link #getSlotDuration()}.
     * <p>
     * When specifying this property, give the {@code CalendarDuration.ofHours(1)} value, this will cause the header
     * labels to appear on the hour marks, even if {@link #getSlotDuration()} was hypothetically {@code 15} or
     * {@code 30} minutes long.
     *
     * @param duration the interval to set
     */
    public void setSlotLabelInterval(@Nullable CalendarDuration duration) {
        options.getSlotLabelInterval().setValue(duration);
    }

    /**
     * @return thr slot minimum time or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getSlotMinTime() {
        return options.getSlotMinTime().getValue();
    }

    /**
     * Sets the first time slot that will be displayed for each day.
     * <p>
     * The default value is {@code 00:00:00}. It means the start time will be at the very beginning of
     * the day (midnight). Determines the first time slot, even when the scrollbars have been scrolled
     * all the way back.
     *
     * @param slotMinTime slot minimum time to set
     */
    public void setSlotMinTime(@Nullable CalendarDuration slotMinTime) {
        options.getSlotMinTime().setValue(slotMinTime);
    }

    /**
     * @return the slot maximum time or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getSlotMaxTime() {
        return options.getSlotMaxTime().getValue();
    }

    /**
     * Sets the last time slot that will be displayed for each day. The specified value represents the exclusive
     * end time, meaning the time slot ending at this value will not be included.
     * <p>
     * The default value is {@code 24:00:00}. It means that the end time will be at the very end of the day (midnight).
     * Determines the last slot, even when the scrollbars have been scrolled all the way back.
     *
     * @param slotMaxTime the slot maximum time
     */
    public void setSlotMaxTime(@Nullable CalendarDuration slotMaxTime) {
        options.getSlotMaxTime().setValue(slotMaxTime);
    }

    /**
     * @return the scroll time or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getScrollTime() {
        return options.getScrollTime().getValue();
    }

    /**
     * Sets the initial scroll position to a specific time.
     * <p>
     * The user will be able to scroll back to see events before this time. If you want to prevent users
     * from doing this, use the {@link #setSlotMinTime(CalendarDuration)} instead.
     * <p>
     * By default, scroll time is reapplied to the calendar whenever the date range changes. To disable this,
     * set {@link #setScrollTimeReset(boolean)} to {@code false}.
     * <p>
     * The default value is {@code 06:00:00}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param scrollTime the time to scroll to
     */
    public void setScrollTime(@Nullable CalendarDuration scrollTime) {
        options.getScrollTime().setValue(scrollTime);
    }

    /**
     * @return {@code true} if the scroll position should be reset every time
     */
    public boolean isScrollTimeReset() {
        return options.getScrollTimeReset().getNotNullValue();
    }

    /**
     * Sets whether the calendar should scroll to {@link #getScrollTime()} every time
     * the date range changes.
     * <p>
     * By default, whenever the date range changes either via calendar methods or the user actions,
     * the scroll is reset. Set to {@code false} to disable this behaviour.
     * <p>
     * The default value is {@code true}.
     *
     * @param scrollTimeReset whether to reset scroll
     */
    public void setScrollTimeReset(boolean scrollTimeReset) {
        options.getScrollTimeReset().setValue(scrollTimeReset);
    }

    /**
     * @return {@code true} if events without {@code allDay} property is shown as all-day
     */
    public boolean isDefaultAllDay() {
        return options.getDefaultAllDay().getNotNullValue();
    }

    /**
     * Sets the default value for each calendar event's {@code allDay} property when it is unspecified.
     * <p>
     * If the property is set to {@code true}, all events without {@code allDay} property (e.g. is not specified
     * mapping in data provider or calendar event returns {@code null} for {@code getAllDay()}) will be considered
     * as all-day events.
     * <p>
     * Note, in this case, events without {@code allDay} property will be shown as all-day events. If such event
     * will be changed by dragging or resizing, and you have listeners for these events, the component will set to
     * calendar event {@code setAllDay()}, which can lead to modifying entity/DTO instance.
     * <p>
     * The default value is {@code false}.
     *
     * @param defaultAllDay whether to show events as all-day
     */
    public void setDefaultAllDay(boolean defaultAllDay) {
        options.getDefaultAllDay().setValue(defaultAllDay);
    }

    /**
     * @return the default all-day duration or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getDefaultAllDayEventDuration() {
        return options.getDefaultAllDayEventDuration().getValue();
    }

    /**
     * Sets the default duration of all-day events if they don't specify {@code endDateTime} property.
     * <p>
     * For instance, the property is set to {@code 2 days}. The event that has {@code allDay = true} and do not
     * have {@code endDateTime} property, will be shown in all-day section and will occupy two days.
     * <p>
     * Note, calendar event's {@code endDateTime} property will remain unset,
     * unless {@link #setForceEventDuration(boolean)} has been set to {@code true}. In this case, the calendar
     * will explicitly assign end date to an event. If such event will be changed by dragging or resizing, and
     * you have listeners for these events, the component will set to calendar event {@code setEndDateTime()},
     * which can lead to modifying entity/DTO instance.
     * <p>
     * This property only affects events with {@code allDay = true}.
     * <p>
     * The default value is {@code 1 day}.
     *
     * @param duration the default all-day duration
     */
    public void setDefaultAllDayEventDuration(@Nullable CalendarDuration duration) {
        options.getDefaultAllDayEventDuration().setValue(duration);
    }

    /**
     * @return the default time duration or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getDefaultTimedEventDuration() {
        return options.getDefaultTimedEventDuration().getValue();
    }

    /**
     * Sets the default duration for timed events if they don't specify {@code endDateTime} property.
     * <p>
     * For instance, the property is set to {@code 2 hours}. The event that has {@code allDay = false} and do not
     * have {@code endDateTime} property, will occupy two hours in time slots.
     * <p>
     * Note, calendar event's end property will remain unset, unless {@link #setForceEventDuration(boolean)} has been
     * set to {@code true}. In this case, the calendar will explicitly assign end date to an event. If such event
     * will be changed by dragging or resizing, and you have listeners for these events, the component will set to
     * calendar event {@code setEndDateTime()}, which can lead to modifying entity/DTO instance.
     * <p>
     * This property only affects events with {@code allDay = false}.
     * <p>
     * The default value is {@code 1 hour}.
     *
     * @param duration the default time duration
     */
    public void setDefaultTimedEventDuration(@Nullable CalendarDuration duration) {
        options.getDefaultTimedEventDuration().setValue(duration);
    }

    /**
     * @return {@code true} if {@code end} property value is assigned by component to events that do not have it,
     * or returns {@code null} if not set
     */
    public boolean isForceEventDuration() {
        return options.getForceEventDuration().getNotNullValue();
    }

    /**
     * Makes the component to assign {@code end} property value to events if they do not have it.
     * <p>
     * Note, if event does not specify {@code end} property, the component will set it explicitly. If such event
     * will be changed by dragging or resizing, and you have listeners for these events, the component will set to
     * calendar event {@code setEndDateTime()}, which can lead to modifying entity/DTO instance.
     * <p>
     * The default value is {@code false}.
     *
     * @param forceEventDuration whether to assign end date to events
     */
    public void setForceEventDuration(boolean forceEventDuration) {
        options.getForceEventDuration().setValue(forceEventDuration);
    }

    /**
     * @return the initial date to show or {@code null} if not set
     */
    @Nullable
    public LocalDate getInitialDate() {
        return options.getInitialDate().getValue();
    }

    /**
     * Sets the initial date that will be displayed when the component is attached to UI.
     * <p>
     * If not specified, the current date will be used.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param initialDate initial date to show
     */
    public void setInitialDate(@Nullable LocalDate initialDate) {
        options.getInitialDate().setValue(initialDate);
    }

    /**
     * @return the step duration or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getDateIncrement() {
        return options.getDateIncrement().getValue();
    }

    /**
     * Defines the step of {@link #navigateToNext()}/{@link #navigateToPrevious()} methods.
     * <p>
     * It is unnecessary to define this property if predefined display modes are used, because the step is calculated
     * automatically based on display mode.
     * <p>
     * For {@link CustomCalendarDisplayMode} the {@link CustomCalendarDisplayMode#getDuration()} will be used as step
     * if specified.
     *
     * @param dateIncrement the step duration
     */
    public void setDateIncrement(CalendarDuration dateIncrement) {
        options.getDateIncrement().setValue(dateIncrement);
    }

    /**
     * @return the first visible date of {@link CustomCalendarDisplayMode} or {@code null} if not set
     */
    @Nullable
    public String getDateAlignment() {
        return options.getDateAlignment().getValue();
    }

    /**
     * Sets the first visible date of {@link CustomCalendarDisplayMode}.
     * <p>
     * When a custom calendar display mode is being used, and you’d like to guarantee that the
     * calendar begins at a certain interval, like the start-of-week or start-of-month, specify a value like
     * {@code "week"} or {@code "month"}. If the property is not specified, a reasonable default will be
     * generated based on the display mode’s duration.
     * <p>
     * If a display mode’s range is explicitly defined with {@link #setVisibleRange(LocalDate, LocalDate)},
     * this property will be ignored.
     *
     * @param alignment the first visible date, like {@code "week"} or {@code "month"}
     */
    public void setDateAlignment(@Nullable String alignment) {
        options.getDateAlignment().setValue(alignment);
    }

    /**
     * @return {@code true} if all events are focusable/tabbable
     */
    public boolean isEventInteractive() {
        return options.getEventInteractive().getNotNullValue();
    }

    /**
     * Enables all events to be focusable/tabbable.
     * <p>
     * Note, calendar events can override this value if they specify {@code getInteractive()}.
     * <p>
     * The default value is {@code false}.
     *
     * @param interactive whether events should be focusable/tabbable
     */
    public void setEventInteractive(boolean interactive) {
        options.getEventInteractive().setValue(interactive);
    }

    /**
     * @return the event long press delay or {@code null} if not set
     */
    @Nullable
    public Integer getEventLongPressDelay() {
        return options.getEventLongPressDelay().getValue();
    }

    /**
     * Sets amount of time the user must hold down before an event becomes draggable.
     * <p>
     * For touch devices.
     * <p>
     * The default value is {@code 1000} milliseconds.
     *
     * @param delay the delay to set
     * @see #setSelectLongPressDelay(Integer)
     */
    public void setEventLongPressDelay(@Nullable Integer delay) {
        options.getEventLongPressDelay().setValue(delay);
    }

    /**
     * @return the select long press delay or {@code null} if not set
     */
    @Nullable
    public Integer getSelectLongPressDelay() {
        return options.getSelectLongPressDelay().getValue();
    }

    /**
     * Sets the amount of time the user must hold down before a date becomes selectable on touch devices.
     * <p>
     * The default value is {@code 1000} milliseconds.
     *
     * @param delay the delay to set
     * @see #setEventLongPressDelay(Integer)
     */
    public void setSelectLongPressDelay(@Nullable Integer delay) {
        options.getSelectLongPressDelay().setValue(delay);
    }

    /**
     * @return {@code true} if now indicator is visible
     */
    public boolean isNowIndicatorVisible() {
        return options.getNowIndicator().getNotNullValue();
    }

    /**
     * Enables displaying a marker indicating the current time. The property applies in time-grid display modes.
     * <p>
     * The default value is {@code false}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param nowIndicatorVisible whether to display a now indicator
     */
    public void setNowIndicatorVisible(boolean nowIndicatorVisible) {
        options.getNowIndicator().setValue(nowIndicatorVisible);
    }

    /**
     * @return {@code true} if rows are expanded in time-grid display modes
     */
    public boolean isExpandRows() {
        return options.getExpandRows().getNotNullValue();
    }

    /**
     * Enables to expand rows of time-grid display modes if they don’t take up the entire height.
     * <p>
     * The default value is {@code false}.
     *
     * @param expandRows whether to expand rows
     */
    public void setExpandRows(boolean expandRows) {
        options.getExpandRows().setValue(expandRows);
    }

    /**
     * @return the delay or {@code null} if not set
     */
    @Nullable
    public Integer getWindowResizeDelay() {
        return options.getWindowResizeDelay().getValue();
    }

    /**
     * Sets the delay that the calendar will wait before adjusting its size after a window resize occurs.
     * <p>
     * The default value is {@code 100}.
     *
     * @param windowResizeDelay the delay in milliseconds
     */
    public void setWindowResizeDelay(@Nullable Integer windowResizeDelay) {
        options.getWindowResizeDelay().setValue(windowResizeDelay);
    }

    /**
     * @return event background color or {@code null} if not set
     */
    @Nullable
    public String getEventBackgroundColor() {
        return options.getEventBackgroundColor().getValue();
    }

    /**
     * Sets the background color for all events on the calendar.
     * <p>
     * Note, calendar events can override this value if they specify {@code getBackgroundColor()}.
     * <p>
     * Supported values are:
     * <ul>
     *     <li>
     *         <code>#f00</code>
     *     </li>
     *     <li>
     *         <code>#ff0000</code>
     *     </li>
     *     <li>
     *         <code>rgb(255,0,0)</code>
     *     </li>
     *     <li>
     *         Color name - <code>red</code>
     *     </li>
     * </ul>
     *
     * @param eventBackgroundColor background color to set
     */
    public void setEventBackgroundColor(@Nullable String eventBackgroundColor) {
        options.getEventBackgroundColor().setValue(eventBackgroundColor);
    }

    /**
     * @return the event border color or {@code null} if not set
     */
    @Nullable
    public String getEventBorderColor() {
        return options.getEventBorderColor().getValue();
    }

    /**
     * Sets the border color for all events on the calendar.
     * <p>
     * Note, calendar events can override this value if they specify {@code getBorderColor()}.
     * <p>
     * Supported values are:
     * <ul>
     *     <li>
     *         <code>#f00</code>
     *     </li>
     *     <li>
     *         <code>#ff0000</code>
     *     </li>
     *     <li>
     *         <code>rgb(255,0,0)</code>
     *     </li>
     *     <li>
     *         Color name - <code>red</code>
     *     </li>
     * </ul>
     *
     * @param borderColor event border color
     */
    public void setEventBorderColor(@Nullable String borderColor) {
        options.getEventBorderColor().setValue(borderColor);
    }

    /**
     * @return the event text color or {@code null} if not set
     */
    @Nullable
    public String getEventTextColor() {
        return options.getEventTextColor().getValue();
    }

    /**
     * Sets the text color for all events on the calendar.
     * <p>
     * Note, calendar events can override this value if they specify {@code getTextColor()}.
     * <p>
     * Supported values are:
     * <ul>
     *     <li>
     *         <code>#f00</code>
     *     </li>
     *     <li>
     *         <code>#ff0000</code>
     *     </li>
     *     <li>
     *         <code>rgb(255,0,0)</code>
     *     </li>
     *     <li>
     *         Color name - <code>red</code>
     *     </li>
     * </ul>
     * <p>
     * The color applies in time-grid display modes and for all-day events in day-grid display modes.
     *
     * @param textColor event text color
     */
    public void setEventTextColor(@Nullable String textColor) {
        options.getEventTextColor().setValue(textColor);
    }

    /**
     * @return {@code true} if time-text of events is shown
     */
    public boolean isDisplayEventTime() {
        return options.getDisplayEventTime().getNotNullValue();
    }

    /**
     * Determines visibility of time text of each event.
     * <p>
     * The property applies to timed-events (not all-day). If set to {@code true}, time text will always be
     * displayed on the event. If set to {@code false}, time text will never be displayed on the event. Events
     * that are all-day will never display time text anyhow.
     * <p>
     * The default value is {@code true}.
     *
     * @param displayEventTime whether to display time text of events
     */
    public void setDisplayEventTime(boolean displayEventTime) {
        options.getDisplayEventTime().setValue(displayEventTime);
    }

    /**
     * @return the next day threshold time or {@code null} if not set
     */
    @Nullable
    public CalendarDuration getNextDayThreshold() {
        return options.getNextDayThreshold().getValue();
    }

    /**
     * Sets the minimum time that event's end date should achieve to render event as it if were on that day.
     * <p>
     * For instance, the property is set to {@code 09:00:00}. And there is an event with dates:
     * <ul>
     *     <li>
     *         start - {@code 2024-09-01T20:00:00};
     *     </li>
     *     <li>
     *         end - {@code 2024-09-02T02:00:00};
     *     </li>
     * </ul>
     * <p>
     * So, the event spans to another day, but it will be rendered as a one-day event, because the
     * {@code nextDayThreshold} property is specified. However, If the event has end date - {@code 2024-09-02T10:00:00},
     * it will be rendered as two-day event.
     * <p>
     * Only affects timed events that appear on whole-days cells. Whole-day cells occur in day-grid display modes and
     * the all-day slots in the time-grid display modes.
     * <p>
     * Note that this property is ignored when event's {@code allDay} property is set to {@code true}.
     * <p>
     * The default value is {@code 00:00:00}.
     *
     * @param nextDayThreshold the next day threshold time
     * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">FullCalendar docs :: nextDayThreshold</a>
     */
    public void setNextDayThreshold(@Nullable CalendarDuration nextDayThreshold) {
        options.getNextDayThreshold().setValue(nextDayThreshold);
    }

    /**
     * @return {@code true} if the component strictly follows the specified event order
     */
    public boolean isEventOrderStrict() {
        return options.getEventOrderStrict().getNotNullValue();
    }

    /**
     * Enables the component to strictly follow the specified event order.
     * <p>
     * By default, this property is {@code false}, meaning the event order is not strict and compactness
     * will be prioritized over following event order exactly.
     *
     * @param eventOrderStrict whether to strictly follow event order
     * @see #setEventOrder(List)
     * @see #setEventOrderJsFunction(JsFunction)
     */
    public void setEventOrderStrict(boolean eventOrderStrict) {
        options.getEventOrderStrict().setValue(eventOrderStrict);
    }

    /**
     * @return list of properties involved in the sorting process
     */
    public List<String> getEventOrder() {
        return options.getEventOrder().getEventOrder();
    }

    /**
     * Sets the list of calendar event properties that should participate in sorting. It is also available to specify
     * properties from event's {@code additionalProperties}.
     * <p>
     * For most calendar display modes, this property determines the vertical ordering of events within the same day.
     * For time-grid display modes however, it determines the horizontal ordering of events within the same time slot.
     * <p>
     * The default value puts earlier events first. If tied, it puts longer events first. If still tied, it puts
     * all-day events first. If still tied, orders events by title, alphabetically.
     * <p>
     * If putting a lower-precedent event before a higher-precedent improves compactness, the algorithm will do so.
     * To disable this behavior, set {@link #setEventOrderStrict(boolean)} to {@code true}.
     * <p>
     * The provided properties are sorted in ascending order. If a property is prefixed with a minus sign like "-title",
     * sorting will be in descending order.
     *
     * @param eventOrder list of properties
     * @see <a href="https://fullcalendar.io/docs/eventOrder">FullCalendar documentation :: eventOrder</a>
     */
    public void setEventOrder(@Nullable List<String> eventOrder) {
        options.getEventOrder().setEventOrder(eventOrder);
    }

    /**
     * @return event order JavaScript function or {@code null} if not set
     */
    @Nullable
    public JsFunction getEventOrderJsFunction() {
        return options.getEventOrder().getJsFunction();
    }

    /**
     * Sets JavaScript function definition that should sort event in the same day cell or time slot.
     * The function should return {@code -1} if first event is "less" than second one, and {@code 1} if the first
     * event is "greater" than second one.
     * <p>
     * For instance, function sorts by additional property:
     * <pre>{@code
     *  calendar.setEventOrderJsFunction(new JsFunction("""
     *     function (event1, event2) {
     *         if (event1.priority === event2.priority) {
     *             return 0;
     *         }
     *         return event1.priority > event2.priority ? 1 : -1;
     *     }
     *     """));
     *  }</pre>
     * The {@code event1} is the first event to be compared.
     * <p>
     * The {@code event2} is the second event to be compared.
     * <p>
     * The available properties of {@code event1} and {@code event2} you can find in
     * <a href="https://fullcalendar.io/docs/event-object">FullCalendar docs</a>
     * <p>
     * Note that JavaScript function takes precedence over the {@link #setEventOrder(List)}.
     *
     * @param jsFunction JavaScript function
     * @see <a href="https://fullcalendar.io/docs/eventOrder">FullCalendar documentation :: eventOrder</a>
     */
    public void setEventOrderJsFunction(@Nullable JsFunction jsFunction) {
        options.getEventOrder().setJsFunction(jsFunction);
    }

    /**
     * @return {@code true} if progressive event rendering is enabled
     */
    public boolean isProgressiveEventRendering() {
        return options.getProgressiveEventRendering().getNotNullValue();
    }

    /**
     * Enables rendering of events from the data provider as soon as the events are loaded.
     * <ul>
     *     <li>
     *         {@code true} - renders each data provider as it is received. Will result in more renders.
     *     </li>
     *     <li>
     *         {@code false} - waits until all data providers have received their data and then renders everything
     *         at once, resulting in fewer renders.
     *     </li>
     * </ul>
     * <p>
     * The default value is {@code false}.
     *
     * @param progressiveEventRendering whether to enable progressive event rendering
     */
    public void setProgressiveEventRendering(boolean progressiveEventRendering) {
        options.getProgressiveEventRendering().setValue(progressiveEventRendering);
    }

    protected JsonFactory createJsonFactory() {
        return new JreJsonFactory();
    }

    protected JmixFullCalendarSerializer createSerializer() {
        return new JmixFullCalendarSerializer();
    }

    protected JmixFullCalendarDeserializer createDeserializer() {
        return new JmixFullCalendarDeserializer();
    }

    protected JmixFullCalendarOptions createOptions() {
        return new JmixFullCalendarOptions();
    }

    protected CalendarDuration inverseDuration(CalendarDuration duration) {
        return CalendarDuration.ofYears(-duration.getYears())
                .minusMonths(duration.getMonths())
                .minusWeeks(duration.getWeeks())
                .minusDays(duration.getDays())
                .minusHours(duration.getHours())
                .minusMinutes(duration.getMinutes())
                .minusSeconds(duration.getSeconds())
                .minusMilliseconds(duration.getMilliseconds());
    }

    protected void requestUpdateOptions(boolean onlyDirty) {
        // Do not call if it's still updating
        if (synchronizeOptionsExecution != null) {
            return;
        }

        getUI().ifPresent(ui -> synchronizeOptionsExecution =
                ui.beforeClientResponse(this, (context) -> performUpdateOptions(onlyDirty)));
    }

    protected void performUpdateOptions(boolean onlyDirty) {
        JsonObject json = serializer.serializeOptions(onlyDirty
                ? options.getDirtyOptions()
                : options.getUpdatableOptions());

        getElement().callJsFunction("updateOptions", json);

        options.unmarkAllAsDirty();
        synchronizeOptionsExecution = null;
    }

    protected void performCompleteInit() {
        getUI().ifPresent(ui ->
                ui.beforeClientResponse(this, (context) ->
                        getElement().callJsFunction("_onCompleteInit")
                                .then((__) -> onCompleteInitialization())));
    }

    protected void onCompleteInitialization() {
        initialized = true;
    }

    protected void updateInitialOptions() {
        // Before the component is fully initialized, consider all options as initial
        JsonObject json = serializer.serializeOptions(options.getAllOptions());
        getElement().setPropertyJson("initialOptions", json);
    }

    protected void attachCalendarOptionChangeListener() {
        options.setOptionChangeListener(this::onOptionChange);
    }

    protected void onOptionChange(JmixFullCalendarOptions.OptionChangeEvent event) {
        if (!options.isInitial(event.getOption())) {
            requestUpdateOptions(true);
        }
    }

    protected void attachDatesSetDomEventListener() {
        if (datesSetDomRegistration == null) {
            datesSetDomRegistration =
                    addListener(DatesSetDomEvent.class, this::onDatesSet);
        }
    }

    protected void attachMoreLinkClickDomEventListener() {
        if (moreLinkClickDomRegistration == null) {
            moreLinkClickDomRegistration = addListener(MoreLinkClickDomEvent.class, this::onMoreLinkClick);
        }
    }

    protected void attachEventClickDomEventListener() {
        if (eventClickDomRegistration == null) {
            eventClickDomRegistration = addListener(EventClickDomEvent.class, this::onEventClick);
        }
    }

    protected void detachEventClickDomEventListener() {
        if (eventClickDomRegistration != null) {
            eventClickDomRegistration.remove();
            eventClickDomRegistration = null;
        }
    }

    protected void attachEventMouseEnterDomEventListener() {
        if (eventMouseEnterDomRegistration == null) {
            eventMouseEnterDomRegistration = addListener(EventMouseEnterDomEvent.class, this::onEventMouseEnter);
        }
    }

    protected void detachEventMouseEnterDomEventListener() {
        if (eventMouseEnterDomRegistration != null) {
            eventMouseEnterDomRegistration.remove();
            eventMouseEnterDomRegistration = null;
        }
    }

    protected void attachEventMouseLeaveDomEventListener() {
        if (eventMouseLeaveDomRegistration == null) {
            eventMouseLeaveDomRegistration = addListener(EventMouseLeaveDomEvent.class, this::onEventMouseLeave);
        }
    }

    protected void detachEventMouseLeaveDomEventListener() {
        if (eventMouseLeaveDomRegistration != null) {
            eventMouseLeaveDomRegistration.remove();
            eventMouseLeaveDomRegistration = null;
        }
    }

    protected void attachEventDropDomEventListener() {
        if (eventDropDomRegistration == null) {
            eventDropDomRegistration = addListener(EventDropDomEvent.class, this::onEventDrop);
        }
    }

    protected void detachEventDropDomEventListener() {
        if (eventDropDomRegistration != null) {
            eventDropDomRegistration.remove();
            eventDropDomRegistration = null;
        }
    }

    protected void attachEventResizeDomEventListener() {
        if (eventResizeDomRegistration == null) {
            eventResizeDomRegistration = addListener(EventResizeDomEvent.class, this::onEventResize);
        }
    }

    protected void detachEventResizeDomEventListener() {
        if (eventResizeDomRegistration != null) {
            eventResizeDomRegistration.remove();
            eventResizeDomRegistration = null;
        }
    }

    protected void attachDateClickDomEventListener() {
        if (dateClickDomRegistration == null) {
            dateClickDomRegistration = addListener(DateClickDomEvent.class, this::onDateClick);
        }
    }

    protected void detachDateClickDomEventListener() {
        if (dateClickDomRegistration != null) {
            dateClickDomRegistration.remove();
            dateClickDomRegistration = null;
        }
    }

    protected void attachSelectDomEventListener() {
        if (selectDomRegistration == null) {
            selectDomRegistration = addListener(SelectDomEvent.class, this::onSelect);
        }
    }

    protected void detachSelectDomEventListener() {
        if (selectDomRegistration != null) {
            selectDomRegistration.remove();
            selectDomRegistration = null;
        }
    }

    protected void attachUnselectDomEventListener() {
        if (unselectDomRegistration == null) {
            unselectDomRegistration = addListener(UnselectDomEvent.class, this::onUnselect);
        }
    }

    protected void detachUnselectDomEventListener() {
        if (unselectDomRegistration != null) {
            unselectDomRegistration.remove();
            unselectDomRegistration = null;
        }
    }

    protected void attachDayNavigationLinkClickDomEventListener() {
        if (dayNavigationLinkClickDomRegistration == null) {
            dayNavigationLinkClickDomRegistration =
                    addListener(DayNavigationLinkClickDomEvent.class, this::onDayNavigationLinkClick);
        }
    }

    protected void attachWeekNavigationLinkClickDomEventListener() {
        if (weekNavigationLinkClickDomRegistration == null) {
            weekNavigationLinkClickDomRegistration =
                    addListener(WeekNavigationLinkClickDomEvent.class, this::onWeekNavigationLinkClick);
        }
    }

    protected void onEventClick(EventClickDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onEventMouseEnter(EventMouseEnterDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onEventMouseLeave(EventMouseLeaveDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onDatesSet(DatesSetDomEvent event) {
        displayMode = getDisplayMode(event.getViewType());
        currentDate = CalendarDateTimeUtils.parseIsoDate(event.getCurrentDate());
    }

    protected void onMoreLinkClick(MoreLinkClickDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onEventDrop(EventDropDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onEventResize(EventResizeDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onDateClick(DateClickDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onSelect(SelectDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onUnselect(UnselectDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onDayNavigationLinkClick(DayNavigationLinkClickDomEvent event) {
        // Stub, is used in inheritors
    }

    protected void onWeekNavigationLinkClick(WeekNavigationLinkClickDomEvent event) {
        // Stub, is used in inheritors
    }

    protected CalendarDisplayMode getDisplayMode(String id) {
        CalendarDisplayMode displayMode = CalendarDisplayModes.fromId(id);
        if (displayMode != null) {
            return displayMode;
        }
        displayMode = GenericCalendarDisplayModes.fromId(id);
        if (displayMode != null) {
            return displayMode;
        }
        CustomCalendarDisplayMode customDisplayMode = options.getDisplayModes().getCustomCalendarDisplayMode(id);

        return customDisplayMode != null ? customDisplayMode.getDisplayMode() : () -> id;
    }

    @ClientCallable
    protected JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        // Stub, is used in inheritors
        return jsonFactory.createArray();
    }

    @ClientCallable
    protected JsonArray getMoreLinkClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return jsonFactory.createArray();
    }

    @ClientCallable
    protected JsonArray getDayHeaderClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return jsonFactory.createArray();
    }

    @ClientCallable
    protected JsonArray getDayCellClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return jsonFactory.createArray();
    }

    @ClientCallable
    protected JsonArray getSlotLabelClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return jsonFactory.createArray();
    }

    @ClientCallable
    protected JsonArray getNowIndicatorClassNames(JsonObject jsonContext) {
        return jsonFactory.createArray();
    }

    @ClientCallable
    protected JsonObject getDayCellBottomTextInfo(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return jsonFactory.createObject();
    }

    protected void addDataProvidersOnAttach() {
        // Stub, is used in inheritors
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // All request-update methods do not register JS function call,
        // since the component is not attached. Thus, in onAttach we should
        // call them again.
        // Also, if a component was reattached, it looses all configuration
        // on a client side, so we should restore it.
        updateInitialOptions();

        addDataProvidersOnAttach();

        performCompleteInit();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        initialized = false;
    }
}
