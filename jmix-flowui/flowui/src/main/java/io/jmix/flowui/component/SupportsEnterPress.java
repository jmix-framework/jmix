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
import com.vaadin.flow.component.ComponentEvent;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

/**
 * A component that supports Enter key handling.
 */
public interface SupportsEnterPress<C extends Component> {

    /**
     * Sets code to execute when Enter key is pressed.
     *
     * @param handler code to execute when Enter key is pressed
     *                or {@code null} to remove previously set.
     */
    void setEnterPressHandler(@Nullable Consumer<EnterPressEvent<C>> handler);

    /**
     * Event when Enter shortcut is detected.
     *
     * @param <C> the event source type
     */
    class EnterPressEvent<C extends Component> extends ComponentEvent<C> {

        /**
         * Creates a new event using the given source.
         *
         * @param source the source component
         */
        public EnterPressEvent(C source) {
            super(source, true);
        }
    }
}
