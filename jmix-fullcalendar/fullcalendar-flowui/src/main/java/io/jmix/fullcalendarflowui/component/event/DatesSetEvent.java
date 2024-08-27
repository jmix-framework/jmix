package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.time.LocalDate;

public class DatesSetEvent extends ComponentEvent<FullCalendar> {

    protected final LocalDate startDate;
    protected final LocalDate endDate;

    protected final ViewInfo viewInfo;

    public DatesSetEvent(FullCalendar fullCalendar, boolean fromClient,
                         LocalDate startDate,
                         LocalDate endDate,
                         ViewInfo viewInfo) {
        super(fullCalendar, fromClient);

        this.startDate = startDate;
        this.endDate = endDate;

        this.viewInfo = viewInfo;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
