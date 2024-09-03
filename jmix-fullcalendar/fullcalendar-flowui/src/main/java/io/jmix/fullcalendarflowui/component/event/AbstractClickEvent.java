package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

/**
 * Base class for events that contains information about mouse click.
 */
public class AbstractClickEvent extends ComponentEvent<FullCalendar> {

    protected final MouseEventDetails mouseEventDetails;

    public AbstractClickEvent(FullCalendar fullCalendar,
                              boolean fromClient,
                              MouseEventDetails mouseEventDetails) {
        super(fullCalendar, fromClient);

        this.mouseEventDetails = mouseEventDetails;
    }

    /**
     * @return information about mouse click
     */
    public MouseEventDetails getMouseEventDetails() {
        return mouseEventDetails;
    }
}
