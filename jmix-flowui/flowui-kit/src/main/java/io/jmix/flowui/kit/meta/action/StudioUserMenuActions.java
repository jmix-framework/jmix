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

package io.jmix.flowui.kit.meta.action;

import io.jmix.flowui.kit.meta.StudioAction;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;

@StudioUiKit
interface StudioUserMenuActions {

    @StudioAction(
            type = "userMenu_themeSwitch",
            description = "Switches visual themes in a user menu. It requires the " +
                    "'@JsModule(\"./src/theme/color-scheme-switching-support.js\")' " +
                    "import to be added to the main application class",
            classFqn = "io.jmix.flowui.action.usermenu.UserMenuThemeSwitchAction",
            propertyGroups = StudioActionPropertyGroups.UserMenuThemeSwitchActionComponent.class)
    void userMenuThemeSwitchAction();
}
