/*
 * Copyright 2025 Haulmont.
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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;

/**
 * Interface for views that support read-only state change tracking.
 */
public interface ReadOnlyTracker extends ReadOnlyAwareView {

    /**
     * Add a listener to {@link ReadOnlyTracker.ReadOnlyChangeEvent}.
     *
     * @param listener listener
     * @return registration object for removing the listener
     */
    Registration addReadOnlyStateChangeListener(ComponentEventListener<ReadOnlyChangeEvent> listener);

    /**
     * The event is dispatched when a view changes its read-only state.
     * Use this event listener to perform additional actions on view components to change their read-only state.
     * <p>
     * For example:
     * <pre>{@code
     *     @Subscribe
     *     public void onReadOnlyChangeEvent(ReadOnlyChangeEvent event) {
     *          myComponent.setReadOnly(event.isReadOnly());
     *     }
     * }</pre>
     */
    class ReadOnlyChangeEvent extends ComponentEvent<View<?>> {

        protected boolean readOnly;

        public ReadOnlyChangeEvent(View<?> source, boolean readOnly) {
            super(source, false);
            this.readOnly = readOnly;
        }

        /**
         * @return {@code true} if the view is read-only, {@code false} otherwise
         */
        public boolean isReadOnly() {
            return readOnly;
        }
    }
}
