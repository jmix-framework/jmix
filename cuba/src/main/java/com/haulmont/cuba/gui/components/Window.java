/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.DialogOptions;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowContext;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.compatibility.*;
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.validation.group.UiCrossFieldChecks;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.component.CloseOriginType;
import io.jmix.ui.component.Component;
import io.jmix.ui.screen.*;
import io.jmix.ui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @deprecated Use {@link io.jmix.ui.component.Window} instead
 */
@Deprecated
public interface Window extends io.jmix.ui.component.Window, Frame {

    @Override
    WindowContext getContext();

    /**
     * Closes the screen.
     * <br> If the window has uncommitted changes in its {@link com.haulmont.cuba.gui.data.DsContext},
     * and force=false, the confirmation dialog will be shown.
     *
     * @param actionId action ID that will be propagated to attached {@link CloseListener}s.
     *                 Use {@link #COMMIT_ACTION_ID} if some changes have just been committed, or
     *                 {@link #CLOSE_ACTION_ID} otherwise. These constants are recognized by various mechanisms of the
     *                 framework.
     * @param force    if true, no confirmation dialog will be shown even if the screen has uncommitted changes
     */
    @Deprecated
    default boolean close(String actionId, boolean force) {
        OperationResult result = getFrameOwner().close(new StandardCloseAction(actionId, !force));
        return result.getStatus() == OperationResult.Status.SUCCESS;
    }

    /**
     * Closes the screen.
     * <br>
     * If the screen has uncommitted changes in its {@link DsContext}, the confirmation dialog will be shown.
     * <br>
     * Don't override this method in subclasses, use hook {@link AbstractWindow#preClose(String)}
     *
     * @param actionId action ID that will be propagated to attached {@link CloseListener}s.
     *                 Use {@link #COMMIT_ACTION_ID} if some changes have just been committed, or
     *                 {@link #CLOSE_ACTION_ID} otherwise. These constants are recognized by various mechanisms of the
     *                 framework.
     */
    @Deprecated
    default boolean close(String actionId) {
        OperationResult result = getFrameOwner().close(new StandardCloseAction(actionId));
        return result.getStatus() == OperationResult.Status.SUCCESS;
    }

    /**
     * @return window manager instance
     * @deprecated Use {@link Screens} and {@link Notifications} instead.
     */
    @Deprecated
    WindowManager getWindowManager();

    /**
     * @return dialog options of window. Options will be applied only if window opened with {@link OpenMode#DIALOG}
     * @deprecated Cast a window instance to {@link io.jmix.ui.component.DialogWindow}
     */
    @Deprecated
    DialogOptions getDialogOptions();

    /**
     * Adds a listener that will be notified when this screen is closed.
     *
     * @param listener listener instance
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead.
     */
    @Deprecated
    default void addListener(CloseListener listener) {
        getFrameOwner().addAfterCloseListener(new AfterCloseListenerAdapter(listener));
    }

    /**
     * Removes a previously registered CloseListener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead.
     */
    @Deprecated
    default void removeListener(CloseListener listener) {
        EventHub eventHub = UiControllerUtils.getEventHub(getFrameOwner());
        eventHub.unsubscribe(Screen.AfterCloseEvent.class, new AfterCloseListenerAdapter(listener));
    }

    /**
     * Addы a listener that will be notified when this screen is closed.
     *
     * @param listener listener instance
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead.
     */
    @Deprecated
    default void addCloseListener(CloseListener listener) {
        getFrameOwner().addAfterCloseListener(new AfterCloseListenerAdapter(listener));
    }

    /**
     * Removes a previously registered CloseListener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead.
     */
    @Deprecated
    default void removeCloseListener(CloseListener listener) {
        EventHub eventHub = UiControllerUtils.getEventHub(getFrameOwner());
        eventHub.unsubscribe(Screen.AfterCloseEvent.class, new AfterCloseListenerAdapter(listener));
    }

    /**
     * Add a listener that will be notified when this screen is closed with actionId {@link #COMMIT_ACTION_ID}.
     *
     * @param listener listener instance
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead.
     */
    @Deprecated
    default void addCloseWithCommitListener(CloseWithCommitListener listener) {
        addCloseListener(new CloseListenerAdapter(listener));
    }

    /**
     * Listener to be notified when a screen is closed.
     *
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead
     */
    @Deprecated
    interface CloseListener {
        /**
         * Called when a screen is closed.
         *
         * @param actionId ID of action caused the screen closing, passed here from {@link Screen#close(CloseAction)}
         *                 methods
         */
        void windowClosed(String actionId);
    }

    /**
     * Listener to be notified when a screen is closed with actionId {@link #COMMIT_ACTION_ID}.
     *
     * @deprecated Use {@link Screen#addAfterCloseListener(Consumer)} instead
     */
    @Deprecated
    interface CloseWithCommitListener {
        /**
         * Called when a screen is closed with actionId {@link #CLOSE_ACTION_ID}.
         */
        void windowClosedWithCommitAction();
    }

    /**
     * Only for compatibility with old screens.
     */
    @Deprecated
    interface Editor<T> extends Window, EditorScreen<T>, io.jmix.ui.component.Window.Committable, LegacyFrame {
        /**
         * Name that is used to register a client type specific screen implementation in
         * {@link UiComponents}
         */
        @Deprecated
        String NAME = "window.editor";

        /**
         * @return currently edited entity instance
         */
        T getItem();

        /**
         * Called by the framework to set an edited entity after creation of all components and datasources, and
         * after <code>init()</code>.
         *
         * @param item entity instance
         */
        void setItem(T item);

        /**
         * Called by the framework to validate the screen components and commit changes.
         *
         * @return true if commit was successful
         */
        boolean commit();

        /**
         * Called by the framework to commit changes with optional validation.
         *
         * @param validate false to avoid validation
         * @return true if commit was successful
         */
        boolean commit(boolean validate);

        /**
         * @return parent datasource if it is set
         */
        @Nullable
        Datasource getParentDs();

        /**
         * This method is called by the framework to set parent datasource to commit into this datasource instead
         * of directly to the database.
         */
        void setParentDs(Datasource parentDs);

        /**
         * @return true if Editor will perform additional validation on {@link io.jmix.ui.component.Window#validateAll()}
         * call using {@link javax.validation.Validator}.
         * @see javax.validation.Validator
         */
        boolean isCrossFieldValidate();

        /**
         * Sets whether cross field validation on {@link io.jmix.ui.component.Window#validateAll()} is enabled.
         * <p>
         * Cross field validation is triggered for item of main datasource with {@link UiCrossFieldChecks} group only
         * (without {@link javax.validation.groups.Default} group) when there are no other validation errors in UI
         * components.
         * <p>
         * Cross field validation is triggered before {@link AbstractWindow#postValidate} hook.
         *
         * @param crossFieldValidate cross field validate flag
         * @see javax.validation.Validator
         */
        void setCrossFieldValidate(boolean crossFieldValidate);
    }

    /**
     * Represents a lookup screen.
     */
    @Deprecated
    interface Lookup<T> extends Window, LookupScreen<T>, LegacyFrame {

        String LOOKUP_ITEM_CLICK_ACTION_ID = "lookupItemClickAction";
        String LOOKUP_ENTER_PRESSED_ACTION_ID = "lookupEnterPressed";

        /**
         * Name that is used to register a client type specific screen implementation in
         * {@link UiComponents}
         */
        String NAME = "window.lookup";

        /**
         * @return component that is used to lookup entity instances
         */
        Component getLookupComponent();

        /**
         * Sets component that is used to lookup entity instances.
         */
        void setLookupComponent(Component lookupComponent);

        /**
         * @return current lookup handler
         */
        @Nullable
        @Deprecated
        default Handler getLookupHandler() {
            Consumer<Collection<T>> selectHandler = getSelectHandler();

            if (!(selectHandler instanceof SelectHandlerAdapter)) {
                return null;
            }

            return ((SelectHandlerAdapter) selectHandler).getHandler();
        }

        /**
         * Sets a lookup handler.
         *
         * @param handler handler implementation
         */
        @Deprecated
        default void setLookupHandler(Handler handler) {
            setSelectHandler(new SelectHandlerAdapter<>(handler));
        }

        /**
         * @return current lookup validator
         */
        @Deprecated
        @Nullable
        default Validator getLookupValidator() {
            Predicate<ValidationContext<T>> selectValidator = getSelectValidator();

            if (!(selectValidator instanceof SelectValidatorAdapter)) {
                return null;
            }

            return ((SelectValidatorAdapter<T>) selectValidator).getValidator();
        }

        /**
         * Sets a lookup validator
         *
         * @param validator validator implementation
         */
        @SuppressWarnings("unchecked")
        @Deprecated
        default void setLookupValidator(Validator validator) {
            setSelectValidator(new SelectValidatorAdapter(validator));
        }

        /**
         * INTERNAL.
         * Invoked by the framework after creating the window to give it a chance to setup a specific layout.
         */
        @Internal
        void initLookupLayout();

        /**
         * Callback interface to receive selected entities.
         * <br> Implementations of this interface must be passed to
         * {@link LegacyFrame#openLookup}
         * methods or set directly in the screen instance via {@link #setLookupHandler}.
         */
        interface Handler {
            /**
             * Called upon selection.
             *
             * @param items selected entity instances
             */
            void handleLookup(Collection items);
        }

        /**
         * Callback interface to validate the lookup screen upon selection before calling
         * {@link Handler#handleLookup(java.util.Collection)} method.
         * <br> Implementations of this interface must be set in the screen instance via {@link #setLookupValidator}.
         */
        interface Validator {
            /**
             * Called upon selection.
             *
             * @return true to proceed with selection, false to interrupt the selection and don't close the screen
             */
            boolean validate();
        }
    }

    /**
     * Marker interface implemented by top-level windows of the application: login window and main window. Only one
     * top-level window exists at a time, depending on the connection state.
     *
     * @deprecated Is not required for screen controllers anymore
     */
    @Deprecated
    interface TopLevelWindow {
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    default void removeBeforeWindowCloseListener(Consumer<BeforeCloseEvent> listener) {
        EventHub eventHub = UiControllerUtils.getEventHub(getFrameOwner());
        eventHub.unsubscribe(BeforeCloseEvent.class, listener);
    }

    /**
     * An event that is fired before a screen is closed with {@link UiScreenProperties#getCloseShortcut()}.
     *
     * @deprecated Use {@link BeforeCloseEvent} with {@link CloseOrigin}
     */
    @Deprecated
    class BeforeCloseWithShortcutEvent extends BeforeCloseEvent {
        /**
         * @param source the window to be closed
         */
        public BeforeCloseWithShortcutEvent(io.jmix.ui.component.Window source) {
            super(source, CloseOriginType.SHORTCUT);
        }
    }

    /**
     * Registers a new before close with shortcut listener.
     *
     * @param listener the listener to register
     */
    @Deprecated
    default void addBeforeCloseWithShortcutListener(Consumer<BeforeCloseWithShortcutEvent> listener) {
        addBeforeWindowCloseListener(new BeforeCloseWithShortcutListenerAdapter(listener));
    }

    /**
     * Removes a previously registered before close with shortcut listener.
     *
     * @param listener the listener to remove
     */
    @Deprecated
    default void removeBeforeCloseWithShortcutListener(Consumer<BeforeCloseWithShortcutEvent> listener) {
        removeBeforeWindowCloseListener(new BeforeCloseWithShortcutListenerAdapter(listener));
    }

    /**
     * An event that is fired before a screen is closed with one of the following approaches:
     * screen's close button, bread crumbs, TabSheet tabs' close actions (Close, Close All, Close Others).
     *
     * @deprecated Use {@link BeforeCloseEvent} with {@link CloseOrigin}
     */
    @Deprecated
    class BeforeCloseWithCloseButtonEvent extends BeforeCloseEvent {
        /**
         * @param source the window to be closed
         */
        public BeforeCloseWithCloseButtonEvent(io.jmix.ui.component.Window source) {
            super(source, CloseOriginType.CLOSE_BUTTON);
        }
    }

    /**
     * Registers a new before close with close button listener.
     *
     * @param listener the listener to register
     */
    @Deprecated
    default void addBeforeCloseWithCloseButtonListener(Consumer<BeforeCloseWithCloseButtonEvent> listener) {
        addBeforeWindowCloseListener(new BeforeCloseWithCloseButtonListenerAdapter(listener));
    }

    /**
     * Removes a previously registered before close with close button listener.
     *
     * @param listener the listener to remove
     */
    @Deprecated
    default void removeBeforeCloseWithCloseButtonListener(Consumer<BeforeCloseWithCloseButtonEvent> listener) {
        removeBeforeWindowCloseListener(new BeforeCloseWithCloseButtonListenerAdapter(listener));
    }

    /**
     * INTERNAL.
     * Interface implemented by screen controllers which are not themselves windows,
     * but has {@link io.jmix.ui.component.Window} interface and delegate work to wrapped real window.
     */
    @Internal
    @Deprecated
    interface Wrapper {
        io.jmix.ui.component.Window getWrappedWindow();
    }

    /**
     * Window having a folders pane.
     */
    @Deprecated
    interface HasFoldersPane {

        /**
         * @return a folders pane component
         */
        @Nullable
        FoldersPane getFoldersPane();
    }
}
