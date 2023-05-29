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

package com.haulmont.cuba.gui.sys;

import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.WindowContext;
import io.jmix.ui.component.Frame;
import io.jmix.ui.screen.OpenMode;

@Deprecated
public class WindowContextImpl extends io.jmix.ui.sys.WindowContextImpl implements WindowContext {

    public WindowContextImpl(Frame window, OpenMode openMode) {
        super(window, openMode);
    }

    public WindowContextImpl(Frame window, Screens.LaunchMode launchMode) {
        super(window, ((com.haulmont.cuba.gui.screen.OpenMode) launchMode).getOpenMode());
    }
}
