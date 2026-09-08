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
import io.jmix.flowui.view.ViewController;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides a fluent interface to configure and open a read view in a {@link DialogWindow}.
 *
 * @param <E> shown entity type
 * @param <V> a view type which is opened in a dialog window
 */
public class ReadWindowBuilder<E, V extends View<?>> extends AbstractWindowBuilder<V> {

    protected final Class<E> entityClass;

    @Nullable
    protected E entity;

    public ReadWindowBuilder(View<?> origin,
                             Class<E> entityClass,
                             Function<? extends ReadWindowBuilder<E, V>, DialogWindow<V>> handler) {
        super(origin, handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    protected ReadWindowBuilder(ReadWindowBuilder<E, V> builder) {
        super(builder.origin, builder.handler);

        this.entity = builder.entity;

        this.entityClass = builder.entityClass;
        this.viewId = builder.viewId;

        this.afterOpenListener = builder.afterOpenListener;
        this.afterCloseListener = builder.afterCloseListener;
        this.draggedListener = builder.draggedListener;
        this.resizeListener = builder.resizeListener;
        this.viewConfigurer = builder.viewConfigurer;
    }

    /**
     * Sets the entity instance to show and returns the builder for chaining.
     *
     * @param entity entity instance to show
     * @return this instance for chaining
     */
    public ReadWindowBuilder<E, V> readEntity(E entity) {
        checkNotNullArgument(entity);

        this.entity = entity;
        return this;
    }

    /**
     * Sets the opened view class.
     *
     * @param viewClass opened view class
     * @param <T>       view type
     * @return {@link ReadWindowClassBuilder} instance for chaining
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends View<?> & ReadView<E>> ReadWindowClassBuilder<E, T> withViewClass(Class<T> viewClass) {
        return new ReadWindowClassBuilder(this, viewClass);
    }

    /**
     * Sets identifier of the opened view as specified in the {@link ViewController} annotation.
     *
     * @param viewId identifier of the opened view as specified in the {@link ViewController} annotation
     * @return this instance for chaining
     */
    public ReadWindowBuilder<E, V> withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    @Override
    public ReadWindowBuilder<E, V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public ReadWindowBuilder<E, V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public ReadWindowBuilder<E, V> withDraggedListener(
            @Nullable ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
        super.withDraggedListener(listener);
        return this;
    }

    @Override
    public ReadWindowBuilder<E, V> withResizeListener(
            @Nullable ComponentEventListener<Dialog.DialogResizeEvent> listener) {
        super.withResizeListener(listener);
        return this;
    }

    @Override
    public ReadWindowBuilder<E, V> withViewConfigurer(@Nullable Consumer<V> configurer) {
        super.withViewConfigurer(configurer);
        return this;
    }

    /**
     * @return the class of the shown entity
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * @return the entity instance to show, or an empty {@link Optional} if it is not set
     */
    public Optional<E> getEntity() {
        return Optional.ofNullable(entity);
    }
}
