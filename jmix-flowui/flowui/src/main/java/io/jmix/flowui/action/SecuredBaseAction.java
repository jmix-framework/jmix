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
import io.jmix.flowui.kit.component.ComponentUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class SecuredBaseAction extends ObservableBaseAction<SecuredBaseAction> implements SecuredAction {

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

    @Deprecated(since = "2.8", forRemoval = true)
    @Override
    public SecuredBaseAction withIcon(@Nullable Icon icon) {
        setIcon(icon);
        return this;
    }

    @Override
    public SecuredBaseAction withIcon(@Nullable VaadinIcon icon) {
        setIcon(ComponentUtils.convertToIcon(icon));
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
