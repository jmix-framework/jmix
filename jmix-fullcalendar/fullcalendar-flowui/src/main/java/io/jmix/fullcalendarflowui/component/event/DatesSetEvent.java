package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.time.LocalDateTime;

public class DatesSetEvent extends ComponentEvent<FullCalendar> {

    protected final LocalDateTime startDateTime;
    protected final LocalDateTime endDateTime;

    protected final ViewInfo viewInfo;

    public DatesSetEvent(FullCalendar source, boolean fromClient,
                         LocalDateTime startDateTime,
                         LocalDateTime endDateTime,
                         ViewInfo viewInfo) {
        super(source, fromClient);

        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;

        this.viewInfo = viewInfo;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
