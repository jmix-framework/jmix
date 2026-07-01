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

@StudioUiKit(requiredDependencies = "io.jmix.flowui:jmix-flowui-data-starter")
interface StudioGenericFilterDataActions {

    @StudioAction(
            type = "genericFilter_remove",
            description = "Removes current run-time filter configuration",
            classFqn = "io.jmix.flowuidata.action.genericfilter.GenericFilterRemoveAction",
            propertyGroups = StudioActionPropertyGroups.GenericFilterDataRemoveActionComponent.class)
    void removeAction();

    @StudioAction(
            type = "genericFilter_makeDefault",
            description = "Makes the filter configuration default for this view",
            classFqn = "io.jmix.flowuidata.action.genericfilter.GenericFilterMakeDefaultAction",
            propertyGroups = StudioActionPropertyGroups.MakeDefaultActionComponent.class)
    void makeDefaultAction();

    @StudioAction(
            type = "genericFilter_save",
            description = "Saves changes to current filter configuration",
            classFqn = "io.jmix.flowuidata.action.genericfilter.GenericFilterSaveAction",
            propertyGroups = StudioActionPropertyGroups.SaveActionComponent.class)
    void saveAction();

    @StudioAction(
            type = "genericFilter_saveAs",
            description = "Saves current filter configuration under a new id and name",
            classFqn = "io.jmix.flowuidata.action.genericfilter.GenericFilterSaveAsAction",
            propertyGroups = StudioActionPropertyGroups.SaveAsActionComponent.class)
    void saveAsAction();

    @StudioAction(
            type = "genericFilter_saveWithValues",
            description = "Saves changes to current filter configuration using the values in filter components as default values",
            classFqn = "io.jmix.flowuidata.action.genericfilter.GenericFilterSaveWithValuesAction",
            propertyGroups = StudioActionPropertyGroups.SaveWithValuesActionComponent.class)
    void saveWithValuesAction();
}
