/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.builder;

import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Editor screen builder that knows the concrete screen class. It's {@link #build()} method returns that class.
 */
public class EditorClassBuilder<E, S extends Screen & EditorScreen<E>> extends EditorBuilder<E> {

    protected Class<S> screenClass;
    protected Consumer<AfterScreenCloseEvent<S>> afterCloseListener;
    protected Consumer<AfterScreenShowEvent<S>> afterShowListener;

    public EditorClassBuilder(EditorBuilder<E> builder, Class<S> screenClass) {
        super(builder);

        this.screenClass = screenClass;
    }

    @Override
    public EditorClassBuilder<E, S> newEntity() {
        super.newEntity();
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> editEntity(E entity) {
        super.editEntity(entity);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> newEntity(E entity) {
        super.newEntity(entity);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withContainer(CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withInitializer(Consumer<E> initializer) {
        super.withInitializer(initializer);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withAddFirst(boolean addFirst) {
        super.withAddFirst(addFirst);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withOpenMode(OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withParentDataContext(DataContext parentDataContext) {
        super.withParentDataContext(parentDataContext);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withListComponent(ListComponent<E> listComponent) {
        super.withListComponent(listComponent);
        return this;
    }

    @Override
    public EditorBuilder<E> withScreenId(String screenId) {
        throw new IllegalStateException("EditorClassBuilder does not support screenId");
    }

    /**
     * Adds {@link Screen.AfterShowEvent} listener to the screen.
     *
     * @param listener listener
     */
    public EditorClassBuilder<E, S> withAfterShowListener(Consumer<AfterScreenShowEvent<S>> listener) {
        afterShowListener = listener;
        return this;
    }

    /**
     * Adds {@link Screen.AfterCloseEvent} listener to the screen.
     *
     * @param listener listener
     */
    public EditorClassBuilder<E, S> withAfterCloseListener(Consumer<AfterScreenCloseEvent<S>> listener) {
        afterCloseListener = listener;
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withTransformation(@Nullable Function<E, E> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public <T extends HasValue<E>> EditorClassBuilder<E, S> withField(T field) {
        super.withField(field);
        return this;
    }

    /**
     * Returns editor screen class.
     */
    @Nullable
    public Class<S> getScreenClass() {
        return screenClass;
    }

    /**
     * @return after show screen listener
     */
    public Consumer<AfterScreenShowEvent<S>> getAfterShowListener() {
        return afterShowListener;
    }

    /**
     * @return after close screen listener
     */
    public Consumer<AfterScreenCloseEvent<S>> getAfterCloseListener() {
        return afterCloseListener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S build() {
        return (S) handler.apply(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S show() {
        return (S) super.show();
    }
}
