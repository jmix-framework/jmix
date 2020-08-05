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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Base interface for list components.
 *
 * @param <V> value type: single value or {@code Collection<I>}
 * @param <I> item type
 */
public interface SelectList<V, I> extends OptionsField<V, I>, Component.Focusable {

    /**
     * Adds a listener that is fired when user double-clicks on a list item.
     *
     * @param listener a listener to add
     */
    Subscription addDoubleClickListener(Consumer<DoubleClickEvent<I>> listener);

    /**
     * The event sent when the user double-clicks mouse on a list item.
     *
     * @param <I> item type
     */
    class DoubleClickEvent<I> extends EventObject {
        protected I item;

        public DoubleClickEvent(SelectList source, I item) {
            super(source);
            this.item = item;
        }

        @Override
        public SelectList getSource() {
            return (SelectList) super.getSource();
        }

        public I getItem() {
            return item;
        }
    }
}
