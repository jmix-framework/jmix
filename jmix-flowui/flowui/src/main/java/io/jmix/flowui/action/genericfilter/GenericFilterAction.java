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

package io.jmix.flowui.action.genericfilter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.KeyCombination;

import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class GenericFilterAction<A extends GenericFilterAction<A>> extends SecuredBaseAction
        implements TargetAction<GenericFilter>, ExecutableAction {

    protected GenericFilter target;

    protected boolean visibleBySpecificUiPermission = true;

    public GenericFilterAction(String id) {
        super(id);
        initAction();
    }

    protected void initAction() {
        // hook to be implemented
    }

    @Nullable
    @Override
    public GenericFilter getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable GenericFilter target) {
        if (!Objects.equals(this.target, target)) {
            setTargetInternal(target);

            refreshState();
        }
    }

    protected void setTargetInternal(@Nullable GenericFilter target) {
        this.target = target;
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
        return getTarget() != null;
    }

    @Override
    protected boolean isPermitted() {
        return target != null
                && super.isPermitted();
    }

    @Override
    public boolean isVisibleByUiPermissions() {
        return visibleBySpecificUiPermission
                && super.isVisibleByUiPermissions();
    }

    @SuppressWarnings("unchecked")
    public A withTarget(@Nullable GenericFilter target) {
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

    protected void checkTarget() {
        if (target == null) {
            throw new IllegalStateException(String.format("%s target is not set", getClass().getSimpleName()));
        }
    }
}
