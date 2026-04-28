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

import io.jmix.flowui.kit.meta.*;

@StudioUiKit(requiredDependencies = "io.jmix.reports:jmix-reports-starter")
interface StudioReportsActions {

        @StudioAction(
                type = "report_runReport",
                description = "A standard action that displays the list of all available reports.",
                classFqn = "io.jmix.reportsflowui.action.RunReportAction",
                target = {"io.jmix.flowui.action.list.ListDataComponentAction"},
            propertyGroups = StudioActionPropertyGroups.RequiredIconTextActionDefaultProperties.class)
        void runReportAction();


    @StudioAction(
            type = "report_runSingleEntityReport",
            description = "A standard action for printing reports for entity instance.",
            classFqn = "io.jmix.reportsflowui.action.RunSingleEntityReportAction",
            target = {"io.jmix.flowui.action.view.OperationResultViewAction"},
            propertyGroups = StudioActionPropertyGroups.RequiredIconTextActionDefaultProperties.class)
    void runSingleEntityReportAction();


    @StudioAction(
            type = "report_runListEntityReport",
            description = "A standard action for printing reports for entity instances associated with a list component (Table, DataGrid, etc.).",
            classFqn = "io.jmix.reportsflowui.action.RunListEntityReportAction",
            target = {"io.jmix.flowui.action.list.ListDataComponentAction"},
            propertyGroups = StudioActionPropertyGroups.RequiredIconTextActionDefaultProperties.class)
    void runListEntityReportAction();

    @StudioAction(
            type = "report_showExecutionReportHistory",
            description = "A standard action for displaying the report execution history.",
            classFqn = "io.jmix.reportsflowui.action.ShowExecutionReportHistoryAction",
            target = {"io.jmix.flowui.action.list.ListDataComponentAction"},
            propertyGroups = StudioActionPropertyGroups.RequiredIconTextActionDefaultProperties.class)
    void showExecutionReportHistoryAction();

}
