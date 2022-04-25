package io.jmix.flowui.action;

import io.jmix.flowui.kit.action.Action;

/**
 * Indicates that the action can be affected by UI permissions.
 */
public interface SecuredAction extends Action {

    boolean isEnabledByUiPermissions();

    void setEnabledByUiPermissions(boolean enabledByUiPermissions);

    boolean isVisibleByUiPermissions();

    void setVisibleByUiPermissions(boolean visibleByUiPermissions);
}
