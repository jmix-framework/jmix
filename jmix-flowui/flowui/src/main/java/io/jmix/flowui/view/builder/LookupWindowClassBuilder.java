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
import io.jmix.flowui.view.LookupView;
import io.jmix.flowui.view.View;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides a fluent interface to configure and open a lookup view with
 * the specific class in a {@link DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 * @param <E> entity type
 */
public class LookupWindowClassBuilder<E, V extends View<?> & LookupView<E>> extends LookupWindowBuilder<E, V>
        implements DialogWindowClassBuilder<V> {

    protected Class<V> viewClass;

    protected LookupWindowClassBuilder(LookupWindowBuilder<E, V> builder, Class<V> viewClass) {
        super(builder);

        this.viewClass = viewClass;
    }

    public LookupWindowClassBuilder(View<?> origin,
                                    Class<E> entityClass,
                                    Class<V> viewClass,
                                    Function<? extends LookupWindowClassBuilder<E, V>, DialogWindow<V>> handler) {
        super(origin, entityClass, handler);

        this.viewClass = viewClass;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public LookupWindowClassBuilder<E, V> withSelectHandler(Consumer<Collection<E>> selectHandler) {
        super.withSelectHandler(selectHandler);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withSelectValidator(Predicate<LookupView.ValidationContext<E>> selectValidator) {
        super.withSelectValidator(selectValidator);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withTransformation(@Nullable Function<Collection<E>, Collection<E>> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withContainer(@Nullable CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        super.withListDataComponent(listDataComponent);
        return this;
    }

    @Override
    public <T extends HasValue<?, E>> LookupWindowClassBuilder<E, V> withField(@Nullable T field) {
        super.withField(field);
        return this;
    }

    @Override
    public <T extends HasValue<?, Collection<E>>> LookupWindowClassBuilder<E, V> withMultiValueField(@Nullable T field) {
        super.withMultiValueField(field);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<V>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, V> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<V>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<V>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }
}
