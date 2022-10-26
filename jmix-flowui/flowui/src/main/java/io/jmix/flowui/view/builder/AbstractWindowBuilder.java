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

public class AbstractWindowBuilder<V extends View<?>> implements DialogWindowBuilder<V> {

    protected final View<?> origin;
    protected final Function<AbstractWindowBuilder<V>, DialogWindow<V>> handler;

    protected String viewId;

    protected Consumer<AfterOpenEvent<V>> afterOpenListener;
    protected Consumer<AfterCloseEvent<V>> afterCloseListener;

    protected AbstractWindowBuilder(View<?> origin,
                                    Function<? extends AbstractWindowBuilder<V>, DialogWindow<V>> handler) {
        this.origin = origin;
        //noinspection unchecked
        this.handler = (Function<AbstractWindowBuilder<V>, DialogWindow<V>>) handler;
    }

    /**
     * Adds {@link AfterOpenEvent} listener to the dialog window.
     *
     * @param listener the listener to add
     * @return this instance for chaining
     */
    public AbstractWindowBuilder<V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        this.afterOpenListener = listener;
        return this;
    }

    /**
     * Adds {@link AfterCloseEvent} listener to the dialog window.
     *
     * @param listener the listener to add
     * @return this instance for chaining
     */
    public AbstractWindowBuilder<V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        this.afterCloseListener = listener;
        return this;
    }

    @Override
    public View<?> getOrigin() {
        return origin;
    }

    @Override
    public Optional<String> getViewId() {
        return Optional.ofNullable(viewId);
    }

    @Override
    public Optional<Consumer<AfterOpenEvent<V>>> getAfterOpenListener() {
        return Optional.ofNullable(afterOpenListener);
    }

    @Override
    public Optional<Consumer<AfterCloseEvent<V>>> getAfterCloseListener() {
        return Optional.ofNullable(afterCloseListener);
    }

    /**
     * Builds the dialog window. Dialog window should be shown using {@link DialogWindow#open()}.
     *
     * @return built dialog window
     */
    public DialogWindow<V> build() {
        return handler.apply(this);
    }

    /**
     * Opens built dialog window.
     *
     * @return built dialog window
     */
    public DialogWindow<V> open() {
        DialogWindow<V> dialogWindow = build();
        dialogWindow.open();
        return dialogWindow;
    }
}
