package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public class SelectEvent extends ComponentEvent<FullCalendar> {

    protected final LocalDateTime startDateTime;
    protected final LocalDateTime endDateTime;

    protected final boolean allDay;

    protected final MouseEventDetails mouseEventDetails;

    protected final ViewInfo viewInfo;

    public SelectEvent(FullCalendar source,
                       boolean fromClient,
                       @Nullable MouseEventDetails mouseEventDetails,
                       LocalDateTime startDateTime,
                       LocalDateTime endDateTime,
                       boolean allDay,
                       ViewInfo viewInfo) {
        super(source, fromClient);

        this.mouseEventDetails = mouseEventDetails;

        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.allDay = allDay;
        this.viewInfo = viewInfo;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    @Nullable
    public MouseEventDetails getMouseEventDetails() {
        return mouseEventDetails;
    }
}
