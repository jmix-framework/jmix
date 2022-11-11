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

package io.jmix.flowui.view.builder;

import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * An interface to be implemented by builders which opens a view in a {@link io.jmix.flowui.view.DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 */
public interface DialogWindowBuilder<V extends View<?>> {

    /**
     * @return invoking view
     */
    View<?> getOrigin();

    /**
     * @return identifier of the opened view as specified in the {@link ViewController} annotation
     */
    Optional<String> getViewId();

    /**
     * @return after open dialog listener
     */
    Optional<Consumer<AfterOpenEvent<V>>> getAfterOpenListener();

    /**
     * @return after close dialog listener
     */
    Optional<Consumer<AfterCloseEvent<V>>> getAfterCloseListener();
}
