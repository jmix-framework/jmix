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

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class SecuredBaseAction extends BaseAction implements SecuredAction {

    protected boolean enabledByUiPermissions = true;
    protected boolean visibleByUiPermissions = true;

    protected List<EnabledRule> enabledRules;

    public SecuredBaseAction(String id) {
        super(id);
    }

    @Override
    public void refreshState() {
        setVisibleInternal(visibleExplicitly && isVisibleByUiPermissions());

        setEnabledInternal(enabledExplicitly && isEnabledByUiPermissions() && isVisibleByUiPermissions()
                && isPermitted() && isApplicable() && isEnabledByRule());
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
        if (enabledRules != null) {
            enabledRules.remove(enabledRule);
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
    public SecuredBaseAction withIcon(@Nullable Icon icon) {
        setIcon(icon);
        return this;
    }

    @Override
    public SecuredBaseAction withIcon(@Nullable VaadinIcon icon) {
        setIcon(FlowuiComponentUtils.convertToIcon(icon));
        return this;
    }

    @Override
    public SecuredBaseAction withTitle(@Nullable String title) {
        setDescription(title);
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
