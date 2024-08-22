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

import java.util.*;
import java.util.function.Consumer;

public class JmixFullCalendarOptions {

    protected SimpleOption<Boolean> weekNumbers = new SimpleOption<>("weekNumbers", false);
    protected ValidRange validRange = new ValidRange();
    protected SimpleOption<TimeZone> timeZone = new SimpleOption<>("timeZone", TimeZone.getDefault());
    protected SimpleOption<CalendarView> initialView = new SimpleOption<>("initialView", CalendarViewType.DAY_GRID_MONTH);
    protected SimpleOption<Boolean> navLinks = new SimpleOption<>("navLinks", false);
    protected DayMaxEventRows dayMaxEventRows = new DayMaxEventRows();
    protected SimpleOption<Integer> eventMaxStack = new SimpleOption<>("eventMaxStack", -1);
    protected DayMaxEvents dayMaxEvents = new DayMaxEvents();
    protected MoreLinkClick moreLinkClick = new MoreLinkClick();

    protected MoreLinkClassNames moreLinkClassNames = new MoreLinkClassNames();
    protected SimpleOption<Boolean> eventStartEditable = new SimpleOption<>("eventStartEditable", false);
    protected SimpleOption<Boolean> eventDurationEditable = new SimpleOption<>("eventDurationEditable", false);
    protected SimpleOption<Boolean> eventResizableFromStart = new SimpleOption<>("eventResizableFromStart", false);
    protected SimpleOption<Integer> eventDragMinDistance = new SimpleOption<>("eventDragMinDistance", 5);
    protected EventOverlap eventOverlap = new EventOverlap();
    protected SimpleOption<Integer> dragRevertDuration = new SimpleOption<>("dragRevertDuration", 500);
    protected SimpleOption<CalendarDuration> snapDuration = new SimpleOption<>("snapDuration", CalendarDuration.ofMinutes(30));
    protected SimpleOption<Boolean> dragScroll = new SimpleOption<>("dragScroll", true);
    protected SimpleOption<Boolean> allDayMaintainDuration = new SimpleOption<>("allDayMaintainDuration", false);

    protected Views views = new Views();
    protected VisibleRange visibleRange = new VisibleRange();

    protected SimpleOption<Boolean> selectable = new SimpleOption<>("selectable", false);
    protected SimpleOption<Boolean> selectMirror = new SimpleOption<>("selectMirror", false);
    protected SimpleOption<Boolean> unselectAuto = new SimpleOption<>("unselectAuto", true);
    protected SimpleOption<String> unselectCancel = new SimpleOption<>("unselectCancel", "");
    protected SelectOverlap selectOverlap = new SelectOverlap();

    protected SimpleOption<JsFunction> selectAllow = new SimpleOption<>("selectAllow");
    protected SimpleOption<Integer> selectMinDistance = new SimpleOption<>("selectMinDistance", 0);

    protected SimpleOption<String> dayPopoverFormat = new SimpleOption<>("dayPopoverFormat", "MMMM D, YYYY");
    protected SimpleOption<String> dayHeaderFormat = new SimpleOption<>("dayHeaderFormat", "ddd");
    protected SimpleOption<String> weekNumberFormat = new SimpleOption<>("weekNumberFormat");
    protected SimpleOption<String> slotLabelFormat = new SimpleOption<>("slotLabelFormat", "ha");
    protected SimpleOption<String> eventTimeFormat = new SimpleOption<>("eventTimeFormat", "h:mma");

    protected SimpleOption<Integer> eventMinHeight = new SimpleOption<>("eventMinHeight", 15);
    protected SimpleOption<Integer> eventShortHeight = new SimpleOption<>("eventShortHeight", 30);
    protected SimpleOption<Boolean> slotEventOverlap = new SimpleOption<>("slotEventOverlap", true);

    protected SimpleOption<Boolean> weekends = new SimpleOption<>("weekends", true);
    protected SimpleOption<Boolean> dayHeaders = new SimpleOption<>("dayHeaders", true);
    protected SimpleOption<Boolean> dayHeaderClassNames = new SimpleOption<>("dayHeaderClassNames", false);
    protected SimpleOption<Boolean> dayCellClassNames = new SimpleOption<>("dayCellClassNames", false);

    protected SimpleOption<CalendarDuration> slotDuration = new SimpleOption<>("slotDuration", CalendarDuration.ofMinutes(30));
    protected SimpleOption<CalendarDuration> slotLabelInterval = new SimpleOption<>("slotLabelInterval");
    protected SimpleOption<CalendarDuration> slotMinTime = new SimpleOption<>("slotMinTime", CalendarDuration.ofHours(0));
    protected SimpleOption<CalendarDuration> slotMaxTime = new SimpleOption<>("slotMaxTime", CalendarDuration.ofHours(24));
    protected SimpleOption<CalendarDuration> scrollTime = new SimpleOption<>("scrollTime", CalendarDuration.ofHours(6));
    protected SimpleOption<Boolean> scrollTimeReset = new SimpleOption<>("scrollTimeReset", true);
    protected SimpleOption<Boolean> slotLabelClassNames = new SimpleOption<>("slotLabelClassNames", false);

    protected final List<CalendarOption> updatableOptions = new ArrayList<>(28);

    /**
     * Options that applied only at creation time.
     */
    protected final List<CalendarOption> initialOptions = new ArrayList<>(4);

    protected Consumer<OptionChangeEvent> optionChangeListener;

    public JmixFullCalendarOptions() {
        updatableOptions.addAll(List.of(weekNumbers, validRange, timeZone, navLinks, dayMaxEventRows,
                eventMaxStack, dayMaxEvents, moreLinkClick, moreLinkClassNames, eventStartEditable,
                eventDurationEditable, eventResizableFromStart, eventDragMinDistance, eventOverlap, dragRevertDuration,
                snapDuration, allDayMaintainDuration, selectable, selectMirror, unselectCancel,
                selectOverlap, selectAllow, visibleRange, weekends, dayHeaderClassNames, dayCellClassNames,
                slotDuration, slotLabelInterval, slotMinTime, slotMaxTime, slotLabelClassNames));

        initialOptions.addAll(List.of(initialView, unselectAuto, unselectCancel, selectMinDistance, views, dragScroll,
                dayPopoverFormat, dayHeaderFormat, weekNumberFormat, slotLabelFormat, eventTimeFormat,
                eventMinHeight, eventShortHeight, slotEventOverlap, scrollTime, scrollTimeReset));

        updatableOptions.forEach(o -> o.addChangeListener(this::onOptionChange));
        initialOptions.forEach(o -> o.addChangeListener(this::onOptionChange));
    }

    public List<CalendarOption> getUpdatableOptions() {
        return updatableOptions;
    }

    public List<CalendarOption> getInitialOptions() {
        return initialOptions;
    }

    public List<CalendarOption> getDirtyOptions() {
        return updatableOptions.stream()
                .filter(CalendarOption::isDirty)
                .toList();
    }

    public void unmarkAllAsDirty() {
        updatableOptions.forEach(CalendarOption::unmarkAsDirty);
    }

    public SimpleOption<Boolean> getWeekNumbers() {
        return weekNumbers;
    }

    public ValidRange getValidRange() {
        return validRange;
    }

    public SimpleOption<TimeZone> getTimeZone() {
        return timeZone;
    }

    public SimpleOption<CalendarView> getInitialView() {
        return initialView;
    }

    public SimpleOption<Boolean> getNavLinks() {
        return navLinks;
    }

    public DayMaxEventRows getDayMaxEventRows() {
        return dayMaxEventRows;
    }

    public SimpleOption<Integer> getEventMaxStack() {
        return eventMaxStack;
    }

    public DayMaxEvents getDayMaxEvents() {
        return dayMaxEvents;
    }

    public MoreLinkClick getMoreLinkClick() {
        return moreLinkClick;
    }

    public MoreLinkClassNames getMoreLinkClassNames() {
        return moreLinkClassNames;
    }

    public SimpleOption<Boolean> getEventStartEditable() {
        return eventStartEditable;
    }

    public SimpleOption<Boolean> getEventDurationEditable() {
        return eventDurationEditable;
    }

    public SimpleOption<Boolean> getEventResizableFromStart() {
        return eventResizableFromStart;
    }

    public SimpleOption<Integer> getEventDragMinDistance() {
        return eventDragMinDistance;
    }

    public EventOverlap getEventOverlap() {
        return eventOverlap;
    }

    public SimpleOption<Integer> getDragRevertDuration() {
        return dragRevertDuration;
    }

    public SimpleOption<Boolean> getDragScroll() {
        return dragScroll;
    }

    public SimpleOption<CalendarDuration> getSnapDuration() {
        return snapDuration;
    }

    public SimpleOption<Boolean> getAllDayMaintainDuration() {
        return allDayMaintainDuration;
    }

    public SimpleOption<Boolean> getSelectable() {
        return selectable;
    }

    public SimpleOption<Boolean> getSelectMirror() {
        return selectMirror;
    }

    public SimpleOption<Boolean> getUnselectAuto() {
        return unselectAuto;
    }

    public SimpleOption<String> getUnselectCancel() {
        return unselectCancel;
    }

    public SelectOverlap getSelectOverlap() {
        return selectOverlap;
    }

    public SimpleOption<JsFunction> getSelectAllow() {
        return selectAllow;
    }

    public SimpleOption<Integer> getSelectMinDistance() {
        return selectMinDistance;
    }

    public Views getViews() {
        return views;
    }

    public VisibleRange getVisibleRange() {
        return visibleRange;
    }

    public SimpleOption<String> getDayPopoverFormat() {
        return dayPopoverFormat;
    }

    public SimpleOption<String> getDayHeaderFormat() {
        return dayHeaderFormat;
    }

    public SimpleOption<String> getWeekNumberFormat() {
        return weekNumberFormat;
    }

    public SimpleOption<String> getSlotLabelFormat() {
        return slotLabelFormat;
    }

    public SimpleOption<String> getEventTimeFormat() {
        return eventTimeFormat;
    }

    public SimpleOption<Integer> getEventMinHeight() {
        return eventMinHeight;
    }

    public SimpleOption<Integer> getEventShortHeight() {
        return eventShortHeight;
    }

    public SimpleOption<Boolean> getSlotEventOverlap() {
        return slotEventOverlap;
    }

    public SimpleOption<Boolean> getWeekends() {
        return weekends;
    }

    public SimpleOption<Boolean> getDayHeaders() {
        return dayHeaders;
    }

    public SimpleOption<Boolean> getDayHeaderClassNames() {
        return dayHeaderClassNames;
    }

    public SimpleOption<Boolean> getDayCellClassNames() {
        return dayCellClassNames;
    }

    public SimpleOption<CalendarDuration> getSlotDuration() {
        return slotDuration;
    }

    public SimpleOption<CalendarDuration> getSlotLabelInterval() {
        return slotLabelInterval;
    }

    public SimpleOption<CalendarDuration> getSlotMinTime() {
        return slotMinTime;
    }

    public SimpleOption<CalendarDuration> getSlotMaxTime() {
        return slotMaxTime;
    }

    public SimpleOption<CalendarDuration> getScrollTime() {
        return scrollTime;
    }

    public SimpleOption<Boolean> getScrollTimeReset() {
        return scrollTimeReset;
    }

    public SimpleOption<Boolean> getSlotLabelClassNames() {
        return slotLabelClassNames;
    }

    public boolean isInitial(CalendarOption option) {
        return initialOptions.contains(option);
    }

    public void setOptionChangeListener(@Nullable Consumer<OptionChangeEvent> listener) {
        this.optionChangeListener = listener;
    }

    protected void onOptionChange(CalendarOption.OptionChangeEvent event) {
        fireChangeEvent(event.getSource());
    }

    protected void fireChangeEvent(CalendarOption option) {
        if (optionChangeListener != null) {
            optionChangeListener.accept(new OptionChangeEvent(option));
        }
    }

    public static class OptionChangeEvent extends EventObject {

        public OptionChangeEvent(CalendarOption source) {
            super(source);
        }

        public CalendarOption getOption() {
            return (CalendarOption) source;
        }
    }
}
