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
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.LookupScreen.ValidationContext;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Deprecated
public class LookupBuilder<E> extends io.jmix.ui.builder.LookupBuilder<E> {

    public LookupBuilder(io.jmix.ui.builder.LookupBuilder<E> builder) {
        super(builder);
    }

    public LookupBuilder(FrameOwner origin, Class<E> entityClass,
                         Function<io.jmix.ui.builder.LookupBuilder<E>, Screen> handler) {
        super(origin, entityClass, handler);
    }

    /**
     * Sets {@link Screens.LaunchMode} for the lookup screen and returns the builder for chaining.
     * <p>For example: {@code builder.withLaunchMode(OpenMode.DIALOG).build();}
     *
     * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    public LookupBuilder<E> withLaunchMode(Screens.LaunchMode launchMode) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        withOpenMode(((OpenMode) launchMode).getOpenMode());
        return this;
    }

    @Override
    public LookupBuilder<E> withOpenMode(io.jmix.ui.screen.OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public LookupBuilder<E> withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public LookupBuilder<E> withSelectValidator(Predicate<ValidationContext<E>> selectValidator) {
        super.withSelectValidator(selectValidator);
        return this;
    }

    @Override
    public LookupBuilder<E> withSelectHandler(@Nullable Consumer<Collection<E>> selectHandler) {
        super.withSelectHandler(selectHandler);
        return this;
    }

    @Override
    public <T extends HasValue<E>> LookupBuilder<E> withField(T field) {
        super.withField(field);
        return this;
    }

    @Override
    public <S extends Screen & LookupScreen<E>> LookupClassBuilder<E, S> withScreenClass(Class<S> screenClass) {
        return new LookupClassBuilder<>(this, screenClass);
    }

    @Override
    public LookupBuilder<E> withScreenId(String screenId) {
        super.withScreenId(screenId);
        return this;
    }

    @Override
    public LookupBuilder<E> withListComponent(ListComponent<E> target) {
        super.withListComponent(target);
        return this;
    }

    @Override
    public LookupBuilder<E> withContainer(CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public LookupBuilder<E> withTransformation(Function<Collection<E>, Collection<E>> transformation) {
        super.withTransformation(transformation);
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
