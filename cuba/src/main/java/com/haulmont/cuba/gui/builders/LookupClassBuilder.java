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
import io.jmix.ui.builder.AfterScreenCloseEvent;
import io.jmix.ui.builder.AfterScreenShowEvent;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Deprecated
public class LookupClassBuilder<E, S extends Screen & LookupScreen<E>>
        extends io.jmix.ui.builder.LookupClassBuilder<E, S> {

    public LookupClassBuilder(io.jmix.ui.builder.LookupBuilder<E> builder, Class<S> screenClass) {
        super(builder, screenClass);
    }

    /**
     * Sets {@link Screens.LaunchMode} for the lookup screen and returns the builder for chaining.
     * <p>For example: {@code builder.withLaunchMode(OpenMode.DIALOG).build();}
     *
     * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    public LookupClassBuilder<E, S> withLaunchMode(Screens.LaunchMode launchMode) {
        Preconditions.checkArgument(launchMode instanceof com.haulmont.cuba.gui.screen.OpenMode,
                "Unsupported LaunchMode " + launchMode);

        withOpenMode(((com.haulmont.cuba.gui.screen.OpenMode) launchMode).getOpenMode());
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withOpenMode(OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withSelectValidator(Predicate<LookupScreen.ValidationContext<E>> selectValidator) {
        super.withSelectValidator(selectValidator);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withSelectHandler(@Nullable Consumer<Collection<E>> selectHandler) {
        super.withSelectHandler(selectHandler);
        return this;
    }

    @Override
    public <T extends HasValue<E>> LookupClassBuilder<E, S> withField(T field) {
        super.withField(field);
        return this;
    }

    @Override
    public LookupBuilder<E> withScreenId(String screenId) {
        return (LookupBuilder<E>) super.withScreenId(screenId);
    }

    @Override
    public LookupClassBuilder<E, S> withListComponent(ListComponent<E> target) {
        super.withListComponent(target);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withContainer(CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withAfterShowListener(Consumer<AfterScreenShowEvent<S>> listener) {
        super.withAfterShowListener(listener);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withAfterCloseListener(Consumer<AfterScreenCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withTransformation(Function<Collection<E>, Collection<E>> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    /**
     * @return after screen close listener.
     * @deprecated Use {@link #getAfterCloseListener()} instead.
     */
    @Deprecated
    public Consumer<AfterScreenCloseEvent<S>> getCloseListener() {
        return getAfterCloseListener();
    }

    /**
     * Returns launch mode set by {@link #withLaunchMode(Screens.LaunchMode)}.
     *
     * @deprecated Use {@link #getOpenMode()} instead
     */
    @Deprecated
    public Screens.LaunchMode getLaunchMode() {
        return com.haulmont.cuba.gui.screen.OpenMode.from(getOpenMode());
    }
}
