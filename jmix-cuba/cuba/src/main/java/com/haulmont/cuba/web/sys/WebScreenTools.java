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

package com.haulmont.cuba.web.sys;

import io.jmix.ui.component.Window;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.sys.ScreenToolsImpl;

public class WebScreenTools extends ScreenToolsImpl {

    @Override
    protected void setDefaultScreenWindow(Window window) {
        if (window instanceof com.haulmont.cuba.gui.components.Window.Wrapper) {
            window = ((com.haulmont.cuba.gui.components.Window.Wrapper) window).getWrappedWindow();
            ((WindowImpl) window).setDefaultScreenWindow(true);
        } else {
            super.setDefaultScreenWindow(window);
        }
    }
}
