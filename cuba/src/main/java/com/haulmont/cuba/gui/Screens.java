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

package com.haulmont.cuba.gui;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.gui.screen.OpenMode;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

/**
 * Interface defining methods for creation and displaying of UI screens.
 *
 * @deprecated Use {@link io.jmix.ui.Screens} instead
 */
@Deprecated
public interface Screens extends io.jmix.ui.Screens {

    /**
     * Creates a screen by its controller class.
     *
     * @param screenClass screen controller class
     * @param launchMode  how the screen should be opened
     * @deprecated Use {@link #create(Class, io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    default <T extends Screen> T create(Class<T> screenClass, LaunchMode launchMode) {
        return create(screenClass, launchMode, FrameOwner.NO_OPTIONS);
    }

    /**
     * Creates a screen by its screen id.
     *
     * @param screenId   screen id
     * @param launchMode how the screen should be opened
     * @deprecated Use {@link #create(String, io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    default Screen create(String screenId, LaunchMode launchMode) {
        return create(screenId, launchMode, FrameOwner.NO_OPTIONS);
    }

    /**
     * Creates a screen by its controller class.
     *
     * @param screenClass screen controller class
     * @param launchMode  how the screen should be opened
     * @param options     screen parameters
     * @deprecated Use {@link #create(String, io.jmix.ui.screen.OpenMode, ScreenOptions)} instead
     */
    @Deprecated
    default <T extends Screen> T create(Class<T> screenClass, LaunchMode launchMode, ScreenOptions options) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        return create(screenClass, ((OpenMode) launchMode).getOpenMode(), options);
    }

    /**
     * Creates a screen by its screen id.
     *
     * @param screenId   screen id
     * @param launchMode how the screen should be opened
     * @param options    screen parameters
     * @deprecated Use {@link #create(String, io.jmix.ui.screen.OpenMode, ScreenOptions)} instead
     */
    @Deprecated
    default Screen create(String screenId, LaunchMode launchMode, ScreenOptions options) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        return create(screenId, ((OpenMode) launchMode).getOpenMode(), options);
    }

    /**
     * Marker interface for screen launch modes.
     *
     * @deprecated Use {@link io.jmix.ui.screen.OpenMode} instead
     */
    @Deprecated
    interface LaunchMode {
    }
}
