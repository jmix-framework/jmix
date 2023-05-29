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

import com.haulmont.cuba.gui.components.AbstractWindow;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import com.haulmont.cuba.gui.components.compatibility.AfterCloseListenerAdapter;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Wrapper object for compatibility with legacy code.
 */
@Deprecated
public class ScreenWrapper extends AbstractWindow {

    private Screen screen;

    public ScreenWrapper(Screen screen) {
        this.screen = screen;
    }

    @Override
    public Frame getWrappedFrame() {
        return screen.getWindow();
    }

    @Override
    public void addListener(CloseListener listener) {
        screen.addAfterCloseListener(new AfterCloseListenerAdapter(listener));
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        screen.addAfterCloseListener(new AfterCloseListenerAdapter(listener));
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return screen.getWindow().getComponent(id);
    }
}