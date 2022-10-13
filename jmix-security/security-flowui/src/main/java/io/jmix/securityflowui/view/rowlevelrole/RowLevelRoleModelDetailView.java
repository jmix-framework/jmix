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

import com.google.common.base.Strings;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityflowui.model.*;
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
    private Tabs tabs;
    @ViewComponent
    private VerticalLayout childRolesWrapper;
    @ViewComponent
    private VerticalLayout rowLevelPoliciesWrapper;
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
    @ViewComponent
    private CollectionPropertyContainer<RowLevelPolicyModel> rowLevelPoliciesDc;

    @Autowired
    private Messages messages;
    @Autowired
    private Metadata metadata;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private RowLevelRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;

    private boolean openedByCreateAction = false;
    private final Set<UUID> forRemove = new HashSet<>();

    @Subscribe
    public void onInit(InitEvent event) {
        tabs.addSelectedChangeListener(this::onSelectedTabChange);
    }

    private void onSelectedTabChange(Tabs.SelectedChangeEvent event) {
        String tabId = event.getSelectedTab().getId()
                .orElse("<no_id>");

        switch (tabId) {
            case "rowLevelPoliciesTab":
                rowLevelPoliciesWrapper.setVisible(true);
                childRolesWrapper.setVisible(false);
                break;
            case "childRolesTab":
                rowLevelPoliciesWrapper.setVisible(false);
                childRolesWrapper.setVisible(true);
                break;
            default:
                rowLevelPoliciesWrapper.setVisible(false);
                childRolesWrapper.setVisible(false);
        }
    }

    @Subscribe
    public void onInitNewEntity(InitEntityEvent<RowLevelRoleModel> event) {
        openedByCreateAction = true;

        codeField.setReadOnly(false);

        RowLevelRoleModel entity = event.getEntity();
        entity.setSource(RoleSource.DATABASE);
    }

    @Override
    protected String getRouteParamName() {
        return ROUTE_PARAM_NAME;
    }

    @Override
    protected void initExistingEntity(String serializedEntityCode) {
        String code = urlParamSerializer.deserialize(String.class, serializedEntityCode);
        RowLevelRole roleByCode = roleRepository.findRoleByCode(code);

        RowLevelRoleModel rowLevelRoleModel = roleModelConverter.createRowLevelRoleModel(roleByCode);
        roleModelDc.setItem(rowLevelRoleModel);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        //non-persistent entities are automatically marked as modified. If isNew is not set, we must remove
        //all entities from dataContext.modifiedInstances collection
        if (!openedByCreateAction) {
            Set<Object> modified = new HashSet<>(getDataContext().getModified());
            for (Object entity : modified) {
                getDataContext().setModified(entity, false);
            }
        }

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

    @Install(to = "childRolesDl", target = Target.DATA_LOADER)
    private List<RowLevelRoleModel> childRolesDlLoadDelegate(LoadContext<RowLevelRoleModel> loadContext) {
        RowLevelRoleModel editedRoleModel = getEditedEntity();
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

    @Install(to = "rowLevelPoliciesTable.remove", subject = "afterActionPerformedHandler")
    private void rowLevelPoliciesTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<RowLevelPolicyModel> event) {
        List<RowLevelPolicyModel> policyModels = event.getItems();
        Set<UUID> databaseIds = policyModels.stream()
                .map(resourcePolicyModel -> resourcePolicyModel.getCustomProperties().get("databaseId"))
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        forRemove.addAll(databaseIds);
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

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPreSave(DataContext.PreSaveEvent event) {
        if (isDatabaseSource()) {
            saveRoleEntityToDatabase(event.getModifiedInstances());
        }
    }

    private void saveRoleEntityToDatabase(Collection<?> modifiedInstances) {
        RowLevelRoleModel roleModel = getEditedEntity();
        String roleDatabaseId = roleModel.getCustomProperties().get("databaseId");

        RowLevelRoleEntity roleEntity;
        if (!Strings.isNullOrEmpty(roleDatabaseId)) {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.load(RowLevelRoleEntity.class).id(roleEntityId).one();
        } else {
            roleEntity = metadata.create(RowLevelRoleEntity.class);
        }

        roleEntity = getDataContext().merge(roleEntity);
        roleEntity.setName(roleModel.getName());
        roleEntity.setCode(roleModel.getCode());
        roleEntity.setDescription(roleModel.getDescription());

        boolean roleModelModified = modifiedInstances.stream()
                .anyMatch(entity -> entity instanceof ResourceRoleModel);

        if (roleModelModified) {
            roleEntity = getDataContext().merge(roleEntity);
        }

        List<RowLevelPolicyModel> rowLevelPolicyModels = modifiedInstances.stream()
                .filter(entity -> entity instanceof RowLevelPolicyModel)
                .map(entity -> (RowLevelPolicyModel) entity)
                //modifiedInstances may contain row level policies from just added child role. We should not analyze them here
                .filter(entity -> rowLevelPoliciesDc.containsItem(entity.getId()))
                .collect(Collectors.toList());

        for (RowLevelPolicyModel policyModel : rowLevelPolicyModels) {
            String databaseId = policyModel.getCustomProperties().get("databaseId");
            RowLevelPolicyEntity policyEntity;
            if (!Strings.isNullOrEmpty(databaseId)) {
                UUID entityId = UUID.fromString(databaseId);
                policyEntity = dataManager.load(RowLevelPolicyEntity.class)
                        .id(entityId)
                        .one();
            } else {
                policyEntity = metadata.create(RowLevelPolicyEntity.class);
                policyEntity.setRole(roleEntity);
            }
            policyEntity = getDataContext().merge(policyEntity);
            policyEntity.setEntityName(policyModel.getEntityName());
            policyEntity.setAction(policyModel.getAction());
            policyEntity.setType(policyModel.getType());
            policyEntity.setWhereClause(policyModel.getWhereClause());
            policyEntity.setJoinClause(policyModel.getJoinClause());
            policyEntity.setScript(policyModel.getScript());
        }

        for (UUID databaseId : forRemove) {
            dataManager.remove(Id.of(databaseId, RowLevelPolicyEntity.class));
        }

        Set<String> childRoles = childRolesDc.getItems().stream()
                .map(BaseRoleModel::getCode)
                .collect(Collectors.toSet());

        roleModel.setChildRoles(childRoles);
        roleEntity.setChildRoles(childRoles);
    }

    private boolean isDatabaseSource() {
        return RoleSource.DATABASE.equals(getEditedEntity().getSource());
    }

    private DataContext getDataContext() {
        return getViewData().getDataContext();
    }
}
