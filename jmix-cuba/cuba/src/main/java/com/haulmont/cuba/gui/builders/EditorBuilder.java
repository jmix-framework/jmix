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
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

@Deprecated
public class EditorBuilder<E> extends io.jmix.ui.builder.EditorBuilder<E> {

    public EditorBuilder(io.jmix.ui.builder.EditorBuilder<E> builder) {
        super(builder);
    }

    public EditorBuilder(FrameOwner origin, Class<E> entityClass,
                         Function<io.jmix.ui.builder.EditorBuilder<E>, Screen> handler) {
        super(origin, entityClass, handler);
    }

    /**
     * Sets {@link Screens.LaunchMode} for the editor screen and returns the builder for chaining.
     * <p>For example: {@code builder.withLaunchMode(OpenMode.DIALOG).build();}
     *
     * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    public EditorBuilder<E> withLaunchMode(Screens.LaunchMode launchMode) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        withOpenMode(((OpenMode) launchMode).getOpenMode());
        return this;
    }

    @Override
    public EditorBuilder<E> newEntity() {
        super.newEntity();
        return this;
    }

    @Override
    public EditorBuilder<E> newEntity(@Nullable E entity) {
        super.newEntity(entity);
        return this;
    }

    @Override
    public EditorBuilder<E> editEntity(@Nullable E entity) {
        super.editEntity(entity);
        return this;
    }

    @Override
    public EditorBuilder<E> withContainer(CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public EditorBuilder<E> withInitializer(Consumer<E> initializer) {
        super.withInitializer(initializer);
        return this;
    }

    @Override
    public EditorBuilder<E> withTransformation(@Nullable Function<E, E> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public EditorBuilder<E> withOpenMode(io.jmix.ui.screen.OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public EditorBuilder<E> withAddFirst(boolean addFirst) {
        super.withAddFirst(addFirst);
        return this;
    }

    @Override
    public EditorBuilder<E> withParentDataContext(@Nullable DataContext parentDataContext) {
        super.withParentDataContext(parentDataContext);
        return this;
    }

    @Override
    public EditorBuilder<E> withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public EditorBuilder<E> withListComponent(ListComponent<E> listComponent) {
        super.withListComponent(listComponent);
        return this;
    }

    @Override
    public EditorBuilder<E> withScreenId(String screenId) {
        super.withScreenId(screenId);
        return this;
    }

    @Override
    public <S extends Screen & EditorScreen<E>> EditorClassBuilder<E, S> withScreenClass(Class<S> screenClass) {
        return new EditorClassBuilder<>(this, screenClass);
    }

    @Override
    public <T extends HasValue<E>> EditorBuilder<E> withField(T field) {
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
