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

package io.jmix.flowui.kit.meta.action;

import io.jmix.flowui.kit.meta.StudioAction;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioActions {

    @StudioAction(
            classFqn = "io.jmix.flowui.kit.action.BaseAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            unsupportedTarget = {"io.jmix.flowui.kit.component.valuepicker.ValuePickerBase"},
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void baseAction();
}
