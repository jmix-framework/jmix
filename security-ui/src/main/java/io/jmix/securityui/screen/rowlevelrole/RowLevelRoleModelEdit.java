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

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityui.model.*;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.list.*;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.stream.Collectors;

@UiController("sec_RowLevelRoleModel.edit")
@UiDescriptor("row-level-role-model-edit.xml")
@EditedEntityContainer("roleModelDc")
@Route(value = "rowLevelRoles/edit", parentPrefix = "rowLevelRoles")
public class RowLevelRoleModelEdit extends StandardEditor<RowLevelRoleModel> {
    private static final Logger log = LoggerFactory.getLogger(RowLevelRoleModelEdit.class);

    @Autowired
    private TextField<String> codeField;

    @Autowired
    @Qualifier("rowLevelPoliciesTable.create")
    private CreateAction<RowLevelPolicyModel> rowLevelPoliciesTableCreate;

    @Autowired
    @Qualifier("rowLevelPoliciesTable.view")
    private ViewAction<RowLevelPolicyModel> rowLevelPoliciesTableView;

    @Autowired
    @Qualifier("rowLevelPoliciesTable.edit")
    private EditAction<RowLevelPolicyModel> rowLevelPoliciesTableEdit;

    @Autowired
    @Qualifier("rowLevelPoliciesTable.remove")
    private RemoveAction<RowLevelPolicyModel> rowLevelPoliciesTableRemove;

    @Autowired
    @Qualifier("childRolesTable.add")
    private AddAction<RowLevelRoleModel> childRolesTableAdd;

    @Autowired
    @Qualifier("childRolesTable.remove")
    private RemoveAction<RowLevelRoleModel> childRolesTableRemove;

    @Autowired
    private CollectionContainer<RowLevelRoleModel> childRolesDc;

    @Autowired
    private GroupTable<RowLevelPolicyModel> rowLevelPoliciesTable;

    @Autowired
    private TextField<String> sourceField;

    @Autowired
    private RowLevelRoleRepository roleRepository;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DataContext dataContext;

    @Autowired
    private Metadata metadata;

    @Autowired
    private Messages messages;

    @Autowired
    private CollectionPropertyContainer<RowLevelPolicyModel> rowLevelPoliciesDc;

    private boolean openedByCreateAction;

    private Set<UUID> forRemove;

    public void setOpenedByCreateAction(boolean openedByCreateAction) {
        this.openedByCreateAction = openedByCreateAction;
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

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        //non-persistent entities are automatically marked as modified. If isNew is not set, we must remove
        //all entities from dataContext.modifiedInstances collection
        if (!openedByCreateAction) {
            Set<Object> modified = new HashSet<>(dataContext.getModified());
            for (Object entity : modified) {
                dataContext.setModified(entity, false);
            }
        }

        setupRoleViewMode();
        childRolesTableRemove.setConfirmation(false);
        sourceField.setValue(messages.getMessage("io.jmix.securityui.model/roleSource." + getEditedEntity().getSource()));

        codeField.addValidator(s -> {
            RowLevelRoleModel editedEntity = getEditedEntity();
            boolean exist = roleRepository.getAllRoles().stream()
                    .filter(resourceRole -> {
                        if (resourceRole.getCustomProperties().isEmpty()) {
                            return true;
                        }
                        return !resourceRole.getCustomProperties().get("databaseId").equals(editedEntity.getCustomProperties().get("databaseId"));
                    })
                    .anyMatch(resourceRole -> resourceRole.getCode().equals(s));
            if (exist) {
                throw new ValidationException(messages.getMessage("io.jmix.securityui.screen.role/RoleModelEdit.uniqueCode"));
            }
        });

        forRemove = new HashSet<>();

        if (!Strings.isNullOrEmpty(getEditedEntity().getCode())) {
            codeField.setEnabled(false);
        }
    }

    @Install(to = "rowLevelPoliciesTable.create", subject = "initializer")
    private void rowLevelPoliciesTableCreateInitializer(RowLevelPolicyModel rowLevelPolicyModel) {
        rowLevelPolicyModel.setType(RowLevelPolicyType.JPQL);
        rowLevelPolicyModel.setAction(RowLevelPolicyAction.READ);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPreCommit(DataContext.PreCommitEvent event) {
        if (isDatabaseSource()) {
            saveRoleEntityToDatabase(event.getModifiedInstances());
        }
    }

    private void setupRoleViewMode() {
        boolean isDatabaseSource = isDatabaseSource();
        setReadOnly(!isDatabaseSource);

        childRolesTableAdd.setVisible(isDatabaseSource);
        childRolesTableRemove.setVisible(isDatabaseSource);

        rowLevelPoliciesTableCreate.setVisible(isDatabaseSource);
        rowLevelPoliciesTableRemove.setVisible(isDatabaseSource);
        rowLevelPoliciesTableEdit.setVisible(isDatabaseSource);
        rowLevelPoliciesTableView.setVisible(!isDatabaseSource);

        if (!isDatabaseSource) {
            rowLevelPoliciesTable.setItemClickAction(rowLevelPoliciesTableView);
        }
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

    private void saveRoleEntityToDatabase(Collection<Object> modifiedInstances) {
        RowLevelRoleModel roleModel = getEditedEntity();
        String roleDatabaseId = roleModel.getCustomProperties().get("databaseId");

        RowLevelRoleEntity roleEntity;
        if (!Strings.isNullOrEmpty(roleDatabaseId)) {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.load(RowLevelRoleEntity.class).id(roleEntityId).one();
        } else {
            roleEntity = metadata.create(RowLevelRoleEntity.class);
        }

        roleEntity = dataContext.merge(roleEntity);
        roleEntity.setName(roleModel.getName());
        roleEntity.setCode(roleModel.getCode());
        roleEntity.setDescription(roleModel.getDescription());

        boolean roleModelModified = modifiedInstances.stream()
                .anyMatch(entity -> entity instanceof ResourceRoleModel);

        if (roleModelModified) {
            roleEntity = dataContext.merge(roleEntity);
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
            policyEntity = dataContext.merge(policyEntity);
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
}