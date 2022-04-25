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

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.event.EventBus;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractAction implements Action {

    protected final String id;

    protected String text;
    protected boolean enabled = true;
    protected boolean visible = true;
    protected String icon;
    protected String title;
    protected ActionVariant variant = ActionVariant.DEFAULT;
    protected KeyCombination shortcutCombination;

    protected EventBus eventBus;

    protected AbstractAction(String id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(@Nullable String text) {
        String oldValue = this.text;
        if (!Objects.equals(oldValue, text)) {
            this.text = text;
            firePropertyChange(Action.PROP_TEXT, oldValue, text);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean oldValue = this.enabled;
        if (!Objects.equals(this.enabled, enabled)) {
            this.enabled = enabled;
            firePropertyChange(Action.PROP_ENABLED, oldValue, enabled);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        boolean oldValue = this.visible;
        if (!Objects.equals(this.visible, visible)) {
            this.visible = visible;
            firePropertyChange(Action.PROP_VISIBLE, oldValue, visible);
        }
    }

    @Nullable
    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public void setIcon(@Nullable String icon) {
        String oldValue = this.icon;
        if (!Objects.equals(oldValue, icon)) {
            this.icon = icon;
            firePropertyChange(Action.PROP_ICON, oldValue, icon);
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(@Nullable String title) {
        String oldValue = this.title;
        if (!Objects.equals(oldValue, title)) {
            this.title = title;
            firePropertyChange(Action.PROP_TITLE, oldValue, title);
        }
    }

    @Override
    public ActionVariant getVariant() {
        return variant;
    }

    @Override
    public void setVariant(ActionVariant variant) {
        Objects.requireNonNull(variant);

        ActionVariant oldValue = this.variant;
        if (!Objects.equals(oldValue, variant)) {
            this.variant = variant;
            firePropertyChange(Action.PROP_VARIANT, oldValue, variant);
        }
    }

    @Nullable
    @Override
    public KeyCombination getShortcutCombination() {
        return shortcutCombination;
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        KeyCombination oldValue = this.shortcutCombination;
        if (!Objects.equals(oldValue, shortcutCombination)) {
            this.shortcutCombination = shortcutCombination;
            firePropertyChange(Action.PROP_SHORTCUT, oldValue, shortcutCombination);
        }
    }

    @Override
    public Registration addPropertyChangeListener(Consumer<PropertyChangeEvent> listener) {
        return getEventBus().addListener(PropertyChangeEvent.class, listener);
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }

    protected boolean hasListener(Class<? extends EventObject> eventType) {
        return eventBus != null && eventBus.hasListener(eventType);
    }

    protected void firePropertyChange(String propertyName, @Nullable Object oldValue, @Nullable Object newValue) {
        if (hasListener(PropertyChangeEvent.class)) {
            PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
            getEventBus().fireEvent(event);
        }
    }
}
