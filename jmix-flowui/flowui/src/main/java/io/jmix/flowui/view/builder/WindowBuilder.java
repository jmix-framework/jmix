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
 * Provides a fluent interface to configure and open a view in a {@link DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 */
public class WindowBuilder<V extends View<?>> extends AbstractWindowBuilder<V> implements DialogWindowClassBuilder<V> {

    protected Class<V> viewClass;

    protected WindowBuilder(WindowBuilder<V> builder, Class<V> viewClass) {
        super(builder.origin, builder.handler);

        this.viewId = builder.viewId;

        this.afterOpenListener = builder.afterOpenListener;
        this.afterCloseListener = builder.afterCloseListener;
        this.draggedListener = builder.draggedListener;
        this.resizeListener = builder.resizeListener;

        this.viewClass = viewClass;
    }

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
    public WindowBuilder<V> withDraggedListener(@Nullable ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
        super.withDraggedListener(listener);
        return this;
    }

    @Override
    public WindowBuilder<V> withResizeListener(@Nullable ComponentEventListener<Dialog.DialogResizeEvent> listener) {
        super.withResizeListener(listener);
        return this;
    }

    @Override
    public WindowBuilder<V> withViewConfigurer(@Nullable Consumer<V> configurer) {
        super.withViewConfigurer(configurer);
        return this;
    }

    /**
     * Sets the view identifier to be opened.
     *
     * @param viewId the identifier of the view to be opened
     * @return this instance for chaining
     */
    public WindowBuilder<V> withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    /**
     * Configures the window builder with the specified view class.
     *
     * @param <T>       the type of the view that extends {@code View<?>}
     * @param viewClass the class of the view to be opened
     * @return a new {@code WindowBuilder} instance configured with the specified view class
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends View<?>> WindowBuilder<T> withViewClass(Class<T> viewClass) {
        return new WindowBuilder(this, viewClass);
    }

    @Override
    public Optional<Class<V>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }
}
