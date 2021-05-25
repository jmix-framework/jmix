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

import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen builder that knows the concrete screen class. It's {@link #build()} method returns that class.
 */
public class ScreenClassBuilder<S extends Screen> extends ScreenBuilder {

    protected Class<S> screenClass;
    protected Consumer<AfterScreenShowEvent<S>> afterShowListener;
    protected Consumer<AfterScreenCloseEvent<S>> afterCloseListener;

    public ScreenClassBuilder(ScreenBuilder builder, Class<S> screenClass) {
        super(builder);

        this.screenClass = screenClass;
    }

    @Override
    public ScreenClassBuilder<S> withOpenMode(OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public ScreenClassBuilder<S> withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public ScreenBuilder withScreenId(String screenId) {
        throw new IllegalStateException("ScreenClassBuilder does not support screenId");
    }

    /**
     * Adds {@link Screen.AfterShowEvent} listener to the screen.
     *
     * @param listener listener
     */
    public ScreenClassBuilder<S> withAfterShowListener(Consumer<AfterScreenShowEvent<S>> listener) {
        afterShowListener = listener;
        return this;
    }

    /**
     * Adds {@link Screen.AfterCloseEvent} listener to the screen.
     *
     * @param listener listener
     */
    public ScreenClassBuilder<S> withAfterCloseListener(Consumer<AfterScreenCloseEvent<S>> listener) {
        afterCloseListener = listener;
        return this;
    }

    /**
     * Returns screen class.
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

    @Override
    public S build() {
        return (S) super.build();
    }

    @Override
    public S show() {
        return (S) super.show();
    }
}