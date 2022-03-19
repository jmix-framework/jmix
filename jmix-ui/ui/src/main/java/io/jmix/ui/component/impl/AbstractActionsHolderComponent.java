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

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsPermissions;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.SecuredActionsHolder;
import io.jmix.ui.sys.ShortcutsDelegate;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixButton;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.ComponentsHelper.findActionById;

/**
 * Base class for action holders with shortcuts support
 */
public abstract class AbstractActionsHolderComponent<T extends com.vaadin.ui.AbstractComponent
        & com.vaadin.event.Action.ShortcutNotifier>
        extends AbstractComponent<T> implements SecuredActionsHolder {

    protected List<Action> actionList = new ArrayList<>(5);
    protected Map<Action, JmixButton> actionButtons = new HashMap<>(5);

    protected VerticalLayout contextMenuPopup;

    protected ShortcutsDelegate<ShortcutListener> shortcutsDelegate;
    protected ActionsPermissions actionsPermissions = new ActionsPermissions(this);

    protected boolean showIconsForPopupMenuActions;

    protected Consumer<PropertyChangeEvent> actionPropertyChangeListener = this::actionPropertyChanged;

    protected AbstractActionsHolderComponent() {
        contextMenuPopup = createContextMenuPopup();
        initContextMenuPopup(contextMenuPopup);

        shortcutsDelegate = createShortcutsDelegate();
    }

    protected VerticalLayout createContextMenuPopup() {
        return new VerticalLayout();
    }

    protected void initContextMenuPopup(VerticalLayout contextMenuPopup) {
        contextMenuPopup.setSpacing(false);
        contextMenuPopup.setMargin(false);

        contextMenuPopup.setSizeUndefined();
        contextMenuPopup.setStyleName("jmix-cm-container");

        AppUI ui = AppUI.getCurrent();
        if (ui != null && ui.isTestMode()) {
            contextMenuPopup.setJTestId("jmixContextMenu");
        }
    }

    protected ShortcutsDelegate<ShortcutListener> createShortcutsDelegate() {
        return new ShortcutsDelegate<ShortcutListener>() {
            @Override
            protected ShortcutListener attachShortcut(String actionId, KeyCombination keyCombination) {
                ShortcutListener shortcut =
                        new ShortcutListenerDelegate(actionId, keyCombination.getKey().getCode(),
                                KeyCombination.Modifier.codes(keyCombination.getModifiers())
                        ).withHandler((sender, target) -> {
                            if (target == component) {
                                Action action = getAction(actionId);
                                if (action != null && action.isEnabled() && action.isVisible()) {
                                    action.actionPerform(AbstractActionsHolderComponent.this);
                                }
                            }
                        });

                component.addShortcutListener(shortcut);
                return shortcut;
            }

            @Override
            protected void detachShortcut(Action action, ShortcutListener shortcutDescriptor) {
                component.removeShortcutListener(shortcutDescriptor);
            }

            @Override
            protected Collection<Action> getActions() {
                return AbstractActionsHolderComponent.this.getActions();
            }
        };
    }

    protected void actionPropertyChanged(PropertyChangeEvent evt) {
        Action action = (Action) evt.getSource();
        JmixButton button = actionButtons.get(action);

        if (Action.PROP_ICON.equals(evt.getPropertyName())) {
            setContextMenuButtonIcon(button, showIconsForPopupMenuActions
                    ? action.getIcon()
                    : null);
        } else if (Action.PROP_CAPTION.equals(evt.getPropertyName())) {
            setContextMenuButtonCaption(button, action.getCaption(), action.getShortcutCombination());
        } else if (Action.PROP_DESCRIPTION.equals(evt.getPropertyName())) {
            button.setDescription(action.getDescription());
        } else if (Action.PROP_ENABLED.equals(evt.getPropertyName())) {
            button.setEnabled(action.isEnabled());
        } else if (Action.PROP_VISIBLE.equals(evt.getPropertyName())) {
            button.setVisible(action.isVisible());
        }
    }

    protected void setContextMenuButtonCaption(JmixButton button, @Nullable String caption, @Nullable KeyCombination shortcutCombination) {
        if (!Strings.isNullOrEmpty(caption)
                && shortcutCombination != null) {
            caption = caption + " (" + shortcutCombination.format() + ")";
        }

        button.setCaption(caption);
    }

    protected void setContextMenuButtonIcon(JmixButton button, @Nullable String icon) {
        if (!StringUtils.isEmpty(icon)) {
            Resource iconResource = getIconResource(icon);
            button.setIcon(iconResource);
        } else {
            button.setIcon(null);
        }
    }

    protected void setContextMenuButtonAction(JmixButton button, Action action) {
        setContextMenuButtonIcon(button, showIconsForPopupMenuActions
                ? action.getIcon()
                : null);
        setContextMenuButtonCaption(button, action.getCaption(), action.getShortcutCombination());
        button.setDescription(action.getDescription());
        button.setEnabled(action.isEnabled());
        button.setVisible(action.isVisible());

        action.addPropertyChangeListener(actionPropertyChangeListener);
        button.setClickHandler(event -> {
            beforeContextMenuButtonHandlerPerformed();
            action.actionPerform(AbstractActionsHolderComponent.this);
        });
    }

    protected void beforeContextMenuButtonHandlerPerformed() {
    }

    @Autowired
    public void setThemeConstantsManager(ThemeConstantsManager themeConstantsManager) {
        ThemeConstants theme = themeConstantsManager.getConstants();
        this.showIconsForPopupMenuActions = theme.getBoolean("jmix.ui.showIconsForPopupMenuActions", false);
    }

    @Override
    public void addAction(Action action) {
        int index = findActionById(actionList, action.getId());
        if (index < 0) {
            index = actionList.size();
        }

        addAction(action, index);
    }

    @Override
    public void addAction(Action action, int index) {
        checkNotNullArgument(action, "action must be non null");

        int oldIndex = findActionById(actionList, action.getId());
        if (oldIndex >= 0) {
            removeAction(actionList.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        if (StringUtils.isNotEmpty(action.getCaption())) {
            JmixButton contextMenuButton = createContextMenuButton();
            initContextMenuButton(contextMenuButton, action);

            int visibleActionsIndex = 0;
            int i = 0;
            while (i < index && i < actionList.size()) {
                Action componentAction = actionList.get(i);
                if (StringUtils.isNotEmpty(componentAction.getCaption())
                        && actionButtons.containsKey(componentAction)) {
                    visibleActionsIndex++;
                }

                i++;
            }

            contextMenuPopup.addComponent(contextMenuButton, visibleActionsIndex);
            actionButtons.put(action, contextMenuButton);
        }

        actionList.add(index, action);
        shortcutsDelegate.addAction(null, action);

        attachAction(action);

        actionsPermissions.apply(action);
    }

    protected void initContextMenuButton(JmixButton contextMenuButton, Action action) {
        contextMenuButton.setStyleName("jmix-cm-button");
        setContextMenuButtonAction(contextMenuButton, action);
    }

    protected void attachAction(Action action) {
        action.refreshState();
    }

    protected abstract JmixButton createContextMenuButton();

    @Override
    public void removeAction(Action action) {
        if (actionList.remove(action)) {
            shortcutsDelegate.removeAction(action);

            action.removePropertyChangeListener(actionPropertyChangeListener);

            Button button = actionButtons.remove(action);
            if (button != null) {
                contextMenuPopup.removeComponent(button);
            }
        }
    }

    @Override
    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    @Override
    public void removeAllActions() {
        for (Action action : new ArrayList<>(actionList)) {
            removeAction(action);
        }
    }

    @Override
    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actionList);
    }

    @Override
    @Nullable
    public Action getAction(String id) {
        for (Action action : getActions()) {
            if (Objects.equals(action.getId(), id)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public ActionsPermissions getActionsPermissions() {
        return actionsPermissions;
    }
}
