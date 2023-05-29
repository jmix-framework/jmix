/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.gui.screen.compatibility;

import com.haulmont.cuba.gui.components.AbstractFrame;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.screen.ScreenFragment;

import javax.annotation.Nullable;

/**
 * Wrapper object for compatibility with legacy code.
 */
@Deprecated
public class ScreenFragmentWrapper extends AbstractFrame {

    private ScreenFragment screen;

    public ScreenFragmentWrapper(ScreenFragment screen) {
        this.screen = screen;
    }

    @Override
    public Frame getWrappedFrame() {
        return screen.getFragment();
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return screen.getFragment().getComponent(id);
    }
}