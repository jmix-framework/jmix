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

package io.jmix.flowui.view.navigation;

import io.jmix.flowui.view.View;

import java.util.EventObject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An interface to be implemented by {@link ViewNavigator} that fires {@link AfterViewNavigationEvent}.
 *
 * @param <V> view type
 */
public interface SupportsAfterViewNavigationHandler<V extends View> {

    /**
     * @return a handler that must be called if navigation to a view actually happened
     */
    Optional<Consumer<AfterViewNavigationEvent<V>>> getAfterNavigationHandler();

    class AfterViewNavigationEvent<V> extends EventObject {

        protected final V view;

        public AfterViewNavigationEvent(Object source, V view) {
            super(source);
            this.view = view;
        }

        public V getView() {
            return view;
        }
    }
}
