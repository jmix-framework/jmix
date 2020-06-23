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
import io.jmix.core.entity.BaseUser;
import io.jmix.security.model.Role;
import io.jmix.security.role.RoleRepository;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.model.RoleModel;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.stream.Collectors;

@UiController("sec_RoleAssignmentFragment")
@UiDescriptor("role-assignment-fragment.xml")
public class RoleAssignmentFragment extends ScreenFragment {

    @Autowired
    private CollectionLoader<RoleAssignmentEntity> roleAssignmentEntitiesDl;

    @Autowired
    private CollectionContainer<RoleAssignmentEntity> roleAssignmentEntitiesDc;

    @Autowired
    private Metadata metadata;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UiComponents uiComponents;

    @Autowired
    private GroupTable<RoleAssignmentEntity> roleAssignmentsTable;

    @Autowired
    private EntityStates entityStates;

    private InstanceContainer<? extends BaseUser> userDc;

    @Subscribe
    public void onInit(InitEvent event) {
        roleAssignmentsTable.addGeneratedColumn("roleName", roleAssignmentEntity -> {
            Role role = roleRepository.getRoleByCode(roleAssignmentEntity.getRoleCode());
            Label<String> label = uiComponents.create(Label.TYPE_DEFAULT);
            if (role != null) {
                label.setValue(role.getName());
            }
            return label;
        });
    }

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onAfterShow(Screen.AfterShowEvent event) {
        BaseUser user = userDc.getItem();
        if (!entityStates.isNew(user)) {
            roleAssignmentEntitiesDl.setParameter("userKey", user.getKey());
            roleAssignmentEntitiesDl.load();
        }
    }

    public void setUserDc(InstanceContainer<? extends BaseUser> userDc) {
        this.userDc = userDc;
    }

    @Install(to = "roleAssignmentsTable.add", subject = "transformation")
    private Collection<RoleAssignmentEntity> roleAssignmentsTableAddTransformation(Collection<RoleModel> roleModels) {
        BaseUser user = userDc.getItem();
        Collection<String> assignedRoleCodes = getAssignedRoleCodes();
        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
                    roleAssignmentEntity.setRoleCode(roleModel.getCode());
                    roleAssignmentEntity.setUserKey(user.getKey());
                    return roleAssignmentEntity;
                })
                .collect(Collectors.toList());
    }

    private Collection<String> getAssignedRoleCodes() {
        return roleAssignmentEntitiesDc.getItems().stream()
                .map(RoleAssignmentEntity::getRoleCode)
                .collect(Collectors.toList());
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPreCommit(DataContext.PreCommitEvent event) {
        //we don't know userKey when a new user is created, so in this case we set userKey for role assignment when
        //user is saved
        BaseUser user = userDc.getItem();
        if (entityStates.isNew(user)) {
            event.getModifiedInstances().stream()
                    .filter(entity -> entity instanceof RoleAssignmentEntity)
                    .forEach(roleAssEntity -> ((RoleAssignmentEntity) roleAssEntity).setUserKey(user.getKey()));
        }
    }
}