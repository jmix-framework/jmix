/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.meta;

import io.jmix.flowui.kit.meta.*;

@StudioUiKit(requiredDependencies = "io.jmix.pivottable:jmix-pivottable-flowui-starter")
public interface StudioPivotTableActions {

    @StudioAction(
            type = "pvttbl_showPivotTableAction",
            description = "Shows the Pivot Table component",
            classFqn = "io.jmix.pivottableflowui.action.ShowPivotTableAction",
            propertyGroups = StudioPivotTablePropertyGroups.ShowPivotTableActionComponent.class,
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.ENABLED_BY_UI_PERMISSIONS, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VISIBLE_BY_UI_PERMISSIONS, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.OPEN_MODE, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"})
            }
    )
    void showPivotTableAction();
}
