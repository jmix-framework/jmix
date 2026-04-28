/*
 * Copyright 2023 Haulmont.
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

import io.jmix.flowui.kit.meta.*;

@StudioUiKit
interface StudioGenericFilterActions {

    @StudioAction(
            type = "genericFilter_addCondition",
            description = "Adds condition to current filter configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterAddConditionAction",
            propertyGroups = StudioActionPropertyGroups.AddConditionActionComponent.class)
    void addConditionAction();

    @StudioAction(
            type = "genericFilter_clearValues",
            description = "Clears the filter condition values",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction",
            propertyGroups = StudioActionPropertyGroups.ClearValuesActionComponent.class)
    void clearValuesAction();

    @StudioAction(
            type = "genericFilter_copy",
            description = "Copies all conditions from design-time configuration to run-time configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterCopyAction",
            propertyGroups = StudioActionPropertyGroups.CopyActionComponent.class)
    void copyAction();

    @StudioAction(
            type = "genericFilter_edit",
            description = "Edits current configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterEditAction",
            propertyGroups = StudioActionPropertyGroups.GenericFilterEditActionComponent.class)
    void editAction();

    @StudioAction(
            type = "genericFilter_reset",
            description = "Resets current configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterResetAction",
            propertyGroups = StudioActionPropertyGroups.ResetActionComponent.class)
    void resetAction();
}
