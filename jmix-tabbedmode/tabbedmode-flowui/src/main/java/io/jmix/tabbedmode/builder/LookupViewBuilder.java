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
import io.jmix.flowui.view.LookupView.ValidationContext;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class LookupViewBuilder<E, V extends View<?>> extends AbstractViewBuilder<V, LookupViewBuilder<E, V>> {

    protected final Class<E> entityClass;

    protected Consumer<Collection<E>> selectHandler;
    protected Predicate<ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    protected CollectionContainer<E> container;

    protected ListDataComponent<E> listDataComponent;
    protected HasValue<?, ?> field;

    protected boolean lookupComponentMultiSelect;

    protected boolean fieldCollectionValue = false;

    public LookupViewBuilder(View<?> origin,
                             Class<E> entityClass,
                             Function<LookupViewBuilder<E, V>, V> buildHandler,
                             Consumer<ViewOpeningContext> openHandler) {
        super(origin, buildHandler, openHandler);

        this.entityClass = entityClass;
    }

    public LookupViewBuilder(View<?> origin,
                             Class<E> entityClass,
                             String viewId,
                             Function<LookupViewBuilder<E, V>, V> buildHandler,
                             Consumer<ViewOpeningContext> openHandler) {
        this(origin, entityClass, buildHandler, openHandler);

        this.viewId = viewId;
    }

    public LookupViewBuilder(View<?> origin,
                             Class<E> entityClass,
                             Class<V> viewClass,
                             Function<LookupViewBuilder<E, V>, V> buildHandler,
                             Consumer<ViewOpeningContext> openHandler) {
        this(origin, entityClass, buildHandler, openHandler);

        this.viewClass = viewClass;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<Consumer<Collection<E>>> getSelectHandler() {
        return Optional.ofNullable(selectHandler);
    }

    public Optional<Predicate<ValidationContext<E>>> getSelectValidator() {
        return Optional.ofNullable(selectValidator);
    }

    public Optional<Function<Collection<E>, Collection<E>>> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    public Optional<CollectionContainer<E>> getContainer() {
        return Optional.ofNullable(container);
    }

    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    public Optional<HasValue<?, ?>> getField() {
        return Optional.ofNullable(field);
    }

    public boolean isLookupComponentMultiSelect() {
        return lookupComponentMultiSelect;
    }

    public boolean isFieldCollectionValue() {
        return fieldCollectionValue;
    }

    public LookupViewBuilder<E, V> withSelectHandler(@Nullable Consumer<Collection<E>> selectHandler) {
        this.selectHandler = selectHandler;
        return this;
    }

    public LookupViewBuilder<E, V> withSelectValidator(@Nullable Predicate<ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
        return this;
    }

    public LookupViewBuilder<E, V> withTransformation(@Nullable Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
        return this;
    }

    public LookupViewBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
        return this;
    }

    public LookupViewBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    public <T extends HasValue<?, E>> LookupViewBuilder<E, V> withField(@Nullable T field) {
        this.field = field;
        this.fieldCollectionValue = false;
        return this;
    }

    public <T extends HasValue<?, Collection<E>>> LookupViewBuilder<E, V> withMultiValueField(@Nullable T field) {
        this.field = field;
        this.fieldCollectionValue = true;
        return this;
    }

    public LookupViewBuilder<E, V> withLookupComponentMultiSelect(boolean lookupComponentMultiSelect) {
        this.lookupComponentMultiSelect = lookupComponentMultiSelect;
        return this;
    }
}
