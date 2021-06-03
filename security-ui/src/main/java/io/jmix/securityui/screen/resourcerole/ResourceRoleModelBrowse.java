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

package io.jmix.securityui.screen.resourcerole;

import com.google.common.collect.Sets;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.model.BaseRoleModel;
import io.jmix.securityui.model.ResourceRoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.securityui.screen.role.RemoveRoleConsumer;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UiController("sec_ResourceRoleModel.browse")
@UiDescriptor("resource-role-model-browse.xml")
@LookupComponent("roleModelsTable")
@Route("resourceRoles")
public class ResourceRoleModelBrowse extends StandardLookup<ResourceRoleModel> {

    @Autowired
    private CollectionContainer<ResourceRoleModel> roleModelsDc;

    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private GroupTable<ResourceRoleModel> roleModelsTable;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    protected RemoveOperation removeOperation;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles();
    }

    @Subscribe("roleModelsTable.refresh")
    public void onRoleModelsTableRefresh(Action.ActionPerformedEvent event) {
        loadRoles();
    }

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    private boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    @Subscribe("roleModelsTable.create")
    public void onRoleModelsTableCreateResourceRole(Action.ActionPerformedEvent event) {
        ResourceRoleModelEdit editor = screenBuilders.editor(roleModelsTable)
                .withScreenClass(ResourceRoleModelEdit.class)
                .newEntity()
                .withInitializer(roleModel -> {
                    roleModel.setSource(RoleSource.DATABASE);
                    roleModel.setScopes(Sets.newHashSet(SecurityScope.UI));
                })
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                        loadRoles();
                    }
                })
                .build();
        editor.setOpenedByCreateAction(true);
        editor.show();
    }


    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(Action.ActionPerformedEvent event) {
        removeOperation.builder(roleModelsTable)
                .withConfirmation(true)
                .beforeActionPerformed(new RemoveRoleConsumer<>(roleRepository, notifications, messages))
                .afterActionPerformed((afterActionConsumer) -> {
                    List<RoleAssignmentEntity> roleAssignmentEntities = dataManager.load(RoleAssignmentEntity.class)
                            .query("e.roleCode IN :codes")
                            .parameter("codes", afterActionConsumer.getItems().stream()
                                    .map(BaseRoleModel::getCode)
                                    .collect(Collectors.toList()))
                            .list();
                    dataManager.remove(roleAssignmentEntities);
                })
                .remove();
    }

    @Install(to = "roleModelsTable.source", subject = "valueProvider")
    public Object roleModelsTableSourceValueProvider(ResourceRoleModel roleModel) {
        return messages.getMessage("io.jmix.securityui.model/roleSource." + roleModel.getSource());
    }

    protected void loadRoles() {
        Collection<ResourceRole> roles = roleRepository.getAllRoles();
        List<ResourceRoleModel> roleModels = roles.stream()
                .map(roleModelConverter::createResourceRoleModel)
                .sorted(Comparator.comparing(ResourceRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.getMutableItems().clear();
        roleModelsDc.getMutableItems().addAll(roleModels);
    }

    protected boolean isDatabaseRoleSelected() {
        Set<ResourceRoleModel> selected = roleModelsTable.getSelected();
        if (selected.size() == 1) {
            ResourceRoleModel roleModel = selected.iterator().next();
            return RoleSource.DATABASE.equals(roleModel.getSource());
        }
        return false;
    }
}