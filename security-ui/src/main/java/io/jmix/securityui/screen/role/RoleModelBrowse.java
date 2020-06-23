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

import io.jmix.security.model.Role;
import io.jmix.security.model.RoleSource;
import io.jmix.security.role.RoleRepository;
import io.jmix.securityui.model.RoleModel;
import io.jmix.securityui.model.RoleModelConverter;
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

    @Install(to = "roleModelsTable.create", subject = "initializer")
    private void roleModelsTableCreateInitializer(RoleModel roleModel) {
        roleModel.setSource(RoleSource.DATABASE);
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

    @Install(to = "roleModelsTable.create", subject = "screenConfigurer")
    private void roleModelsTableCreateScreenConfigurer(Screen screen) {
        ((RoleModelEdit) screen).setOpenedByCreateAction(true);
    }
}