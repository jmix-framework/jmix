/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.action.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ViewAction<A extends ViewAction<A, V>, V extends View> extends SecuredBaseAction
        implements TargetAction<V>, ExecutableAction {

    protected V target;

    public ViewAction(String id) {
        super(id);

        initAction();
    }

    protected void initAction() {
        // hook to be implemented
    }

    @Nullable
    @Override
    public V getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable V target) {
        if (!Objects.equals(this.target, target)) {
            this.target = target;

            refreshState();
        }
    }

    @SuppressWarnings("unchecked")
    public A withTarget(@Nullable V target) {
        setTarget(target);
        return ((A) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withText(@Nullable String text) {
        return ((A) super.withText(text));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withEnabled(boolean enabled) {
        return ((A) super.withEnabled(enabled));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withVisible(boolean visible) {
        return ((A) super.withVisible(visible));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withIcon(@Nullable Icon icon) {
        return ((A) super.withIcon(icon));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withIcon(@Nullable VaadinIcon icon) {
        return ((A) super.withIcon(icon));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withTitle(@Nullable String title) {
        return ((A) super.withTitle(title));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withVariant(ActionVariant actionVariant) {
        return ((A) super.withVariant(actionVariant));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        return ((A) super.withShortcutCombination(shortcutCombination));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        return ((A) super.withHandler(handler));
    }

    @SuppressWarnings("unchecked")
    @Override
    public A withEnabledByUiPermissions(boolean enabledByUiPermissions) {
        return ((A) super.withEnabledByUiPermissions(enabledByUiPermissions));
    }

    @SuppressWarnings("unchecked")
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
