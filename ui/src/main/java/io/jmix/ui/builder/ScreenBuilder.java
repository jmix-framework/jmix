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

import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Screen builder that is not aware of concrete screen class. It's {@link #build()} method returns {@link Screen}.
 */
public class ScreenBuilder {

    protected final FrameOwner origin;
    protected final Function<ScreenBuilder, Screen> handler;

    protected OpenMode openMode = OpenMode.THIS_TAB;
    protected ScreenOptions options = FrameOwner.NO_OPTIONS;
    protected String screenId;

    public ScreenBuilder(ScreenBuilder builder) {
        this.origin = builder.origin;
        this.handler = builder.handler;

        this.options = builder.options;
        this.openMode = builder.openMode;
        this.screenId = builder.screenId;
    }

    public ScreenBuilder(FrameOwner origin, Function<ScreenBuilder, Screen> handler) {
        this.origin = origin;
        this.handler = handler;
    }

    /**
     * Sets {@link OpenMode} for the screen and returns the builder for chaining.
     * <p>For example: {@code builder.withOpenMode(OpenMode.DIALOG).build();}
     */
    public ScreenBuilder withOpenMode(OpenMode openMode) {
        checkNotNullArgument(openMode);

        this.openMode = openMode;
        return this;
    }

    /**
     * Sets screen id and returns the builder for chaining.
     *
     * @param screenId identifier of the screen as specified in the {@code UiController} annotation
     *                 or {@code screens.xml}.
     */
    public ScreenBuilder withScreenId(String screenId) {
        this.screenId = screenId;
        return this;
    }

    /**
     * Sets {@link ScreenOptions} for the screen and returns the builder for chaining.
     */
    public ScreenBuilder withOptions(ScreenOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Sets screen class and returns the {@link EditorClassBuilder} for chaining.
     *
     * @param screenClass class of the screen controller
     */
    public <S extends Screen> ScreenClassBuilder<S> withScreenClass(Class<S> screenClass) {
        return new ScreenClassBuilder<>(this, screenClass);
    }

    public FrameOwner getOrigin() {
        return origin;
    }

    /**
     * Returns open mode set by {@link #withOpenMode(OpenMode)}.
     */
    public OpenMode getOpenMode() {
        return openMode;
    }

    /**
     * Returns screen options set by {@link #withOptions(ScreenOptions)}.
     */
    public ScreenOptions getOptions() {
        return options;
    }

    /**
     * Returns screen id set by {@link #withScreenId(String)}.
     */
    @Nullable
    public String getScreenId() {
        return screenId;
    }

    /**
     * Builds the screen. Screen should be shown using {@link Screen#show()}.
     */
    public Screen build() {
        return handler.apply(this);
    }

    /**
     * Builds and shows the editor screen.
     */
    public Screen show() {
        return handler.apply(this)
                .show();
    }
}