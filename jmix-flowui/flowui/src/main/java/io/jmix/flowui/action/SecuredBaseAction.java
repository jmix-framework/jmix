package io.jmix.flowui.action;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SecuredBaseAction extends BaseAction implements SecuredAction {

    protected boolean enabledByUiPermissions = true;
    protected boolean visibleByUiPermissions = true;

    public SecuredBaseAction(String id) {
        super(id);
    }

    @Override
    public void refreshState() {
        setVisibleInternal(visibleExplicitly && isVisibleByUiPermissions());

        setEnabledInternal(enabledExplicitly && isEnabledByUiPermissions() && isVisibleByUiPermissions()
                && isPermitted() && isApplicable());
    }

    @Override
    public boolean isEnabledByUiPermissions() {
        return enabledByUiPermissions;
    }

    @Override
    public void setEnabledByUiPermissions(boolean enabledByUiPermissions) {
        if (this.enabledByUiPermissions != enabledByUiPermissions) {
            this.enabledByUiPermissions = enabledByUiPermissions;

            refreshState();
        }
    }

    @Override
    public boolean isVisibleByUiPermissions() {
        return visibleByUiPermissions;
    }

    @Override
    public void setVisibleByUiPermissions(boolean visibleByUiPermissions) {
        if (this.visibleByUiPermissions != visibleByUiPermissions) {
            this.visibleByUiPermissions = visibleByUiPermissions;

            refreshState();
        }
    }

    protected boolean isPermitted() {
        return true;
    }

    @Override
    public SecuredBaseAction withText(@Nullable String text) {
        setText(text);
        return this;
    }

    @Override
    public SecuredBaseAction withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    @Override
    public SecuredBaseAction withVisible(boolean visible) {
        setVisible(visible);
        return this;
    }

    @Override
    public SecuredBaseAction withIcon(@Nullable String icon) {
        setIcon(icon);
        return this;
    }

    @Override
    public SecuredBaseAction withIcon(@Nullable VaadinIcon icon) {
        setIcon(FlowUiComponentUtils.iconToSting(icon));
        return this;
    }

    @Override
    public SecuredBaseAction withTitle(@Nullable String title) {
        setTitle(title);
        return this;
    }

    @Override
    public SecuredBaseAction withVariant(ActionVariant actionVariant) {
        setVariant(actionVariant);
        return this;
    }

    @Override
    public SecuredBaseAction withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        setShortcutCombination(shortcutCombination);
        return this;
    }

    @Override
    public SecuredBaseAction withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        if (handler == null) {
            if (getEventBus().hasListener(ActionPerformedEvent.class)) {
                getEventBus().removeListener(ActionPerformedEvent.class);
            }
        } else {
            addActionPerformedListener(handler);
        }

        return this;
    }

    public SecuredBaseAction withEnabledByUiPermissions(boolean enabledByUiPermissions) {
        setEnabledByUiPermissions(enabledByUiPermissions);
        return this;
    }

    public SecuredBaseAction withVisibleByUiPermissions(boolean visibleByUiPermissions) {
        setVisibleByUiPermissions(visibleByUiPermissions);
        return this;
    }
}
