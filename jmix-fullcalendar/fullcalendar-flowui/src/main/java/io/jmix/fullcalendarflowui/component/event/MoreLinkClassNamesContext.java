package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;

/**
 * The context for generating class names for "more" link.
 */
public class MoreLinkClassNamesContext extends AbstractFullCalendarContext {

    protected final Integer eventsCount;

    protected final DisplayModeInfo displayModeInfo;

    public MoreLinkClassNamesContext(FullCalendar fullCalendar,
                                     Integer eventsCount,
                                     DisplayModeInfo displayModeInfo) {
        super(fullCalendar);
        this.eventsCount = eventsCount;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * @return count of hidden events
     */
    public Integer getEventsCount() {
        return eventsCount;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
