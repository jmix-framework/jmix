/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component.impl;

import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.widget.JmixButton;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.Objects;
import java.util.function.Consumer;

import static io.jmix.ui.theme.ThemeClassNames.PRIMARY_ACTION;

public class ButtonImpl extends AbstractComponent<JmixButton> implements Button {

    protected Action action;
    protected Consumer<PropertyChangeEvent> actionPropertyChangeListener;

    protected KeyCombination shortcut;

    @Internal
    protected Boolean enabled;
    @Internal
    protected Boolean visible;
    @Internal
    protected Boolean primary;

    public ButtonImpl() {
        component = createComponent();
        initComponent(component);
    }

    protected JmixButton createComponent() {
        return new JmixButton();
    }

    protected void initComponent(JmixButton component) {
        component.setClickHandler(this::buttonClicked);
        // The default description value is empty string,
        // that prevents obtaining a description value from an action
        component.setDescription(null);
    }

    // override in descendants if needed
    protected void beforeActionPerformed() {
    }

    protected void buttonClicked(@SuppressWarnings("unused") MouseEventDetails mouseEventDetails) {
        beforeActionPerformed();
        if (action != null) {
            action.actionPerform(getActionEventTarget());
        }
        if (hasSubscriptions(ClickEvent.class)) {
            publish(ClickEvent.class, new ClickEvent(this));
        }
        afterActionPerformed();
    }

    protected Component getActionEventTarget() {
        return this;
    }

    // override in descendants if needed
    protected void afterActionPerformed() {
    }

    @Nullable
    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public void setIcon(@Nullable String icon) {
        this.icon = icon;

        // -icon style is added automatically on the client-side of JmixButton
        if (StringUtils.isNotEmpty(icon)) {
            Resource iconResource = getIconResource(icon);
            component.setIcon(iconResource);
        } else {
            component.setIcon(null);
        }
    }

    @Override
    public void setAction(@Nullable Action action, boolean overrideOwnerProperties) {
        if (action != this.action) {
            if (this.action != null) {
                this.action.removeOwner(this);
                this.action.removePropertyChangeListener(actionPropertyChangeListener);
                if (Objects.equals(this.action.getCaption(), getCaption())) {
                    setCaption(null);
                }
                if (Objects.equals(this.action.getDescription(), getDescription())) {
                    setDescription(null);
                }
                if (Objects.equals(this.action.getIcon(), getIcon())) {
                    setIcon(null);
                }
            }

            this.action = action;

            if (action != null) {
                String caption = action.getCaption();
                if (caption != null
                        && (component.getCaption() == null || overrideOwnerProperties)) {
                    component.setCaption(caption);
                }

                String description = action.getDescription();
                KeyCombination shortcutCombination = action.getShortcutCombination();
                if (shortcutCombination != null
                        && (getShortcutCombination() == null || overrideOwnerProperties)) {
                    setShortcutCombination(shortcutCombination);

                    if (description == null) {
                        description = shortcutCombination.format();
                    }
                }
                if (description != null
                        && (component.getDescription() == null || overrideOwnerProperties)) {
                    component.setDescription(description);
                }

                if (enabled == null || overrideOwnerProperties) {
                    component.setEnabled(action.isEnabled());
                }
                if (visible == null || overrideOwnerProperties) {
                    component.setVisible(action.isVisible());
                }

                if (action.getIcon() != null
                        && (getIcon() == null || overrideOwnerProperties)) {
                    setIcon(action.getIcon());
                }

                action.addOwner(this);

                actionPropertyChangeListener = evt -> {
                    String propertyName = evt.getPropertyName();
                    if (Action.PROP_ICON.equals(propertyName)) {
                        setIcon(this.action.getIcon());
                    } else if (Action.PROP_CAPTION.equals(propertyName)) {
                        setCaption(this.action.getCaption());
                    } else if (Action.PROP_DESCRIPTION.equals(propertyName)) {
                        setDescription(this.action.getDescription());
                    } else if (Action.PROP_ENABLED.equals(propertyName)) {
                        setEnabled(this.action.isEnabled());
                    } else if (Action.PROP_VISIBLE.equals(propertyName)) {
                        setVisible(this.action.isVisible());
                    } else if (Action.PROP_SHORTCUT.equals(propertyName)) {
                        setShortcutCombination(this.action.getShortcutCombination());
                    }
                };
                action.addPropertyChangeListener(actionPropertyChangeListener);

                if (component.getJTestId() == null) {
                    AppUI ui = AppUI.getCurrent();
                    if (ui != null && ui.isTestMode()) {
                        component.setJTestId(action.getId());
                    }
                }
            }

            if (primary == null || overrideOwnerProperties) {
                boolean primaryAction = action instanceof AbstractAction && ((AbstractAction) action).isPrimary();
                if (primaryAction) {
                    addStyleName(PRIMARY_ACTION);
                } else {
                    removeStyleName(PRIMARY_ACTION);
                }
            }
        }
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public void setDisableOnClick(boolean value) {
        component.setDisableOnClick(value);
    }

    @Override
    public boolean isDisableOnClick() {
        return component.isDisableOnClick();
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        component.setCaptionAsHtml(captionAsHtml);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return component.isCaptionAsHtml();
    }

    @Nullable
    @Override
    public KeyCombination getShortcutCombination() {
        return shortcut;
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcut) {
        KeyCombination oldValue = this.shortcut;
        if (!Objects.equals(oldValue, shortcut)) {
            this.shortcut = shortcut;

            if (shortcut != null) {
                int[] shortcutModifiers = KeyCombination.Modifier.codes(shortcut.getModifiers());
                int shortcutCode = shortcut.getKey().getCode();

                component.setClickShortcut(shortcutCode, shortcutModifiers);
            } else {
                component.removeClickShortcut();
            }
        }
    }

    @Override
    public void setShortcut(@Nullable String shortcut) {
        if (shortcut != null) {
            setShortcutCombination(KeyCombination.create(shortcut));
        } else {
            setShortcutCombination(null);
        }
    }

    @Override
    public void click() {
        component.click();
    }

    @Override
    public Subscription addClickListener(Consumer<ClickEvent> listener) {
        return getEventHub().subscribe(ClickEvent.class, listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.visible = visible;
    }

    @Override
    public void addStyleName(String styleName) {
        super.addStyleName(styleName);

        if (PRIMARY_ACTION.equals(styleName)) {
            primary = true;
        }
    }

    @Override
    public void removeStyleName(String styleName) {
        super.removeStyleName(styleName);

        if (PRIMARY_ACTION.equals(styleName)) {
            primary = false;
        }
    }
}
