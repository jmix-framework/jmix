/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.view;

/**
 * Standard open modes of {@link io.jmix.flowui.view.View Views}.
 */
public enum ViewOpenMode {

    /**
     * Open a screen in new tab of the main window.
     * <p>
     * In Web Client with {@code AppWindow.Mode.SINGLE} the new screen replaces current screen.
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
     * TODO: gg, fix javadoc
     * In Web Client opens a screen as main
     */
    ROOT
}
