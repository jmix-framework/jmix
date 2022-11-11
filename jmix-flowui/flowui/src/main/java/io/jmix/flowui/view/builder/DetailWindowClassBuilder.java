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

import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.DetailView;
import io.jmix.flowui.view.View;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides a fluent interface to configure and open a details view with
 * the specific class in a {@link DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 * @param <E> edited entity type
 */
public class DetailWindowClassBuilder<E, V extends View<?> & DetailView<E>> extends DetailWindowBuilder<E, V>
        implements DialogWindowClassBuilder<V> {

    protected Class<V> viewClass;

    protected DetailWindowClassBuilder(DetailWindowBuilder<E, V> builder, Class<V> viewClass) {
        super(builder);

        this.viewClass = viewClass;
    }

    public DetailWindowClassBuilder(View<?> origin,
                                    Class<E> entityClass,
                                    Class<V> viewClass,
                                    Function<? extends DetailWindowClassBuilder<E, V>, DialogWindow<V>> handler) {
        super(origin, entityClass, handler);

        this.viewClass = viewClass;
    }

    @Override
    public DetailWindowClassBuilder<E, V> newEntity() {
        super.newEntity();
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> newEntity(E entity) {
        super.newEntity(entity);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> editEntity(E entity) {
        super.editEntity(entity);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public DetailWindowClassBuilder<E, V> withInitializer(@Nullable Consumer<E> initializer) {
        super.withInitializer(initializer);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withTransformation(@Nullable Function<E, E> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withParentDataContext(@Nullable DataContext parentDataContext) {
        super.withParentDataContext(parentDataContext);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        super.withListDataComponent(listDataComponent);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withField(@Nullable HasValue<?, E> field) {
        super.withField(field);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withAddFirst(@Nullable Boolean addFirst) {
        super.withAddFirst(addFirst);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<V>> getViewClass() {
        return Optional.of(viewClass);
    }
}
