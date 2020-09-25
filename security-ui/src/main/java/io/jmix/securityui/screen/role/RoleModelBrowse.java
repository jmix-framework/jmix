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

package io.jmix.securityui.screen.role;

import io.jmix.core.Messages;
import io.jmix.security.model.Role;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.RoleType;
import io.jmix.security.role.RoleRepository;
import io.jmix.securityui.model.RoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
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

@UiController("sec_RoleModel.browse")
@UiDescriptor("role-model-browse.xml")
@LookupComponent("roleModelsTable")
@Route("roles")
public class RoleModelBrowse extends StandardLookup<RoleModel> {

    @Autowired
    private CollectionContainer<RoleModel> roleModelsDc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupTable<RoleModel> roleModelsTable;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private Dialogs dialogs;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        reloadRoles();
    }

    private void reloadRoles() {
        Collection<Role> roles = roleRepository.getAllRoles();
        List<RoleModel> roleModels = roles.stream()
                .map(roleModelConverter::createRoleModel)
                .sorted(Comparator.comparing(RoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.getMutableItems().clear();
        roleModelsDc.getMutableItems().addAll(roleModels);
    }

    @Subscribe("roleModelsTable.refresh")
    public void onRoleModelsTableRefresh(Action.ActionPerformedEvent event) {
        reloadRoles();
    }

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    private boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    @Subscribe("roleModelsTable.createResourceRole")
    public void onRoleModelsTableCreateResourceRole(Action.ActionPerformedEvent event) {
        RoleModelEdit editor = screenBuilders.editor(roleModelsTable)
                .withScreenClass(RoleModelEdit.class)
                .newEntity()
                .withInitializer(roleModel -> {
                    roleModel.setSource(RoleSource.DATABASE);
                    roleModel.setRoleType(RoleType.RESOURCE);
                })
                .build();
        editor.setOpenedByCreateAction(true);
        editor.show();
    }

    @Subscribe("roleModelsTable.createRowLevelRole")
    public void onRoleModelsTableCreateRowLevelRole(Action.ActionPerformedEvent event) {
        RoleModelEdit editor = screenBuilders.editor(roleModelsTable)
                .withScreenClass(RoleModelEdit.class)
                .newEntity()
                .withInitializer(roleModel -> {
                    roleModel.setSource(RoleSource.DATABASE);
                    roleModel.setRoleType(RoleType.ROW_LEVEL);
                })
                .build();
        editor.setOpenedByCreateAction(true);
        editor.show();
    }

    @Subscribe("roleModelsTable.createAggregatedRole")
    public void onRoleModelsTableCreateAggregatedRole(Action.ActionPerformedEvent event) {
        RoleModelEdit editor = screenBuilders.editor(roleModelsTable)
                .withScreenClass(RoleModelEdit.class)
                .newEntity()
                .withInitializer(roleModel -> {
                    roleModel.setSource(RoleSource.DATABASE);
                    roleModel.setRoleType(RoleType.AGGREGATED);
                })
                .build();
        editor.setOpenedByCreateAction(true);
        editor.show();
    }

    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(Action.ActionPerformedEvent event) {
        dialogs.createOptionDialog()
                .withCaption(messages.getMessage("dialogs.Confirmation"))
                .withMessage(messages.getMessage("dialogs.Confirmation.Remove"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                            RoleModel roleModel = roleModelsTable.getSingleSelected();
                            if (roleModel == null) {
                                return;
                            }
                            String code = roleModel.getCode();
                            try {
                                boolean deleted = roleRepository.deleteRole(code);
                                if (deleted) {
                                    roleModelsDc.getMutableItems().remove(roleModel);
                                } else {
                                    notifications.create()
                                            .withCaption(messageBundle.getMessage("RoleModelBrowse.unableToRemove"))
                                            .withDescription(messageBundle.getMessage("RoleModelBrowse.noPermission"))
                                            .withType(Notifications.NotificationType.HUMANIZED)
                                            .show();
                                }
                            } catch (UnsupportedOperationException | IllegalArgumentException ex) {
                                notifications.create()
                                        .withCaption(messageBundle.getMessage("RoleModelBrowse.unableToRemove"))
                                        .withDescription(ex.getMessage())
                                        .withType(Notifications.NotificationType.ERROR)
                                        .show();
                            }
                        }),
                        new DialogAction(DialogAction.Type.NO)
                )
                .show();
    }

    private boolean isDatabaseRoleSelected() {
        Set<RoleModel> selected = roleModelsTable.getSelected();
        if (selected.size() == 1) {
            RoleModel roleModel = selected.iterator().next();
            return RoleSource.DATABASE.equals(roleModel.getSource());
        }
        return false;
    }

    @Install(to = "roleModelsTable.source", subject = "valueProvider")
    private Object roleModelsTableSourceValueProvider(RoleModel roleModel) {
        return messageBundle.getMessage("roleSource." + roleModel.getSource());
    }
}