package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.contextmenu.GridSubMenu;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.flowui.component.contextmenu.JmixMenuManager;

import javax.annotation.Nullable;

public class JmixGridContextMenu<E> extends GridContextMenu<E> {

    public GridMenuItem<E> addItemAtIndex(int index, String text) {
        return getMenuManager().addItemAtIndex(index, text);
    }

    public GridMenuItem<E> addItemAtIndex(int index, String text,
                                          @Nullable ComponentEventListener<GridContextMenuItemClickEvent<E>> clickListener) {
        GridMenuItem<E> menuItem = getMenuManager().addItemAtIndex(index, text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    public GridMenuItem<E> addItemAtIndex(int index, Component component) {
        return getMenuManager().addItemAtIndex(index, component);
    }

    public GridMenuItem<E> addItemAtIndex(Component component,
                                          @Nullable ComponentEventListener<GridContextMenuItemClickEvent<E>> clickListener) {
        GridMenuItem<E> menuItem = getMenuManager().addItem(component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    protected MenuManager<GridContextMenu<E>, GridMenuItem<E>, GridSubMenu<E>> createMenuManager(SerializableRunnable contentReset) {
        SerializableBiFunction itemFactory = (menu, reset) -> new GridMenuItem<>((GridContextMenu<?>) menu,
                (SerializableRunnable) reset);
        return new JmixMenuManager(this, contentReset, itemFactory, GridMenuItem.class, null);
    }

    @Override
    protected JmixMenuManager<GridContextMenu<E>, GridMenuItem<E>, GridSubMenu<E>> getMenuManager() {
        return (JmixMenuManager<GridContextMenu<E>, GridMenuItem<E>, GridSubMenu<E>>) super.getMenuManager();
    }
}
