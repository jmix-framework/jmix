/*
 * Copyright 2026 Haulmont.
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
import io.jmix.flowui.view.ReadView;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides a fluent interface to configure and open a read view with the specific class
 * in a {@link DialogWindow}.
 *
 * @param <E> shown entity type
 * @param <V> a view type which is opened in a dialog window
 */
@NullMarked
public class ReadWindowClassBuilder<E, V extends View<?> & ReadView<E>> extends ReadWindowBuilder<E, V>
        implements DialogWindowClassBuilder<V> {

    protected Class<V> viewClass;

    protected ReadWindowClassBuilder(ReadWindowBuilder<E, V> builder, Class<V> viewClass) {
        super(builder);

        this.viewClass = viewClass;
    }

    public ReadWindowClassBuilder(View<?> origin,
                                  Class<E> entityClass,
                                  Class<V> viewClass,
                                  Function<? extends ReadWindowClassBuilder<E, V>, DialogWindow<V>> handler) {
        super(origin, entityClass, handler);

        this.viewClass = viewClass;
    }

    @Override
    public ReadWindowClassBuilder<E, V> readEntity(E entity) {
        super.readEntity(entity);
        return this;
    }

    public ReadWindowClassBuilder<E, V> withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    @Override
    public ReadWindowClassBuilder<E, V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public ReadWindowClassBuilder<E, V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public ReadWindowClassBuilder<E, V> withDraggedListener(
            @Nullable ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
        super.withDraggedListener(listener);
        return this;
    }

    @Override
    public ReadWindowClassBuilder<E, V> withResizeListener(
            @Nullable ComponentEventListener<Dialog.DialogResizeEvent> listener) {
        super.withResizeListener(listener);
        return this;
    }

    @Override
    public ReadWindowClassBuilder<E, V> withViewConfigurer(@Nullable Consumer<V> configurer) {
        super.withViewConfigurer(configurer);
        return this;
    }

    @Override
    public Optional<Class<V>> getViewClass() {
        return Optional.of(viewClass);
    }
}
