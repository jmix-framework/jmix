package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.Component;
import jakarta.annotation.Nullable;

/**
 * Represents a user menu item that contains text and an optional icon.
 */
public interface TextUserMenuItem extends UserMenuItem, UserMenuItem.HasClickListener<TextUserMenuItem> {

    /**
     * Returns the text contained within this user menu item.
     *
     * @return the text of the menu item
     */
    String getText();

    /**
     * Sets the text contained within this user menu item.
     *
     * @param text the text to be set for the menu item
     */
    void setText(String text);

    /**
     * Retrieves the icon associated with this user menu item, if one exists.
     *
     * @return the icon component of this menu item, or {@code null} if no icon is set
     */
    @Nullable
    Component getIcon();

    /**
     * Sets the icon for this user menu item.
     *
     * @param icon the icon to set for this menu item; can be {@code null} to remove the icon
     */
    void setIcon(@Nullable Component icon);
}
