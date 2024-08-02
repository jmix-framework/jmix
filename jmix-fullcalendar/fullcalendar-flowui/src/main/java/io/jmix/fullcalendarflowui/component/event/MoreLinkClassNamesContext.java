package io.jmix.fullcalendarflowui.component.event;

public class MoreLinkClassNamesContext {

    protected final Integer eventsCount;
    protected final String shortText;
    protected final String text;
    protected final ViewInfo viewInfo;

    public MoreLinkClassNamesContext(Integer eventsCount, String shortText, String text, ViewInfo viewInfo) {
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
