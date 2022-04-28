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

package io.jmix.flowui.kit.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BaseAction extends AbstractAction {

    protected boolean enabledExplicitly = true;
    protected boolean visibleExplicitly = true;

    public BaseAction(String id) {
        super(id);
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.visibleExplicitly != visible) {
            this.visibleExplicitly = visible;

            refreshState();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabledExplicitly != enabled) {
            this.enabledExplicitly = enabled;

            refreshState();
        }
    }

    @Override
    public void refreshState() {
        setVisibleInternal(visibleExplicitly);

        setEnabledInternal(enabledExplicitly && isApplicable());
    }

    @Override
    public void actionPerform(Component component) {
        if (eventBus != null) {
            ActionPerformedEvent event = new ActionPerformedEvent(this, component);
            getEventBus().fireEvent(event);
        }
    }

    @SuppressWarnings("unused") // called on declarative events registration
    public Registration addActionPerformedListener(Consumer<ActionPerformedEvent> listener) {
        return getEventBus().addListener(ActionPerformedEvent.class, listener);
    }

    public BaseAction withText(@Nullable String text) {
        setText(text);
        return this;
    }

    public BaseAction withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public BaseAction withVisible(boolean visible) {
        setVisible(visible);
        return this;
    }

    public BaseAction withIcon(@Nullable String icon) {
        setIcon(icon);
        return this;
    }

    public BaseAction withIcon(@Nullable VaadinIcon icon) {
        setIcon(FlowUiComponentUtils.iconToSting(icon));
        return this;
    }

    public BaseAction withTitle(@Nullable String title) {
        setTitle(title);
        return this;
    }

    public BaseAction withVariant(ActionVariant actionVariant) {
        setVariant(actionVariant);
        return this;
    }

    public BaseAction withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        setShortcutCombination(shortcutCombination);
        return this;
    }

    public BaseAction withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        if (handler == null) {
            if (getEventBus().hasListener(ActionPerformedEvent.class)) {
                getEventBus().removeListener(ActionPerformedEvent.class);
            }
        } else {
            addActionPerformedListener(handler);
        }

        return this;
    }

    protected void setVisibleInternal(boolean visible) {
        super.setVisible(visible);
    }

    protected void setEnabledInternal(boolean enabled) {
        super.setEnabled(enabled);
    }

    protected boolean isApplicable() {
        return true;
    }
}
