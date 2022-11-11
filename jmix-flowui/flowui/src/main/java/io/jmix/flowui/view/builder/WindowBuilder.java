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

import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides a fluent interface to configure and open a view in a {@link DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 */
public class WindowBuilder<V extends View<?>> extends AbstractWindowBuilder<V> implements DialogWindowClassBuilder<V> {

    protected Class<V> viewClass;

    public WindowBuilder(View<?> origin,
                         Class<V> viewClass,
                         Function<? extends WindowBuilder<V>, DialogWindow<V>> handler) {
        super(origin, handler);

        this.viewClass = viewClass;
    }

    public WindowBuilder(View<?> origin,
                         String viewId,
                         Function<? extends WindowBuilder<V>, DialogWindow<V>> handler) {
        super(origin, handler);

        this.viewId = viewId;
    }

    @Override
    public WindowBuilder<V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public WindowBuilder<V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<V>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }
}
