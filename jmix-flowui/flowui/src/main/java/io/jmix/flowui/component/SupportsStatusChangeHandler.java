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

package io.jmix.flowui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;

import org.springframework.lang.Nullable;
import java.util.function.Consumer;

/**
 * A component that supports delegation of status changes e.g. instead of displaying
 * validation messages below the component use a separate component such as Label.
 *
 * @param <C> the component type
 */
public interface SupportsStatusChangeHandler<C extends Component> {

    /**
     * Sets a callback to be used to handle component status changes,
     * e.g. validation messages set by {@link HasValidation#setErrorMessage(String)}.
     *
     * @param handler a handler to set
     */
    void setStatusChangeHandler(@Nullable Consumer<StatusContext<C>> handler);

    /**
     * Represents the context of a status change for a specific component.
     * <p>
     * This class encapsulates information about the component and an optional
     * description, typically related to validation or status handling processes.
     *
     * @param <C> the type of the component associated with the status context
     */
    class StatusContext<C extends Component> {

        protected final C component;
        protected final String description;

        public StatusContext(C component, @Nullable String description) {
            this.component = component;
            this.description = description;
        }

        /**
         * Returns the component associated with this instance.
         *
         * @return the component
         */
        public C getComponent() {
            return component;
        }

        /**
         * Returns the description associated with this object.
         *
         * @return the description, or {@code null} if no description is set
         */
        @Nullable
        public String getDescription() {
            return description;
        }
    }
}
