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
import io.jmix.fullcalendarflowui.component.serialization.deserializer.FullCalendarDeserializer;
import org.springframework.lang.Nullable;

import java.util.function.Function;

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

    public static FullCalendarContextMenu create(FullCalendar target) {
        return new FullCalendarContextMenu(target);
    }

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
    public void setTarget(Component target) {
        if (target != null && !(target instanceof FullCalendar)) {
            throw new IllegalArgumentException(
                    "Only an instance of FullCalendar can be used as the target for FullCalendarContextMenu. "
                            + "Use ContextMenu for any other component.");
        }
        super.setTarget(target);
    }

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
