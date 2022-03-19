/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.web.gui;

import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.UiControllerDependencyManager;

public class CubaUiControllerDependencyManager extends UiControllerDependencyManager {

    @Override
    public void inject(FrameOwner controller, ScreenOptions options) {
        if (controller instanceof LegacyFragmentAdapter) {
            controller = ((LegacyFragmentAdapter) controller).getRealScreen();
        }

        super.inject(controller, options);
    }
}
