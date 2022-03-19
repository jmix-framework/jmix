/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.builders;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.screen.OpenMode;
import io.jmix.ui.builder.AfterScreenCloseEvent;
import io.jmix.ui.builder.AfterScreenShowEvent;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

@Deprecated
public class EditorClassBuilder<E, S extends Screen & EditorScreen<E>>
        extends io.jmix.ui.builder.EditorClassBuilder<E, S> {

    public EditorClassBuilder(EditorBuilder<E> builder, Class<S> screenClass) {
        super(builder, screenClass);
    }

    /**
     * Sets {@link Screens.LaunchMode} for the editor screen and returns the builder for chaining.
     * <p>For example: {@code builder.withLaunchMode(OpenMode.DIALOG).build();}
     *
     * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    public EditorClassBuilder<E, S> withLaunchMode(Screens.LaunchMode launchMode) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        withOpenMode(((OpenMode) launchMode).getOpenMode());
        return this;
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
    public EditorClassBuilder<E, S> withOpenMode(io.jmix.ui.screen.OpenMode openMode) {
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
        return (EditorBuilder<E>) super.withScreenId(screenId);
    }

    @Override
    public EditorClassBuilder<E, S> withAfterShowListener(Consumer<AfterScreenShowEvent<S>> listener) {
        super.withAfterShowListener(listener);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withAfterCloseListener(Consumer<AfterScreenCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public EditorClassBuilder<E, S> withTransformation(@Nullable Function<E, E> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    /**
     * @return after close screen listener
     * @deprecated Use {@link #getAfterCloseListener()} instead.
     */
    @Deprecated
    public Consumer<AfterScreenCloseEvent<S>> getCloseListener() {
        return getAfterCloseListener();
    }

    @Override
    public <T extends HasValue<E>> EditorClassBuilder<E, S> withField(T field) {
        super.withField(field);
        return this;
    }

    /**
     * Returns launch mode set by {@link #withLaunchMode(Screens.LaunchMode)}.
     *
     * @deprecated Use {@link #getOpenMode()} instead
     */
    @Deprecated
    public Screens.LaunchMode getLaunchMode() {
        return OpenMode.from(getOpenMode());
    }
}
