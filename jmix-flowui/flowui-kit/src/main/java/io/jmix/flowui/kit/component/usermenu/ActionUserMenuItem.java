package io.jmix.flowui.kit.component.usermenu;

import io.jmix.flowui.kit.action.Action;

/**
 * Represents a specific type of {@link UserMenuItem} that is associated with an {@link Action}.
 */
public interface ActionUserMenuItem extends UserMenuItem {

    /**
     * Returns the {@link Action} associated with this item.
     *
     * @return the associated action
     */
    Action getAction();
}
