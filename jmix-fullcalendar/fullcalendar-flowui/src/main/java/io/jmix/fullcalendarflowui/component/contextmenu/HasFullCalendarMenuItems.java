package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.io.Serializable;

/**
 * Interface to be implemented by context menu that works with {@link FullCalendar}.
 */
public interface HasFullCalendarMenuItems extends Serializable {

    /**
     * Adds new menu item.
     *
     * @param text          item's text
     * @param clickListener click listener
     * @return added menu item
     */
    FullCalendarMenuItem addItem(String text,
                                 ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener);

    /**
     * Adds new menu item.
     *
     * @param component     component that should be used as a content of menu item
     * @param clickListener click listener
     * @return added menu item
     */
    FullCalendarMenuItem addItem(Component component,
                                 ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener);

    /**
     * Event is fired when menu item in context menu is clicked.
     */
    class FullCalendarClickContextMenuItemEvent extends ComponentEvent<FullCalendarMenuItem> {

        public FullCalendarClickContextMenuItemEvent(FullCalendarMenuItem source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
