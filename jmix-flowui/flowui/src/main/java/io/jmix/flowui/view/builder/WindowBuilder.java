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

public class WindowBuilder<S extends View<?>> extends AbstractWindowBuilder<S> implements DialogWindowClassBuilder<S> {

    protected Class<S> viewClass;

    public WindowBuilder(View<?> origin,
                         Class<S> viewClass,
                         Function<? extends WindowBuilder<S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.viewClass = viewClass;
    }

    public WindowBuilder(View<?> origin,
                         String viewId,
                         Function<? extends WindowBuilder<S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.viewId = viewId;
    }

    @Override
    public WindowBuilder<S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public WindowBuilder<S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<S>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }
}
