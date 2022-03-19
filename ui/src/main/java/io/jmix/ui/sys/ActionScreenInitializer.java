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

package io.jmix.ui.sys;

import io.jmix.ui.builder.EditorBuilder;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.builder.ScreenBuilder;
import io.jmix.ui.relatedentities.RelatedEntitiesBuilder;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used in actions that open screens ({@code EditAction}, etc.) to initialize a screen builder.
 */
public class ActionScreenInitializer {

    protected OpenMode openMode;
    protected String screenId;
    protected Class<? extends Screen> screenClass;
    protected Supplier<ScreenOptions> screenOptionsSupplier;
    protected Consumer<Screen> screenConfigurer;
    protected Consumer<Screen.AfterCloseEvent> afterCloseHandler;

    @Nullable
    public OpenMode getOpenMode() {
        return openMode;
    }

    @Nullable
    public String getScreenId() {
        return screenId;
    }

    @Nullable
    public Class<? extends Screen> getScreenClass() {
        return screenClass;
    }

    public void setOpenMode(@Nullable OpenMode openMode) {
        this.openMode = openMode;
    }

    public void setScreenId(@Nullable String screenId) {
        this.screenId = screenId;
    }

    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        this.screenClass = screenClass;
    }

    public void setScreenOptionsSupplier(@Nullable Supplier<ScreenOptions> screenOptionsSupplier) {
        this.screenOptionsSupplier = screenOptionsSupplier;
    }

    public void setScreenConfigurer(@Nullable Consumer<Screen> screenConfigurer) {
        this.screenConfigurer = screenConfigurer;
    }

    public void setAfterCloseHandler(@Nullable Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        this.afterCloseHandler = afterCloseHandler;
    }

    public ScreenBuilder initBuilder(ScreenBuilder builder) {
        if (screenClass != null) {
            builder = builder.withScreenClass(screenClass);
        }

        if (screenId != null) {
            builder = builder.withScreenId(screenId);
        }

        if (screenOptionsSupplier != null) {
            ScreenOptions screenOptions = screenOptionsSupplier.get();
            builder = builder.withOptions(screenOptions);
        }

        if (openMode != null) {
            builder = builder.withOpenMode(openMode);
        }

        return builder;
    }

    public EditorBuilder initBuilder(EditorBuilder builder) {
        if (screenClass != null) {
            builder = builder.withScreenClass(screenClass);
        }

        if (screenId != null) {
            builder = builder.withScreenId(screenId);
        }

        if (screenOptionsSupplier != null) {
            ScreenOptions screenOptions = screenOptionsSupplier.get();
            builder = builder.withOptions(screenOptions);
        }

        if (openMode != null) {
            builder = builder.withOpenMode(openMode);
        }

        return builder;
    }

    public LookupBuilder initBuilder(LookupBuilder builder) {
        if (screenClass != null) {
            builder = builder.withScreenClass(screenClass);
        }

        if (screenId != null) {
            builder = builder.withScreenId(screenId);
        }

        if (screenOptionsSupplier != null) {
            ScreenOptions screenOptions = screenOptionsSupplier.get();
            builder = builder.withOptions(screenOptions);
        }

        if (openMode != null) {
            builder = builder.withOpenMode(openMode);
        }

        return builder;
    }

    public RelatedEntitiesBuilder initBuilder(RelatedEntitiesBuilder builder) {
        if (screenClass != null) {
            builder = builder.withScreenClass(screenClass);
        }

        if (screenId != null) {
            builder = builder.withScreenId(screenId);
        }

        if (screenOptionsSupplier != null) {
            ScreenOptions screenOptions = screenOptionsSupplier.get();
            builder = builder.withOptions(screenOptions);
        }

        if (openMode != null) {
            builder = builder.withOpenMode(openMode);
        }

        return builder;
    }

    public void initScreen(Screen screen) {
        if (afterCloseHandler != null) {
            screen.addAfterCloseListener(afterCloseHandler);
        }

        if (screenConfigurer != null) {
            screenConfigurer.accept(screen);
        }
    }
}
