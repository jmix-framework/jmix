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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import io.jmix.fullcalendarflowui.kit.component.model.*;
import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class JmixFullCalendarOptions {

    private static final JsFunction NULL_FUNCTION = new JsFunction("");

    protected Option locale = new Option("locale", Locale.ENGLISH);
    protected Option weekNumbers = new Option("weekNumbers", false);
    protected Option validRangeDates = new Option("validRange", new ValidRange());
    protected Option timeZone = new Option("timeZone", TimeZone.getDefault().getID());
    protected Option initialView = new Option("initialView", CalendarViewType.DAY_GRID_MONTH);
    protected Option navLinks = new Option("navLinks", false);
    protected Option dayMaxEventRows = new Option("dayMaxEventRows", new DayMaxEventRows());
    protected Option eventMaxStack = new Option("eventMaxStack", -1);
    protected Option dayMaxEvents = new Option("dayMaxEvents", new DayMaxEvents());
    protected Option moreLinkClick = new Option("moreLinkClick", (CalendarView) () -> "popover");
    protected Option moreLinkClickFunction = new Option("moreLinkClickFunction", false);
    protected Option moreLinkClassNames = new Option("moreLinkClassNames", new MoreLinkClassNames());
    protected Option eventStartEditable = new Option("eventStartEditable", false);
    protected Option eventDurationEditable = new Option("eventDurationEditable", false);
    protected Option eventResizableFromStart = new Option("eventResizableFromStart", false);
    protected Option eventDragMinDistance = new Option("eventDragMinDistance", 5);
    protected Option eventOverlap = new Option("eventOverlap", new EventOverlap());
    protected Option dragRevertDuration = new Option("dragRevertDuration", 500);
    protected Option dragScroll = new Option("dragScroll", true);
    protected Option snapDuration = new Option("snapDuration", CalendarDuration.ofMinutes(30));
    protected Option allDayMaintainDuration = new Option("allDayMaintainDuration", false);


    protected Option selectable = new Option("selectable", false);
    protected Option selectMirror = new Option("selectMirror", false);
    protected Option unselectAuto = new Option("unselectAuto", true);
    protected Option unselectCancel = new Option("unselectCancel", "");
    protected Option selectOverlap = new Option("selectOverlap", new SelectOverlap());
    protected Option selectAllow = new Option("selectAllow", NULL_FUNCTION);
    protected Option selectMinDistance = new Option("selectMinDistance", 0);

    protected final List<Option> options = new ArrayList<>(28);

    /**
     * Options that applied only at creation time.
     */
    protected final List<Option> initialOptions = new ArrayList<>(4);

    protected Consumer<OptionChangeEvent> optionChangeListener;

    public JmixFullCalendarOptions() {
        options.addAll(List.of(locale, weekNumbers, validRangeDates, timeZone, initialView,
                navLinks, dayMaxEventRows, eventMaxStack, dayMaxEvents, moreLinkClick, moreLinkClickFunction,
                moreLinkClassNames, eventStartEditable, eventDurationEditable, eventResizableFromStart,
                eventDragMinDistance, eventOverlap, dragRevertDuration, dragScroll, snapDuration,
                allDayMaintainDuration, selectable, selectMirror, unselectAuto,
                unselectCancel, selectOverlap, selectAllow, selectMinDistance));

        initialOptions.addAll(List.of(initialView, unselectAuto, unselectCancel, selectMinDistance));
    }

    public List<Option> getOptions() {
        return options;
    }

    public List<Option> getInitialOptions() {
        return initialOptions;
    }

    public List<Option> getDirtyOptions() {
        return options.stream()
                .filter(Option::isDirty)
                .toList();
    }

    public void markAllAsDirty(boolean dirty) {
        options.forEach(o -> o.markAsDirty(dirty));
    }

    public Locale getLocale() {
        return locale.getValue();
    }

    public void setLocale(Locale locale) {
        this.locale.setValue(locale);
    }

    public boolean isWeekNumbers() {
        return weekNumbers.getValue();
    }

    public void setWeekNumbers(boolean weekNumbers) {
        this.weekNumbers.setValue(weekNumbers);
    }

    public void setValidRange(LocalDate start, LocalDate end) {
        ValidRange value = new ValidRange();
        value.setStart(start);
        value.setEnd(end);

        validRangeDates.setValue(value);
    }

    public ValidRange getValidRange() {
        return validRangeDates.getValue();
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone((String) timeZone.getValue());
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone.setValue(timeZone.getID());
    }

    public CalendarView getInitialCalendarView() {
        return initialView.getValue();
    }

    public void setInitialCalendarView(CalendarView calendarView) {
        this.initialView.setValue(calendarView);
    }

    public boolean isNavLinks() {
        return navLinks.getValue();
    }

    public void setNavLinks(boolean navLinks) {
        this.navLinks.setValue(navLinks);
    }

    public DayMaxEventRows getDayMaxEventRows() {
        return dayMaxEventRows.getValue();
    }

    public void setDayMaxEventRows(boolean enabled, Integer maxRows) {
        this.dayMaxEventRows.setValue(new DayMaxEventRows(enabled, maxRows));
    }

    public Integer getEventMaxStack() {
        return eventMaxStack.getValue();
    }

    public void setEventMaxStack(Integer eventMaxStack) {
        this.eventMaxStack.setValue(eventMaxStack);
    }

    public DayMaxEvents getDayMaxEvent() {
        return dayMaxEvents.getValue();
    }

    public void setDayMaxEvent(boolean enabled, Integer maxRows) {
        this.dayMaxEvents.setValue(new DayMaxEvents(enabled, maxRows));
    }

    @Nullable
    public CalendarView getMoreLinkCalendarView() {
        CalendarView calendarView = moreLinkClick.getValue();
        return calendarView.getId().equals("popover") ? null : calendarView;
    }

    public void setMoreLinkCalendarView(@Nullable CalendarView moreLinkNavigationView) {
        this.moreLinkClick.setValue(
                moreLinkNavigationView == null
                        ? (CalendarView) () -> "popover"
                        : moreLinkNavigationView);

        moreLinkClickFunction.markAsDirty(true);
    }

    public boolean getMoreLinkClickFunction() {
        return moreLinkClickFunction.getValue();
    }

    public void setMoreLinkClickFunction(boolean function) {
        moreLinkClickFunction.setValue(function);

        moreLinkClick.markAsDirty(true);
    }

    public MoreLinkClassNames getMoreLinkClassNames() {
        return moreLinkClassNames.getValue();
    }

    public void setMoreLinkClassNames(List<String> classNames, boolean function) {
        moreLinkClassNames.setValue(new MoreLinkClassNames(classNames, function));
    }

    public boolean isEventStartEditable() {
        return eventStartEditable.getValue();
    }

    public void setEventStartEditable(boolean editable) {
        eventStartEditable.setValue(editable);
    }

    public boolean isEventDurationEditable() {
        return eventDurationEditable.getValue();
    }

    public void setEventDurationEditable(boolean editable) {
        eventDurationEditable.setValue(editable);
    }

    public boolean isEventResizableFromStart() {
        return eventResizableFromStart.getValue();
    }

    public void setEventResizableFromStart(boolean resizableFromStart) {
        eventResizableFromStart.setValue(resizableFromStart);
    }

    public int getEventDragMinDistance() {
        return eventDragMinDistance.getValue();
    }

    public void setEventDragMinDistance(int minDistance) {
        eventDragMinDistance.setValue(minDistance);
    }

    public EventOverlap getEventOverlap() {
        return eventOverlap.getValue();
    }

    public void setEventOverlap(boolean overlap, @Nullable JsFunction jsFunction) {
        eventOverlap.setValue(new EventOverlap(overlap, jsFunction));
    }

    public int getDragRevertDuration() {
        return dragRevertDuration.getValue();
    }

    public void setDragRevertDuration(int revertDuration) {
        dragRevertDuration.setValue(revertDuration);
    }

    public boolean isDragScroll() {
        return dragScroll.getValue();
    }

    public void setDragScroll(boolean enabled) {
        dragScroll.setValue(enabled);
    }

    public CalendarDuration getSnapDuration() {
        return snapDuration.getValue();
    }

    public void setSnapDuration(CalendarDuration snapDuration) {
        this.snapDuration.setValue(snapDuration);
    }

    public boolean isAllDayMaintainDuration() {
        return allDayMaintainDuration.getValue();
    }

    public void setAllDayMaintainDuration(boolean allDayMaintainDuration) {
        this.allDayMaintainDuration.setValue(allDayMaintainDuration);
    }


    public boolean isSelectable() {
        return selectable.getValue();
    }

    public void setSelectable(boolean selectionEnabled) {
        selectable.setValue(selectionEnabled);
    }

    public boolean isSelectMirror() {
        return selectMirror.getValue();
    }

    public void setSelectMirror(boolean selectMirror) {
        this.selectMirror.setValue(selectMirror);
    }

    public boolean isUnselectAuto() {
        return unselectAuto.getValue();
    }

    public void setUnselectAuto(boolean unselectAuto) {
        this.unselectAuto.setValue(unselectAuto);
    }

    @Nullable
    public String getUnselectCancel() {
        return ((String) unselectCancel.getValue()).isEmpty() ? null : unselectCancel.getValue();
    }

    public void setUnselectCancel(@Nullable String unselectCancel) {
        this.unselectCancel.setValue(unselectCancel == null ? "" : unselectCancel);
    }

    public SelectOverlap getSelectOverlap() {
        return selectOverlap.getValue();
    }

    public void setSelectOverlap(boolean enabled, @Nullable JsFunction jsFunction) {
        selectOverlap.setValue(new SelectOverlap(enabled, jsFunction));
    }


    @Nullable
    public JsFunction getSelectAllow() {
        return selectAllow.getValue().equals(NULL_FUNCTION) ? null : selectAllow.getValue();
    }

    public void setSelectAllow(@Nullable JsFunction jsFunction) {
        selectAllow.setValue(jsFunction == null ? NULL_FUNCTION : jsFunction);
    }

    public int getSelectMinDistance() {
        return selectMinDistance.getValue();
    }

    public void setSelectMinDistance(int minDistance) {
        selectMinDistance.setValue(minDistance);
    }

    public boolean isInitial(Option option) {
        return initialOptions.contains(option);
    }

    public void setOptionChangeListener(@Nullable Consumer<OptionChangeEvent> listener) {
        this.optionChangeListener = listener;
    }

    protected void fireChangeEvent(Option option) {
        if (optionChangeListener != null) {
            optionChangeListener.accept(new OptionChangeEvent(option));
        }
    }

    public class Option {
        protected String name;
        protected Object value;
        protected boolean dirty = false;

        public Option(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public <V> V getValue() {
            return (V) value;
        }

        public <V> void setValue(V value) {
            if (!Objects.equals(this.value, value)) {
                this.value = value;
                markAsDirty(true);
                fireChangeEvent(this);
            }
        }

        public void markAsDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public boolean isDirty() {
            return dirty;
        }
    }

    public static class OptionChangeEvent extends EventObject {

        public OptionChangeEvent(Option source) {
            super(source);
        }

        public Option getOption() {
            return (Option) source;
        }
    }
}
