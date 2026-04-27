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
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioUiKit(requiredDependencies = "io.jmix.security:jmix-security-flowui-starter")
interface StudioSecurityActions {

    @StudioAction(
            type = "sec_showRoleAssignments",
            description = "Shows the role assignments for the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ShowRoleAssignmentsAction",
            propertyGroups = StudioActionPropertyGroups.ShowRoleAssignmentsActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "showRoleAssignments"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.ShowRoleAssignments"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void showRoleAssignmentsAction();

    @StudioAction(
            type = "sec_showUserSubstitutions",
            description = "Shows the user substitutions",
            classFqn = "io.jmix.securityflowui.action.ShowUserSubstitutionsAction",
            propertyGroups = StudioActionPropertyGroups.ShowUserSubstitutionsActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "showUserSubstitutions"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.showUserSubstitutions"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void showUserSubstitutionsAction();

    @StudioAction(
            type = "sec_assignToUsers",
            description = "Assign the role to selected users",
            classFqn = "io.jmix.securityflowui.action.AssignToUsersAction",
            propertyGroups = StudioActionPropertyGroups.AssignToUsersActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "USERS",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "assignToUsers"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.assignToUsers"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void assignToUsersAction();

    @StudioAction(
            type = "sec_changePassword",
            description = "Changes the password of the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ChangePasswordAction",
            propertyGroups = StudioActionPropertyGroups.ChangePasswordActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "changePassword"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.changePassword"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void changePasswordAction();

    @StudioAction(
            type = "sec_resetPassword",
            description = "Resets the password of the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ResetPasswordAction",
            propertyGroups = StudioActionPropertyGroups.ResetPasswordActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "resetPassword"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.resetPassword"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void resetPasswordAction();

    @StudioAction(
            type = "sec_userMenuSubstituteUser",
            description = "An action that, depending on the number of substituted users, either opens a lookup view " +
                    "to select a substituted user or shows a submenu with the substituted users.",
            classFqn = "io.jmix.securityflowui.action.UserMenuSubstituteUserAction",
            propertyGroups = StudioActionPropertyGroups.UserMenuSubstituteUserComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "substituteUserAction"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.userMenu.SubstituteUser"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "EXCHANGE"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_SUBSTITUTIONS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"})
            }
    )
    void userMenuSubstituteUser();
}
