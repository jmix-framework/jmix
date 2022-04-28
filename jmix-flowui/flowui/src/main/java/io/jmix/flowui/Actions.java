package io.jmix.flowui;

import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.action.Action;

/**
 * Factory to create actions declared as {@link ActionType}.
 *
 * @see Action
 */
public interface Actions {

    Action create(String actionTypeId);

    Action create(String actionTypeId, String id);

    <T extends Action> T create(Class<T> actionTypeClass);

    <T extends Action> T create(Class<T> actionTypeClass, String id);
}
