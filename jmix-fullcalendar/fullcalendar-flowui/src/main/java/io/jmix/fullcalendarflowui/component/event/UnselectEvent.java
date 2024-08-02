package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.springframework.lang.Nullable;

public class UnselectEvent extends ComponentEvent<FullCalendar> {

    protected final ViewInfo viewInfo;
    protected final MouseEventDetails mouseEventDetails;

    public UnselectEvent(FullCalendar source, boolean fromClient,
                         ViewInfo viewInfo,
                         @Nullable MouseEventDetails mouseEventDetails) {
        super(source, fromClient);

        this.viewInfo = viewInfo;
        this.mouseEventDetails = mouseEventDetails;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    @Nullable
    public MouseEventDetails getMouseEventDetails() {
        return mouseEventDetails;
    }
}
