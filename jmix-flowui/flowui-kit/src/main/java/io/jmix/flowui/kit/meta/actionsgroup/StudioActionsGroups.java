/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.meta.actionsgroup;

import io.jmix.flowui.kit.meta.StudioActionsGroup;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioActionsGroups {

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.valuepicker.PickerAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.component.PickerComponent"}
    )
    void pickerActions();

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.view.ViewAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.view.View"},
            unsupportedTarget = {"io.jmix.flowui.app.main.StandardMainView"}
    )
    void viewActions();

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.security.LogoutAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.app.main.StandardMainView"}
    )
    void mainViewActions();

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.list.ListDataComponentAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.component.ListDataComponent"}
    )
    void listDataComponentActions();
}
