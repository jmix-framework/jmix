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
import io.jmix.fullcalendarflowui.kit.component.model.option.JmixFullCalendarOptions;
import io.jmix.fullcalendarflowui.kit.component.serialization.deserializer.JmixFullCalendarDeserializer;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixFullCalendarSerializer;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Tag("jmix-full-calendar")
@NpmPackage(value = "@fullcalendar/core", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/interaction", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/daygrid", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/timegrid", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/list", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/multimonth", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/moment-timezone", version = "6.1.14")
@NpmPackage(value = "@fullcalendar/moment", version = "6.1.14")
@JsModule("./src/fullcalendar/jmix-full-calendar.js")
@JsModule("./src/fullcalendar/jmix-full-calendar-connector.js")
@CssImport("./src/fullcalendar/jmix-full-calendar.css")
public class JmixFullCalendar extends Component implements HasSize, HasStyle {
    private static final Logger log = LoggerFactory.getLogger(JmixFullCalendar.class);

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

    public CalendarView getInitialCalendarView() {
        return options.getInitialCalendarView();
    }

    public void setInitialCalendarView(CalendarView calendarView) {
        options.setInitialCalendarView(calendarView);
    }

    public CalendarView getCurrentCalendarView() {
        return calendarView != null ? calendarView : options.getInitialCalendarView();
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
    public void addCalendarCustomView(CalendarCustomView calendarCustomView) {
        Objects.requireNonNull(calendarCustomView);

        options.addCalendarCustomView(calendarCustomView);
    }

    /**
     * Removes calendar custom view.
     * <p>
     * Note that it is initial option and dynamically changing/adding/removing custom view will not apply after
     * attaching component to UI.
     *
     * @param calendarCustomView calendar custom view to remove
     */
    public void removeCalendarCustomView(CalendarCustomView calendarCustomView) {
        Objects.requireNonNull(calendarCustomView);

        options.removeCalendarCustomView(calendarCustomView);
    }

    public List<CalendarCustomView> getCalendarCustomViews() {
        return options.getCalendarCustomViews();
    }

    public boolean isWeekNumbersVisible() {
        return options.isWeekNumbers();
    }

    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        options.setWeekNumbers(weekNumbersVisible);
    }

    public LocalDate getValidRangeStart() {
        return options.getValidRange().getStart();
    }

    public void setValidRangeStart(LocalDate start) {
        options.setValidRange(start, options.getValidRange().getEnd());
    }

    public LocalDate getValidRangeEnd() {
        return options.getValidRange().getEnd();
    }

    public void setValidRangeEnd(LocalDate end) {
        options.setValidRange(options.getValidRange().getStart(), end);
    }

    public void setValidRange(LocalDate start, LocalDate end) {
        options.setValidRange(start, end);
    }

    @Nullable
    public LocalDate getVisibleRangeStart() {
        return options.getVisibleRange() != null ? options.getVisibleRange().getStart() : null;
    }

    @Nullable
    public LocalDate getVisibleRangeEnd() {
        return options.getVisibleRange() != null ? options.getVisibleRange().getEnd() : null;
    }

    public void setVisibleRange(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        options.setVisibleRange(start, end);
    }

    public TimeZone getTimeZone() {
        return options.getTimeZone();
    }

    public void setTimeZone(@Nullable TimeZone timeZone) {
        options.setTimeZone(timeZone != null ? timeZone : TimeZone.getDefault());
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
        return options.isNavLinks();
    }

    public void setNavigationLinksEnabled(boolean enabled) {
        options.setNavLinks(enabled);
    }

    public boolean isDayMaxEventRowsEnabled() {
        return options.getDayMaxEventRows().isEnabled();
    }

    public void setDayMaxEventRowsEnabled(boolean enabled) {
        options.setDayMaxEventRows(enabled, options.getDayMaxEventRows().getMax());
    }

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
        options.setDayMaxEventRows(options.getDayMaxEventRows().isEnabled(), maxEventRows);
    }

    public boolean isDayMaxEventsEnabled() {
        return options.getDayMaxEvent().isEnabled();
    }

    public void setDayMaxEventsEnabled(boolean enabled) {
        options.setDayMaxEvent(enabled, options.getDayMaxEvent().getMax());
    }

    @Nullable
    public Integer getDayMaxEvents() {
        return options.getDayMaxEvent().getMax();
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
        options.setDayMaxEvent(options.getDayMaxEvent().isEnabled(), maxRows);
    }

    /**
     * @return maximum number of events to stack or {@code -1} if not set
     */
    public Integer getEventMaxStack() {
        return options.getEventMaxStack();
    }

    /**
     * Sets the maximum number of events that stack top-to-bottom for TIME_GREED_xxx views.
     *
     * @param eventMaxStack the maximum number of events that stack. The {@code null} or {@code -1}
     *                      values set default behaviour.
     */
    public void setEventMaxStack(@Nullable Integer eventMaxStack) {
        int maxStack = eventMaxStack == null ? -1 : eventMaxStack;
        if (maxStack < -1) {
            throw new IllegalArgumentException("Event max stack value must be >= -1");
        }
        options.setEventMaxStack(maxStack);
    }

    @Nullable
    public CalendarView getMoreLinkCalendarView() {
        return options.getMoreLinkCalendarView();
    }

    public void setMoreLinkCalendarView(@Nullable CalendarView navigationView) {
        options.setMoreLinkCalendarView(navigationView);
    }

    public List<String> getMoreLinkClassNames() {
        return options.getMoreLinkClassNames().getClassNames();
    }

    public void setMoreLinkClassNames(@Nullable List<String> classNames) {
        options.setMoreLinkClassNames(classNames, options.getMoreLinkClassNames().isFunction());
    }

    public void addMoreLinkClassName(String className) {
        List<String> classNames = new ArrayList<>(options.getMoreLinkClassNames().getClassNames());
        classNames.add(className);
        options.setMoreLinkClassNames(classNames, options.getMoreLinkClassNames().isFunction());
    }

    public void addMoreLinkClassNames(String... classNames) {
        List<String> optionValue = new ArrayList<>(options.getMoreLinkClassNames().getClassNames());
        optionValue.addAll(List.of(classNames));
        options.setMoreLinkClassNames(optionValue, options.getMoreLinkClassNames().isFunction());
    }

    public boolean isEventStartEditable() {
        return options.isEventStartEditable();
    }

    public void setEventStartEditable(boolean editable) {
        options.setEventStartEditable(editable);
    }

    public boolean isEventDurationEditable() {
        return options.isEventDurationEditable();
    }

    public void setEventDurationEditable(boolean editable) {
        options.setEventDurationEditable(editable);
    }

    public boolean isEventResizableFromStart() {
        return options.isEventResizableFromStart();
    }

    public void setEventResizableFromStart(boolean resizableFromStart) {
        options.setEventResizableFromStart(resizableFromStart);
    }

    public int getEventDragMinDistance() {
        return options.getEventDragMinDistance();
    }

    public void setEventDragMinDistance(int minDistance) {
        options.setEventDragMinDistance(minDistance);
    }

    public int getDragRevertDuration() {
        return options.getDragRevertDuration();
    }

    public void setDragRevertDuration(int revertDuration) {
        options.setDragRevertDuration(revertDuration);
    }

    public boolean isDragScrollEnabled() {
        return options.isDragScroll();
    }

    public void setDragScrollEnabled(boolean enabled) {
        options.setDragScroll(enabled);
    }

    public CalendarDuration getSnapDuration() {
        return options.getSnapDuration();
    }

    public void setSnapDuration(CalendarDuration snapDuration) {
        options.setSnapDuration(snapDuration);
    }

    public boolean isAllMaintainDurationEnabled() {
        return options.isAllDayMaintainDuration();
    }

    public void setAllDayMaintainDurationEnabled(boolean enabled) {
        options.setAllDayMaintainDuration(enabled);
    }

    public boolean isEventOverlapEnabled() {
        return options.getEventOverlap().isEnabled();
    }

    public void setEventOverlapEnabled(boolean enabled) {
        options.setEventOverlap(enabled, options.getEventOverlap().getJsFunction());
    }

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
     * JavaScript function can be overrided by calendar event's "overlap" property value.
     *
     * @param jsFunction JavaScript function
     */
    public void setEventOverlapJsFunction(@Nullable JsFunction jsFunction) {
        options.setEventOverlap(options.getEventOverlap().isEnabled(), jsFunction);
    }

    public boolean isSelectionEnabled() {
        return options.isSelectable();
    }

    public void setSelectionEnabled(boolean enabled) {
        options.setSelectable(enabled);
    }

    public boolean isSelectMirrorEnabled() {
        return options.isSelectMirror();
    }

    public void setSelectMirrorEnabled(boolean enabled) {
        options.setSelectMirror(enabled);
    }

    public boolean isUnselectAutoEnabled() {
        return options.isUnselectAuto();
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
        options.setUnselectAuto(enabled);
    }

    @Nullable
    public String getUnselectCancelClassName() {
        return options.getUnselectCancel();
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
        options.setUnselectCancel(className);
    }

    public boolean isSelectOverlapEnabled() {
        return options.getSelectOverlap().isEnabled();
    }

    public void setSelectOverlapEnabled(boolean enabled) {
        options.setSelectOverlap(enabled, options.getSelectOverlap().getJsFunction());
    }

    public JsFunction getSelectOverlapJsFunction() {
        return options.getSelectOverlap().getJsFunction();
    }

    public void setSelectOverlapJsFunction(@Nullable JsFunction jsFunction) {
        options.setSelectOverlap(options.getSelectOverlap().isEnabled(), jsFunction);
    }

    @Nullable
    public JsFunction getSelectAllowJsFunction() {
        return options.getSelectAllow();
    }

    public void setSelectAllowJsFunction(@Nullable JsFunction jsFunction) {
        options.setSelectAllow(jsFunction);
    }

    public int getSelectMinDistance() {
        return options.getSelectMinDistance();
    }

    public void setSelectMinDistance(int minDistance) {
        options.setSelectMinDistance(minDistance);
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
        JsonObject json = serializer.serializeOptions(onlyDirty ? options.getDirtyOptions() : options.getOptions());

        getElement().callJsFunction("updateOptions", json);

        options.markAllAsDirty(false);
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
        if (options.isInitial(event.getOption())) {
            updateInitialOptions();
        }
        requestUpdateOptions(true);
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

        CalendarCustomView customView = options.getCalendarCustomView(id);

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
