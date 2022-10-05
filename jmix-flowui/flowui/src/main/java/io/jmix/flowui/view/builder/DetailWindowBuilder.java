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
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.DetailView;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.DetailViewMode;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public DetailWindowBuilder<E, V> newEntity() {
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    public DetailWindowBuilder<E, V> newEntity(E entity) {
        this.newEntity = entity;
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    public DetailWindowBuilder<E, V> editEntity(E entity) {
        this.editedEntity = entity;
        this.mode = DetailViewMode.EDIT;
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends View<?> & DetailView<E>> DetailWindowClassBuilder<E, T> withViewClass(Class<T> viewClass) {
        return new DetailWindowClassBuilder(this, viewClass);
    }

    public DetailWindowBuilder<E, V> withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    public DetailWindowBuilder<E, V> withInitializer(@Nullable Consumer<E> initializer) {
        this.initializer = initializer;
        return this;
    }

    public DetailWindowBuilder<E, V> withTransformation(@Nullable Function<E, E> transformation) {
        this.transformation = transformation;
        return this;
    }

    public DetailWindowBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
        return this;
    }

    public DetailWindowBuilder<E, V> withParentDataContext(@Nullable DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
        return this;
    }

    public DetailWindowBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    public DetailWindowBuilder<E, V> withField(@Nullable HasValue<?, E> field) {
        this.field = field;
        return this;
    }

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

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<E> getNewEntity() {
        return Optional.ofNullable(newEntity);
    }

    public Optional<E> getEditedEntity() {
        return Optional.ofNullable(editedEntity);
    }

    public Optional<Consumer<E>> getInitializer() {
        return Optional.ofNullable(initializer);
    }

    public Optional<Function<E, E>> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    public Optional<CollectionContainer<E>> getContainer() {
        return Optional.ofNullable(container);
    }

    public Optional<DataContext> getParentDataContext() {
        return Optional.ofNullable(parentDataContext);
    }

    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    public Optional<HasValue<?, E>> getField() {
        return Optional.ofNullable(field);
    }

    @Nullable
    public Boolean getAddFirst() {
        return addFirst;
    }

    public DetailViewMode getMode() {
        return mode;
    }
}
