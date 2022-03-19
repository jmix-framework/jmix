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
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import java.util.function.Function;

@Deprecated
public class ScreenBuilder extends io.jmix.ui.builder.ScreenBuilder {

    public ScreenBuilder(io.jmix.ui.builder.ScreenBuilder builder) {
        super(builder);
    }

    public ScreenBuilder(FrameOwner origin, Function<io.jmix.ui.builder.ScreenBuilder, Screen> handler) {
        super(origin, handler);
    }

    /**
     * Sets {@link Screens.LaunchMode} for the screen and returns the builder for chaining.
     * <p>For example: {@code builder.withLaunchMode(OpenMode.DIALOG).build();}
     *
     * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    public ScreenBuilder withLaunchMode(Screens.LaunchMode launchMode) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        withOpenMode(((OpenMode) launchMode).getOpenMode());
        return this;
    }

    @Override
    public ScreenBuilder withOpenMode(io.jmix.ui.screen.OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public ScreenBuilder withScreenId(String screenId) {
        super.withScreenId(screenId);
        return this;
    }

    @Override
    public ScreenBuilder withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public <S extends Screen> ScreenClassBuilder<S> withScreenClass(Class<S> screenClass) {
        return new ScreenClassBuilder<>(this, screenClass);
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
