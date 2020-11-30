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

import com.google.common.base.Strings;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.security.model.*;
import io.jmix.security.role.RoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.RoleEntity;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.securityui.model.RoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.securityui.model.RowLevelPolicyModel;
import io.jmix.securityui.screen.resourcepolicy.*;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.*;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionChangeType;
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

@UiController("sec_RoleModel.edit")
@UiDescriptor("role-model-edit.xml")
@EditedEntityContainer("roleModelDc")
@Route(value = "roles/edit", parentPrefix = "roles")
public class RoleModelEdit extends StandardEditor<RoleModel> {

    private static final Logger log = LoggerFactory.getLogger(RoleModelEdit.class);

    @Autowired
    @Qualifier("resourcePoliciesTable.edit")
    private EditAction<ResourcePolicyModel> resourcePoliciesTableEdit;

    @Autowired
    @Qualifier("resourcePoliciesTable.view")
    private ViewAction<ResourcePolicyModel> resourcePoliciesTableView;

    @Autowired
    @Qualifier("resourcePoliciesTable.remove")
    private RemoveAction<ResourcePolicyModel> resourcePoliciesTableRemove;

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
    private AddAction<RoleModel> childRolesTableAdd;

    @Autowired
    @Qualifier("childRolesTable.remove")
    private RemoveAction<RoleModel> childRolesTableRemove;

    @Autowired
    private CollectionContainer<RoleModel> childRolesDc;

    @Autowired
    private TabSheet policiesTabSheet;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private GroupTable<ResourcePolicyModel> resourcePoliciesTable;

    @Autowired
    private GroupTable<RowLevelPolicyModel> rowLevelPoliciesTable;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DataContext dataContext;

    private boolean openedByCreateAction;

    @Autowired
    private Metadata metadata;

    @Autowired
    private TextField<String> sourceField;

    @Autowired
    private TextField<RoleType> roleTypeField;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private PopupButton createResourcePolicyPopupBtn;

    @Autowired
    private CollectionPropertyContainer<ResourcePolicyModel> resourcePoliciesDc;
    private boolean resourcePoliciesTableExpanded = true;

    public void setOpenedByCreateAction(boolean openedByCreateAction) {
        this.openedByCreateAction = openedByCreateAction;
    }

    @Install(to = "childRolesDl", target = Target.DATA_LOADER)
    private List<RoleModel> childRolesDlLoadDelegate(LoadContext<RoleModel> loadContext) {
        RoleModel editedRoleModel = getEditedEntity();
        if (editedRoleModel.getChildRoles() == null || editedRoleModel.getChildRoles().isEmpty()) {
            return Collections.emptyList();
        }
        List<RoleModel> childRoleModels = new ArrayList<>();
        for (String code : editedRoleModel.getChildRoles()) {
            Role child = roleRepository.getRoleByCode(code);
            if (child != null) {
                childRoleModels.add(roleModelConverter.createRoleModel(child));
            } else {
                log.warn("Role {} was not found while collecting child roles for aggregated role {}",
                        editedRoleModel.getCode(), code);
            }
        }
        return childRoleModels;
    }

    @Subscribe(id = "childRolesDc", target = Target.DATA_CONTAINER)
    public void onChildRolesDcCollectionChange(CollectionContainer.CollectionChangeEvent<RoleModel> event) {
        RoleModel editedEntity = getEditedEntity();
        if (getEditedEntity().getRoleType() != RoleType.AGGREGATED) {
            return;
        }
        if (event.getChangeType() == CollectionChangeType.ADD_ITEMS ||
                event.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
            List<ResourcePolicyModel> resourcePolicies = new ArrayList<>();
            List<RowLevelPolicyModel> rowLevelPolicies = new ArrayList<>();
            Set<String> resourceIds = new HashSet<>();
            Set<String> rowLevelIds = new HashSet<>();
            for (RoleModel childRole : childRolesDc.getItems()) {
                for (ResourcePolicyModel resourcePolicy : childRole.getResourcePolicies()) {
                    String uniqueKey = resourcePolicy.getCustomProperties().get("uniqueKey");
                    if (uniqueKey == null) {
                        resourcePolicies.add(resourcePolicy);
                    } else if (!resourceIds.contains(uniqueKey)) {
                        resourceIds.add(uniqueKey);
                        resourcePolicies.add(resourcePolicy);
                    }
                }
                for (RowLevelPolicyModel rowLevelPolicy : childRole.getRowLevelPolicies()) {
                    String uniqueKey = rowLevelPolicy.getCustomProperties().get("uniqueKey");
                    if (uniqueKey == null) {
                        rowLevelPolicies.add(rowLevelPolicy);
                    } else if (!rowLevelIds.contains(uniqueKey)) {
                        rowLevelIds.add(uniqueKey);
                        rowLevelPolicies.add(rowLevelPolicy);
                    }
                }
            }
            editedEntity.setResourcePolicies(resourcePolicies);
            editedEntity.setRowLevelPolicies(rowLevelPolicies);
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean isDatabaseSource = isDatabaseSource();
        setReadOnly(!isDatabaseSource);
        RoleType roleType = getEditedEntity().getRoleType();
        boolean isAggregated = roleType == RoleType.AGGREGATED;
        if (isDatabaseSource) {
            if (!isAggregated) {
                policiesTabSheet.removeTab("childRolesTab");
                if (roleType == RoleType.RESOURCE) {
                    policiesTabSheet.removeTab("rowLevelPoliciesTab");
                } else if (roleType == RoleType.ROW_LEVEL) {
                    policiesTabSheet.removeTab("resourcePoliciesTab");
                }
            }
        } else {
            childRolesTableAdd.setVisible(false);
            childRolesTableRemove.setVisible(false);
            policiesTabSheet.setSelectedTab("resourcePoliciesTab");
            resourcePoliciesTable.setItemClickAction(resourcePoliciesTableView);
            rowLevelPoliciesTable.setItemClickAction(rowLevelPoliciesTableView);
        }
        childRolesTableRemove.setConfirmation(false);
        roleTypeField.setVisible(isDatabaseSource);
        resourcePoliciesTableEdit.setVisible(isDatabaseSource && !isAggregated);
        resourcePoliciesTableRemove.setVisible(isDatabaseSource && !isAggregated);
        resourcePoliciesTableView.setVisible(!isDatabaseSource || isAggregated);
        createResourcePolicyPopupBtn.setEnabled(isDatabaseSource && !isAggregated);
        createResourcePolicyPopupBtn.getActions().forEach(action -> action.setEnabled(isDatabaseSource && !isAggregated));

        rowLevelPoliciesTableCreate.setVisible(isDatabaseSource && !isAggregated);
        rowLevelPoliciesTableRemove.setVisible(isDatabaseSource && !isAggregated);
        rowLevelPoliciesTableEdit.setVisible(isDatabaseSource && !isAggregated);
        rowLevelPoliciesTableView.setVisible(!isDatabaseSource || isAggregated);

        //non-persistent entities are automatically marked as modified. If isNew is not set, we must remove
        //all entities from dataContext.modifiedInstances collection
        if (!openedByCreateAction) {
            Set<Object> modified = new HashSet<>(dataContext.getModified());
            for (Object entity : modified) {
                dataContext.setModified(entity, false);
            }
        }

        sourceField.setValue(messageBundle.getMessage("roleSource." + getEditedEntity().getSource()));
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        resourcePoliciesTable.expandAll();
        resourcePoliciesTableExpanded = true;
    }

    @Subscribe("resourcePoliciesTable.createMenuPolicy")
    public void onResourcePoliciesTableCreateMenuPolicy(Action.ActionPerformedEvent event) {
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(MenuResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.MENU);
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
                })
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.createScreenPolicy")
    public void onResourcePoliciesTableCreateScreen(Action.ActionPerformedEvent event) {
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(ScreenResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.SCREEN);
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
                })
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.createEntityPolicy")
    public void onResourcePoliciesTableCreateEntity(Action.ActionPerformedEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(EntityResourcePolicyModelCreate.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesScreen)
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.createEntityAttributePolicy")
    public void onResourcePoliciesTableCreateEntityAttribute(Action.ActionPerformedEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(EntityAttributeResourcePolicyModelCreate.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesScreen)
                .build()
                .show();
    }

    private void addPoliciesFromMultiplePoliciesScreen(AfterCloseEvent afterCloseEvent) {
        if (MultipleResourcePolicyModelCreateScreen.COMMIT_ACTION_ID
                .equals(((StandardCloseAction) afterCloseEvent.getCloseAction()).getActionId())) {
            MultipleResourcePolicyModelCreateScreen screen =
                    (MultipleResourcePolicyModelCreateScreen) afterCloseEvent.getSource();
            for (ResourcePolicyModel resourcePolicyModel : screen.getResourcePolicies()) {
                boolean policyExists = resourcePoliciesDc.getItems().stream()
                        .anyMatch(rpm -> resourcePolicyModel.getType().equals(rpm.getType()) &&
                                resourcePolicyModel.getAction().equals(rpm.getAction()) &&
                                resourcePolicyModel.getResource().equals(rpm.getResource()));
                if (!policyExists) {
                    resourcePoliciesDc.getMutableItems().add(resourcePolicyModel);
                }
            }
        }
    }

    @Subscribe("resourcePoliciesTable.createSpecificPolicy")
    public void onResourcePoliciesTableCreateSpecificPolicy(Action.ActionPerformedEvent event) {
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(SpecificResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.SPECIFIC);
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                })
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.createCustomPolicy")
    public void onResourcePoliciesTableCreateCustomPolicy(Action.ActionPerformedEvent event) {
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(ResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
                    resourcePolicyModel.setEffect(ResourcePolicy.DEFAULT_EFFECT);
                })
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.edit")
    public void onResourcePoliciesTableEdit(Action.ActionPerformedEvent event) {
        ResourcePolicyModel editedResourcePolicyModel = resourcePoliciesTable.getSingleSelected();
        if (editedResourcePolicyModel == null) return;
        Screen editor = buildResourcePolicyEditorScreen(editedResourcePolicyModel);
        editor.show();
    }

    @Subscribe("resourcePoliciesTable.view")
    public void onResourcePoliciesTableView(Action.ActionPerformedEvent event) {
        ResourcePolicyModel editedResourcePolicyModel = resourcePoliciesTable.getSingleSelected();
        if (editedResourcePolicyModel == null) return;
        Screen editor = buildResourcePolicyEditorScreen(editedResourcePolicyModel);
        if (editor instanceof ReadOnlyAwareScreen) {
            ((ReadOnlyAwareScreen) editor).setReadOnly(true);
        } else {
            throw new IllegalStateException(String.format("Screen '%s' does not implement ReadOnlyAwareScreen: %s",
                    editor.getId(), editor.getClass()));
        }
        editor.show();
    }

    private Screen buildResourcePolicyEditorScreen(ResourcePolicyModel editedResourcePolicyModel) {
        return screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(getResourcePolicyEditorClass(editedResourcePolicyModel))
                .withParentDataContext(getScreenData().getDataContext())
                .build();
    }

    private Class getResourcePolicyEditorClass(ResourcePolicyModel resourcePolicyModel) {
        switch (resourcePolicyModel.getType()) {
            case ResourcePolicyType.MENU:
                return MenuResourcePolicyModelEdit.class;
            case ResourcePolicyType.SCREEN:
                return ScreenResourcePolicyModelEdit.class;
            case ResourcePolicyType.ENTITY:
                return EntityResourcePolicyModelEdit.class;
            case ResourcePolicyType.ENTITY_ATTRIBUTE:
                return EntityAttributeResourcePolicyModelEdit.class;
            case ResourcePolicyType.SPECIFIC:
                return SpecificResourcePolicyModelEdit.class;
            default:
                return ResourcePolicyModelEdit.class;

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

    private void saveRoleEntityToDatabase(Collection<Object> modifiedInstances) {
        RoleModel roleModel = getEditedEntity();
        String roleDatabaseId = roleModel.getCustomProperties().get("databaseId");
        RoleEntity roleEntity;
        if (!Strings.isNullOrEmpty(roleDatabaseId)) {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.load(RoleEntity.class).id(roleEntityId).one();
        } else {
            roleEntity = metadata.create(RoleEntity.class);
        }
        roleEntity = dataContext.merge(roleEntity);
        roleEntity.setName(roleModel.getName());
        roleEntity.setCode(roleModel.getCode());
        roleEntity.setRoleType(roleModel.getRoleType());

        boolean roleModelModified = modifiedInstances.stream()
                .anyMatch(entity -> entity instanceof RoleModel);

        if (roleModelModified) {
            roleEntity = dataContext.merge(roleEntity);
        }

        if (roleModel.getRoleType() == RoleType.RESOURCE) {
            List<ResourcePolicyModel> resourcePolicyModels = modifiedInstances.stream()
                    .filter(entity -> entity instanceof ResourcePolicyModel)
                    .map(entity -> (ResourcePolicyModel) entity)
                    .collect(Collectors.toList());

            for (ResourcePolicyModel policyModel : resourcePolicyModels) {
                String databaseId = policyModel.getCustomProperties().get("databaseId");
                ResourcePolicyEntity policyEntity;
                if (!Strings.isNullOrEmpty(databaseId)) {
                    UUID entityId = UUID.fromString(databaseId);
                    policyEntity = dataManager.load(ResourcePolicyEntity.class)
                            .id(entityId)
                            .one();
                } else {
                    policyEntity = metadata.create(ResourcePolicyEntity.class);
                    policyEntity.setRole(roleEntity);
                }
                policyEntity = dataContext.merge(policyEntity);
                policyEntity.setType(policyModel.getType());
                policyEntity.setResource(policyModel.getResource());
                policyEntity.setAction(policyModel.getAction());
                policyEntity.setEffect(policyModel.getEffect());
                policyEntity.setScope(policyModel.getScope() != null ?
                        policyModel.getScope() :
                        ResourcePolicy.DEFAULT_SCOPE);
                policyEntity.setPolicyGroup(policyModel.getPolicyGroup());
            }
        } else if (roleModel.getRoleType() == RoleType.ROW_LEVEL) {
            List<RowLevelPolicyModel> rowLevelPolicyModels = modifiedInstances.stream()
                    .filter(entity -> entity instanceof RowLevelPolicyModel)
                    .map(entity -> (RowLevelPolicyModel) entity)
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
            }
        } else if (roleModel.getRoleType() == RoleType.AGGREGATED) {
            Set<String> childRoles = new HashSet<>();
            for (RoleModel child : childRolesDc.getItems()) {
                childRoles.add(child.getCode());
            }
            roleModel.setChildRoles(childRoles);
            roleEntity.setChildRoles(childRoles);
        }
    }

    private boolean isDatabaseSource() {
        return RoleSource.DATABASE.equals(getEditedEntity().getSource());
    }

    @Subscribe("resourcePoliciesTable.expandCollapse")
    public void onResourcePoliciesTableExpandCollapse(Action.ActionPerformedEvent event) {
        if (resourcePoliciesTableExpanded) {
            resourcePoliciesTable.collapseAll();
        } else {
            resourcePoliciesTable.expandAll();
        }
        resourcePoliciesTableExpanded = !resourcePoliciesTableExpanded;
    }

    @Subscribe(id = "resourcePoliciesDc", target = Target.DATA_CONTAINER)
    public void onResourcePoliciesDcCollectionChange(CollectionContainer.CollectionChangeEvent<ResourcePolicyModel> event) {
        if (event.getChangeType() == CollectionChangeType.ADD_ITEMS) {
            Collection<? extends ResourcePolicyModel> addedItems = event.getChanges();
            addedItems.forEach(resourcePolicy -> resourcePoliciesTable.expandPath(resourcePolicy));
            removeDuplicateResourcePolicy(addedItems);
        }
    }

    private void removeDuplicateResourcePolicy(Collection<? extends ResourcePolicyModel> addedItems) {
        List<ResourcePolicyModel> allItems = resourcePoliciesDc.getMutableItems();
        List<ResourcePolicyModel> forRemove = new ArrayList<>();
        for (ResourcePolicyModel addedItem : addedItems) {
            for (ResourcePolicyModel allItem : allItems) {
                if (Objects.equals(allItem.getType(), addedItem.getType()) &&
                        Objects.equals(allItem.getAction(), addedItem.getAction()) &&
                        Objects.equals(allItem.getResource(), addedItem.getResource()) &&
                        !Objects.equals(allItem.getId(), addedItem.getId())) {
                    forRemove.add(addedItem);
                }
            }
        }
        resourcePoliciesDc.getMutableItems().removeAll(forRemove);
    }
}