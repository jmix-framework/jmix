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
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import io.jmix.fullcalendarflowui.kit.component.event.dom.*;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import io.jmix.fullcalendarflowui.kit.component.model.AbstractCalendarViewProperties;
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
@JsModule("./src/fullcalendar/jmix-full-calendar.js")
@CssImport("./src/fullcalendar/jmix-full-calendar.css")
public class JmixFullCalendar extends Component implements HasSize, HasStyle {

    protected JmixFullCalendarSerializer serializer;
    protected JmixFullCalendarDeserializer deserializer;
    protected JmixFullCalendarOptions options;

    protected CalendarView calendarView;

    protected Map<String, StateTree.ExecutionRegistration> itemsEventProvidersExecutionMap = new HashMap<>(2);
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

    protected boolean initialized = false;

    public JmixFullCalendar() {
        serializer = createSerializer();
        deserializer = createDeserializer();
        options = createOptions();

        attachCalendarOptionChangeListener();
        attachDatesSetDomEventListener();
        attachMoreLinkClickDomEventListener();
    }

    /**
     * @return initial calendar view or {@code null} if not set
     */
    @Nullable
    public CalendarView getInitialCalendarView() {
        return options.getInitialView().getValue();
    }

    /**
     * Sets initial calendar view that will be shown after attaching component to th UI.
     * <p>
     * The default value is {@link CalendarViewType#DAY_GRID_MONTH}.
     * <p>
     * The property change is not applied after component attached to the UI.
     *
     * @param calendarView initial calendar view
     */
    public void setInitialCalendarView(CalendarView calendarView) {
        Objects.requireNonNull(calendarView);

        options.getInitialView().setValue(calendarView);
    }

    /**
     * @return current calendar view
     */
    public CalendarView getCurrentCalendarView() {
        if (calendarView != null) {
            return calendarView;
        }
        CalendarView initialCalendarView = options.getInitialView().getValue();
        if (initialCalendarView != null) {
            return initialCalendarView;
        }
        CalendarView defaultCalendarView = options.getInitialView().getDefaultValue();
        return Objects.requireNonNull(defaultCalendarView);
    }

    /**
     * Switches currently shown view to the provider one.
     *
     * @param calendarView calendar view to set
     */
    public void setCalendarView(CalendarView calendarView) {
        Objects.requireNonNull(calendarView);

        getElement().executeJs("this.calendar.changeView($0)", calendarView.getId());
    }

    /**
     * Adds custom view to the calendar. Then custom view can be shown by:
     * <ul>
     *     <li>
     *          {@link #setCalendarView(CalendarView)}
     *     </li>
     *     <li>
     *          {@link #setInitialCalendarView(CalendarView)}.
     *     </li>
     * </ul>
     * Note that it is initial option and dynamically changing/adding/removing custom view will not apply after
     * attaching component to the UI.
     *
     * @param calendarCustomView calendar custom view to add
     */
    public void addCustomCalendarView(CustomCalendarView calendarCustomView) {
        Objects.requireNonNull(calendarCustomView);

        options.getViews().addCustomCalendarView(calendarCustomView);
    }

    /**
     * Removes calendar custom view.
     * <p>
     * Note that it is initial option and dynamically changing/adding/removing custom view will not apply after
     * attaching component to UI.
     *
     * @param calendarCustomView calendar custom view to remove
     */
    public void removeCustomCalendarView(CustomCalendarView calendarCustomView) {
        Objects.requireNonNull(calendarCustomView);

        options.getViews().removeCustomCalendarView(calendarCustomView);
    }

    /**
     * Returns custom calendar view by its ID.
     *
     * @param viewId the ID of custom calendar view
     * @return custom calendar view or {@code null} if there is no custom calendar view with provded ID
     */
    @Nullable
    public CustomCalendarView getCustomCalendarView(String viewId) {
        return options.getViews().getCustomCalendarView(viewId);
    }

    /**
     * @return list of custom calendar views added to the component
     */
    public List<CustomCalendarView> getCustomCalendarViews() {
        return options.getViews().getCustomCalendarViews();
    }

    /**
     * Returns properties for the specific calendar view. Almost all calendar views have
     * a specific set of properties. Moreover, the view properties object can override some properties
     * from the component.
     *
     * @param calendarView calendar view type to get properties
     * @param <T>          type of view properties
     * @return view properties object that correspond to the provided calendar view type
     */
    public <T extends AbstractCalendarViewProperties> T getCalendarViewProperties(CalendarViewType calendarView) {
        return options.getViews().getCalendarViewProperties(calendarView);
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
     * The visible date range is applied together with generic calendar views {@link GenericCalendarViewType}.
     * For instance, you can set {@link GenericCalendarViewType#DAY_GRID}, visible range from {@code 2024-09-01} to
     * {@code 2024-09-03} and component will show two days.
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
     * Moves the calendar one step forward. For instance:
     * <ul>
     *     <li>
     *         For {@link CalendarViewType#DAY_GRID_DAY} and other day-views calendar will be moved one day forward.
     *     </li>
     *     <li>
     *         For {@link CalendarViewType#DAY_GRID_WEEK} and other week-views calendar will be moved one week forward.
     *     </li>
     *     <li>
     *         And so on.
     *     </li>
     * </ul>
     * The duration of {@link CustomCalendarView} also will be respected.
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
     *         For {@link CalendarViewType#DAY_GRID_DAY} and other day-views calendar will be moved one day back.
     *     </li>
     *     <li>
     *         For {@link CalendarViewType#DAY_GRID_WEEK} and other week-views calendar will be moved one week back.
     *     </li>
     *     <li>
     *         And so on.
     *     </li>
     * </ul>
     * The duration of {@link CustomCalendarView} also will be respected.
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
     * Scrolling to a specific time works when the calendar view is either {@link CalendarViewType#TIME_GRID_DAY} or
     * {@link CalendarViewType#TIME_GRID_WEEK}.
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
     * Scrolling to a specific time works when the calendar view is either {@link CalendarViewType#TIME_GRID_DAY} or
     * {@link CalendarViewType#TIME_GRID_WEEK}.
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
     * This property is applied for day-grid calendar views and in all-day cells.
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
     * Sets rows count of events in day-grid views and in "all-day" section in time-grid views.
     * <p>
     * Note that "+x more" row is included to count. For instance, if dayMaxEventRows = 1, only the "+x more" row
     * will be shown.
     * <p>
     * Takes precedence over {@link #setDefaultDayMaxEventRowsEnabled(boolean)}.
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
     * This property is applied for day-grid calendar views and in all-day cells.
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
     * Sets rows count of events in day-grid views and in "all-day" section in time-grid views.
     * <p>
     * Note that "+ more" row <strong>is not included</strong> to count. For instance, if dayMaxEventRows = 1,
     * one event and the "+x more" row will be shown.
     * <p>
     * Takes precedence over the {@link #setDefaultDayMaxEventsEnabled(boolean)}.
     *
     * @param maxRows maximum value of event rows in cell excluding "+x more" row
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
     * Sets the maximum number of events that stack top-to-bottom for time-grid views.
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
     * @return the calendar view that should be shown when "more" more link is clicked or {@code null} if not set
     */
    @Nullable
    public CalendarView getMoreLinkCalendarView() {
        return options.getMoreLinkClick().getCalendarView();
    }

    /**
     * Sets the calendar view that should be shown when "more" more link is clicked.
     *
     * @param calendarView the view that should be opened
     */
    public void setMoreLinkCalendarView(@Nullable CalendarView calendarView) {
        options.getMoreLinkClick().setCalendarView(calendarView);
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
     * Note, this property can be overridden on per-event by {@code CalendarEvent#getDurationEditable()} property.
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
     * Sets the time interval at which a dragged event will snap to the time axis. Also affects
     * the granularity at which selections can be made.
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
     * Enables to draw a "placeholder" event while the user is dragging.
     * <p>
     * This property is applied to time-grid views.
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
     * Sets whether clicking elsewhere on the page will cause the current selection to be cleared.
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
     *             return currentDate < selectionInfo.end;
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
     * This setting is only applicable to mouse-related interaction. For touch interaction, see
     * {@link #setSelectLongPressDelay(Integer)}.
     * <p>
     * The default value is {@code 0}.
     *
     * @param minDistance minimum distance in pixels
     */
    public void setSelectMinDistance(int minDistance) {
        options.getSelectMinDistance().setValue(minDistance);
    }

    /**
     * @return day popover format or {@code null} if not set
     */
    @Nullable
    public String getDefaultDayPopoverFormat() {
        return options.getDayPopoverFormat().getValue();
    }

    /**
     * Sets the date format of title of the popover that is shown when "more" link is clicked. By default,
     * component sets localized format from messages when is created.
     * <p>
     * This property act as default format for all views and {@link CustomCalendarView} until specific
     * property won't be set for these views. However, all views properties by default explicitly specify format.
     * Thus, they override this property.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "MMM D, YY"} produces {@code Sep 9, 24}.
     *
     * @param format format to set
     */
    public void setDefaultDayPopoverFormat(@Nullable String format) {
        options.getDayPopoverFormat().setValue(format);
    }

    @Nullable
    public String getDefaultDayHeaderFormat() {
        return options.getDayHeaderFormat().getValue();
    }

    public void setDefaultDayHeaderFormat(@Nullable String format) {
        options.getDayHeaderFormat().setValue(format);
    }

    @Nullable
    public String getDefaultWeekNumberFormat() {
        return options.getWeekNumberFormat().getValue();
    }

    /**
     * Note that it override the {@code weekText} value in i18n object.
     *
     * @param format
     */
    public void setDefaultWeekNumberFormat(@Nullable String format) {
        options.getWeekNumberFormat().setValue(format);
    }

    @Nullable
    public String getDefaultSlotNumberFormat() {
        return options.getSlotLabelFormat().getValue();
    }

    public void setDefaultSlotNumberFormat(@Nullable String format) {
        options.getSlotLabelFormat().setValue(format);
    }

    @Nullable
    public String getDefaultEventTimeFormat() {
        return options.getEventTimeFormat().getValue();
    }

    public void setDefaultEventTimeFormat(@Nullable String format) {
        options.getEventTimeFormat().setValue(format);
    }

    public boolean isWeekendsVisible() {
        return options.getWeekends().getNotNullValue();
    }

    public void setWeekendsVisible(boolean visible) {
        options.getWeekends().setValue(visible);
    }

    public boolean isDayHeadersVisible() {
        return options.getDayHeaders().getNotNullValue();
    }

    public void setDayHeadersVisible(boolean visible) {
        options.getDayHeaders().setValue(visible);
    }

    @Nullable
    public CalendarDuration getSlotDuration() {
        return options.getSlotDuration().getValue();
    }

    public void setSlotDuration(@Nullable CalendarDuration duration) {
        options.getSlotDuration().setValue(duration);
    }

    @Nullable
    public CalendarDuration getSlotLabelInterval() {
        return options.getSlotLabelInterval().getValue();
    }

    public void setSlotLabelInterval(@Nullable CalendarDuration duration) {
        options.getSlotLabelInterval().setValue(duration);
    }

    @Nullable
    public CalendarDuration getSlotMinTime() {
        return options.getSlotMinTime().getValue();
    }

    public void setSlotMinTime(@Nullable CalendarDuration slotMinTime) {
        options.getSlotMinTime().setValue(slotMinTime);
    }

    @Nullable
    public CalendarDuration getSlotMaxTime() {
        return options.getSlotMaxTime().getValue();
    }

    public void setSlotMaxTime(@Nullable CalendarDuration slotMaxTime) {
        options.getSlotMaxTime().setValue(slotMaxTime);
    }

    @Nullable
    public CalendarDuration getScrollTime() {
        return options.getScrollTime().getValue();
    }

    public void setScrollTime(@Nullable CalendarDuration scrollTime) {
        options.getScrollTime().setValue(scrollTime);
    }

    public boolean isScrollTimeReset() {
        return options.getScrollTimeReset().getNotNullValue();
    }

    public void setScrollTimeReset(boolean scrollTimeReset) {
        options.getScrollTimeReset().setValue(scrollTimeReset);
    }

    public boolean isDefaultAllDay() {
        return options.getDefaultAllDay().getNotNullValue();
    }

    /**
     * Note, events without allDay property will be shown as all-day events. If such event will be changed by dragging
     * or resizing calendar will set allDay = true to entity instance.
     *
     * @param defaultAllDay
     */
    public void setDefaultAllDay(boolean defaultAllDay) {
        options.getDefaultAllDay().setValue(defaultAllDay);
    }

    @Nullable
    public CalendarDuration getDefaultAllDayEventDuration() {
        return options.getDefaultAllDayEventDuration().getValue();
    }

    public void setDefaultAllDayEventDuration(@Nullable CalendarDuration duration) {
        options.getDefaultAllDayEventDuration().setValue(duration);
    }

    @Nullable
    public CalendarDuration getDefaultTimedEventDuration() {
        return options.getDefaultTimedEventDuration().getValue();
    }

    public void setDefaultTimedEventDuration(@Nullable CalendarDuration duration) {
        options.getDefaultTimedEventDuration().setValue(duration);
    }

    public boolean isForceEventDuration() {
        return options.getForceEventDuration().getNotNullValue();
    }

    /**
     * Note, if event does not have end date, calendar will set it explicitly. For instance, if event is entity
     * instance the `endDate` property will be changed after dragging or resizing event.
     *
     * @param forceEventDuration
     */
    public void setForceEventDuration(boolean forceEventDuration) {
        options.getForceEventDuration().setValue(forceEventDuration);
    }

    @Nullable
    public LocalDate getInitialDate() {
        return options.getInitialDate().getValue();
    }

    public void setInitialDate(@Nullable LocalDate initialDate) {
        options.getInitialDate().setValue(initialDate);
    }

    @Nullable
    public CalendarDuration getDateIncrement() {
        return options.getDateIncrement().getValue();
    }

    public void setDateIncrement(CalendarDuration dateIncrement) {
        options.getDateIncrement().setValue(dateIncrement);
    }

    @Nullable
    public String getDateAlignment() {
        return options.getDateAlignment().getValue();
    }

    public void setDateAlignment(@Nullable String alignment) {
        options.getDateAlignment().setValue(alignment);
    }

    public boolean isEventInteractive() {
        return options.getEventInteractive().getNotNullValue();
    }

    public void setEventInteractive(boolean interactive) {
        options.getEventInteractive().setValue(interactive);
    }

    @Nullable
    public Integer getLongPressDelay() {
        return options.getLongPressDelay().getValue();
    }

    public void setLongPressDelay(@Nullable Integer delay) {
        options.getLongPressDelay().setValue(delay);
    }

    @Nullable
    public Integer getSelectLongPressDelay() {
        return options.getSelectLongPressDelay().getValue();
    }

    public void setSelectLongPressDelay(@Nullable Integer delay) {
        options.getSelectLongPressDelay().setValue(delay);
    }

    public boolean isNowIndicatorVisible() {
        return options.getNowIndicator().getNotNullValue();
    }

    public void setNowIndicatorVisible(boolean nowIndicatorVisible) {
        options.getNowIndicator().setValue(nowIndicatorVisible);
    }

    public boolean isExpandRows() {
        return options.getExpandRows().getNotNullValue();
    }

    public void setExpandRows(boolean expandRows) {
        options.getExpandRows().setValue(expandRows);
    }

    @Nullable
    public Integer getWindowResizeDelay() {
        return options.getWindowResizeDelay().getValue();
    }

    public void setWindowResizeDelay(@Nullable Integer windowResizeDelay) {
        options.getWindowResizeDelay().setValue(windowResizeDelay);
    }

    @Nullable
    public String getEventBackgroundColor() {
        return options.getEventBackgroundColor().getValue();
    }

    public void setEventBackgroundColor(@Nullable String eventBackgroundColor) {
        options.getEventBackgroundColor().setValue(eventBackgroundColor);
    }

    @Nullable
    public String getEventBorderColor() {
        return options.getEventBorderColor().getValue();
    }

    public void setEventBorderColor(@Nullable String borderColor) {
        options.getEventBorderColor().setValue(borderColor);
    }

    @Nullable
    public String getEventTextColor() {
        return options.getEventTextColor().getValue();
    }

    public void setEventTextColor(@Nullable String eventTextColor) {
        options.getEventTextColor().setValue(eventTextColor);
    }

    public boolean isDisplayEventTime() {
        return options.getDisplayEventTime().getNotNullValue();
    }

    public void setDisplayEventTime(boolean displayEventTime) {
        options.getDisplayEventTime().setValue(displayEventTime);
    }

    @Nullable
    public CalendarDuration getNextDayThreshold() {
        return options.getNextDayThreshold().getValue();
    }

    public void setNextDayThreshold(@Nullable CalendarDuration nextDayThreshold) {
        options.getNextDayThreshold().setValue(nextDayThreshold);
    }

    public boolean isEventOrderStrict() {
        return options.getEventOrderStrict().getNotNullValue();
    }

    public void setEventOrderStrict(boolean eventOrderStrict) {
        options.getEventOrderStrict().setValue(eventOrderStrict);
    }

    public boolean isProgressiveEventRendering() {
        return options.getProgressiveEventRendering().getNotNullValue();
    }

    public void setProgressiveEventRendering(boolean progressiveEventRendering) {
        options.getProgressiveEventRendering().setValue(progressiveEventRendering);
    }

    public List<String> getEventOrder() {
        return options.getEventOrder().getEventOrder();
    }

    public void setEventOrder(@Nullable List<String> eventOrder) {
        options.getEventOrder().setEventOrder(eventOrder);
    }

    @Nullable
    public JsFunction getEventOrderJsFunction() {
        return options.getEventOrder().getJsFunction();
    }

    public void setEventOrderJsFunction(@Nullable JsFunction jsFunction) {
        options.getEventOrder().setJsFunction(jsFunction);
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
                                .then((__) -> initialized = true)));
    }

    protected void updateInitialOptions() {
        JsonObject json = serializer.serializeOptions(options.getInitialOptions());
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
        calendarView = getCalendarView(event.getViewType());
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

    protected CalendarView getCalendarView(String id) {
        CalendarView calendarView = CalendarViewType.fromId(id);
        if (calendarView != null) {
            return calendarView;
        }
        calendarView = GenericCalendarViewType.fromId(id);
        if (calendarView != null) {
            return calendarView;
        }
        CustomCalendarView customView = options.getViews().getCustomCalendarView(id);

        return customView != null ? customView.getCalendarView() : () -> id;
    }

    @ClientCallable
    protected JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        // Stub, is used in inheritors
        return new JreJsonFactory().createArray();
    }

    @ClientCallable
    protected JsonArray getMoreLinkClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return new JreJsonFactory().createArray();
    }

    @ClientCallable
    protected JsonArray getDayHeaderClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return new JreJsonFactory().createArray();
    }

    @ClientCallable
    protected JsonArray getDayCellClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return new JreJsonFactory().createArray();
    }

    @ClientCallable
    protected JsonArray getSlotLabelClassNames(JsonObject jsonContext) {
        // Stub, is used in inheritors
        return new JreJsonFactory().createArray();
    }

    @ClientCallable
    protected JsonArray getNowIndicatorClassNames(JsonObject jsonContext) {
        return new JreJsonFactory().createArray();
    }

    protected void addEventProvidersOnAttach() {
        // Stub, is used in inheritors
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // All request-update methods do not register JS function call,
        // since the component is not attached. Thus, in onAttach we should
        // call them again.
        // Also, if a component was reattached, it looses all configuration
        // on a client-side, so we should restore it.
        updateInitialOptions();

        requestUpdateOptions(false);

        addEventProvidersOnAttach();

        performCompleteInit();
    }
}
