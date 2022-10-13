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

package io.jmix.securityflowui.view.rowlevelrole;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import io.jmix.securityflowui.model.*;
import io.jmix.securityflowui.util.RemoveRoleConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "sec/rowlevelrolemodels", layout = DefaultMainViewParent.class)
@ViewController("sec_RowLevelRoleModel.list")
@ViewDescriptor("row-level-role-model-list-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em", height = "37.5em")
public class RowLevelRoleModelListView extends StandardListView<RowLevelRoleModel> {

    @ViewComponent
    private DataGrid<RowLevelRoleModel> roleModelsTable;

    @ViewComponent
    private CollectionContainer<RowLevelRoleModel> roleModelsDc;

    @Autowired
    private Messages messages;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private RemoveOperation removeOperation;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private RowLevelRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilter();
    }

    private void initFilter() {
        RoleFilter filter = uiComponents.create(RoleFilter.class);
        filter.addRoleFilterChangeListener(this::onRoleFilterChange);

        getContent().addComponentAsFirst(filter);
    }

    private void onRoleFilterChange(RoleFilterChangeEvent event) {
        loadRoles(event);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles(null);
    }

    private void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<RowLevelRoleModel> roleModels = roleRepository.getAllRoles().stream()
                .filter(role -> event == null || event.matches(role))
                .map(roleModelConverter::createRowLevelRoleModel)
                .sorted(Comparator.comparing(RowLevelRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.setItems(roleModels);
    }

    @Install(to = "roleModelsTable.create", subject = "routeParametersProvider")
    public RouteParameters roleModelsTableCreateRouteParametersProvider() {
        return new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, StandardDetailView.NEW_ENTITY_ID);
    }

    @Install(to = "roleModelsTable.edit", subject = "routeParametersProvider")
    public RouteParameters roleModelsTableEditRouteParametersProvider() {
        RowLevelRoleModel selectedItem = roleModelsTable.getSingleSelectedItem();
        if (selectedItem != null) {
            String serializedCode = urlParamSerializer.serialize(selectedItem.getCode());
            return new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode);
        }

        return null;
    }

    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(ActionPerformedEvent event) {
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

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    private boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    private boolean isDatabaseRoleSelected() {
        Set<RowLevelRoleModel> selected = roleModelsTable.getSelectedItems();
        if (selected.size() == 1) {
            RowLevelRoleModel roleModel = selected.iterator().next();
            return RoleSource.DATABASE.equals(roleModel.getSource());
        }

        return false;
    }
}