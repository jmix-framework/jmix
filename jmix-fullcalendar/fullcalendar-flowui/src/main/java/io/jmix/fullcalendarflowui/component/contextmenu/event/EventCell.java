package io.jmix.fullcalendarflowui.component.contextmenu.event;

import io.jmix.fullcalendarflowui.kit.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEventProvider;

public class EventCell {

    protected boolean isFuture;
    protected boolean isMirror;
    protected boolean isPast;
    protected boolean isToday;
    protected CalendarEvent event;
    protected CalendarEventProvider eventProvider;

    public EventCell(boolean isFuture, boolean isMirror, boolean isPast, boolean isToday, CalendarEvent event,
                     CalendarEventProvider eventProvider) {
        this.isFuture = isFuture;
        this.isMirror = isMirror;
        this.isPast = isPast;
        this.isToday = isToday;
        this.event = event;
        this.eventProvider = eventProvider;
    }

    public boolean isFuture() {
        return isFuture;
    }

    public boolean isMirror() {
        return isMirror;
    }

    public boolean isPast() {
        return isPast;
    }

    public boolean isToday() {
        return isToday;
    }

    public CalendarEvent getEvent() {
        return event;
    }

    public CalendarEventProvider getEventProvider() {
        return eventProvider;
    }
}
