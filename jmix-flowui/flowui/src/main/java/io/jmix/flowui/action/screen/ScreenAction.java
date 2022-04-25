package io.jmix.flowui.action.screen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ScreenAction<A extends ScreenAction<A, S>, S extends Screen>
        extends SecuredBaseAction
        implements TargetAction<S>, ExecutableAction {

    protected S target;

    public ScreenAction(String id) {
        super(id);

        initAction();
    }

    protected void initAction() {
        // hook to be implemented
    }

    @Nullable
    @Override
    public S getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable S target) {
        if (!Objects.equals(this.target, target)) {
            this.target = target;

            refreshState();
        }
    }

    public A withTarget(@Nullable S target) {
        setTarget(target);
        return ((A) this);
    }

    @Override
    public A withText(@Nullable String text) {
        return ((A) super.withText(text));
    }

    @Override
    public A withEnabled(boolean enabled) {
        return ((A) super.withEnabled(enabled));
    }

    @Override
    public A withVisible(boolean visible) {
        return ((A) super.withVisible(visible));
    }

    @Override
    public A withIcon(@Nullable String icon) {
        return ((A) super.withIcon(icon));
    }

    @Override
    public A withIcon(@Nullable VaadinIcon icon) {
        return ((A) super.withIcon(icon));
    }

    @Override
    public A withTitle(@Nullable String title) {
        return ((A) super.withTitle(title));
    }

    @Override
    public A withVariant(ActionVariant actionVariant) {
        return ((A) super.withVariant(actionVariant));
    }

    @Override
    public A withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        return ((A) super.withShortcutCombination(shortcutCombination));
    }

    @Override
    public A withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        return ((A) super.withHandler(handler));
    }

    @Override
    public A withEnabledByUiPermissions(boolean enabledByUiPermissions) {
        return ((A) super.withEnabledByUiPermissions(enabledByUiPermissions));
    }

    @Override
    public A withVisibleByUiPermissions(boolean visibleByUiPermissions) {
        return ((A) super.withVisibleByUiPermissions(visibleByUiPermissions));
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target != null;
    }

    protected void checkTarget() {
        if (target == null) {
            throw new IllegalStateException(String.format("%s target is not set", getClass().getSimpleName()));
        }
    }
}
