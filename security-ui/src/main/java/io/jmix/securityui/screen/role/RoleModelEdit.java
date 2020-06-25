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
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.security.model.*;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.RoleEntity;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.securityui.model.RoleModel;
import io.jmix.securityui.model.RowLevelPolicyModel;
import io.jmix.securityui.screen.resourcepolicy.*;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.ViewAction;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@UiController("sec_RoleModel.edit")
@UiDescriptor("role-model-edit.xml")
@EditedEntityContainer("roleModelDc")
@Route(value = "roles/edit", parentPrefix = "roles")
@LoadDataBeforeShow
public class RoleModelEdit extends StandardEditor<RoleModel> {

    @Named("resourcePoliciesTable.edit")
    private EditAction<ResourcePolicyModel> resourcePoliciesTableEdit;

    @Named("resourcePoliciesTable.view")
    private ViewAction<ResourcePolicyModel> resourcePoliciesTableView;

    @Named("rowLevelPoliciesTable.view")
    private ViewAction<RowLevelPolicyModel> rowLevelPoliciesTableView;

    @Named("rowLevelPoliciesTable.edit")
    private EditAction<RowLevelPolicyModel> rowLevelPoliciesTableEdit;

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
    private MessageBundle messageBundle;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private PopupButton createResourcePolicyPopupBtn;

    public void setOpenedByCreateAction(boolean openedByCreateAction) {
        this.openedByCreateAction = openedByCreateAction;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean isDatabaseSource = isDatabaseSource();
        setReadOnly(!isDatabaseSource());
        if (!isDatabaseSource) {
            resourcePoliciesTable.setItemClickAction(resourcePoliciesTableView);
            rowLevelPoliciesTable.setItemClickAction(rowLevelPoliciesTableView);
        }
        resourcePoliciesTableEdit.setVisible(isDatabaseSource);
        resourcePoliciesTableView.setVisible(!isDatabaseSource);
        createResourcePolicyPopupBtn.setEnabled(isDatabaseSource);
        createResourcePolicyPopupBtn.getActions().forEach(action -> action.setEnabled(isDatabaseSource));

        rowLevelPoliciesTableEdit.setVisible(isDatabaseSource);
        rowLevelPoliciesTableView.setVisible(!isDatabaseSource);

        //non-persistent entities are automatically marked as modified. If isNew is not set, we must remove
        //all entities from dataContext.modifiedInstances collection
        if (!openedByCreateAction) {
            Set<Entity> modified = new HashSet<>(dataContext.getModified());
            for (Entity entity : modified) {
                dataContext.setModified(entity, false);
            }
        }

        sourceField.setValue(messageBundle.getMessage("roleSource." + getEditedEntity().getSource()));
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
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(EntityResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.ENTITY);
                    resourcePolicyModel.setAction(EntityPolicyAction.CREATE.getId());
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                })
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.createEntityAttributePolicy")
    public void onResourcePoliciesTableCreateEntityAttribute(Action.ActionPerformedEvent event) {
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(EntityAttributeResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.ENTITY_ATTRIBUTE);
                    resourcePolicyModel.setAction(EntityAttributePolicyAction.READ.getId());
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                })
                .build()
                .show();
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

    private void saveRoleEntityToDatabase(Collection<Entity> modifiedInstances) {
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

        boolean roleModelModified = modifiedInstances.stream()
                .anyMatch(entity -> entity instanceof RoleModel);

        if (roleModelModified) {
            roleEntity = dataContext.merge(roleEntity);
        }

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
        }

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
    }

    private boolean isDatabaseSource() {
        return RoleSource.DATABASE.equals(getEditedEntity().getSource());
    }
}