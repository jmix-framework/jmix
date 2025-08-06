package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.Component;

public interface ComponentUserMenuItem extends UserMenuItem, UserMenuItem.HasClickListener<ComponentUserMenuItem> {

    /**
     * Returns the content component associated with this menu item.
     *
     * @return the {@link Component} currently set as the content
     */
    Component getContent();

    /**
     * Sets the content component for the menu item.
     *
     * @param content the {@link Component} to be set as the content
     */
    void setContent(Component content);
}
