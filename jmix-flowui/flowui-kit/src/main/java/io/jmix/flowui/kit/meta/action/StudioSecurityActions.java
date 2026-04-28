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
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;

@StudioUiKit(requiredDependencies = "io.jmix.security:jmix-security-flowui-starter")
interface StudioSecurityActions {

    @StudioAction(
            type = "sec_showRoleAssignments",
            description = "Shows the role assignments for the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ShowRoleAssignmentsAction",
            propertyGroups = StudioActionPropertyGroups.ShowRoleAssignmentsActionComponent.class)
    void showRoleAssignmentsAction();

    @StudioAction(
            type = "sec_showUserSubstitutions",
            description = "Shows the user substitutions",
            classFqn = "io.jmix.securityflowui.action.ShowUserSubstitutionsAction",
            propertyGroups = StudioActionPropertyGroups.ShowUserSubstitutionsActionComponent.class)
    void showUserSubstitutionsAction();

    @StudioAction(
            type = "sec_assignToUsers",
            description = "Assign the role to selected users",
            classFqn = "io.jmix.securityflowui.action.AssignToUsersAction",
            propertyGroups = StudioActionPropertyGroups.AssignToUsersActionComponent.class)
    void assignToUsersAction();

    @StudioAction(
            type = "sec_changePassword",
            description = "Changes the password of the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ChangePasswordAction",
            propertyGroups = StudioActionPropertyGroups.ChangePasswordActionComponent.class)
    void changePasswordAction();

    @StudioAction(
            type = "sec_resetPassword",
            description = "Resets the password of the UserDetails instance",
            classFqn = "io.jmix.securityflowui.action.ResetPasswordAction",
            propertyGroups = StudioActionPropertyGroups.ResetPasswordActionComponent.class)
    void resetPasswordAction();

    @StudioAction(
            type = "sec_userMenuSubstituteUser",
            description = "An action that, depending on the number of substituted users, either opens a lookup view " +
                    "to select a substituted user or shows a submenu with the substituted users.",
            classFqn = "io.jmix.securityflowui.action.UserMenuSubstituteUserAction",
            propertyGroups = StudioActionPropertyGroups.UserMenuSubstituteUserComponent.class)
    void userMenuSubstituteUser();
}
