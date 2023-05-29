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
 *
 */

package com.haulmont.cuba.gui.screen;


import com.haulmont.cuba.gui.Screens;

/**
 * Standard launch modes of {@link io.jmix.ui.screen.Screen}s.
 *
 * @deprecated Use {@link io.jmix.ui.screen.OpenMode} instead
 */
@Deprecated
public enum OpenMode implements Screens.LaunchMode {
    /**
     * Open a screen in new tab of the main window.
     * <br> In Web Client with {@code AppWindow.Mode.SINGLE} the new screen replaces current screen.
     */
    NEW_TAB,
    /**
     * Open a screen on top of the current tab screens stack.
     */
    THIS_TAB,
    /**
     * Open a screen as modal dialog.
     */
    DIALOG,
    /**
     * In Desktop Client open a screen in new main window, in Web Client the same as new {@link #NEW_TAB}
     */
    NEW_WINDOW,
    /**
     * In Web Client opens a screen as main
     */
    ROOT;

    public static OpenMode from(io.jmix.ui.screen.OpenMode openMode) {
        switch (openMode) {
            case NEW_TAB:
                return OpenMode.NEW_TAB;
            case THIS_TAB:
                return OpenMode.THIS_TAB;
            case DIALOG:
                return OpenMode.DIALOG;
            case NEW_WINDOW:
                return OpenMode.NEW_WINDOW;
            case ROOT:
                return OpenMode.ROOT;
            default:
                throw new IllegalStateException("Unknown open mode: " + openMode);
        }
    }

    public io.jmix.ui.screen.OpenMode getOpenMode() {
        switch (this) {
            case NEW_TAB:
                return io.jmix.ui.screen.OpenMode.NEW_TAB;
            case THIS_TAB:
                return io.jmix.ui.screen.OpenMode.THIS_TAB;
            case DIALOG:
                return io.jmix.ui.screen.OpenMode.DIALOG;
            case NEW_WINDOW:
                return io.jmix.ui.screen.OpenMode.NEW_WINDOW;
            case ROOT:
                return io.jmix.ui.screen.OpenMode.ROOT;
            default:
                throw new IllegalStateException("Unknown open mode: " + this);
        }
    }
}