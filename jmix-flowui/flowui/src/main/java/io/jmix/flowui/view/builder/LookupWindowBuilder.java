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
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.Nested;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.LookupView;
import io.jmix.flowui.view.LookupView.ValidationContext;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides a fluent interface to configure and open a lookup view
 * in a {@link DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 */
public class LookupWindowBuilder<E, V extends View<?>> extends AbstractWindowBuilder<V> {

    protected final Class<E> entityClass;

    protected Consumer<Collection<E>> selectHandler;
    protected Predicate<ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    protected CollectionContainer<E> container;

    protected ListDataComponent<E> listDataComponent;
    protected HasValue field;

    protected boolean fieldCollectionValue = false;

    protected LookupWindowBuilder(LookupWindowBuilder<E, V> builder) {
        super(builder.origin, builder.handler);

        this.entityClass = builder.entityClass;

        this.viewId = builder.viewId;

        this.selectHandler = builder.selectHandler;
        this.selectValidator = builder.selectValidator;
        this.transformation = builder.transformation;

        this.container = builder.container;

        this.listDataComponent = builder.listDataComponent;
        this.field = builder.field;

        this.fieldCollectionValue = builder.fieldCollectionValue;

        this.afterOpenListener = builder.afterOpenListener;
        this.afterCloseListener = builder.afterCloseListener;
    }

    public LookupWindowBuilder(View<?> origin,
                               Class<E> entityClass,
                               Function<? extends LookupWindowBuilder<E, V>, DialogWindow<V>> handler) {
        super(origin, handler);

        this.entityClass = entityClass;
    }

    /**
     * Sets identifier of the opened view as specified in the {@link ViewController} annotation.
     *
     * @param viewId identifier of the opened view as specified in the {@link ViewController} annotation
     * @return this instance for chaining
     */
    public LookupWindowBuilder<E, V> withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    /**
     * Sets opened view class.
     *
     * @param viewClass opened view class
     * @param <T>       view type
     * @return {@link LookupWindowClassBuilder} instance for chaining
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends View<?> & LookupView<E>> LookupWindowClassBuilder<E, T> withViewClass(Class<T> viewClass) {
        return new LookupWindowClassBuilder(this, viewClass);
    }

    /**
     * Sets selection handler for the lookup view.
     *
     * @param selectHandler handler to set
     * @return this instance for chaining
     */
    public LookupWindowBuilder<E, V> withSelectHandler(Consumer<Collection<E>> selectHandler) {
        this.selectHandler = selectHandler;
        return this;
    }

    /**
     * Sets selection validator for the lookup view.
     *
     * @param selectValidator validator to set
     * @return this instance for chaining
     */
    public LookupWindowBuilder<E, V> withSelectValidator(Predicate<ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
        return this;
    }

    /**
     * Sets code to transform the entities after selection.
     * <p>
     * Applied only if either field or container or listDataComponent is assigned.
     *
     * @param transformation edited entity transformation object
     * @return this instance for chaining
     * @see #withContainer(CollectionContainer)
     * @see #withField(HasValue)
     * @see #withMultiValueField(HasValue)
     * @see #withListDataComponent(ListDataComponent)
     */
    public LookupWindowBuilder<E, V> withTransformation(@Nullable Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
        return this;
    }

    /**
     * Sets {@link CollectionContainer} to updated after the lookup view is closed.
     * <p>
     * If the container is {@link Nested}, the framework automatically initializes
     * the reference to the parent entity and sets up data contexts for added
     * One-To-Many and Many-To-Many relations.
     *
     * @param container the container to update after the lookup view is closed
     * @return this instance for chaining
     */
    public LookupWindowBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
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
    public LookupWindowBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    /**
     * Sets the field in which the framework sets the selected entity after successful lookup.
     *
     * @param field the field to set
     * @return this instance for chaining
     */
    public <T extends HasValue<?, E>> LookupWindowBuilder<E, V> withField(@Nullable T field) {
        this.field = field;
        this.fieldCollectionValue = false;
        return this;
    }

    /**
     * Sets the field in which the framework sets the selected entities after successful lookup.
     *
     * @param field the field to set
     * @return this instance for chaining
     */
    public <T extends HasValue<?, Collection<E>>> LookupWindowBuilder<E, V> withMultiValueField(@Nullable T field) {
        this.field = field;
        this.fieldCollectionValue = true;
        return this;
    }

    public LookupWindowBuilder<E, V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    public LookupWindowBuilder<E, V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
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
     * @return select handler set by {@link #withSelectHandler(Consumer)}.
     */
    public Optional<Consumer<Collection<E>>> getSelectHandler() {
        return Optional.ofNullable(selectHandler);
    }

    /**
     * @return select validator set by {@link #withSelectValidator(Predicate)}.
     */
    public Optional<Predicate<ValidationContext<E>>> getSelectValidator() {
        return Optional.ofNullable(selectValidator);
    }

    /**
     * @return transformation set by {@link #withTransformation(Function)}.
     */
    public Optional<Function<Collection<E>, Collection<E>>> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    /**
     * @return container set by {@link #withContainer(CollectionContainer)}.
     */
    public Optional<CollectionContainer<E>> getContainer() {
        return Optional.ofNullable(container);
    }

    /**
     * @return list data component set by {@link #withListDataComponent(ListDataComponent)}.
     */
    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    /**
     * @return field set by either {@link #withField(HasValue)} or {@link #withMultiValueField(HasValue)}.
     */
    public Optional<HasValue> getField() {
        return Optional.ofNullable(field);
    }

    /**
     * @return whether a field stores multiple values
     */
    public boolean isFieldCollectionValue() {
        return fieldCollectionValue;
    }
}
