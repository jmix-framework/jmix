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

package io.jmix.securityui.screen.rowlevelrole;

import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.security.model.RoleSource;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.model.BaseRoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.securityui.model.RowLevelRoleModel;
import io.jmix.securityui.screen.role.RemoveRoleConsumer;
import io.jmix.securityui.screen.rolefilter.RoleFilter;
import io.jmix.securityui.screen.rolefilter.RoleFilterFragment;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UiController("sec_RowLevelRoleModel.browse")
@UiDescriptor("row-level-role-model-browse.xml")
@LookupComponent("roleModelsTable")
@Route("rowLevelRoles")
public class RowLevelRoleModelBrowse extends StandardLookup<RowLevelRoleModel> {

    @Autowired
    private CollectionContainer<RowLevelRoleModel> roleModelsDc;

    @Autowired
    private RowLevelRoleRepository roleRepository;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private GroupTable<RowLevelRoleModel> roleModelsTable;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private RemoveOperation removeOperation;

    @Autowired
    private RoleFilterFragment filterFragment;

    private RoleFilter roleFilter = new RoleFilter();

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles();

        filterFragment.setChangeListener(roleFilter -> {
            this.roleFilter = roleFilter;
            loadRoles();
        });
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
        RowLevelRoleModelEdit editor = screenBuilders.editor(roleModelsTable)
                .withScreenClass(RowLevelRoleModelEdit.class)
                .newEntity()
                .withInitializer(roleModel -> roleModel.setSource(RoleSource.DATABASE))
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
    private Object roleModelsTableSourceValueProvider(RowLevelRoleModel roleModel) {
        return messages.getMessage("io.jmix.securityui.model/roleSource." + roleModel.getSource());
    }

    protected void loadRoles() {
        List<RowLevelRoleModel> roleModels = roleRepository.getAllRoles().stream()
                .filter(role -> roleFilter.matches(role))
                .map(roleModelConverter::createRowLevelRoleModel)
                .sorted(Comparator.comparing(RowLevelRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.getMutableItems().clear();
        roleModelsDc.getMutableItems().addAll(roleModels);
    }

    protected boolean isDatabaseRoleSelected() {
        Set<RowLevelRoleModel> selected = roleModelsTable.getSelected();
        if (selected.size() == 1) {
            RowLevelRoleModel roleModel = selected.iterator().next();
            return RoleSource.DATABASE.equals(roleModel.getSource());
        }
        return false;
    }
}