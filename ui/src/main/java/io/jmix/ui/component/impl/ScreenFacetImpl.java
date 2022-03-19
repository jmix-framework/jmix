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

import io.jmix.core.DevelopmentException;
import io.jmix.ui.Screens;
import io.jmix.ui.component.Frame;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;

@SuppressWarnings("unchecked")
public class ScreenFacetImpl<S extends Screen> extends AbstractScreenFacet<S> {

    @Override
    public S create() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Screen facet is not attached to Frame");
        }

        screen = createScreen(owner.getFrameOwner());

        initScreenListeners(screen);
        injectScreenProperties(screen, properties);
        applyScreenConfigurer(screen);

        return screen;
    }

    @Override
    public S show() {
        return (S) create().show();
    }

    protected S createScreen(FrameOwner frameOwner) {
        S screen;

        Screens screens = UiControllerUtils.getScreenContext(frameOwner)
                .getScreens();

        if (screenId != null) {
            screen = (S) screens.create(screenId, openMode, getScreenOptions());
        } else if (screenClass != null) {
            screen = screens.create(screenClass, openMode, getScreenOptions());
        } else {
            throw new DevelopmentException("Unable to open screen because no screen id or screen class are specified");
        }

        return screen;
    }
}
