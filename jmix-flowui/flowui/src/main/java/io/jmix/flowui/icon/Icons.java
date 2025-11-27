package io.jmix.flowui.icon;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.icon.IconFactory;

/**
 * Central interface to provide UI components representing icons.
 */
public interface Icons {

    /**
     * Returns a UI {@link Component} representing an icon specified by the name
     * of the passed {@link IconFactory}.
     * <p>
     * This method attempts to create a {@link Component} for the given icon name.
     * If the icon name contains ':' delimiter then a new {@link Icon} is created
     * using icon collection and icon name values. Otherwise, it will
     * attempt to locate and create the icon using predefined icon sets.
     *
     * @param icon the {@link IconFactory} instance to get the icon name from
     * @return the {@link Component} representing the icon
     * @throws IllegalArgumentException if the icon name is {@code null} or cannot be
     *                                  resolved as a valid icon component
     */
    Component get(IconFactory<?> icon);

    /**
     * Returns a UI {@link Component} representing an icon specified by its name.
     * <p>
     * This method attempts to create a {@link Component} for the given icon name.
     * If the icon name contains ':' delimiter then a new {@link Icon} is created
     * using icon collection and icon name values. Otherwise, it will
     * attempt to locate and create the icon using predefined icon sets.
     *
     * @param iconName the name of the icon
     * @return the {@link Component} representing the icon
     * @throws IllegalArgumentException if the icon name is {@code null} or cannot be
     *                                  resolved as a valid icon component
     */
    Component get(String iconName);
}
