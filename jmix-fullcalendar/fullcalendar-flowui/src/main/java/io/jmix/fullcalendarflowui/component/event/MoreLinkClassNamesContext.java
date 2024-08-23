package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;

public class MoreLinkClassNamesContext extends AbstractClassNamesContext {

    protected final Integer eventsCount;
    protected final String shortText;
    protected final String text;
    protected final ViewInfo viewInfo;

    public MoreLinkClassNamesContext(FullCalendar fullCalendar,
                                     Integer eventsCount,
                                     String shortText,
                                     String text,
                                     ViewInfo viewInfo) {
        super(fullCalendar);
        this.eventsCount = eventsCount;
        this.shortText = shortText;
        this.text = text;
        this.viewInfo = viewInfo;
    }

    public Integer getEventsCount() {
        return eventsCount;
    }

    public String getShortText() {
        return shortText;
    }

    public String getText() {
        return text;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
