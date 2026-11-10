/*
 * Copyright 2026 Haulmont.
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
import io.jmix.flowui.kit.meta.StudioPropertiesItem;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioUiKit(requiredDependencies = {"io.jmix.aitools:jmix-aitools-flowui-starter",
        "io.jmix.gridexport:jmix-gridexport-flowui-starter"})
interface StudioAiToolsActions {

    @StudioAction(
            type = "aitls_askAiAboutGridData",
            description = "Opens a dialog to ask the AI chat about the selected rows or the current page of a data grid",
            classFqn = "io.jmix.aitoolsflowui.gridexport.AskAiAboutGridDataAction",
            target = {"io.jmix.flowui.component.ListDataComponent"},
            propertyGroups = StudioActionPropertyGroups.AskAiAboutGridDataActionComponent.class,
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.COLUMNS_TO_EXPORT, type = StudioPropertyType.ENUMERATION,
                            options = {"ALL_COLUMNS", "VISIBLE_COLUMNS"}),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.COLUMN_KEYS_TO_EXPORT, type = StudioPropertyType.VALUES_LIST)
            }
    )
    void askAiAboutGridData();
}
