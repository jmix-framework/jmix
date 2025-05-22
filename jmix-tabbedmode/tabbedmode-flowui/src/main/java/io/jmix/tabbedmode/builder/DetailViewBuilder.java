/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.builder;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.DetailViewMode;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class DetailViewBuilder<E, V extends View<?>> extends AbstractViewBuilder<V, DetailViewBuilder<E, V>> {

    protected final Class<E> entityClass;

    protected E entity;

    protected Consumer<E> initializer;
    protected Function<E, E> transformation;

    protected CollectionContainer<E> container;

    protected DataContext parentDataContext;

    protected ListDataComponent<E> listDataComponent;
    protected HasValue<?, E> field;

    protected Boolean addFirst;

    protected DetailViewMode mode = DetailViewMode.CREATE;

    public DetailViewBuilder(View<?> origin,
                             Class<E> entityClass,
                             Function<DetailViewBuilder<E, V>, V> buildHandler,
                             Consumer<ViewOpeningContext> openHandler) {
        super(origin, buildHandler, openHandler);

        this.entityClass = entityClass;
    }

    public DetailViewBuilder(View<?> origin,
                             Class<E> entityClass,
                             String viewId,
                             Function<DetailViewBuilder<E, V>, V> buildHandler,
                             Consumer<ViewOpeningContext> openHandler) {
        this(origin, entityClass, buildHandler, openHandler);

        this.viewId = viewId;
    }

    public DetailViewBuilder(View<?> origin,
                             Class<E> entityClass,
                             Class<V> viewClass,
                             Function<DetailViewBuilder<E, V>, V> buildHandler,
                             Consumer<ViewOpeningContext> openHandler) {
        this(origin, entityClass, buildHandler, openHandler);

        this.viewClass = viewClass;
    }

    public DetailViewBuilder<E, V> newEntity() {
        this.entity = null;
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    public DetailViewBuilder<E, V> newEntity(E entity) {
        this.entity = entity;
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    public DetailViewBuilder<E, V> editEntity(E entity) {
        this.entity = entity;
        this.mode = DetailViewMode.EDIT;
        return this;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<E> getEntity() {
        return Optional.ofNullable(entity);
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

    public DetailViewBuilder<E, V> withInitializer(@Nullable Consumer<E> initializer) {
        this.initializer = initializer;
        return this;
    }

    public DetailViewBuilder<E, V> withTransformation(@Nullable Function<E, E> transformation) {
        this.transformation = transformation;
        return this;
    }

    public DetailViewBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
        return this;
    }

    public DetailViewBuilder<E, V> withParentDataContext(@Nullable DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
        return this;
    }

    public DetailViewBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    public DetailViewBuilder<E, V> withField(@Nullable HasValue<?, E> field) {
        this.field = field;
        return this;
    }

    public DetailViewBuilder<E, V> withAddFirst(@Nullable Boolean addFirst) {
        this.addFirst = addFirst;
        return this;
    }
}
