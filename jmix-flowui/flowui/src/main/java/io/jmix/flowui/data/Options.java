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

package io.jmix.flowui.data;

import io.jmix.core.common.event.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Options object that can provide items as options for UI components.
 *
 * @param <I> type of option object
 */
public interface Options<I> extends DataUnit {
    Stream<I> getOptions();

    Subscription addOptionsChangeListener(Consumer<OptionsChangeEvent<I>> listener);

    class OptionsChangeEvent<T> extends EventObject {
        public OptionsChangeEvent(Options<T> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Options<T> getSource() {
            return (Options<T>) super.getSource();
        }
    }
}
