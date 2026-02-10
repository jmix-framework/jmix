package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.contextmenu.event.FullCalendarCellContext;
import io.jmix.fullcalendarflowui.component.contextmenu.event.FullCalendarContextMenuOpenedEvent;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarDeserializer;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Component for context menu in {@link FullCalendar}.
 */
public class FullCalendarContextMenu extends ContextMenuBase<FullCalendarContextMenu, FullCalendarMenuItem,
        FullCalendarSubMenu> implements HasFullCalendarMenuItems {

    protected FullCalendarCellContext cellContext;

    protected Function<FullCalendarCellContext, Boolean> contentMenuHandler;

    protected FullCalendarDeserializer deserializer = new FullCalendarDeserializer();

    public FullCalendarContextMenu() {
    }

    public FullCalendarContextMenu(FullCalendar target) {
        setTarget(target);
    }

    /**
     * Creates new instance of context menu with provided {@link FullCalendar} target.
     *
     * @param target calendar to bound with a context menu
     * @return new instance of context menu
     */
    public static FullCalendarContextMenu create(FullCalendar target) {
        return new FullCalendarContextMenu(target);
    }

    /**
     * Adds a listener to handle changing opened/closed state.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener from context menu
     */
    public Registration addContextMenuOpenedListener(ComponentEventListener<FullCalendarContextMenuOpenedEvent> listener) {
        ComponentEventListener<OpenedChangeEvent<FullCalendarContextMenu>> baseListener = (e) -> {
            listener.onComponentEvent(
                    new FullCalendarContextMenuOpenedEvent(e.getSource(), e.isFromClient(), cellContext));
        };
        return super.addOpenedChangeListener(baseListener);
    }

    @Nullable
    public Function<FullCalendarCellContext, Boolean> getContentMenuHandler() {
        return contentMenuHandler;
    }

    /**
     * Sets a handler to configure content of context menu. For instance:
     * <pre>{@code
     * contextMenu.setContentMenuHandler(context -> {
     *     contextMenu.removeAll();
     *     if (context.getCalendarEvent() != null) {
     *         contextMenu.addItem("Event menu item", event -> {}); // do something
     *         return true;
     *     } else if (context.getDayCell() != null) {
     *         if (context.getDayCell().isDisabled()) {
     *             return false;
     *         } else {
     *             contextMenu.addItem("Simple day cell menu item", event -> {}); // do something
     *             return true;
     *         }
     *     } else {
     *         return false;
     *     }
     * });
     * }</pre>
     *
     * @param contentMenuHandler handler to add
     */
    public void setContentMenuHandler(@Nullable Function<FullCalendarCellContext, Boolean> contentMenuHandler) {
        this.contentMenuHandler = contentMenuHandler;
    }

    @Override
    public FullCalendarMenuItem addItem(String text,
                                        ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener) {
        FullCalendarMenuItem menuItem = getMenuManager().addItem(text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    public FullCalendarMenuItem addItem(Component component,
                                        ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener) {
        FullCalendarMenuItem menuItem = getMenuManager().addItem(component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    public void setTarget(@Nullable Component target) {
        if (target != null && !(target instanceof FullCalendar)) {
            throw new IllegalArgumentException(
                    "Only an instance of FullCalendar can be used as the target for FullCalendarContextMenu. "
                            + "Use ContextMenu for any other component.");
        }
        super.setTarget(target);
    }

    @Nullable
    @Override
    public FullCalendar getTarget() {
        return (FullCalendar) super.getTarget();
    }

    @Override
    protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
        cellContext = deserializer.deserializeCalendarCellContext(eventDetail, getTarget());

        if (contentMenuHandler != null) {
            return contentMenuHandler.apply(cellContext);
        }

        return super.onBeforeOpenMenu(eventDetail);
    }

    @Override
    protected MenuManager<FullCalendarContextMenu, FullCalendarMenuItem, FullCalendarSubMenu> createMenuManager(
            SerializableRunnable contentReset) {
        return new MenuManager<>(this, contentReset, FullCalendarMenuItem::new,
                FullCalendarMenuItem.class, null);
    }
}
