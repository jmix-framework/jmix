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

package io.jmix.flowui.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.KeyCombination;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class SecuredBaseAction extends BaseAction {

    protected List<EnabledRule> enabledRules;

    public SecuredBaseAction(String id) {
        super(id);
    }

    @Override
    public void refreshState() {
        setVisibleInternal(visibleExplicitly);

        setEnabledInternal(enabledExplicitly
                && isPermitted() && isApplicable() && isEnabledByRule());
    }

    protected boolean isPermitted() {
        return true;
    }

    protected boolean isEnabledByRule() {
        if (enabledRules == null) {
            return true;
        }

        for (EnabledRule rule : enabledRules) {
            if (!rule.isActionEnabled()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add new enabled rule for the action.
     *
     * @param enabledRule boolean rule for the action enabled state
     */
    public void addEnabledRule(EnabledRule enabledRule) {
        checkNotNullArgument(enabledRule);

        if (enabledRules == null) {
            enabledRules = new ArrayList<>(2);
        }
        if (!enabledRules.contains(enabledRule)) {
            enabledRules.add(enabledRule);
        }
    }

    /**
     * Remove enabled rule.
     *
     * @param enabledRule boolean rule for the action enabled state
     */
    public void removeEnabledRule(EnabledRule enabledRule) {
        if (enabledRules != null
                && enabledRules.remove(enabledRule)) {
            refreshState();
        }
    }

    /**
     * Callback interface which is invoked by the action to determine its enabled state.
     *
     * @see #addEnabledRule(EnabledRule)
     */
    @FunctionalInterface
    public interface EnabledRule {
        boolean isActionEnabled();
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
    public SecuredBaseAction withIcon(@Nullable Component icon) {
        return (SecuredBaseAction) super.withIcon(icon);
    }

    @Deprecated(since = "3.0", forRemoval = true)
    @Override
    public SecuredBaseAction withIcon(@Nullable Icon icon) {
        setIcon(icon);
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
}
