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
import io.jmix.core.Messages;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.*;
import io.jmix.security.role.RolePersistence;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "sec/rowlevelrolemodels/:code", layout = DefaultMainViewParent.class)
@ViewController("sec_RowLevelRoleModel.detail")
@ViewDescriptor("row-level-role-model-detail-view.xml")
@EditedEntityContainer("roleModelDc")
public class RowLevelRoleModelDetailView extends StandardDetailView<RowLevelRoleModel> {

    private static final Logger log = LoggerFactory.getLogger(RowLevelRoleModelDetailView.class);

    public static final String ROUTE_PARAM_NAME = "code";

    @ViewComponent
    private TypedTextField<String> codeField;
    @ViewComponent
    private DataGrid<RowLevelRoleModel> childRolesTable;
    @ViewComponent
    private DataGrid<RowLevelPolicyModel> rowLevelPoliciesTable;

    @ViewComponent
    private InstanceContainer<RowLevelRoleModel> roleModelDc;
    @ViewComponent
    private CollectionContainer<RowLevelRoleModel> childRolesDc;

    @Autowired
    private Messages messages;
    @Autowired(required = false)
    private RolePersistence rolePersistence;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private RowLevelRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private DataContext dataContext;

    @Subscribe
    public void onInitNewEntity(InitEntityEvent<RowLevelRoleModel> event) {
        codeField.setReadOnly(false);

        RowLevelRoleModel entity = event.getEntity();
        entity.setSource(RoleSourceType.DATABASE);
    }

    @Override
    protected String getRouteParamName() {
        return ROUTE_PARAM_NAME;
    }

    @Override
    protected void setupEntityToEdit(RowLevelRoleModel entityToEdit) {
        // do nothing
    }

    @Override
    protected void initExistingEntity(String serializedEntityCode) {
        String code = urlParamSerializer.deserialize(String.class, serializedEntityCode);
        RowLevelRole roleByCode = roleRepository.findRoleByCode(code);

        RowLevelRoleModel rowLevelRoleModel = roleModelConverter.createRowLevelRoleModel(roleByCode);

        childRolesDc.mute();
        childRolesDc.setItems(loadChildRoleModels(rowLevelRoleModel));
        childRolesDc.unmute();

        RowLevelRoleModel merged = dataContext.merge(rowLevelRoleModel);
        roleModelDc.setItem(merged);
    }

    protected List<RowLevelRoleModel> loadChildRoleModels(RowLevelRoleModel editedRoleModel) {
        if (editedRoleModel.getChildRoles() == null || editedRoleModel.getChildRoles().isEmpty()) {
            return Collections.emptyList();
        }
        List<RowLevelRoleModel> childRoleModels = new ArrayList<>();
        for (String code : editedRoleModel.getChildRoles()) {
            RowLevelRole child = roleRepository.findRoleByCode(code);
            if (child != null) {
                childRoleModels.add(roleModelConverter.createRowLevelRoleModel(child));
            } else {
                log.warn("Role {} was not found while collecting child roles for aggregated role {}",
                        editedRoleModel.getCode(), code);
            }
        }
        return childRoleModels;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        setupRoleReadOnlyMode();
    }

    private void setupRoleReadOnlyMode() {
        boolean isDatabaseSource = isDatabaseSource();
        setReadOnly(!isDatabaseSource);

        Collection<Action> resourcePoliciesActions = rowLevelPoliciesTable.getActions();
        for (Action action : resourcePoliciesActions) {
            action.setVisible(ReadAction.ID.equals(action.getId()) != isDatabaseSource);
        }

        Collection<Action> childRolesActions = childRolesTable.getActions();
        for (Action action : childRolesActions) {
            action.setEnabled(isDatabaseSource);
        }
    }

    @Subscribe("childRolesTable.add")
    public void onChildRolesTableAdd(ActionPerformedEvent event) {
        DialogWindow<RowLevelRoleModelLookupView> lookupDialog = dialogWindows.lookup(childRolesTable)
                .withViewClass(RowLevelRoleModelLookupView.class)
                .build();

        List<String> excludedRolesCodes = childRolesDc.getItems().stream()
                .map(BaseRoleModel::getCode)
                .collect(Collectors.toList());

        if (codeField.isReadOnly()) {
            excludedRolesCodes.add(getEditedEntity().getCode());
        }

        lookupDialog.getView().setExcludedRoles(excludedRolesCodes);

        lookupDialog.open();
    }

    @Install(to = "rowLevelPoliciesTable.create", subject = "initializer")
    private void rowLevelPoliciesTableCreateInitializer(RowLevelPolicyModel rowLevelPolicyModel) {
        rowLevelPolicyModel.setType(RowLevelPolicyType.JPQL);
        rowLevelPolicyModel.setAction(RowLevelPolicyAction.READ);
    }

    @Install(to = "codeField", subject = "validator")
    private void codeFieldValidator(String value) {
        RowLevelRoleModel editedEntity = getEditedEntity();
        boolean exist = roleRepository.getAllRoles().stream()
                .filter(rowLevelRole -> {
                    if (rowLevelRole.getCustomProperties().isEmpty()) {
                        return true;
                    }
                    return !rowLevelRole.getCustomProperties().get("databaseId")
                            .equals(editedEntity.getCustomProperties().get("databaseId"));
                })
                .anyMatch(rowLevelRole -> rowLevelRole.getCode().equals(value));
        if (exist) {
            throw new ValidationException(messages.getMessage("io.jmix.securityflowui.view.rowlevelrole/uniqueCode"));
        }
    }

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(final SaveContext saveContext) {
        if (isDatabaseSource()) {
            getRolePersistence().save(getEditedEntity());
            return Set.of(getEditedEntity());
        } else {
            return Set.of();
        }
    }

    @Subscribe(id = "childRolesDc", target = Target.DATA_CONTAINER)
    public void onChildRolesDcCollectionChange(
            final CollectionContainer.CollectionChangeEvent<ResourceRoleModel> event) {
        Set<String> childRoles = childRolesDc.getItems().stream()
                .map(BaseRoleModel::getCode)
                .collect(Collectors.toSet());

        getEditedEntity().setChildRoles(childRoles);
    }

    private boolean isDatabaseSource() {
        return RoleSourceType.DATABASE.equals(getEditedEntity().getSource());
    }

    private RolePersistence getRolePersistence() {
        if (rolePersistence == null) {
            throw new IllegalStateException("RolePersistence is not available");
        }
        return rolePersistence;
    }
}
