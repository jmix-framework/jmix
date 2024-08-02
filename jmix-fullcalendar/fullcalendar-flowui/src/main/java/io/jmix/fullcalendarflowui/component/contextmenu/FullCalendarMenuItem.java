package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.contextmenu.HasFullCalendarMenuItems.FullCalendarClickContextMenuItemEvent;

public class FullCalendarMenuItem extends MenuItemBase<FullCalendarContextMenu, FullCalendarMenuItem, FullCalendarSubMenu> {

    protected final SerializableRunnable contentReset;

    public FullCalendarMenuItem(FullCalendarContextMenu contextMenu, SerializableRunnable contentReset) {
        super(contextMenu);
        Preconditions.checkNotNullArgument(contextMenu);
        Preconditions.checkNotNullArgument(contentReset);

        this.contentReset = contentReset;
    }

    public Registration addMenuItemClickListener(
            ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener) {
        return getElement().addEventListener("click", event ->
                clickListener.onComponentEvent(new FullCalendarClickContextMenuItemEvent(this, true)));
    }

    @Override
    protected FullCalendarSubMenu createSubMenu() {
        return new FullCalendarSubMenu(this, contentReset);
    }
}
