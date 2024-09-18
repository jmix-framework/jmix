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

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils.*;

/**
 * INTERNAL.
 */
public class JmixFullCalendarOptions {

    /**
     * Options that applied only at creation time.
     */
    protected Map<String, CalendarOption> initialOptionsMap = new HashMap<>(15);

    /**
     * Options that can be updated during runtime.
     */
    protected Map<String, CalendarOption> optionsMap = new HashMap<>(57);

    {
        initialOptionsMap.put(DAY_HEADER_FORMAT, new SimpleOption<>(DAY_HEADER_FORMAT, "ddd"));
        initialOptionsMap.put(DAY_POPOVER_FORMAT, new SimpleOption<>(DAY_POPOVER_FORMAT, "MMMM D, YYYY"));
        initialOptionsMap.put(DRAG_SCROLL, new SimpleOption<>(DRAG_SCROLL, true));
        initialOptionsMap.put(EVENT_TIME_FORMAT, new SimpleOption<>(EVENT_TIME_FORMAT, "h:mma"));
        initialOptionsMap.put(INITIAL_DATE, new SimpleOption<>(INITIAL_DATE));
        initialOptionsMap.put(INITIAL_VIEW, new SimpleOption<>(INITIAL_VIEW, CalendarDisplayModes.DAY_GRID_MONTH));
        initialOptionsMap.put(NOW_INDICATOR, new SimpleOption<>(NOW_INDICATOR, false));
        initialOptionsMap.put(SCROLL_TIME, new SimpleOption<>(SCROLL_TIME, CalendarDuration.ofHours(6)));
        initialOptionsMap.put(SCROLL_TIME_RESET, new SimpleOption<>(SCROLL_TIME_RESET, true));
        initialOptionsMap.put(SELECT_MIN_DISTANCE, new SimpleOption<>(SELECT_MIN_DISTANCE, 0));
        initialOptionsMap.put(SLOT_LABEL_FORMAT, new SimpleOption<>(SLOT_LABEL_FORMAT, "ha"));
        initialOptionsMap.put(UNSELECT_AUTO, new SimpleOption<>(UNSELECT_AUTO, true));
        initialOptionsMap.put(UNSELECT_CANCEL, new SimpleOption<>(UNSELECT_CANCEL, ""));
        initialOptionsMap.put(VIEWS, new DisplayModes());
        initialOptionsMap.put(WEEK_NUMBER_FORMAT, new SimpleOption<>(WEEK_NUMBER_FORMAT));

        optionsMap.put(ALL_DAY_MAINTAIN_DURATION, new SimpleOption<>(ALL_DAY_MAINTAIN_DURATION, false));
        optionsMap.put(DAY_HEADERS, new SimpleOption<>(DAY_HEADERS, true));
        optionsMap.put(DAY_MAX_EVENTS, new DayMaxEvents());
        optionsMap.put(DAY_MAX_EVENT_ROWS, new DayMaxEventRows());
        optionsMap.put(DATE_ALIGNMENT, new SimpleOption<>(DATE_ALIGNMENT, ""));
        optionsMap.put(DAY_HEADER_CLASS_NAMES, new SimpleOption<>(DAY_HEADER_CLASS_NAMES, false));
        optionsMap.put(DAY_CELL_CLASS_NAMES, new SimpleOption<>(DAY_CELL_CLASS_NAMES, false));
        optionsMap.put(DEFAULT_ALL_DAY, new SimpleOption<>(DEFAULT_ALL_DAY, false));
        optionsMap.put(DEFAULT_ALL_DAY_EVENT_DURATION, new SimpleOption<>(DEFAULT_ALL_DAY_EVENT_DURATION, CalendarDuration.ofDays(1)));
        optionsMap.put(DRAG_REVERT_DURATION, new SimpleOption<>(DRAG_REVERT_DURATION, 500));
        optionsMap.put(DEFAULT_TIMED_EVENT_DURATION, new SimpleOption<>(DEFAULT_TIMED_EVENT_DURATION, CalendarDuration.ofHours(1)));
        optionsMap.put(DATE_INCREMENT, new SimpleOption<>(DATE_INCREMENT));
        optionsMap.put(DISPLAY_EVENT_TIME, new SimpleOption<>(DISPLAY_EVENT_TIME, true));
        optionsMap.put(EVENT_MAX_STACK, new SimpleOption<>(EVENT_MAX_STACK, -1));
        optionsMap.put(EVENT_START_EDITABLE, new SimpleOption<>(EVENT_START_EDITABLE, false));
        optionsMap.put(EVENT_DURATION_EDITABLE, new SimpleOption<>(EVENT_DURATION_EDITABLE, false));
        optionsMap.put(EVENT_RESIZABLE_FROM_START, new SimpleOption<>(EVENT_RESIZABLE_FROM_START, false));
        optionsMap.put(EVENT_DRAG_MIN_DISTANCE, new SimpleOption<>(EVENT_DRAG_MIN_DISTANCE, 5));
        optionsMap.put(EVENT_OVERLAP, new EventOverlap());
        optionsMap.put(EXPAND_ROWS, new SimpleOption<>(EXPAND_ROWS, false));
        optionsMap.put(EVENT_INTERACTIVE, new SimpleOption<>(EVENT_INTERACTIVE, false));
        optionsMap.put(EVENT_BACKGROUND_COLOR, new SimpleOption<>(EVENT_BACKGROUND_COLOR));
        optionsMap.put(EVENT_BORDER_COLOR, new SimpleOption<>(EVENT_BORDER_COLOR));
        optionsMap.put(EVENT_TEXT_COLOR, new SimpleOption<>(EVENT_TEXT_COLOR));
        optionsMap.put(EVENT_ORDER_STRICT, new SimpleOption<>(EVENT_ORDER_STRICT, false));
        optionsMap.put(EVENT_ORDER, new EventOrder());
        optionsMap.put(FORCE_EVENT_DURATION, new SimpleOption<>(FORCE_EVENT_DURATION, false));
        optionsMap.put(EVENT_LONG_PRESS_DELAY, new SimpleOption<>(EVENT_LONG_PRESS_DELAY, 1000));
        optionsMap.put(MORE_LINK_CLASS_NAMES, new MoreLinkClassNames());
        optionsMap.put(MORE_LINK_CLICK, new MoreLinkClick());
        optionsMap.put(NAV_LINKS, new SimpleOption<>(NAV_LINKS, false));
        optionsMap.put(NEXT_DAY_THRESHOLD, new SimpleOption<>(NEXT_DAY_THRESHOLD, CalendarDuration.ofHours(0)));
        optionsMap.put(NOW_INDICATOR_CLASS_NAMES, new SimpleOption<>(NOW_INDICATOR_CLASS_NAMES, false));
        optionsMap.put(PROGRESSIVE_EVENT_RENDERING, new SimpleOption<>(PROGRESSIVE_EVENT_RENDERING, false));
        optionsMap.put(SELECTABLE, new SimpleOption<>(SELECTABLE, false));
        optionsMap.put(SELECT_ALLOW, new SimpleOption<>(SELECT_ALLOW));
        optionsMap.put(SELECT_LONG_PRESS_DELAY, new SimpleOption<>(SELECT_LONG_PRESS_DELAY, 1000));
        optionsMap.put(SELECT_MIRROR, new SimpleOption<>(SELECT_MIRROR, false));
        optionsMap.put(SELECT_OVERLAP, new SelectOverlap());
        optionsMap.put(SLOT_DURATION, new SimpleOption<>(SLOT_DURATION, CalendarDuration.ofMinutes(30)));
        optionsMap.put(SLOT_LABEL_INTERVAL, new SimpleOption<>(SLOT_LABEL_INTERVAL));
        optionsMap.put(SLOT_MIN_TIME, new SimpleOption<>(SLOT_MIN_TIME, CalendarDuration.ofHours(0)));
        optionsMap.put(SLOT_MAX_TIME, new SimpleOption<>(SLOT_MAX_TIME, CalendarDuration.ofHours(24)));
        optionsMap.put(SLOT_LABEL_CLASS_NAMES, new SimpleOption<>(SLOT_LABEL_CLASS_NAMES, false));
        optionsMap.put(SNAP_DURATION, new SimpleOption<>(SNAP_DURATION, CalendarDuration.ofMinutes(30)));
        optionsMap.put(TIME_ZONE, new SimpleOption<>(TIME_ZONE, TimeZone.getDefault()));
        optionsMap.put(VALID_RANGE, new ValidRange());
        optionsMap.put(VISIBLE_RANGE, new VisibleRange());
        optionsMap.put(WEEK_NUMBERS, new SimpleOption<>(WEEK_NUMBERS, false));
        optionsMap.put(WEEKENDS, new SimpleOption<>(WEEKENDS, true));
        optionsMap.put(WINDOW_RESIZE_DELAY, new SimpleOption<>(WINDOW_RESIZE_DELAY, 100));
    }

    protected Consumer<OptionChangeEvent> optionChangeListener;

    public JmixFullCalendarOptions() {
        addAdditionalOptions();

        optionsMap.values().forEach(o -> o.addChangeListener(this::onOptionChange));
        initialOptionsMap.values().forEach(o -> o.addChangeListener(this::onOptionChange));
    }

    public Collection<CalendarOption> getUpdatableOptions() {
        return optionsMap.values();
    }

    public Collection<CalendarOption> getInitialOptions() {
        return initialOptionsMap.values();
    }

    public List<CalendarOption> getDirtyOptions() {
        return optionsMap.values().stream()
                .filter(CalendarOption::isDirty)
                .toList();
    }

    public void unmarkAllAsDirty() {
        optionsMap.values().forEach(CalendarOption::unmarkAsDirty);
    }

    public SimpleOption<Boolean> getWeekNumbers() {
        return get(WEEK_NUMBERS);
    }

    public ValidRange getValidRange() {
        return get(VALID_RANGE);
    }

    public SimpleOption<TimeZone> getTimeZone() {
        return get(TIME_ZONE);
    }

    public SimpleOption<CalendarDisplayMode> getInitialDisplayMode() {
        return getInitial(INITIAL_VIEW);
    }

    public SimpleOption<Boolean> getNavLinks() {
        return get(NAV_LINKS);
    }

    public DayMaxEventRows getDayMaxEventRows() {
        return get(DAY_MAX_EVENT_ROWS);
    }

    public SimpleOption<Integer> getEventMaxStack() {
        return get(EVENT_MAX_STACK);
    }

    public DayMaxEvents getDayMaxEvents() {
        return get(DAY_MAX_EVENTS);
    }

    public MoreLinkClick getMoreLinkClick() {
        return get(MORE_LINK_CLICK);
    }

    public MoreLinkClassNames getMoreLinkClassNames() {
        return get(MORE_LINK_CLASS_NAMES);
    }

    public SimpleOption<Boolean> getEventStartEditable() {
        return get(EVENT_START_EDITABLE);
    }

    public SimpleOption<Boolean> getEventDurationEditable() {
        return get(EVENT_DURATION_EDITABLE);
    }

    public SimpleOption<Boolean> getEventResizableFromStart() {
        return get(EVENT_RESIZABLE_FROM_START);
    }

    public SimpleOption<Integer> getEventDragMinDistance() {
        return get(EVENT_DRAG_MIN_DISTANCE);
    }

    public EventOverlap getEventOverlap() {
        return get(EVENT_OVERLAP);
    }

    public SimpleOption<Integer> getDragRevertDuration() {
        return get(DRAG_REVERT_DURATION);
    }

    public SimpleOption<Boolean> getDragScroll() {
        return getInitial(DRAG_SCROLL);
    }

    public SimpleOption<CalendarDuration> getSnapDuration() {
        return get(SNAP_DURATION);
    }

    public SimpleOption<Boolean> getAllDayMaintainDuration() {
        return get(ALL_DAY_MAINTAIN_DURATION);
    }

    public SimpleOption<Boolean> getSelectable() {
        return get(SELECTABLE);
    }

    public SimpleOption<Boolean> getSelectMirror() {
        return get(SELECT_MIRROR);
    }

    public SimpleOption<Boolean> getUnselectAuto() {
        return getInitial(UNSELECT_AUTO);
    }

    public SimpleOption<String> getUnselectCancel() {
        return getInitial(UNSELECT_CANCEL);
    }

    public SelectOverlap getSelectOverlap() {
        return get(SELECT_OVERLAP);
    }

    public SimpleOption<JsFunction> getSelectAllow() {
        return get(SELECT_ALLOW);
    }

    public SimpleOption<Integer> getSelectMinDistance() {
        return getInitial(SELECT_MIN_DISTANCE);
    }

    public DisplayModes getDisplayModes() {
        return getInitial(VIEWS);
    }

    public VisibleRange getVisibleRange() {
        return get(VISIBLE_RANGE);
    }

    public SimpleOption<String> getDayPopoverFormat() {
        return getInitial(DAY_POPOVER_FORMAT);
    }

    public SimpleOption<String> getDayHeaderFormat() {
        return getInitial(DAY_HEADER_FORMAT);
    }

    public SimpleOption<String> getWeekNumberFormat() {
        return getInitial(WEEK_NUMBER_FORMAT);
    }

    public SimpleOption<String> getSlotLabelFormat() {
        return getInitial(SLOT_LABEL_FORMAT);
    }

    public SimpleOption<String> getEventTimeFormat() {
        return getInitial(EVENT_TIME_FORMAT);
    }

    public SimpleOption<Boolean> getWeekends() {
        return get(WEEKENDS);
    }

    public SimpleOption<Boolean> getDayHeaders() {
        return get(DAY_HEADERS);
    }

    public SimpleOption<Boolean> getDayHeaderClassNames() {
        return get(DAY_HEADER_CLASS_NAMES);
    }

    public SimpleOption<Boolean> getDayCellClassNames() {
        return get(DAY_CELL_CLASS_NAMES);
    }

    public SimpleOption<CalendarDuration> getSlotDuration() {
        return get(SLOT_DURATION);
    }

    public SimpleOption<CalendarDuration> getSlotLabelInterval() {
        return get(SLOT_LABEL_INTERVAL);
    }

    public SimpleOption<CalendarDuration> getSlotMinTime() {
        return get(SLOT_MIN_TIME);
    }

    public SimpleOption<CalendarDuration> getSlotMaxTime() {
        return get(SLOT_MAX_TIME);
    }

    public SimpleOption<CalendarDuration> getScrollTime() {
        return getInitial(SCROLL_TIME);
    }

    public SimpleOption<Boolean> getScrollTimeReset() {
        return getInitial(SCROLL_TIME_RESET);
    }

    public SimpleOption<Boolean> getSlotLabelClassNames() {
        return get(SLOT_LABEL_CLASS_NAMES);
    }

    public SimpleOption<Boolean> getDefaultAllDay() {
        return get(DEFAULT_ALL_DAY);
    }

    public SimpleOption<CalendarDuration> getDefaultAllDayEventDuration() {
        return get(DEFAULT_ALL_DAY_EVENT_DURATION);
    }

    public SimpleOption<CalendarDuration> getDefaultTimedEventDuration() {
        return get(DEFAULT_TIMED_EVENT_DURATION);
    }

    public SimpleOption<Boolean> getForceEventDuration() {
        return get(FORCE_EVENT_DURATION);
    }

    public SimpleOption<LocalDate> getInitialDate() {
        return getInitial(INITIAL_DATE);
    }

    public SimpleOption<CalendarDuration> getDateIncrement() {
        return get(DATE_INCREMENT);
    }

    public SimpleOption<String> getDateAlignment() {
        return get(DATE_ALIGNMENT);
    }

    public SimpleOption<Boolean> getExpandRows() {
        return get(EXPAND_ROWS);
    }

    public SimpleOption<Integer> getWindowResizeDelay() {
        return get(WINDOW_RESIZE_DELAY);
    }

    public SimpleOption<Boolean> getEventInteractive() {
        return get(EVENT_INTERACTIVE);
    }

    public SimpleOption<Integer> getEventLongPressDelay() {
        return get(EVENT_LONG_PRESS_DELAY);
    }

    public SimpleOption<Integer> getSelectLongPressDelay() {
        return get(SELECT_LONG_PRESS_DELAY);
    }

    public SimpleOption<Boolean> getNowIndicator() {
        return getInitial(NOW_INDICATOR);
    }

    public SimpleOption<Boolean> getNowIndicatorClassNames() {
        return get(NOW_INDICATOR_CLASS_NAMES);
    }

    public SimpleOption<String> getEventBackgroundColor() {
        return get(EVENT_BACKGROUND_COLOR);
    }

    public SimpleOption<String> getEventBorderColor() {
        return get(EVENT_BORDER_COLOR);
    }

    public SimpleOption<String> getEventTextColor() {
        return get(EVENT_TEXT_COLOR);
    }

    public SimpleOption<Boolean> getDisplayEventTime() {
        return get(DISPLAY_EVENT_TIME);
    }

    public SimpleOption<CalendarDuration> getNextDayThreshold() {
        return get(NEXT_DAY_THRESHOLD);
    }

    public SimpleOption<Boolean> getEventOrderStrict() {
        return get(EVENT_ORDER_STRICT);
    }

    public SimpleOption<Boolean> getProgressiveEventRendering() {
        return get(PROGRESSIVE_EVENT_RENDERING);
    }

    public EventOrder getEventOrder() {
        return get(EVENT_ORDER);
    }

    public boolean isInitial(CalendarOption option) {
        return initialOptionsMap.containsKey(option.getName());
    }

    public void setOptionChangeListener(@Nullable Consumer<OptionChangeEvent> listener) {
        this.optionChangeListener = listener;
    }

    @SuppressWarnings("unchecked")
    protected <T extends CalendarOption> T getInitial(String name) {
        return (T) initialOptionsMap.get(name);
    }

    @SuppressWarnings("unchecked")
    protected <T extends CalendarOption> T get(String name) {
        return (T) optionsMap.get(name);
    }

    protected void addAdditionalOptions() {
        // Used in inheritors
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
