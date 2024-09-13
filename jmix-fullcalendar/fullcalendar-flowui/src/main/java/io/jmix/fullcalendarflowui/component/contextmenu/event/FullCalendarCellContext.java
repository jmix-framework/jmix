package io.jmix.fullcalendarflowui.component.contextmenu.event;

import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarViewType;
import org.springframework.lang.Nullable;

/**
 * The context of cell from which context menu is opened.
 */
public class FullCalendarCellContext {

    protected final DayCell dayCell;

    protected final CalendarEvent calendarEvent;

    protected final CalendarDataProvider dataProvider;

    protected final MouseEventDetails mouseDetails;

    public FullCalendarCellContext(@Nullable DayCell dayCell,
                                   @Nullable CalendarEvent calendarEvent,
                                   @Nullable CalendarDataProvider dataProvider,
                                   MouseEventDetails mouseDetails) {
        this.dayCell = dayCell;
        this.calendarEvent = calendarEvent;
        this.dataProvider = dataProvider;
        this.mouseDetails = mouseDetails;
    }

    /**
     * @return day cell information or {@code null} if component's view is Time Grid:
     * {@link CalendarViewType#TIME_GRID_DAY} or {@link CalendarViewType#TIME_GRID_WEEK}.
     */
    @Nullable
    public DayCell getDayCell() {
        return dayCell;
    }

    /**
     * @return calendar event if context menu is invoked from an event
     */
    @Nullable
    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    /**
     * @return calendar event's data provider if context menu is invoked from an event
     */
    @Nullable
    public CalendarDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return information about click
     */
    public MouseEventDetails getMouseDetails() {
        return mouseDetails;
    }
}
