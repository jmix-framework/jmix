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
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Lookup screen builder that knows the concrete screen class. It's {@link #build()} method returns that class.
 */
public class LookupClassBuilder<E, S extends Screen & LookupScreen<E>> extends LookupBuilder<E> {

    protected Class<S> screenClass;
    protected Consumer<AfterScreenShowEvent<S>> afterShowListener;
    protected Consumer<AfterScreenCloseEvent<S>> afterCloseListener;

    public LookupClassBuilder(LookupBuilder<E> builder, Class<S> screenClass) {
        super(builder);

        this.screenClass = screenClass;
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
    public <T extends HasValue<Collection<E>>> LookupClassBuilder<E, S> withValuesField(T field) {
        super.withValuesField(field);
        return this;
    }

    @Override
    public LookupBuilder<E> withScreenId(String screenId) {
        throw new IllegalStateException("LookupClassBuilder does not support screenId");
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

    /**
     * Adds {@link Screen.AfterShowEvent} listener to the screen.
     *
     * @param listener listener
     */
    public LookupClassBuilder<E, S> withAfterShowListener(Consumer<AfterScreenShowEvent<S>> listener) {
        afterShowListener = listener;
        return this;
    }

    /**
     * Adds {@link Screen.AfterCloseEvent} listener to the screen.
     *
     * @param listener listener
     */
    public LookupClassBuilder<E, S> withAfterCloseListener(Consumer<AfterScreenCloseEvent<S>> listener) {
        afterCloseListener = listener;
        return this;
    }

    @Override
    public LookupClassBuilder<E, S> withTransformation(Function<Collection<E>, Collection<E>> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    /**
     * Returns lookup screen class.
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
        return (S) this.handler.apply(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S show() {
        return (S) super.show();
    }
}
