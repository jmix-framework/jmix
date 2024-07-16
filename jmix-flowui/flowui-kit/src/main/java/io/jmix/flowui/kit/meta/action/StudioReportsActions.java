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

@StudioUiKit(requiredDependencies = "io.jmix.reports:jmix-reports")
public interface StudioReportsActions {

        @StudioAction(
                type = "report_runReport",
                description = "A standard action that displays the list of all available reports.",
                classFqn = "io.jmix.reportsflowui.action.RunReportAction",
                icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
                target = {"io.jmix.flowui.action.list.ListDataComponentAction"},
                properties = {
                        @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                                setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                                defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                        @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                        @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                        @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                                setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                        @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL,
                                type = StudioPropertyType.COMPONENT_ID, required = true),
                        @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                        @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                        @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
                }
        )
        void runReportAction();


    @StudioAction(
            type = "report_runSingleEntityReport",
            description = "A standard action for printing reports for entity instance.",
            classFqn = "io.jmix.reportsflowui.action.RunSingleEntityReportAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.action.view.OperationResultViewAction"},
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void runSingleEntityReportAction();


    @StudioAction(
            type = "report_runListEntityReport",
            description = "A standard action for printing reports for entity instances associated with a list component (Table, DataGrid, etc.).",
            classFqn = "io.jmix.reportsflowui.action.RunListEntityReportAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.action.list.ListDataComponentAction"},
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void runListEntityReportAction();

    @StudioAction(
            type = "report_showExecutionReportHistory",
            description = "A standard action for displaying the report execution history.",
            classFqn = "io.jmix.reportsflowui.action.ShowExecutionReportHistoryAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.action.list.ListDataComponentAction"},
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void showExecutionReportHistoryAction();

}
