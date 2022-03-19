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
package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.screen.CloseAction;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Screen.AfterCloseEvent;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents an independent window of application.
 */
public interface Window extends Frame, Component.HasCaption, Component.HasIcon {
    /**
     * Constant that should be passed to {@link Screen#close(CloseAction)} methods when the screen is closed after
     * commit of changes. Propagated to {@link AfterCloseEvent} listeners.
     */
    String COMMIT_ACTION_ID = "commit";

    /**
     * Constant that should be passed to {@link Screen#close(CloseAction)} methods when the screen is closed without
     * commit. Propagated to {@link AfterCloseEvent} listeners.
     */
    String CLOSE_ACTION_ID = "close";

    /**
     * Constant that passed to {@link Screen#close(CloseAction)} method when the lookup screen is closed with selected
     * items. Propagated to {@link AfterCloseEvent} listeners.
     */
    String SELECT_ACTION_ID = "select";

    String BROWSE_WINDOW_SUFFIX = ".browse";

    String LOOKUP_WINDOW_SUFFIX = ".lookup";

    String EDITOR_WINDOW_SUFFIX = ".edit";

    String CREATE_WINDOW_SUFFIX = ".create";

    /**
     * Sets the closable status for the window.
     *
     * @param closeable closeable flag
     */
    void setCloseable(boolean closeable);

    /**
     * @return true if the window can be closed by user with close button or keyboard shortcut
     */
    boolean isCloseable();

    /**
     * Sets minimum CSS width for window layout. Examples: "640px", "auto".
     *
     * @param minWidth minimum width
     */
    void setMinWidth(String minWidth);

    /**
     * @return previously set minimal CSS width or null
     */
    @Nullable
    String getMinWidth();

    /**
     * Sets maximum CSS width for window layout. Examples: "640px", "100%".
     *
     * @param maxWidth maximum width
     */
    void setMaxWidth(String maxWidth);

    /**
     * @return previously set maximum CSS width or null
     */
    @Nullable
    String getMaxWidth();

    /**
     * Sets minimum CSS height for window layout. Examples: "640px", "auto".
     *
     * @param minHeight minimum height
     */
    void setMinHeight(String minHeight);

    /**
     * @return previously set minimum CSS height or null
     */
    @Nullable
    String getMinHeight();

    /**
     * Sets maximum CSS height for window layout. Examples: "640px", "100%".
     *
     * @param maxHeight maximum height
     */
    void setMaxHeight(String maxHeight);

    /**
     * @return previously set maximum CSS height or null
     */
    @Nullable
    String getMaxHeight();

    /**
     * @return UI controller of the window
     */
    @Override
    Screen getFrameOwner();

    /**
     * @return current window context
     */
    @Override
    WindowContext getContext();

    /**
     * Sets a component to be focused after the screen is opened.
     *
     * @param componentId component's ID in XML. If null, then first focusable component will be focused
     */
    void setFocusComponent(@Nullable String componentId);

    /**
     * @return an ID of the component which is set to be focused after the screen is opened
     */
    @Nullable
    String getFocusComponent();

    /**
     * Checks validity by invoking validators on specified components which support them
     * and show validation result notification.
     *
     * @return true if the validation was successful, false if there were any problems
     */
    boolean validate(List<Validatable> fields);

    /**
     * Checks validity by invoking validators on all components which support them
     * and show validation result notification.
     *
     * @return true if the validation was successful, false if there were any problems
     */
    boolean validateAll();

    /**
     * Registers a new before window close listener.
     *
     * @param listener the listener to register
     * @return a registration object for removing an event listener added to a window
     */
    Subscription addBeforeWindowCloseListener(Consumer<BeforeCloseEvent> listener);

    /**
     * Defines how the managed main TabSheet switches a tab with the given window: hides or unloads its content.
     */
    enum ContentSwitchMode {
        /**
         * Tab switching is determined by the managed main TabSheet mode (hide or unload content of a tab).
         */
        DEFAULT,
        /**
         * Tab content should be hidden not considering the TabSheet mode.
         */
        HIDE,
        /**
         * Tab content should be unloaded not considering the TabSheet mode.
         */
        UNLOAD
    }

    /**
     * Represents a window that can be committed on close.
     * <br>
     * Implement this interface in controller if you want to support saving uncommitted changes on window close.
     */
    interface Committable {

        /**
         * @return whether the window contains uncommitted changes
         */
        boolean isModified();

        /**
         * Commit changes and close the window.
         */
        void commitAndClose();
    }

    /**
     * Window having a work area.
     */
    interface HasWorkArea {

        /**
         * @return a work area component
         */
        @Nullable
        AppWorkArea getWorkArea();
    }

    /**
     * Window having a user indicator.
     */
    interface HasUserIndicator {

        /**
         * @return a user indicator component
         */
        @Nullable
        UserIndicator getUserIndicator();
    }

    /**
     * Marker interface for all window close types, which describes the way a window was closed.
     *
     * @see CloseOriginType
     */
    interface CloseOrigin {
    }

    /**
     * Event sent right before the window is closed by an external (relative to the window content) action,
     * like the button in the window tab or by the Esc keyboard shortcut.
     * <p>
     * The way the window is closing can be obtained via {@link #getCloseOrigin()}. Closing can be prevented by
     * invoking {@link #preventWindowClose()}, for example:
     * For example:
     * <pre>
     *     &#64;Subscribe(target = Target.FRAME)
     *     protected void onBeforeCloseFrame(Window.BeforeCloseEvent event) {
     *         if (event.getCloseOrigin() == CloseOriginType.BREADCRUMBS) {
     *             event.preventWindowClose();
     *         }
     *     }
     * </pre>
     *
     * @see CloseOriginType
     */
    class BeforeCloseEvent extends EventObject {
        protected boolean closePrevented = false;
        protected CloseOrigin closeOrigin;

        /**
         * @param source the window to be closed
         */
        public BeforeCloseEvent(Window source, CloseOrigin closeOrigin) {
            super(source);
            this.closeOrigin = closeOrigin;
        }

        @Override
        public Window getSource() {
            return (Window) super.getSource();
        }

        /**
         * @return value that describes the event type: close by shortcut / using close button / from breadcrumbs
         * @see CloseOriginType
         */
        public CloseOrigin getCloseOrigin() {
            return closeOrigin;
        }

        /**
         * Sets closePrevented flag to true and therefore prevents window close.
         */
        public void preventWindowClose() {
            this.closePrevented = true;
        }

        /**
         * @return true if at least one event handler called {@link #preventWindowClose()} and window will not be closed
         */
        public boolean isClosePrevented() {
            return closePrevented;
        }
    }
}
