package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;

import java.io.Serializable;

public interface HasFullCalendarMenuItems extends Serializable {

    FullCalendarMenuItem addItem(String text,
                            ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener);

    FullCalendarMenuItem addItem(Component component,
                                 ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener);

    class FullCalendarClickContextMenuItemEvent extends ComponentEvent<FullCalendarMenuItem> {

        public FullCalendarClickContextMenuItemEvent(FullCalendarMenuItem source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
