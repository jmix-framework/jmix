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

import io.jmix.core.security.EntityOp;
import io.jmix.ui.component.ActionOwner;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioCollection;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
     * @return action's identifier
     */
    String getId();

    /**
     * @return action's caption
     */
    @Nullable
    String getCaption();

    void setCaption(@Nullable String caption);

    /**
     * @return action's description
     */
    @Nullable
    String getDescription();

    void setDescription(@Nullable String description);

    /**
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
     * @return action's icon
     */
    @Nullable
    String getIcon();

    void setIcon(@Nullable String icon);

    /**
     * @return whether the action is currently enabled
     */
    boolean isEnabled();

    void setEnabled(boolean enabled);

    /**
     * @return whether the action is currently visible
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
    @StudioCollection(
            xmlElement = "properties",
            itemXmlElement = "property",
            itemCaption = "Property",
            icon = "io/jmix/ui/icon/element/properties.svg",
            itemIcon = "io/jmix/ui/icon/element/property.svg",
            itemProperties = {
                    @StudioProperty(name = "name", type = PropertyType.STRING, required = true),
                    @StudioProperty(name = "value", type = PropertyType.STRING)
            }
    )
    void refreshState();

    /**
     * @return a single component owning the action. If there are several owners, first will be returned.
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
     * Interface defining constraintOperationType and constraintCode options.
     */
    interface HasSecurityConstraint {
        void setConstraintEntityOp(@Nullable EntityOp entityOp);

        @Nullable
        EntityOp getConstraintEntityOp();
    }

    /**
     * Interface to be implemented by actions that have primary state.
     */
    interface HasPrimaryState {

        /**
         * @return true if action is primary or false otherwise
         */
        boolean isPrimary();

        /**
         * Sets whether action is primary or not.
         *
         * @param primary primary
         */
        void setPrimary(boolean primary);
    }

    /**
     * Interface to be implemented by actions which may adjust
     * their 'enabled' state according to the screen read-only mode.
     */
    interface AdjustWhenScreenReadOnly {

        /**
         * @return whether this action must be disabled when a screen in the read-only mode
         */
        default boolean isDisabledWhenScreenReadOnly() {
            return true;
        }
    }

    /**
     * Interface to be implemented by actions that open a screen.
     */
    interface ScreenOpeningAction {

        /**
         * Returns the screen open mode if it was set by {@link #setOpenMode(OpenMode)}
         * or in the screen XML, otherwise returns {@code null}.
         */
        @Nullable
        OpenMode getOpenMode();

        /**
         * Sets the screen open mode.
         *
         * @param openMode the open mode to set
         */
        void setOpenMode(@Nullable OpenMode openMode);

        /**
         * Returns the screen id if it was set by {@link #setScreenId(String)}
         * or in the screen XML, otherwise returns {@code null}.
         */
        @Nullable
        String getScreenId();

        /**
         * Sets the screen id.
         *
         * @param screenId the screen id to set
         */
        void setScreenId(@Nullable String screenId);

        /**
         * Returns the screen class if it was set by {@link #setScreenClass(Class)}
         * or in the screen XML, otherwise returns {@code null}.
         */
        @Nullable
        Class<? extends Screen> getScreenClass();

        /**
         * Sets the screen class.
         *
         * @param screenClass the screen class to set
         */
        void setScreenClass(@Nullable Class<? extends Screen> screenClass);

        /**
         * Sets the screen options supplier. The supplier provides
         * {@code ScreenOptions} to the opened screen.
         * <p>
         * The preferred way to set the supplier is using a controller method
         * annotated with {@link Install}, e.g.:
         * <pre>
         * &#64;Install(to = "petsTable.view", subject = "screenOptionsSupplier")
         * protected ScreenOptions petsTableViewScreenOptionsSupplier() {
         *     return new MapScreenOptions(ParamsMap.of("someParameter", 10));
         * }
         * </pre>
         */
        void setScreenOptionsSupplier(Supplier<ScreenOptions> screenOptionsSupplier);

        /**
         * Sets the screen configurer. Use the configurer if you need to provide
         * parameters to the opened screen through setters.
         * <p>
         * The preferred way to set the configurer is using a controller method
         * annotated with {@link Install}, e.g.:
         * <pre>
         * &#64;Install(to = "petsTable.view", subject = "screenConfigurer")
         * protected void petsTableViewScreenConfigurer(Screen editorScreen) {
         *     ((PetEdit) editorScreen).setSomeParameter(someValue);
         * }
         * </pre>
         */
        void setScreenConfigurer(Consumer<Screen> screenConfigurer);

        /**
         * Sets the handler to be invoked when the editor screen closes.
         * <p>
         * The preferred way to set the handler is using a controller method
         * annotated with {@link Install}, e.g.:
         * <pre>
         * &#64;Install(to = "petsTable.view", subject = "afterCloseHandler")
         * protected void petsTableViewAfterCloseHandler(AfterCloseEvent event) {
         *     if (event.closedWith(StandardOutcome.COMMIT)) {
         *         System.out.println("Committed");
         *     }
         * }
         * </pre>
         */
        void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler);
    }

    /**
     * Indicates that the action is executable.
     */
    interface ExecutableAction extends Action {

        /**
         * Executes the {@link Action}
         */
        void execute();
    }

    /**
     * An action that can be used as Main TabSheet context menu item.
     */
    interface MainTabSheetAction extends Action {

        /**
         * Determines whether this action is applicable for the given screen.
         *
         * @param screen a screen for which the applicable state is determined
         * @return {@code true} if this action is applicable for the given
         * screen, {@code false} otherwise
         */
        boolean isApplicable(Screen screen);

        /**
         * Executes this {@link Action} for the given screen.
         *
         * @param screen a screen for which this action is executed
         */
        void execute(Screen screen);
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
