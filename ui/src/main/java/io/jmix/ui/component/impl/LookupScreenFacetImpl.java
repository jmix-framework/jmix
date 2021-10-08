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

package io.jmix.ui.component.impl;

import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.LookupScreenFacet;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class LookupScreenFacetImpl<E, S extends Screen & LookupScreen<E>>
        extends AbstractEntityAwareScreenFacet<E, S>
        implements LookupScreenFacet<E, S> {

    protected Consumer<Collection<E>> selectHandler;
    protected Predicate<LookupScreen.ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    @Override
    public void setSelectHandler(@Nullable Consumer<Collection<E>> selectHandler) {
        this.selectHandler = selectHandler;
    }

    @Nullable
    @Override
    public Consumer<Collection<E>> getSelectHandler() {
        return selectHandler;
    }

    @Override
    public void setSelectValidator(@Nullable Predicate<LookupScreen.ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    @Nullable
    @Override
    public Predicate<LookupScreen.ValidationContext<E>> getSelectValidator() {
        return selectValidator;
    }

    @Override
    public void setTransformation(@Nullable Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
    }

    @Nullable
    @Override
    public Function<Collection<E>, Collection<E>> getTransformation() {
        return transformation;
    }

    @Override
    public S create() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Screen facet is not attached to Frame");
        }

        LookupBuilder<E> lookupBuilder = createLookupBuilder(owner);

        screen = createScreen(lookupBuilder);

        initScreenListeners(screen);
        injectScreenProperties(screen, properties);
        applyScreenConfigurer(screen);

        return screen;
    }

    @Override
    public S show() {
        return (S) create().show();
    }

    protected S createScreen(LookupBuilder<E> builder) {
        return (S) builder
                .withField(entityPicker)
                .withListComponent(listComponent)
                .withContainer(container)
                .withOpenMode(openMode)
                .withOptions(getScreenOptions())
                .withSelectValidator(selectValidator)
                .withSelectHandler(selectHandler)
                .withTransformation(transformation)
                .build();
    }

    protected LookupBuilder<E> createLookupBuilder(Frame owner) {
        LookupBuilder<E> builder;

        if (applicationContext == null) {
            throw new IllegalStateException("Unable to create LookupScreenFacet. ApplicationContext is null");
        }
        ScreenBuilders screenBuilders = applicationContext.getBean(ScreenBuilders.class);

        if (entityClass != null) {
            builder = screenBuilders.lookup(entityClass, owner.getFrameOwner());
        } else if (listComponent != null) {
            builder = screenBuilders.lookup(listComponent);
        } else if (entityPicker != null) {
            builder = screenBuilders.lookup(entityPicker);
        } else {
            throw new IllegalStateException(
                    "Unable to create LookupScreenFacet. At least one of entityClass," +
                            "listComponent or field must be specified");
        }

        if (screenClass != null) {
            builder = builder.withScreenClass(screenClass);
        } else {
            builder.withScreenId(screenId);
        }

        return builder;
    }
}
