/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securityui.screen.roleassignment;

import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.model.ResourceRoleModel;
import io.jmix.securityui.model.RowLevelRoleModel;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@UiController("sec_RoleAssignmentFragment")
@UiDescriptor("role-assignment-fragment.xml")
public class RoleAssignmentFragment extends ScreenFragment {

    @Autowired
    private CollectionContainer<RoleAssignmentEntity> rowLevelRoleAssignmentEntitiesDc;

    @Autowired
    private CollectionContainer<RoleAssignmentEntity> resourceRoleAssignmentEntitiesDc;

    @Autowired
    private CollectionLoader<RoleAssignmentEntity> rowLevelRoleAssignmentEntitiesDl;

    @Autowired
    private CollectionLoader<RoleAssignmentEntity> resourceRoleAssignmentEntitiesDl;

    @Autowired
    private Metadata metadata;

    @Autowired
    private ResourceRoleRepository resourceRoleRepository;

    @Autowired
    private RowLevelRoleRepository rowLevelRoleRepository;

    @Autowired
    private UiComponents uiComponents;

    @Autowired
    private EntityStates entityStates;

    private UserDetails user;

    @Autowired
    private Table<RoleAssignmentEntity> resourceRoleAssignmentsTable;

    @Autowired
    private Table<RoleAssignmentEntity> rowLevelRoleAssignmentsTable;

    @Autowired
    private DataContext dataContext;

    @Install(to = "resourceRoleAssignmentsTable.roleName", subject = "columnGenerator")
    private Component resourceRoleAssignmentsTableRoleNameColumnGenerator(RoleAssignmentEntity roleAssignmentEntity) {
        BaseRole role = resourceRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
        Label<String> label = uiComponents.create(Label.TYPE_DEFAULT);
        if (role != null) {
            label.setValue(role.getName());
        }
        return label;
    }

    @Install(to = "rowLevelRoleAssignmentsTable.roleName", subject = "columnGenerator")
    private Component rowLevelRoleAssignmentsTableRoleNameColumnGenerator(RoleAssignmentEntity roleAssignmentEntity) {
        BaseRole role = rowLevelRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
        Label<String> label = uiComponents.create(Label.TYPE_DEFAULT);
        if (role != null) {
            label.setValue(role.getName());
        }
        return label;
    }

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onAfterShow(Screen.AfterShowEvent event) {
        if (!entityStates.isNew(user)) {
            resourceRoleAssignmentEntitiesDl.setParameter("username", user.getUsername());
            resourceRoleAssignmentEntitiesDl.setParameter("roleType", RoleAssignmentRoleType.RESOURCE);
            resourceRoleAssignmentEntitiesDl.load();

            rowLevelRoleAssignmentEntitiesDl.setParameter("username", user.getUsername());
            rowLevelRoleAssignmentEntitiesDl.setParameter("roleType", RoleAssignmentRoleType.ROW_LEVEL);
            rowLevelRoleAssignmentEntitiesDl.load();
        }
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    @Install(to = "resourceRoleAssignmentsTable.addResourceRole", subject = "transformation")
    private Collection<RoleAssignmentEntity> resourceRoleAssignmentsTableAddResourceRoleTransformation(Collection<ResourceRoleModel> roleModels) {
        Collection<String> assignedRoleCodes = resourceRoleAssignmentEntitiesDc.getItems().stream()
                .map(RoleAssignmentEntity::getRoleCode)
                .collect(Collectors.toSet());

        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
                    roleAssignmentEntity.setRoleCode(roleModel.getCode());
                    roleAssignmentEntity.setUsername(user.getUsername());
                    roleAssignmentEntity.setRoleType(RoleAssignmentRoleType.RESOURCE);
                    return roleAssignmentEntity;
                })
                .collect(Collectors.toList());
    }

    @Install(to = "rowLevelRoleAssignmentsTable.addRowLevelRole", subject = "transformation")
    private Collection<RoleAssignmentEntity> rowLevelRoleAssignmentsTableAddRowLevelRoleTransformation(Collection<RowLevelRoleModel> roleModels) {
        Collection<String> assignedRoleCodes = rowLevelRoleAssignmentEntitiesDc.getItems().stream()
                .map(RoleAssignmentEntity::getRoleCode)
                .collect(Collectors.toSet());

        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
                    roleAssignmentEntity.setRoleCode(roleModel.getCode());
                    roleAssignmentEntity.setUsername(user.getUsername());
                    roleAssignmentEntity.setRoleType(RoleAssignmentRoleType.ROW_LEVEL);
                    return roleAssignmentEntity;
                })
                .collect(Collectors.toList());
    }

    @Subscribe("resourceRoleAssignmentsTable.remove")
    public void onResourceRoleAssignmentsTableRemove(Action.ActionPerformedEvent event) {
        Set<RoleAssignmentEntity> selected = resourceRoleAssignmentsTable.getSelected();
        resourceRoleAssignmentEntitiesDc.getMutableItems().removeAll(selected);
        //do not immediately remove role assignments but do that only when role-assignment-screen is committed
        selected.forEach(dataContext::remove);
    }

    @Subscribe("rowLevelRoleAssignmentsTable.remove")
    public void onRowLevelRoleAssignmentsTableRemove(Action.ActionPerformedEvent event) {
        Set<RoleAssignmentEntity> selected = rowLevelRoleAssignmentsTable.getSelected();
        rowLevelRoleAssignmentEntitiesDc.getMutableItems().removeAll(selected);
        //do not immediately remove role assignments but do that only when role-assignment-screen is committed
        selected.forEach(dataContext::remove);
    }
}