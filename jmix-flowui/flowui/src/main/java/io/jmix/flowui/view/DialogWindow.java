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

package io.jmix.flowui.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.view.View.BeforeShowEvent;
import io.jmix.flowui.view.View.ReadyEvent;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Wrapper class representing a {@link View} opened as a dialog window.
 *
 * @param <V> a view type
 */
public class DialogWindow<V extends View<?>> extends AbstractDialogWindow<V> {

    private boolean readyEventFired = false;

    public DialogWindow(V view) {
        super(view);
    }

    @Override
    protected void initView(View<?> view) {
        super.initView(view);

        view.addAfterCloseListener(this::onViewAfterClosed);
    }

    @Override
    protected void initDialog(Dialog dialog) {
        super.initDialog(dialog);

        dialog.addOpenedChangeListener(this::onDialogOpenedChanged);
    }

    protected void onDialogOpenedChanged(Dialog.OpenedChangeEvent openedChangeEvent) {
        if (openedChangeEvent.isOpened() && !readyEventFired) {
            fireViewReadyEvent(view);
            fireViewOpenedEvent(view);

            AfterOpenEvent<V> event = new AfterOpenEvent<>(this);
            publish(AfterOpenEvent.class, event);

            // temporal workaround for https://github.com/vaadin/flow-components/issues/5103
            readyEventFired = true;
        }
    }

    /**
     * Opens the dialog.
     */
    public void open() {
        fireViewBeforeShowEvent(view);

        super.open();
    }

    protected void onViewAfterClosed(View.AfterCloseEvent closeEvent) {
        fireViewAfterCloseEvent(closeEvent);
    }

    protected void fireViewAfterCloseEvent(View.AfterCloseEvent closeEvent) {
        AfterCloseEvent<V> event = new AfterCloseEvent<>(this, closeEvent.getCloseAction());
        publish(AfterCloseEvent.class, event);
    }

    protected void fireViewBeforeShowEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new BeforeShowEvent(view));
    }

    protected void fireViewOpenedEvent(View<?> view) {
        ViewOpenedEvent viewOpenedEvent = new ViewOpenedEvent(view);
        applicationContext.publishEvent(viewOpenedEvent);
    }

    protected void fireViewReadyEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new ReadyEvent(view));
    }

    /**
     * Adds {@link AfterOpenEvent} listener.
     *
     * @param listener the listener to add
     * @return a Registration for removing the event listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addAfterOpenListener(Consumer<AfterOpenEvent<V>> listener) {
        Subscription subscription = getEventHub().subscribe(AfterOpenEvent.class, ((Consumer) listener));
        return Registration.once(subscription::remove);
    }

    /**
     * Adds {@link AfterCloseEvent} listener.
     *
     * @param listener the listener to add
     * @return a Registration for removing the event listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addAfterCloseListener(Consumer<AfterCloseEvent<V>> listener) {
        Subscription subscription = getEventHub().subscribe(AfterCloseEvent.class, ((Consumer) listener));
        return Registration.once(subscription::remove);
    }

    /**
     * This class represents an event that is triggered after the associated dialog window has been opened.
     *
     * @param <V> the type of the view associated with the dialog window
     */
    public static class AfterOpenEvent<V extends View<?>> extends EventObject {

        public AfterOpenEvent(DialogWindow<V> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DialogWindow<V> getSource() {
            return (DialogWindow<V>) super.getSource();
        }

        /**
         * Returns the view associated with the dialog window.
         *
         * @return the view associated with the dialog window
         */
        public V getView() {
            return getSource().getView();
        }
    }

    /**
     * Represents an event that is fired after a dialog window has been closed.
     *
     * @param <V> the type of the view associated with the dialog window
     */
    public static class AfterCloseEvent<V extends View<?>> extends EventObject {

        protected final CloseAction closeAction;

        public AfterCloseEvent(DialogWindow<V> source, CloseAction closeAction) {
            super(source);

            this.closeAction = closeAction;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DialogWindow<V> getSource() {
            return (DialogWindow<V>) super.getSource();
        }

        /**
         * Returns the view associated with the dialog window.
         *
         * @return the view associated with the dialog window
         */
        public V getView() {
            return getSource().getView();
        }

        /**
         * Returns the {@link CloseAction} that describes how the associated {@link View} was closed.
         *
         * @return the close action associated with the closure of the dialog window
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Checks if the dialog window was closed with the given {@link StandardOutcome}.
         *
         * @param outcome the {@link StandardOutcome} to check against
         * @return {@code true} if the dialog window was closed with the
         * specified {@link StandardOutcome}, {@code false} otherwise
         */
        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }
}
