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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityui.model.BaseRoleModel;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.securityui.model.ResourceRoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.securityui.screen.resourcepolicy.*;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.list.AddAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.action.list.ViewAction;
import io.jmix.ui.builder.AfterScreenCloseEvent;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@UiController("sec_ResourceRoleModel.edit")
@UiDescriptor("resource-role-model-edit.xml")
@EditedEntityContainer("roleModelDc")
@Route(value = "resourceRoles/edit", parentPrefix = "resourceRoles")
public class ResourceRoleModelEdit extends StandardEditor<ResourceRoleModel> {

    private static final Logger log = LoggerFactory.getLogger(ResourceRoleModelEdit.class);

    @Autowired
    private TextField<String> codeField;

    @Autowired
    private CheckBoxGroup<String> scopesField;

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
    @Qualifier("childRolesTable.add")
    private AddAction<ResourceRoleModel> childRolesTableAdd;

    @Autowired
    @Qualifier("childRolesTable.remove")
    private RemoveAction<ResourceRoleModel> childRolesTableRemove;

    @Autowired
    private CollectionContainer<ResourceRoleModel> childRolesDc;

    @Autowired
    private GroupTable<ResourcePolicyModel> resourcePoliciesTable;

    @Autowired
    private TextField<String> sourceField;

    @Autowired
    private PopupButton createResourcePolicyPopupBtn;

    @Autowired
    private CollectionPropertyContainer<ResourcePolicyModel> resourcePoliciesDc;

    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DataContext dataContext;

    private boolean openedByCreateAction;

    @Autowired
    private Metadata metadata;

    @Autowired
    private Messages messages;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private UrlRouting urlRouting;

    private Set<UUID> forRemove;

    private boolean resourcePoliciesTableExpanded = true;
    @Named("resourcePoliciesTable.createGraphQLPolicy")
    private BaseAction resourcePoliciesTableCreateGraphQLPolicy;

    public void setOpenedByCreateAction(boolean openedByCreateAction) {
        this.openedByCreateAction = openedByCreateAction;
    }

    @Install(to = "childRolesDl", target = Target.DATA_LOADER)
    private List<ResourceRoleModel> childRolesDlLoadDelegate(LoadContext<ResourceRoleModel> loadContext) {
        ResourceRoleModel editedRoleModel = getEditedEntity();
        if (editedRoleModel.getChildRoles() == null || editedRoleModel.getChildRoles().isEmpty()) {
            return Collections.emptyList();
        }
        List<ResourceRoleModel> childRoleModels = new ArrayList<>();
        for (String code : editedRoleModel.getChildRoles()) {
            ResourceRole child = roleRepository.findRoleByCode(code);
            if (child != null) {
                childRoleModels.add(roleModelConverter.createResourceRoleModel(child));
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

        initCodeField();
        initScopesField();

        forRemove = new HashSet<>();

        if (!Strings.isNullOrEmpty(getEditedEntity().getCode())) {
            codeField.setEnabled(false);
        }

        resourcePoliciesTableCreateGraphQLPolicy.setVisible(isGraphQLEnabled());
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        resourcePoliciesTable.expandAll();
        resourcePoliciesTableExpanded = true;

        if (!openedByCreateAction) {
            // screen opened by URL for creating
            if (getEditedEntity().getSource() == null) {
                getEditedEntity().setSource(RoleSource.DATABASE);
                getEditedEntity().setScopes(Sets.newHashSet(SecurityScope.UI));

                sourceField.setValue(messages.getMessage("io.jmix.securityui.model/roleSource." + getEditedEntity().getSource()));
                setupRoleViewMode();

                // set to false because entity is initialized by default values
                setModifiedAfterOpen(false);
            } else {
                urlRouting.replaceState(this, ImmutableMap.of("code", getEditedEntity().getCode()));
            }
        }
    }

    @Subscribe
    public void onUrlParamsChanged(UrlParamsChangedEvent event) {
        Map<String, String> params = event.getParams();
        if (params.containsKey("code")) {
            String resourceRoleCode = params.get("code");

            ResourceRole resourceRole = roleRepository.getRoleByCode(resourceRoleCode);
            ResourceRoleModel roleModel = roleModelConverter.createResourceRoleModel(resourceRole);
            setEntityToEdit(roleModel);
        }
    }

    @Subscribe("resourcePoliciesTable.createMenuPolicy")
    public void onResourcePoliciesTableCreateMenuPolicy(Action.ActionPerformedEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(MenuResourcePolicyModelCreate.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesScreen)
                .build()
                .show();
    }

    @Subscribe("resourcePoliciesTable.createScreenPolicy")
    public void onResourcePoliciesTableCreateScreen(Action.ActionPerformedEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(ScreenResourcePolicyModelCreate.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesScreen)
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

    private void addPoliciesFromMultiplePoliciesScreen(AfterScreenCloseEvent<? extends MultipleResourcePolicyModelCreateScreen> closeEvent) {
        if (MultipleResourcePolicyModelCreateScreen.COMMIT_ACTION_ID
                .equals(((StandardCloseAction) closeEvent.getCloseAction()).getActionId())) {
            MultipleResourcePolicyModelCreateScreen screen = closeEvent.getSource();
            for (ResourcePolicyModel resourcePolicyModel : screen.getResourcePolicies()) {
                boolean policyExists = resourcePoliciesDc.getItems().stream()
                        .anyMatch(rpm -> resourcePolicyModel.getType().equals(rpm.getType()) &&
                                resourcePolicyModel.getAction().equals(rpm.getAction()) &&
                                resourcePolicyModel.getResource().equals(rpm.getResource()));
                if (!policyExists) {
                    ResourcePolicyModel mergedResourcePolicyModel = dataContext.merge(resourcePolicyModel);
                    resourcePoliciesDc.getMutableItems().add(mergedResourcePolicyModel);
                }
            }
        }
    }

    @Subscribe("resourcePoliciesTable.createGraphQLPolicy")
    public void onGraphQLPoliciesTableCreateGraphQLPolicy(Action.ActionPerformedEvent event) {
        screenBuilders.editor(resourcePoliciesTable)
                .withScreenClass(GraphQLResourcePolicyModelEdit.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.GRAPHQL);
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
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

    private void setupRoleViewMode() {
        boolean isDatabaseSource = isDatabaseSource();
        setReadOnly(!isDatabaseSource);

        if (!isDatabaseSource) {
            resourcePoliciesTable.setItemClickAction(resourcePoliciesTableView);
        }

        resourcePoliciesTableEdit.setVisible(isDatabaseSource);
        resourcePoliciesTableRemove.setVisible(isDatabaseSource);
        resourcePoliciesTableView.setVisible(!isDatabaseSource);
        createResourcePolicyPopupBtn.setEnabled(isDatabaseSource);
        createResourcePolicyPopupBtn.getActions().forEach(action -> action.setEnabled(isDatabaseSource));

        childRolesTableAdd.setVisible(isDatabaseSource);
        childRolesTableRemove.setVisible(isDatabaseSource);
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

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPreCommit(DataContext.PreCommitEvent event) {
        if (isDatabaseSource()) {
            saveRoleEntityToDatabase(event.getModifiedInstances());
        }
    }

    @Install(to = "resourcePoliciesTable.remove", subject = "afterActionPerformedHandler")
    private void resourcePoliciesTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ResourcePolicyModel> event) {
        List<ResourcePolicyModel> policyModels = event.getItems();
        Set<UUID> databaseIds = policyModels.stream()
                .map(resourcePolicyModel -> resourcePolicyModel.getCustomProperties().get("databaseId"))
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        forRemove.addAll(databaseIds);
    }

    private void saveRoleEntityToDatabase(Collection<Object> modifiedInstances) {
        ResourceRoleModel roleModel = getEditedEntity();
        String roleDatabaseId = roleModel.getCustomProperties().get("databaseId");
        ResourceRoleEntity roleEntity;
        if (!Strings.isNullOrEmpty(roleDatabaseId)) {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.load(ResourceRoleEntity.class).id(roleEntityId).one();
        } else {
            roleEntity = metadata.create(ResourceRoleEntity.class);
        }
        roleEntity = dataContext.merge(roleEntity);
        roleEntity.setName(roleModel.getName());
        roleEntity.setCode(roleModel.getCode());
        roleEntity.setDescription(roleModel.getDescription());
        roleEntity.setScopes(roleModel.getScopes());

        boolean roleModelModified = modifiedInstances.stream()
                .anyMatch(entity -> entity instanceof ResourceRoleModel);

        if (roleModelModified) {
            roleEntity = dataContext.merge(roleEntity);
        }

        List<ResourcePolicyModel> resourcePolicyModels = modifiedInstances.stream()
                .filter(entity -> entity instanceof ResourcePolicyModel)
                .map(entity -> (ResourcePolicyModel) entity)
                //modifiedInstances may contain resource policies from just added child role. We should not analyze them here
                .filter(entity -> resourcePoliciesDc.containsItem(entity.getId()))
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
            policyEntity.setPolicyGroup(policyModel.getPolicyGroup());
        }

        for (UUID databaseId : forRemove) {
            dataManager.remove(Id.of(databaseId, ResourcePolicyEntity.class));
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

    protected void initScopesField() {
        scopesField.setOptionsList(Arrays.asList(SecurityScope.UI, SecurityScope.API));
    }

    private void initCodeField() {
        codeField.addValidator(s -> {
            ResourceRoleModel editedEntity = getEditedEntity();
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
    }

    private boolean isGraphQLEnabled() {
        try {
            Class.forName("io.jmix.graphql.security.GraphQLAuthorizedUrlsProvider");
            return true;
        } catch (ClassNotFoundException e) {
        }
        return false;
    }
}