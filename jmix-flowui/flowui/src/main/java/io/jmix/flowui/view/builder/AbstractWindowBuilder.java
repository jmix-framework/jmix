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


import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A generic abstract builder class for constructing dialog windows with specific configurations and listeners.
 *
 * @param <V> the type of the view associated with the dialog window
 */
public class AbstractWindowBuilder<V extends View<?>> implements DialogWindowBuilder<V> {

    protected final View<?> origin;
    protected final Function<AbstractWindowBuilder<V>, DialogWindow<V>> handler;

    protected String viewId;

    protected Consumer<AfterOpenEvent<V>> afterOpenListener;
    protected Consumer<AfterCloseEvent<V>> afterCloseListener;
    protected ComponentEventListener<Dialog.DialogDraggedEvent> draggedListener;
    protected ComponentEventListener<Dialog.DialogResizeEvent> resizeListener;
    protected Consumer<V> viewConfigurer;

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

    /**
     * Adds {@link Dialog.DialogDraggedEvent} listener to the dialog window.
     *
     * @param listener the listener to add
     * @return this instance for chaining
     */
    public AbstractWindowBuilder<V> withDraggedListener(@Nullable ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
        this.draggedListener = listener;
        return this;
    }

    /**
     * Adds {@link Dialog.DialogResizeEvent} listener to the dialog window.
     *
     * @param listener the listener to add
     * @return this instance for chaining
     */
    public AbstractWindowBuilder<V> withResizeListener(@Nullable ComponentEventListener<Dialog.DialogResizeEvent> listener) {
        this.resizeListener = listener;
        return this;
    }

    /**
     * Adds configurer to the dialog window.
     *
     * @param configurer the configurer to add
     * @return the instance for chaining
     */
    public AbstractWindowBuilder<V> withViewConfigurer(@Nullable Consumer<V> configurer) {
        this.viewConfigurer = configurer;
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

    @Override
    public Optional<ComponentEventListener<Dialog.DialogDraggedEvent>> getDraggedListener() {
        return Optional.ofNullable(draggedListener);
    }

    @Override
    public Optional<ComponentEventListener<Dialog.DialogResizeEvent>> getResizeListener() {
        return Optional.ofNullable(resizeListener);
    }

    @Override
    public Optional<Consumer<V>> getViewConfigurer() {
        return Optional.ofNullable(viewConfigurer);
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
