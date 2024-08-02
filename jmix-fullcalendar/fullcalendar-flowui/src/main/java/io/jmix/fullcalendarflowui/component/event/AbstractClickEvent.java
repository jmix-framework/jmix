package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

public class AbstractClickEvent extends ComponentEvent<FullCalendar> {

    protected final MouseEventDetails mouseEventDetails;

    public AbstractClickEvent(FullCalendar source,
                              boolean fromClient,
                              MouseEventDetails mouseEventDetails) {
        super(source, fromClient);

        this.mouseEventDetails = mouseEventDetails;
    }

    public MouseEventDetails getMouseEventDetails() {
        return mouseEventDetails;
    }
}
