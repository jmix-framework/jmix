package io.jmix.fullcalendarflowui.component.contextmenu.event;

import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.springframework.lang.Nullable;

public class FullCalendarCellContext {

    protected final DayCell dayCell;
    protected final EventCell eventCell;
    protected final MouseEventDetails mouseDetails;

    public FullCalendarCellContext(@Nullable DayCell dayCell,
                                   @Nullable EventCell eventCell,
                                   MouseEventDetails mouseDetails) {
        this.dayCell = dayCell;
        this.eventCell = eventCell;
        this.mouseDetails = mouseDetails;
    }

    @Nullable
    public DayCell getDayCell() {
        return dayCell;
    }

    @Nullable
    public EventCell getEventCell() {
        return eventCell;
    }

    public MouseEventDetails getMouseDetails() {
        return mouseDetails;
    }
}
