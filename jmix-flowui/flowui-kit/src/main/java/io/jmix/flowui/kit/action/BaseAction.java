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
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.KeyCombination;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Base implementation of {@link Action}.
 */
public class BaseAction<A extends BaseAction<A>> extends AbstractAction {

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

    /**
     * Adds a listener to be notified when action is performed.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    @SuppressWarnings("UnusedReturnValue")
    public Registration addActionPerformedListener(Consumer<ActionPerformedEvent> listener) {
        return getEventBus().addListener(ActionPerformedEvent.class, listener);
    }

    /**
     * Sets the text property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param text text to set or {@code null} to remove
     * @return this object
     */
    public A withText(@Nullable String text) {
        setText(text);
        return self();
    }

    /**
     * Sets the enabled property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param enabled whether the action is currently enabled
     * @return this object
     */
    public A withEnabled(boolean enabled) {
        setEnabled(enabled);
        return self();
    }

    /**
     * Sets the visible property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param visible whether the action is currently visible
     * @return this object
     */
    public A withVisible(boolean visible) {
        setVisible(visible);
        return self();
    }

    /**
     * Sets the icon property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param icon icon to set or {@code null} to remove
     * @return this object
     */
    public A withIcon(@Nullable Component icon) {
        setIcon(icon);
        return self();
    }

    /**
     * Sets the description property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param description description to set or {@code null} to remove
     * @return this object
     */
    public A withDescription(@Nullable String description) {
        setDescription(description);
        return self();
    }

    /**
     * Sets the variant property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param variant variant to set
     * @return this object
     */
    public A withVariant(ActionVariant variant) {
        setVariant(variant);
        return self();
    }

    /**
     * Sets object that stores information about keys, modifiers and additional
     * settings that describe shortcut combinations.
     *
     * @param shortcutCombination key combination to set or {@code null} to remove
     * @return this object
     */
    public A withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        setShortcutCombination(shortcutCombination);
        return self();
    }

    /**
     * Adds a listener to be notified when action is performed.
     *
     * @param handler listener to add or {@code null} to remove all
     * @return this object
     */
    public A withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        if (handler == null) {
            if (getEventBus().hasListener(ActionPerformedEvent.class)) {
                getEventBus().removeListener(ActionPerformedEvent.class);
            }
        } else {
            addActionPerformedListener(handler);
        }

        return self();
    }

    @SuppressWarnings("unchecked")
    protected A self() {
        return (A) this;
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
