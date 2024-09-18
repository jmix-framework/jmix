package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.FullCalendar;

/**
 * The sub menu of context menu in {@link FullCalendar}.
 */
public class FullCalendarSubMenu extends SubMenuBase<FullCalendarContextMenu, FullCalendarMenuItem,
        FullCalendarSubMenu> {

    protected final SerializableRunnable contentReset;

    public FullCalendarSubMenu(FullCalendarMenuItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem);

        Preconditions.checkNotNullArgument(contentReset);

        this.contentReset = contentReset;
    }

    @Override
    protected MenuManager<FullCalendarContextMenu, FullCalendarMenuItem, FullCalendarSubMenu> createMenuManager() {
        return new MenuManager<>(getParentMenuItem().getContextMenu(), contentReset, FullCalendarMenuItem::new,
                FullCalendarMenuItem.class, getParentMenuItem());
    }
}
