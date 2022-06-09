package io.jmix.flowui.action;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DialogAction extends SecuredBaseAction {

    public enum Type {
        OK("ok", "actions.Ok", VaadinIcon.CHECK),
        CANCEL("cancel", "actions.Cancel", VaadinIcon.BAN),
        YES("yes", "actions.Yes", VaadinIcon.CHECK),
        NO("no", "actions.No", VaadinIcon.BAN),
        CLOSE("close", "actions.Close", VaadinIcon.CLOSE);

        private final String id;
        private final String msgKey;
        private final VaadinIcon vaadinIcon;

        Type(String id, String msgKey, VaadinIcon vaadinIcon) {
            this.id = id;
            this.msgKey = msgKey;
            this.vaadinIcon = vaadinIcon;
        }

        public String getId() {
            return id;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public VaadinIcon getVaadinIcon() {
            return vaadinIcon;
        }
    }

    protected final Type type;

    public DialogAction(Type type) {
        super(type.id);

        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public DialogAction withText(@Nullable String text) {
        setText(text);
        return this;
    }

    @Override
    public DialogAction withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    @Override
    public DialogAction withVisible(boolean visible) {
        setVisible(visible);
        return this;
    }

    @Override
    public DialogAction withIcon(@Nullable String icon) {
        setIcon(icon);
        return this;
    }

    @Override
    public DialogAction withIcon(@Nullable VaadinIcon icon) {
        setIcon(FlowUiComponentUtils.iconToSting(icon));
        return this;
    }

    @Override
    public DialogAction withTitle(@Nullable String title) {
        setDescription(title);
        return this;
    }

    @Override
    public DialogAction withVariant(ActionVariant actionVariant) {
        setVariant(actionVariant);
        return this;
    }

    @Override
    public DialogAction withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        setShortcutCombination(shortcutCombination);
        return this;
    }

    @Override
    public DialogAction withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        if (handler == null) {
            if (getEventBus().hasListener(ActionPerformedEvent.class)) {
                getEventBus().removeListener(ActionPerformedEvent.class);
            }
        } else {
            addActionPerformedListener(handler);
        }

        return this;
    }

    @Override
    public DialogAction withEnabledByUiPermissions(boolean enabledByUiPermissions) {
        setEnabledByUiPermissions(enabledByUiPermissions);
        return this;
    }

    @Override
    public DialogAction withVisibleByUiPermissions(boolean visibleByUiPermissions) {
        setVisibleByUiPermissions(visibleByUiPermissions);
        return this;
    }
}
