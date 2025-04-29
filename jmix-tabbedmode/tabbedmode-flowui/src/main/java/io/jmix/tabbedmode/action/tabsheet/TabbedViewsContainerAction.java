/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.action.tabsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.tabbedmode.component.tabsheet.JmixViewTab;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class TabbedViewsContainerAction<A extends TabbedViewsContainerAction<A>> extends BaseAction
        implements TargetAction<TabbedViewsContainer<?>> {

    protected TabbedViewsContainer<?> target;

    public TabbedViewsContainerAction(String id) {
        super(id);

        initAction();
    }

    protected void initAction() {
        // hook to be implemented
    }

    @Nullable
    @Override
    public TabbedViewsContainer<?> getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable TabbedViewsContainer<?> target) {
        if (!Objects.equals(this.target, target)) {
            if (this.target != null) {
                detachListeners(this.target);
            }

            this.target = target;

            if (target != null) {
                attachListeners(target);
            }

            refreshState();
        }
    }

    protected void detachListeners(TabbedViewsContainer<?> target) {
        // hook to be implemented
    }

    protected void attachListeners(TabbedViewsContainer<?> target) {
        // hook to be implemented
    }

    @SuppressWarnings("unchecked")
    public A withTarget(@Nullable TabbedViewsContainer<?> target) {
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

    @Override
    public void actionPerform(Component trigger) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            execute(trigger);
        } else {
            super.actionPerform(trigger);
        }
    }

    public abstract void execute(@Nullable Component trigger);

    @Nullable
    protected Component findTab(@Nullable Component trigger) {
        // if executed by a context menu
        if (trigger instanceof JmixViewTab tab) {
            return tab;
        // if executed by a shortcut
        } else if (trigger instanceof TabbedViewsContainer<?> viewsContainer) {
            return viewsContainer.getSelectedTab();
        // shouldn't happen
        } else {
            return null;
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target != null;
    }

    @Nullable
    protected Tab findActionTab() {
        String tabId = target.getElement().getProperty("_contextMenuTargetTabId", "<no_id>");
        return target.findTab(tabId).orElse(null);
    }

    protected void checkTarget() {
        if (target == null) {
            throw new IllegalStateException(String.format("%s target is not set", getClass().getSimpleName()));
        }
    }
}
