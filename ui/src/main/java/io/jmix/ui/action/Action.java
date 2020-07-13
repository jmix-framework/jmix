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
package io.jmix.ui.action;

import io.jmix.core.security.ConstraintOperationType;
import io.jmix.ui.component.ActionOwner;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.icon.Icons;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * The {@code Action} interface abstracts away a function from a visual component.
 * <p> The action is executed by invoking its {@link #actionPerform(Component)} method.
 */
public interface Action {

    String PROP_CAPTION = "caption";
    String PROP_DESCRIPTION = "description";
    String PROP_SHORTCUT = "shortcut";
    String PROP_ICON = "icon";
    String PROP_ENABLED = "enabled";
    String PROP_VISIBLE = "visible";

    /**
     * @return  action's identifier
     */
    String getId();

    /**
     * @return  action's caption
     */
    @Nullable
    String getCaption();

    void setCaption(@Nullable String caption);

    /**
     * @return  action's description
     */
    @Nullable
    String getDescription();
    void setDescription(@Nullable String description);

    /**
     *
     * @return action's keyboard shortcut
     */
    @Nullable
    KeyCombination getShortcutCombination();

    void setShortcutCombination(@Nullable KeyCombination shortcut);

    /**
     * Set shortcut from string representation.
     *
     * @param shortcut string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     */
    void setShortcut(@Nullable String shortcut);

    /**
     * @return  action's icon
     */
    @Nullable
    String getIcon();
    void setIcon(@Nullable String icon);
    /**
     * Set an icon from an icon set.
     */
    void setIconFromSet(Icons.Icon icon);

    /**
     * @return  whether the action is currently enabled
     */
    boolean isEnabled();
    void setEnabled(boolean enabled);

    /**
     * @return  whether the action is currently visible
     */
    boolean isVisible();
    void setVisible(boolean visible);

    /**
     * Refresh internal state of the action to initialize enabled, visible, caption, icon, etc. properties depending
     * on programmatically set values and user permissions set at runtime.
     *
     * <p> For example, this method is called by visual components holding actions when they are bound to
     * data. At this moment the action can find out what entity it is connected to and change its state
     * according to the user permissions.
     */
    void refreshState();

    /**
     * @return  a single component owning the action. If there are several owners, first will be returned.
     */
    @Nullable
    ActionOwner getOwner();

    /**
     * @return the collection of owners
     */
    Collection<ActionOwner> getOwners();

    /**
     * Add an owner component.
     *
     * @param actionOwner owner component
     */
    void addOwner(ActionOwner actionOwner);

    /**
     * Remove the owner component.
     *
     * @param actionOwner owner component
     */
    void removeOwner(ActionOwner actionOwner);

    /**
     * Invoked by owning component to execute the action.
     *
     * @param component invoking component
     */
    void actionPerform(Component component);

    /**
     * Adds a listener to be notified about Enabled, Caption or Icon property changes.
     *
     * @param listener a listener object
     */
    void addPropertyChangeListener(Consumer<PropertyChangeEvent> listener);

    /**
     * Removes the listener.
     *
     * @param listener a listener object
     * @see #addPropertyChangeListener
     */
    void removePropertyChangeListener(Consumer<PropertyChangeEvent> listener);

    /**
     * Indicates that the action can be assigned a {@link OpenType} to open a related screen.
     */
    @Deprecated
    interface HasOpenType extends Action {
        OpenType getOpenType();
        void setOpenType(OpenType openType);
    }

    /**
     * Indicates that the action can be affected by UI permissions.
     */
    interface SecuredAction extends Action {
        boolean isEnabledByUiPermissions();
        void setEnabledByUiPermissions(boolean enabledByUiPermissions);

        boolean isVisibleByUiPermissions();
        void setVisibleByUiPermissions(boolean visibleByUiPermissions);
    }

    interface HasTarget extends Action {
        @Nullable
        ListComponent getTarget();

        void setTarget(@Nullable ListComponent target);
    }

    /**
     * Callback interface which is invoked by the action before execution.
     */
    interface BeforeActionPerformedHandler {
        /**
         * Invoked by the action before execution.
         * @return true to continue execution, false to abort
         */
        boolean beforeActionPerformed();
    }

    /**
     * Interface defining methods for adding and removing {@link BeforeActionPerformedHandler}s
     */
    interface HasBeforeActionPerformedHandler extends Action {
        BeforeActionPerformedHandler getBeforeActionPerformedHandler();
        void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler);
    }

    /**
     * Interface defining constraintOperationType and constraintCode options.
     */
    interface HasSecurityConstraint {
        void setConstraintOperationType(ConstraintOperationType constraintOperationType);
        ConstraintOperationType getConstraintOperationType();

        String getConstraintCode();
        void setConstraintCode(String constraintCode);
    }

    /**
     * Marker interface that indicates that the implementing action will
     * change its 'enabled' state according to the screen read-only mode.
     */
    interface DisabledWhenScreenReadOnly {
    }

    /**
     * Used in dialogs to assign a special visual style for a button representing the action.
     */
    enum Status {
        NORMAL,
        PRIMARY
    }

    /**
     * Event sent when the action is performed.
     */
    class ActionPerformedEvent extends EventObject {
        private final Component component;

        public ActionPerformedEvent(Action source, Component component) {
            super(source);
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }

        @Override
        public Action getSource() {
            return (Action) super.getSource();
        }
    }
}
