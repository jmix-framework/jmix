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

import com.vaadin.flow.component.HasValue;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.Nested;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides a fluent interface to configure and open a details view
 * in a {@link DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 */
public class DetailWindowBuilder<E, V extends View<?>> extends AbstractWindowBuilder<V> {

    protected final Class<E> entityClass;

    protected E newEntity;
    protected E editedEntity;

    protected Consumer<E> initializer;
    protected Function<E, E> transformation;
    protected CollectionContainer<E> container;

    protected DataContext parentDataContext;

    protected ListDataComponent<E> listDataComponent;
    protected HasValue<?, E> field;

    protected Boolean addFirst;

    protected DetailViewMode mode = DetailViewMode.CREATE;

    protected DetailWindowBuilder(DetailWindowBuilder<E, V> builder) {
        super(builder.origin, builder.handler);

        this.entityClass = builder.entityClass;

        this.viewId = builder.viewId;

        this.newEntity = builder.newEntity;
        this.editedEntity = builder.editedEntity;
        this.mode = builder.mode;

        this.initializer = builder.initializer;
        this.transformation = builder.transformation;
        this.container = builder.container;
        this.parentDataContext = builder.parentDataContext;

        this.listDataComponent = builder.listDataComponent;
        this.field = builder.field;

        this.addFirst = builder.addFirst;

        this.afterOpenListener = builder.afterOpenListener;
        this.afterCloseListener = builder.afterCloseListener;
    }

    public DetailWindowBuilder(View<?> origin,
                               Class<E> entityClass,
                               Function<? extends DetailWindowBuilder<E, V>, DialogWindow<V>> handler) {
        super(origin, handler);

        this.entityClass = entityClass;
    }

    /**
     * Sets {@link DetailViewMode} to {@code CREATE} and the builder for chaining.
     * <p>
     * A new entity instance will be created automatically. It can be initialized
     * by code passed to the {@link #withInitializer(Consumer)} method.
     *
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> newEntity() {
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    /**
     * Sets {@link DetailViewMode} to {@code CREATE} and the builder for chaining.
     * <p>
     * The new entity instance is accepted as the parameter. It can be initialized
     * by code passed to the {@link #withInitializer(Consumer)} method.
     *
     * @param entity new entity instance to be passed to the detail view
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> newEntity(E entity) {
        this.newEntity = entity;
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    /**
     * Sets {@link DetailViewMode} to {@code EDIT} and the builder for chaining.
     *
     * @param entity entity instance to be passed to the detail view
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> editEntity(E entity) {
        this.editedEntity = entity;
        this.mode = DetailViewMode.EDIT;
        return this;
    }

    /**
     * Sets opened view class.
     *
     * @param viewClass opened view class
     * @param <T>       view type
     * @return {@link DetailWindowClassBuilder} instance for chaining
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends View<?> & DetailView<E>> DetailWindowClassBuilder<E, T> withViewClass(Class<T> viewClass) {
        return new DetailWindowClassBuilder(this, viewClass);
    }

    /**
     * Sets identifier of the opened view as specified in the {@link ViewController} annotation.
     *
     * @param viewId identifier of the opened view as specified in the {@link ViewController} annotation
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    /**
     * Sets callback to initialize a new entity instance.
     * <p>
     * The initializer is invoked only when {@link DetailViewMode} is {@code CREATE},
     * i.e. when {@link #newEntity()} or {@link #newEntity(Object)} methods are invoked on
     * the builder.
     *
     * @param initializer callback to initialize a new entity instance.
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withInitializer(@Nullable Consumer<E> initializer) {
        this.initializer = initializer;
        return this;
    }

    /**
     * Sets code to transform the edited entity after detail view saved.
     * <p>
     * Applied only if either field or container or listDataComponent is assigned.
     *
     * @param transformation edited entity transformation object
     * @return this instance for chaining
     * @see #withContainer(CollectionContainer)
     * @see #withField(HasValue)
     * @see #withListDataComponent(ListDataComponent)
     */
    public DetailWindowBuilder<E, V> withTransformation(@Nullable Function<E, E> transformation) {
        this.transformation = transformation;
        return this;
    }

    /**
     * Sets {@link CollectionContainer} to update after the detail view is saved.
     * <p>
     * If the container is {@link Nested}, the framework automatically initializes
     * the reference to the parent entity and sets up data contexts for editing
     * compositions.
     *
     * @param container the container to update after the detail view is saved.
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
        return this;
    }

    /**
     * Sets parent {@link DataContext} for the detail view.
     * <p>
     * The view will save data to the parent context instead of directly to {@link DataManager}.
     *
     * @param parentDataContext parent data context to set
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withParentDataContext(@Nullable DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
        return this;
    }

    /**
     * Sets list data component that is used to get the {@code container} if it is
     * not set explicitly by {@link #withContainer(CollectionContainer)} method.
     * <p>
     * Usually, the list component is a {@link DataGrid} displaying the list of
     * entities.
     *
     * @param listDataComponent the component to set
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    /**
     * Sets the field in which the framework sets the saved entity after successful
     * detail view saving.
     *
     * @param field the field to set
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withField(@Nullable HasValue<?, E> field) {
        this.field = field;
        return this;
    }

    /**
     * Defines whether a new item will be added to the beginning or to the end
     * of collection. Affects only standalone containers, for nested containers
     * new items are always added to the end.
     *
     * @param addFirst whether a new item will be added to the beginning of collection
     * @return this instance for chaining
     */
    public DetailWindowBuilder<E, V> withAddFirst(@Nullable Boolean addFirst) {
        this.addFirst = addFirst;
        return this;
    }

    @Override
    public DetailWindowBuilder<E, V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public DetailWindowBuilder<E, V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    /**
     * @return entity class
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * @return new entity set by {@link #newEntity(Object)}.
     */
    public Optional<E> getNewEntity() {
        return Optional.ofNullable(newEntity);
    }

    /**
     * @return entity set by {@link #editEntity(Object)}.
     */
    public Optional<E> getEditedEntity() {
        return Optional.ofNullable(editedEntity);
    }

    /**
     * @return initializer set by {@link #withInitializer(Consumer)}.
     */
    public Optional<Consumer<E>> getInitializer() {
        return Optional.ofNullable(initializer);
    }

    /**
     * @return transformation set by {@link #withTransformation(Function)}.
     */
    public Optional<Function<E, E>> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    /**
     * @return container set by {@link #withContainer(CollectionContainer)}.
     */
    public Optional<CollectionContainer<E>> getContainer() {
        return Optional.ofNullable(container);
    }

    /**
     * @return parent data context set by {@link #withParentDataContext(DataContext)}.
     */
    public Optional<DataContext> getParentDataContext() {
        return Optional.ofNullable(parentDataContext);
    }

    /**
     * @return list data component set by {@link #withListDataComponent(ListDataComponent)}.
     */
    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    /**
     * @return field set by {@link #withField(HasValue)}.
     */
    public Optional<HasValue<?, E>> getField() {
        return Optional.ofNullable(field);
    }

    /**
     * @return whether a new item will be added to the beginning of collection
     */
    @Nullable
    public Boolean getAddFirst() {
        return addFirst;
    }

    /**
     * @return builder mode derived from previous calls to {@link #newEntity()} or {@link #editEntity(Object)}
     */
    public DetailViewMode getMode() {
        return mode;
    }
}
