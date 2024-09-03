package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;

/**
 * The context for generating class names for "more" link.
 */
public class MoreLinkClassNamesContext extends AbstractFullCalendarContext {

    protected final Integer eventsCount;

    protected final ViewInfo viewInfo;

    public MoreLinkClassNamesContext(FullCalendar fullCalendar,
                                     Integer eventsCount,
                                     ViewInfo viewInfo) {
        super(fullCalendar);
        this.eventsCount = eventsCount;
        this.viewInfo = viewInfo;
    }

    /**
     * @return count of hidden events
     */
    public Integer getEventsCount() {
        return eventsCount;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
