/*
 * Copyright 2023 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.action;

import io.jmix.ui.component.Component;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for screen actions.
 *
 * @param <A> type of action
 * @param <S> type of screen
 */
public abstract class AbstractScreenAction<A extends AbstractScreenAction<A, S>, S extends Screen>
        extends BaseAction
        implements Action.ScreenAction<S>, Action.ExecutableAction {

    protected S target;

    public AbstractScreenAction(String id) {
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

    @SuppressWarnings("unchecked")
    public A withTarget(@Nullable S target) {
        setTarget(target);
        return (A) this;
    }

    /**
     * Set caption using fluent API method.
     *
     * @param caption caption
     * @return current instance of action
     */
    @SuppressWarnings("unchecked")
    public A withCaption(@Nullable String caption) {
        return (A) super.withCaption(caption);
    }

    /**
     * Set description using fluent API method.
     *
     * @param description description
     * @return current instance of action
     */
    @SuppressWarnings("unchecked")
    public A withDescription(@Nullable String description) {
        return (A) super.withDescription(description);
    }

    @SuppressWarnings("unchecked")
    public A withEnabled(boolean enabled) {
        setEnabled(enabled);
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public A withVisible(boolean visible) {
        setVisible(visible);
        return (A) this;
    }

    /**
     * Set icon using fluent API method.
     *
     * @param icon icon
     * @return current instance of action
     */
    @SuppressWarnings("unchecked")
    public A withIcon(@Nullable String icon) {
        return (A) super.withIcon(icon);
    }

    /**
     * Set shortcut using fluent API method.
     *
     * @param shortcut shortcut
     * @return current instance of action
     */
    @SuppressWarnings("unchecked")
    public A withShortcut(@Nullable String shortcut) {
        return (A) super.withShortcut(shortcut);
    }

    /**
     * Set action performed event handler using fluent API method. Can be used instead of subclassing BaseAction class.
     *
     * @param handler action performed handler
     * @return current instance of action
     */
    @SuppressWarnings("unchecked")
    public A withHandler(Consumer<ActionPerformedEvent> handler) {
        return (A) super.withHandler(handler);
    }

    /**
     * Set whether this action is primary using fluent API method. Can be used instead of subclassing BaseAction class.
     *
     * @param primary primary
     * @return current instance of action
     */
    @SuppressWarnings("unchecked")
    public A withPrimary(boolean primary) {
        return (A) super.withPrimary(primary);
    }

    @SuppressWarnings("unchecked")
    public A withEnabledByUiPermissions(boolean enabledByUiPermissions) {
        setEnabledByUiPermissions(enabledByUiPermissions);
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public A withVisibleByUiPermissions(boolean visibleByUiPermissions) {
        setVisibleByUiPermissions(visibleByUiPermissions);
        return (A) this;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
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
