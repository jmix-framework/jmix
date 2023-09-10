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

@StudioUiKit(requiredDependencies = "io.jmix.security:jmix-security-flowui-starter")
public interface StudioSecurityActions {

    @StudioAction(
            type = "sec_showRoleAssignments",
            description = "Shows the role assignments for the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ShowRoleAssignmentsAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "showRoleAssignments"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.showRoleAssignments"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void showRoleAssignmentsAction();


    @StudioAction(
            type = "sec_showUserSubstitutions",
            description = "Shows the user substitutions",
            classFqn = "io.jmix.securityflowui.action.ShowUserSubstitutionsAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "showUserSubstitutions"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.showUserSubstitutions"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void showUserSubstitutionsAction();

    @StudioAction(
            type = "sec_assignToUsers",
            description = "Assign the role to selected users",
            classFqn = "io.jmix.securityflowui.action.AssignToUsersAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "USERS",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "assignToUsers"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.assignToUsers"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void assignToUsersAction();

    @StudioAction(
            type = "sec_changePassword",
            description = "Changes the password of the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ChangePasswordAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "changePassword"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.changePassword"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void changePasswordAction();

    @StudioAction(
            type = "sec_resetPassword",
            description = "Resets the password of the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ResetPasswordAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "resetPassword"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.resetPassword"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void resetPasswordAction();
}
