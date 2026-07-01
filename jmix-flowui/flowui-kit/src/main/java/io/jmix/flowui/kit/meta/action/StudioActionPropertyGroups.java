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

import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.StudioPropertyGroup;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioAPI
final class StudioActionPropertyGroups {

    private StudioActionPropertyGroups() {
    }

    @StudioPropertyGroup
    public interface BaseAction extends StudioPropertyGroups.ActionVariantWithDefaultDefaultValue,
            StudioPropertyGroups.Description, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface ActionDefaultProperties extends BaseAction {
    }

    @StudioPropertyGroup
    public interface TextActionDefaultProperties extends BaseAction, StudioPropertyGroups.Text {
    }

    @StudioPropertyGroup
    public interface IconActionDefaultProperties extends BaseAction, StudioPropertyGroups.Icon {
    }

    @StudioPropertyGroup
    public interface IconTextActionDefaultProperties extends IconActionDefaultProperties, StudioPropertyGroups.Text {
    }

    @StudioPropertyGroup
    public interface RequiredIconTextActionDefaultProperties extends IconTextActionDefaultProperties,
            StudioPropertyGroups.RequiredId {
    }

    @StudioPropertyGroup
    public interface NoShortcutBaseAction extends
            StudioPropertyGroups.ActionVariantWithDefaultDefaultValue, StudioPropertyGroups.Description,
            StudioPropertyGroups.Enabled, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface ActionDefaultPropertiesWithoutShortcutCombination extends NoShortcutBaseAction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION,
            type = StudioPropertyType.SHORTCUT_COMBINATION,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "ENTER"))
    public interface ShortcutCombinationWithEnterDefaultValue {
    }

    @StudioPropertyGroup
    public interface DangerActionDefaultProperties extends StudioPropertyGroups.ActionVariantWithDangerDefaultValue,
            StudioPropertyGroups.Description, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface PrimaryActionDefaultProperties extends StudioPropertyGroups.ActionVariantWithPrimaryDefaultValue,
            StudioPropertyGroups.Description, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface PrimaryActionDefaultPropertiesWithCreateIdAndPlusIcon extends
            PrimaryActionDefaultProperties, StudioPropertyGroups.RequiredIdWithCreateInitialValue,
            StudioPropertyGroups.IconWithPlusDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "SIGN_OUT",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "logout"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.logout.description")
            }
    )
    public interface LogoutActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "themeSwitchAction"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.userMenu.ThemeSwitch")
            }
    )
    public interface UserMenuThemeSwitchActionComponent extends IconActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "TRASH",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_remove")
            }
    )
    public interface GenericFilterDataRemoveActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "STAR",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_makeDefault")
            }
    )
    public interface MakeDefaultActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ARCHIVE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_save")
            }
    )
    public interface SaveActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ARCHIVE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_saveAs")
            }
    )
    public interface SaveAsActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ARCHIVE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_saveWithValues")
            }
    )
    public interface SaveWithValuesActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "FILE_TABLE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "excelExport"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///excelExporter.label")
            }
    )
    public interface ExcelExportComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "FILE_CODE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "jsonExport"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///jsonExporter.label")
            }
    )
    public interface JsonExportComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "TABLE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "bulkEdit"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.BulkEdit")
            }
    )
    public interface BulkEditActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "showEntityInfo"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg://io.jmix.datatoolsflowui.action/showEntityInfoAction.title")
            }
    )
    public interface ShowEntityInfoActionComponent extends IconActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "showRoleAssignments"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.ShowRoleAssignments")
            }
    )
    public interface ShowRoleAssignmentsActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "showUserSubstitutions"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.showUserSubstitutions")
            }
    )
    public interface ShowUserSubstitutionsActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "USERS",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "assignToUsers"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.assignToUsers")
            }
    )
    public interface AssignToUsersActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "changePassword"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.changePassword")
            }
    )
    public interface ChangePasswordActionComponent extends IconActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "resetPassword"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.resetPassword")
            }
    )
    public interface ResetPasswordActionComponent extends IconActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "substituteUserAction"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.userMenu.SubstituteUser"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "EXCHANGE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_SUBSTITUTIONS,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL)
            }
    )
    public interface UserMenuSubstituteUserComponent extends BaseAction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            required = true,
            initialValue = "ABACUS",
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface BaseActionComponent extends TextActionDefaultProperties, StudioPropertyGroups.RequiredId {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "valueClear"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.valuePicker.clear.description")
            }
    )
    public interface ValueClearActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "entityClear"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.valuePicker.clear.description")
            }
    )
    public interface EntityClearActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ELLIPSIS_DOTS_H",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "entityLookup"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.entityPicker.lookup.description")
            }
    )
    public interface EntityLookupActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "SEARCH",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "entityOpen"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.entityPicker.open.description")
            }
    )
    public interface EntityOpenActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "SEARCH",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "entityOpenComposition"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.entityPicker.open.description")
            }
    )
    public interface EntityOpenCompositionActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ELLIPSIS_DOTS_H",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "multiValueSelect"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.multiValuePicker.select.description")
            }
    )
    public interface MultiValueSelectActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "viewClose"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Cancel")
            }
    )
    public interface ViewCloseActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "CHECK",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "lookupSelect"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Select")
            }
    )
    public interface LookupSelectActionComponent extends PrimaryActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "lookupDiscard"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Cancel")
            }
    )
    public interface LookupDiscardActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "detailClose"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Cancel")
            }
    )
    public interface DetailCloseActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ARCHIVE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "detailSave"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Save")
            }
    )
    public interface DetailSaveActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "CHECK",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "detailSaveClose"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Ok")
            }
    )
    public interface DetailSaveCloseActionComponent extends PrimaryActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "detailDiscard"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Cancel")
            }
    )
    public interface DetailDiscardActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "detailEnableEditing"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.EnableEditing")
            }
    )
    public interface DetailEnableEditingActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true,
            initialValue = "genericFilter_addCondition"))
    public interface AddConditionActionComponent extends TextActionDefaultProperties,
            StudioPropertyGroups.IconWithPlusDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "ERASER",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_clearValues")
            }
    )
    public interface ClearValuesActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "COPY",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_copy")
            }
    )
    public interface CopyActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "genericFilter_edit")
            }
    )
    public interface GenericFilterEditActionComponent extends TextActionDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true,
            initialValue = "genericFilter_reset"))
    public interface ResetActionComponent extends IconTextActionDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "msg:///actions.Create"))
    public interface CreateActionComponent extends PrimaryActionDefaultPropertiesWithCreateIdAndPlusIcon {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "edit"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Edit")
            }
    )
    public interface ListDataComponentEditActionComponent extends NoShortcutBaseAction,
            ShortcutCombinationWithEnterDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "remove"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Remove")
            }
    )
    public interface ListDataComponentRemoveActionComponent extends DangerActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "add"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Add")
            }
    )
    public interface AddActionComponent extends PrimaryActionDefaultProperties,
            StudioPropertyGroups.IconWithPlusDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "exclude"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Exclude")
            }
    )
    public interface ExcludeActionComponent extends DangerActionDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "EYE",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "read"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Read")
            }
    )
    public interface ReadActionComponent extends NoShortcutBaseAction, ShortcutCombinationWithEnterDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "REFRESH",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "refresh"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.Refresh")
            }
    )
    public interface RefreshActionComponent extends BaseAction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true,
            initialValue = "itemTracking"))
    public interface ItemTrackingActionComponent extends IconTextActionDefaultProperties {
    }

}
