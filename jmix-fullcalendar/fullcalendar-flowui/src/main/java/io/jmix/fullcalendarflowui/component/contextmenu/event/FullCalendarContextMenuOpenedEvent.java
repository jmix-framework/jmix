package io.jmix.fullcalendarflowui.component.contextmenu.event;

import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import io.jmix.fullcalendarflowui.component.contextmenu.FullCalendarContextMenu;

/**
 * The event is fired when a context menu has been opened or closed.
 */
public class FullCalendarContextMenuOpenedEvent extends ContextMenuBase.OpenedChangeEvent<FullCalendarContextMenu> {

    protected final FullCalendarCellContext cellContext;

    public FullCalendarContextMenuOpenedEvent(FullCalendarContextMenu source,
                                              boolean fromClient,
                                              FullCalendarCellContext cellContext) {
        super(source, fromClient);
        this.cellContext = cellContext;
    }

    @Override
    public FullCalendarContextMenu getSource() {
        return super.getSource();
    }

    /**
     * @return a cell context that contains information about the cell and its associated event
     */
    public FullCalendarCellContext getCellContext() {
        return cellContext;
    }
}
