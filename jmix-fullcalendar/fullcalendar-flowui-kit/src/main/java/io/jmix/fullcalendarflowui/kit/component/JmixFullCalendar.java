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
import io.jmix.fullcalendarflowui.kit.component.serialization.deserializer.JmixFullCalendarDeserializer;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixFullCalendarSerializer;
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
@JsModule("./src/fullcalendar/jmix-full-calendar-connector.js")
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

    @Nullable
    public CalendarView getInitialCalendarView() {
        return options.getInitialView().getValue();
    }

    public void setInitialCalendarView(CalendarView calendarView) {
        Objects.requireNonNull(calendarView);

        options.getInitialView().setValue(calendarView);
    }

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

    public void changeCalendarView(CalendarView calendarView) {
        Objects.requireNonNull(calendarView);

        getElement().executeJs("this.calendar.changeView($0)", calendarView.getId());
    }

    /**
     * Adds calendar custom view.
     * <p>
     * Note that it is initial option and dynamically changing/adding/removing custom view will not apply after
     * attaching component to UI.
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

    @Nullable
    public CustomCalendarView getCustomCalendarView(String viewId) {
        return options.getViews().getCustomCalendarView(viewId);
    }

    public List<CustomCalendarView> getCustomCalendarViews() {
        return options.getViews().getCustomCalendarViews();
    }

    public <T extends AbstractCalendarViewProperties> T getCalendarViewProperties(CalendarViewType calendarView) {
        return options.getViews().getCalendarViewProperties(calendarView);
    }

    public boolean isWeekNumbersVisible() {
        return options.getWeekNumbers().getNotNullValue();
    }

    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        options.getWeekNumbers().setValue(weekNumbersVisible);
    }

    @Nullable
    public LocalDate getValidRangeStart() {
        return options.getValidRange().getStart();
    }

    public void setValidRangeStart(@Nullable LocalDate start) {
        options.getValidRange().setStart(start);
    }

    @Nullable
    public LocalDate getValidRangeEnd() {
        return options.getValidRange().getEnd();
    }

    public void setValidRangeEnd(@Nullable LocalDate end) {
        options.getValidRange().setEnd(end);
    }

    public void setValidRange(@Nullable LocalDate start, @Nullable LocalDate end) {
        options.getValidRange().setRange(start, end);
    }

    @Nullable
    public LocalDate getVisibleRangeStart() {
        return options.getVisibleRange().getStart();
    }

    @Nullable
    public LocalDate getVisibleRangeEnd() {
        return options.getVisibleRange().getEnd();
    }

    public void setVisibleRange(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        options.getVisibleRange().setRange(start, end);
    }

    /**
     * The default value is user's timezone. If no user's timezone, the system default is used for component.
     *
     * @return {@code null} if not set
     */
    @Nullable
    public TimeZone getTimeZone() {
        return options.getTimeZone().getValue();
    }

    public void setTimeZone(@Nullable TimeZone timeZone) {
        options.getTimeZone().setValue(timeZone != null ? timeZone : TimeZone.getDefault());
    }

    public void navigateToNext() {
        getElement().callJsFunction("navigateToNext");
    }

    public void navigateToNext(CalendarDuration duration) {
        getElement().callJsFunction("incrementDate", serializer.serializeCalendarDuration(duration));
    }

    public void navigateToPrevious() {
        getElement().callJsFunction("navigateToPrevious");
    }

    public void navigateToPrevious(CalendarDuration duration) {
        getElement().callJsFunction("incrementDate",
                serializer.serializeCalendarDuration(inverseDuration(duration)));
    }

    public void navigateToToday() {
        getElement().callJsFunction("navigateToToday");
    }

    public void navigateToDate(LocalDate date) {
        Objects.requireNonNull(date);
        getElement().callJsFunction("navigateToDate", date.toString());
    }

    public void scrollToTime(Long duration) {
        Objects.requireNonNull(duration);
        getElement().callJsFunction("scrollToTimeMs", duration.toString());
    }

    public void scrollToTime(LocalTime localTime) {
        Objects.requireNonNull(localTime);
        getElement().callJsFunction("scrollToTime", localTime.toString());
    }

    public void select(LocalDateTime start) {
        select(start, false);
    }

    public void select(LocalDateTime start, boolean allDay) {
        Objects.requireNonNull(start);
        getElement().callJsFunction("select", allDay, serializer.serializeDateTime(start));
    }

    public void select(LocalDateTime start, LocalDateTime end) {
        select(start, end, false);
    }

    public void select(LocalDateTime start, LocalDateTime end, boolean allDay) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        getElement().callJsFunction("select",
                allDay,
                serializer.serializeDateTime(start),
                serializer.serializeDateTime(end));
    }

    public void unselect() {
        getElement().executeJs("this.calendar.unselect()");
    }

    public boolean isNavigationLinksEnabled() {
        return options.getNavLinks().getNotNullValue();
    }

    public void setNavigationLinksEnabled(boolean enabled) {
        options.getNavLinks().setValue(enabled);
    }

    public boolean isDayMaxEventRowsLimited() {
        return options.getDayMaxEventRows().isLimited();
    }

    public void setLimitedDayMaxEventRows(boolean limited) {
        options.getDayMaxEventRows().setLimited(limited);
    }

    /**
     * @return {@code null} if not set
     */
    @Nullable
    public Integer getDayMaxEventRows() {
        return options.getDayMaxEventRows().getMax();
    }

    /**
     * Sets rows count of events in DAY_GRID_xxx views and in "all-day" section in TIME_GRID_xxx.
     * <p>
     * Note that "+x more" row is included to count. For instance, if dayMaxEventRows = 1, only the "+x more" row
     * will be shown.
     *
     * @param maxEventRows maximum value of event rows in cell
     */
    public void setDayMaxEventRows(@Nullable Integer maxEventRows) {
        options.getDayMaxEventRows().setMax(maxEventRows);
    }

    public boolean isDayMaxEventsLimited() {
        return options.getDayMaxEvents().isLimited();
    }

    public void setLimitedDayMaxEvents(boolean limited) {
        options.getDayMaxEvents().setLimited(limited);
    }

    /**
     * @return {@code null} if not set
     */
    @Nullable
    public Integer getDayMaxEvents() {
        return options.getDayMaxEvents().getMax();
    }

    /**
     * Sets rows count of events in DAY_GRID_xxx views and in "all-day" section in TIME_GRID_xxx.
     * <p>
     * Note that "+ more" row <strong>is not included</strong> to count. For instance, if dayMaxEventRows = 1,
     * one event and the "+x more" row will be shown.
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
     * Sets the maximum number of events that stack top-to-bottom for TIME_GREED_xxx views.
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
     * @return {@code null} if not set
     */
    @Nullable
    public CalendarView getMoreLinkCalendarView() {
        return options.getMoreLinkClick().getCalendarView();
    }

    public void setMoreLinkCalendarView(@Nullable CalendarView navigationView) {
        options.getMoreLinkClick().setCalendarView(navigationView);
    }

    public List<String> getMoreLinkClassNames() {
        return options.getMoreLinkClassNames().getClassNames();
    }

    public void setMoreLinkClassNames(@Nullable List<String> classNames) {
        options.getMoreLinkClassNames().setClassNames(classNames);
    }

    public void addMoreLinkClassName(String className) {
        Objects.requireNonNull(className);

        options.getMoreLinkClassNames().addClassName(className);
    }

    public void addMoreLinkClassNames(String... classNames) {
        Objects.requireNonNull(classNames);

        options.getMoreLinkClassNames().addClassNames(classNames);
    }

    public boolean isEventStartEditable() {
        return options.getEventStartEditable().getNotNullValue();
    }

    public void setEventStartEditable(boolean editable) {
        options.getEventStartEditable().setValue(editable);
    }

    public boolean isEventDurationEditable() {
        return options.getEventDurationEditable().getNotNullValue();
    }

    public void setEventDurationEditable(boolean editable) {
        options.getEventDurationEditable().setValue(editable);
    }

    public boolean isEventResizableFromStart() {
        return options.getEventResizableFromStart().getNotNullValue();
    }

    public void setEventResizableFromStart(boolean resizableFromStart) {
        options.getEventResizableFromStart().setValue(resizableFromStart);
    }

    /**
     * @return {@code null} if not set
     */
    @Nullable
    public Integer getEventDragMinDistance() {
        return options.getEventDragMinDistance().getValue();
    }

    public void setEventDragMinDistance(@Nullable Integer minDistance) {
        options.getEventDragMinDistance().setValue(minDistance);
    }

    /**
     * @return {@code null} if not set
     */
    @Nullable
    public Integer getDragRevertDuration() {
        return options.getDragRevertDuration().getValue();
    }

    public void setDragRevertDuration(@Nullable Integer revertDuration) {
        options.getDragRevertDuration().setValue(revertDuration);
    }

    public boolean isDragScrollEnabled() {
        return options.getDragScroll().getNotNullValue();
    }

    /**
     * Note that this property is supposed to be used at design time. Property changes at runtime may not be applied.
     *
     * @param enabled
     */
    public void setDragScrollEnabled(boolean enabled) {
        options.getDragScroll().setValue(enabled);
    }

    /**
     * @return {@code null} if not set
     */
    @Nullable
    public CalendarDuration getSnapDuration() {
        return options.getSnapDuration().getValue();
    }

    public void setSnapDuration(@Nullable CalendarDuration snapDuration) {
        options.getSnapDuration().setValue(snapDuration);
    }

    public boolean isAllMaintainDurationEnabled() {
        return options.getAllDayMaintainDuration().getNotNullValue();
    }

    public void setAllDayMaintainDurationEnabled(boolean enabled) {
        options.getAllDayMaintainDuration().setValue(enabled);
    }

    public boolean isEventOverlapEnabled() {
        return options.getEventOverlap().isEnabled();
    }

    public void setEventOverlapEnabled(boolean enabled) {
        options.getEventOverlap().setEnabled(enabled);
    }

    /**
     * @return {@code null} if not set
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
     * For instance:
     * <pre>{@code
     * calendar.setEventOverlapJsFunction(new JsFunction("""
     *         function(stillEvent, movingEvent) {
     *             return stillEvent.allDay && movingEvent.allDay;
     *         }
     *         """));
     * }</pre>
     * The {@code movingEvent} is the event that is being dragged or resized. Its start and end dates will remain
     * at their original values when the function is called.
     * <p>
     * The {@code stillEvent} is the event underneath the moving event.
     * <p>
     * The available properties of {@code movingEvent} and {@code stillEvent} you can find in
     * <a href="https://fullcalendar.io/docs/event-object">FullCalendar docs</a>
     * <p>
     * Note that JavaScript function takes precedence over the {@link #setEventOverlapEnabled(boolean)}. And
     * JavaScript function can be overridden by calendar event's "overlap" property value.
     *
     * @param jsFunction JavaScript function
     */
    public void setEventOverlapJsFunction(@Nullable JsFunction jsFunction) {
        options.getEventOverlap().setJsFunction(jsFunction);
    }

    public boolean isSelectionEnabled() {
        return options.getSelectable().getNotNullValue();
    }

    public void setSelectionEnabled(boolean enabled) {
        options.getSelectable().setValue(enabled);
    }

    public boolean isSelectMirrorEnabled() {
        return options.getSelectMirror().getNotNullValue();
    }

    public void setSelectMirrorEnabled(boolean enabled) {
        options.getSelectMirror().setValue(enabled);
    }

    public boolean isUnselectAutoEnabled() {
        return options.getUnselectAuto().getNotNullValue();
    }

    /**
     * Sets whether clicking elsewhere on the page will cause the current selection to be cleared.
     * <p>
     * Works only if {@link #isSelectionEnabled()} is {@code true}.
     * <p>
     * Note that only initial value is applied to component, i.e. value that is set before attaching component.
     * Changing option at runtime won't change the initial value.
     *
     * @param enabled whether option is enabled
     */
    public void setUnselectAutoEnabled(boolean enabled) {
        options.getUnselectAuto().setValue(enabled);
    }

    @Nullable
    public String getUnselectCancelClassName() {
        return options.getUnselectCancel().getValue();
    }

    /**
     * Sets the class name of elements that will ignore the {@link #isUnselectAutoEnabled()} property.
     * <p>
     * Note that only initial value is applied to component, i.e. value that is set before attaching component.
     * Changing property at runtime won't change the initial value.
     *
     * @param className CSS class name, e.g. ".my-element"
     */
    public void setUnselectCancelClassName(@Nullable String className) {
        options.getUnselectCancel().setValue(className);
    }

    public boolean isSelectOverlapEnabled() {
        return options.getSelectOverlap().isEnabled();
    }

    public void setSelectOverlapEnabled(boolean enabled) {
        options.getSelectOverlap().setEnabled(enabled);
    }

    @Nullable
    public JsFunction getSelectOverlapJsFunction() {
        return options.getSelectOverlap().getJsFunction();
    }

    public void setSelectOverlapJsFunction(@Nullable JsFunction jsFunction) {
        options.getSelectOverlap().setJsFunction(jsFunction);
    }

    @Nullable
    public JsFunction getSelectAllowJsFunction() {
        return options.getSelectAllow().getValue();
    }

    public void setSelectAllowJsFunction(@Nullable JsFunction jsFunction) {
        options.getSelectAllow().setValue(jsFunction);
    }

    public int getSelectMinDistance() {
        return options.getSelectMinDistance().getNotNullValue();
    }

    public void setSelectMinDistance(int minDistance) {
        options.getSelectMinDistance().setValue(minDistance);
    }

    @Nullable
    public String getDayPopoverFormat() {
        return options.getDayPopoverFormat().getValue();
    }

    public void setDayPopoverFormat(@Nullable String format) {
        options.getDayPopoverFormat().setValue(format);
    }

    @Nullable
    public String getDayHeaderFormat() {
        return options.getDayHeaderFormat().getValue();
    }

    public void setDayHeaderFormat(@Nullable String format) {
        options.getDayHeaderFormat().setValue(format);
    }

    @Nullable
    public String getWeekNumberFormat() {
        return options.getWeekNumberFormat().getValue();
    }

    /**
     * Note that it override the {@code weekText} value in i18n object.
     *
     * @param format
     */
    public void setWeekNumberFormat(@Nullable String format) {
        options.getWeekNumberFormat().setValue(format);
    }

    @Nullable
    public String getSlotNumberFormat() {
        return options.getSlotLabelFormat().getValue();
    }

    public void setSlotNumberFormat(@Nullable String format) {
        options.getSlotLabelFormat().setValue(format);
    }

    @Nullable
    public String getEventTimeFormat() {
        return options.getEventTimeFormat().getValue();
    }

    public void setEventTimeFormat(@Nullable String format) {
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
        getElement()
                .callJsFunction("_onCompleteInit")
                .then((__) -> initialized = true);
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
        CalendarViewType calendarViewType = Arrays.stream(CalendarViewType.values())
                .filter(cv -> id.equals(cv.getId()))
                .findFirst()
                .orElse(null);

        if (calendarViewType != null) {
            return calendarViewType;
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

    protected void clearEventProvidersOnDetach() {
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

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // As DataHolder is a shared object between calendars on a page,
        // we must remove event sources from it when component is detached.
        clearEventProvidersOnDetach();
    }
}
