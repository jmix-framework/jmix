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
import io.jmix.flowui.kit.meta.StudioPropertiesItem;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioUiKit
interface StudioViewActions {

    @StudioAction(
            type = "view_close",
            description = "Closes the view",
            classFqn = "io.jmix.flowui.action.view.ViewCloseAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#view_close",
            propertyGroups = StudioActionPropertyGroups.ViewCloseActionComponent.class,
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.OUTCOME, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.StandardOutcome",
                            defaultValue = "CLOSE", options = {"CLOSE", "SAVE", "DISCARD", "SELECT"}),
            }
    )
    void viewCloseAction();

    @StudioAction(
            type = "lookup_select",
            description = "Selects item in lookup view",
            classFqn = "io.jmix.flowui.action.view.LookupSelectAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#lookup_select",
            propertyGroups = StudioActionPropertyGroups.LookupSelectActionComponent.class)
    void lookupSelectAction();

    @StudioAction(
            type = "lookup_discard",
            description = "Discards selection in lookup view",
            classFqn = "io.jmix.flowui.action.view.LookupDiscardAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#lookup_discard",
            propertyGroups = StudioActionPropertyGroups.LookupDiscardActionComponent.class)
    void lookupDiscardAction();

    @StudioAction(
            type = "detail_close",
            description = "Closes the detail view",
            classFqn = "io.jmix.flowui.action.view.DetailCloseAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#detail_close",
            propertyGroups = StudioActionPropertyGroups.DetailCloseActionComponent.class)
    void detailCloseAction();

    @StudioAction(
            type = "detail_save",
            description = "Saves changes in the detail view",
            classFqn = "io.jmix.flowui.action.view.DetailSaveAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#detail_save",
            propertyGroups = StudioActionPropertyGroups.DetailSaveActionComponent.class)
    void detailSaveAction();

    @StudioAction(
            type = "detail_saveClose",
            description = "Saves changes and closes the detail view",
            classFqn = "io.jmix.flowui.action.view.DetailSaveCloseAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#detail_saveClose",
            propertyGroups = StudioActionPropertyGroups.DetailSaveCloseActionComponent.class)
    void detailSaveCloseAction();

    @StudioAction(
            type = "detail_discard",
            description = "Discards changes in the detail view",
            classFqn = "io.jmix.flowui.action.view.DetailDiscardAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#detail_discard",
            propertyGroups = StudioActionPropertyGroups.DetailDiscardActionComponent.class)
    void detailDiscardAction();

    @StudioAction(
            type = "detail_enableEditing",
            description = "Enables editing in the detail view",
            classFqn = "io.jmix.flowui.action.view.DetailEnableEditingAction",
            documentationLink = "%VERSION%/flow-ui/actions/view-actions.html#detail_enableEditing",
            propertyGroups = StudioActionPropertyGroups.DetailEnableEditingActionComponent.class)
    void detailEnableEditingAction();
}
