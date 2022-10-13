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

package io.jmix.securityflowui.view.resourcerole;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.util.RemoveOperation.AfterActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.model.*;
import io.jmix.securityflowui.model.ResourcePolicyType;
import io.jmix.securityflowui.model.RoleSource;
import io.jmix.securityflowui.view.resourcepolicy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "sec/resourcerolemodels/:code", layout = DefaultMainViewParent.class)
@ViewController("sec_ResourceRoleModel.detail")
@ViewDescriptor("resource-role-model-detail-view.xml")
@EditedEntityContainer("roleModelDc")
public class ResourceRoleModelDetailView extends StandardDetailView<ResourceRoleModel> {

    private static final Logger log = LoggerFactory.getLogger(ResourceRoleModelDetailView.class);

    public static final String ROUTE_PARAM_NAME = "code";

    @ViewComponent
    private Tabs tabs;
    @ViewComponent
    private VerticalLayout childRolesWrapper;
    @ViewComponent
    private VerticalLayout resourcePoliciesWrapper;
    @ViewComponent
    private TypedTextField<String> codeField;
    @ViewComponent
    private TypedTextField<String> sourceField;
    @ViewComponent
    private JmixCheckboxGroup<String> scopesField;
    @ViewComponent
    private DataGrid<ResourcePolicyModel> resourcePoliciesTable;
    @ViewComponent
    private DataGrid<ResourceRoleModel> childRolesTable;
    @ViewComponent
    private HorizontalLayout resourcePoliciesButtonsPanel;
    @ViewComponent
    private DropdownButton createDropdownButton;

    @ViewComponent
    private InstanceContainer<ResourceRoleModel> roleModelDc;
    @ViewComponent
    private CollectionContainer<ResourceRoleModel> childRolesDc;
    @ViewComponent
    private CollectionPropertyContainer<ResourcePolicyModel> resourcePoliciesDc;

    @Autowired
    private Messages messages;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private Metadata metadata;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private ResourceRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;

    private boolean openedByCreateAction = false;
    private final Set<UUID> forRemove = new HashSet<>();

    @Subscribe
    public void onInit(InitEvent event) {
        // we need to set items (i.e. options) before the value is set,
        // otherwise it will be cleared
        initScopesField();
        initResourcePoliciesTable();
        initTabs();
    }

    @Subscribe
    public void onInitNewEntity(InitEntityEvent<ResourceRoleModel> event) {
        openedByCreateAction = true;

        codeField.setReadOnly(false);

        ResourceRoleModel entity = event.getEntity();
        entity.setSource(RoleSource.DATABASE);
        entity.setScopes(Sets.newHashSet(SecurityScope.UI));
    }

    @Override
    protected String getRouteParamName() {
        return ROUTE_PARAM_NAME;
    }

    @Override
    protected void initExistingEntity(String serializedEntityCode) {
        String code = urlParamSerializer.deserialize(String.class, serializedEntityCode);
        ResourceRole roleByCode = roleRepository.findRoleByCode(code);

        ResourceRoleModel resourceRoleModel = roleModelConverter.createResourceRoleModel(roleByCode);
        roleModelDc.setItem(resourceRoleModel);
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

        boolean isDatabaseSource = isDatabaseSource();
        setupRoleReadOnlyMode(isDatabaseSource);

        Action createGraphQLPolicyAction = resourcePoliciesTable.getAction("createGraphQLPolicy");
        if (createGraphQLPolicyAction != null) {
            createGraphQLPolicyAction.setVisible(isGraphQLEnabled());
        }
    }

    private void setupRoleReadOnlyMode(boolean isDatabaseSource) {
        setReadOnly(!isDatabaseSource);

        createDropdownButton.setVisible(isDatabaseSource);

        Collection<Action> resourcePoliciesActions = resourcePoliciesTable.getActions();
        for (Action action : resourcePoliciesActions) {
            action.setVisible(ReadAction.ID.equals(action.getId()) != isDatabaseSource);
        }

        Collection<Action> childRolesActions = childRolesTable.getActions();
        for (Action action : childRolesActions) {
            action.setEnabled(isDatabaseSource);
        }
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

    private void initTabs() {
        tabs.addSelectedChangeListener(this::onSelectedTabChange);
    }

    private void onSelectedTabChange(Tabs.SelectedChangeEvent event) {
        String tabId = event.getSelectedTab().getId()
                .orElse("<no_id>");

        switch (tabId) {
            case "resourcePoliciesTab":
                resourcePoliciesWrapper.setVisible(true);
                childRolesWrapper.setVisible(false);
                break;
            case "childRolesTab":
                resourcePoliciesWrapper.setVisible(false);
                childRolesWrapper.setVisible(true);
                break;
            default:
                resourcePoliciesWrapper.setVisible(false);
                childRolesWrapper.setVisible(false);
        }
    }

    private void initResourcePoliciesTable() {
        resourcePoliciesTable.addColumn((ValueProvider<ResourcePolicyModel, String>) resourcePolicyModel ->
                        messageBundle.getMessage("roleAction." + resourcePolicyModel.getAction()))
                .setKey("action")
                .setHeader(messageTools.getPropertyCaption(resourcePoliciesDc.getEntityMetaClass(), "action"))
                .setSortable(true);
    }

    private void initScopesField() {
        scopesField.setItems(Arrays.asList(SecurityScope.UI, SecurityScope.API));
    }

    @Install(to = "codeField", subject = "validator")
    private void codeFieldValidator(String value) {
        ResourceRoleModel editedEntity = getEditedEntity();
        boolean exist = roleRepository.getAllRoles().stream()
                .filter(resourceRole -> {
                    if (resourceRole.getCustomProperties().isEmpty()) {
                        return true;
                    }
                    return !resourceRole.getCustomProperties().get("databaseId")
                            .equals(editedEntity.getCustomProperties().get("databaseId"));
                })
                .anyMatch(resourceRole -> resourceRole.getCode().equals(value));
        if (exist) {
            throw new ValidationException(messages.getMessage("io.jmix.securityflowui.view.resourcerole/uniqueCode"));
        }
    }

    @Subscribe("resourcePoliciesTable.createMenuPolicy")
    private void onResourcePoliciesTableCreateMenuPolicy(ActionPerformedEvent event) {
        dialogWindows.view(this, MenuResourcePolicyModelCreateView.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesView)
                .open();
    }

    @Subscribe("resourcePoliciesTable.createViewPolicy")
    private void onResourcePoliciesTableCreateViewPolicy(ActionPerformedEvent event) {
        dialogWindows.view(this, ViewResourcePolicyModelCreateView.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesView)
                .open();
    }

    @Subscribe("resourcePoliciesTable.createEntityPolicy")
    private void onResourcePoliciesTableCreateEntityPolicy(ActionPerformedEvent event) {
        dialogWindows.view(this, EntityResourcePolicyModelCreateView.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesView)
                .open();
    }

    @Subscribe("resourcePoliciesTable.createEntityAttributePolicy")
    private void onResourcePoliciesTableCreateEntityAttributePolicy(ActionPerformedEvent event) {
        dialogWindows.view(this, EntityAttributeResourcePolicyModelCreateView.class)
                .withAfterCloseListener(this::addPoliciesFromMultiplePoliciesView)
                .open();
    }

    private void addPoliciesFromMultiplePoliciesView(
            DialogWindow.AfterCloseEvent<? extends MultipleResourcePolicyModelCreateView> closeEvent) {
        if (closeEvent.closedWith(StandardOutcome.SAVE)) {
            MultipleResourcePolicyModelCreateView view = closeEvent.getSource().getView();
            for (ResourcePolicyModel resourcePolicyModel : view.getResourcePolicies()) {
                boolean policyExists = resourcePoliciesDc.getItems().stream()
                        .anyMatch(rpm ->
                                resourcePolicyModel.getType().equals(rpm.getType())
                                        && resourcePolicyModel.getAction().equals(rpm.getAction())
                                        && resourcePolicyModel.getResource().equals(rpm.getResource())
                        );

                if (!policyExists) {
                    ResourcePolicyModel mergedResourcePolicyModel = getDataContext().merge(resourcePolicyModel);
                    resourcePoliciesDc.getMutableItems().add(mergedResourcePolicyModel);
                }
            }
        }
    }

    @Subscribe("resourcePoliciesTable.createGraphQLPolicy")
    private void onGraphQLPoliciesTableCreateGraphQLPolicy(ActionPerformedEvent event) {
        DialogWindow<GraphQLResourcePolicyModelDetailView> dialog = dialogWindows.detail(resourcePoliciesTable)
                .withViewClass(GraphQLResourcePolicyModelDetailView.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.GRAPHQL);
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                })
                .build();

        dialog.getView().setShowSaveNotification(false);
        dialog.open();
    }

    @Subscribe("resourcePoliciesTable.createSpecificPolicy")
    private void onResourcePoliciesTableCreateSpecificPolicy(ActionPerformedEvent event) {
        DialogWindow<SpecificResourcePolicyModelDetailView> dialog = dialogWindows.detail(resourcePoliciesTable)
                .withViewClass(SpecificResourcePolicyModelDetailView.class)
                .newEntity()
                .withInitializer(resourcePolicyModel -> {
                    resourcePolicyModel.setType(ResourcePolicyType.SPECIFIC);
                    resourcePolicyModel.setAction(ResourcePolicy.DEFAULT_ACTION);
                    resourcePolicyModel.setEffect(ResourcePolicyEffect.ALLOW);
                })
                .open();

        dialog.getView().setShowSaveNotification(false);
        dialog.open();
    }

    @Subscribe("resourcePoliciesTable.edit")
    private void onResourcePoliciesTableEdit(ActionPerformedEvent event) {
        ResourcePolicyModel editedResourcePolicyModel = resourcePoliciesTable.getSingleSelectedItem();
        if (editedResourcePolicyModel == null) {
            return;
        }

        DialogWindow<?> dialog = buildResourcePolicyDetailView(editedResourcePolicyModel);
        dialog.open();
    }

    @Subscribe("resourcePoliciesTable.read")
    private void onResourcePoliciesTableRead(ActionPerformedEvent event) {
        ResourcePolicyModel editedResourcePolicyModel = resourcePoliciesTable.getSingleSelectedItem();
        if (editedResourcePolicyModel == null) {
            return;
        }

        DialogWindow<?> dialog = buildResourcePolicyDetailView(editedResourcePolicyModel);

        View<?> view = dialog.getView();
        if (view instanceof ReadOnlyAwareView) {
            ((ReadOnlyAwareView) view).setReadOnly(true);
        } else {
            throw new IllegalStateException(String.format("%s '%s' does not implement %s: %s",
                    View.class.getSimpleName(), view.getId(),
                    ReadOnlyAwareView.class.getSimpleName(), view.getClass()));
        }

        dialog.open();
    }

    private DialogWindow<?> buildResourcePolicyDetailView(ResourcePolicyModel editedResourcePolicyModel) {
        return dialogWindows.detail(resourcePoliciesTable)
                .withViewClass(getResourcePolicyDetailViewClass(editedResourcePolicyModel))
                .withParentDataContext(getViewData().getDataContext())
                .build();
    }

    private Class<? extends StandardDetailView<ResourcePolicyModel>> getResourcePolicyDetailViewClass(
            ResourcePolicyModel resourcePolicyModel) {
        switch (resourcePolicyModel.getType()) {
            case MENU:
                return MenuResourcePolicyModelDetailView.class;
            case VIEW:
                return ViewResourcePolicyModelDetailView.class;
            case ENTITY:
                return EntityResourcePolicyModelDetailView.class;
            case ENTITY_ATTRIBUTE:
                return EntityAttributeResourcePolicyModelDetailView.class;
            case GRAPHQL:
                return GraphQLResourcePolicyModelDetailView.class;
            case SPECIFIC:
                return SpecificResourcePolicyModelDetailView.class;
            default:
                return ResourcePolicyModelDetailView.class;
        }
    }

    @Install(to = "resourcePoliciesTable.remove", subject = "afterActionPerformedHandler")
    private void resourcePoliciesTableRemoveAfterActionPerformedHandler(AfterActionPerformedEvent<ResourcePolicyModel> event) {
        List<ResourcePolicyModel> policyModels = event.getItems();
        Set<UUID> databaseIds = policyModels.stream()
                .map(resourcePolicyModel -> resourcePolicyModel.getCustomProperties().get("databaseId"))
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        forRemove.addAll(databaseIds);
    }

    @Subscribe(id = "resourcePoliciesDc", target = Target.DATA_CONTAINER)
    private void onResourcePoliciesDcCollectionChange(CollectionContainer.CollectionChangeEvent<ResourcePolicyModel> event) {
        if (event.getChangeType() == CollectionChangeType.ADD_ITEMS) {
            removeDuplicateResourcePolicy(event.getChanges());
        }
    }

    private void removeDuplicateResourcePolicy(Collection<? extends ResourcePolicyModel> addedItems) {
        List<ResourcePolicyModel> allItems = resourcePoliciesDc.getMutableItems();
        List<ResourcePolicyModel> forRemove = new ArrayList<>();
        for (ResourcePolicyModel addedItem : addedItems) {
            for (ResourcePolicyModel allItem : allItems) {
                if (Objects.equals(allItem.getType(), addedItem.getType())
                        && Objects.equals(allItem.getAction(), addedItem.getAction())
                        && Objects.equals(allItem.getResource(), addedItem.getResource())
                        && !Objects.equals(allItem.getId(), addedItem.getId())) {
                    forRemove.add(addedItem);
                }
            }
        }

        resourcePoliciesDc.getMutableItems().removeAll(forRemove);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPreSave(DataContext.PreSaveEvent event) {
        if (isDatabaseSource()) {
            saveRoleEntityToDatabase(event.getModifiedInstances());
        }
    }

    private void saveRoleEntityToDatabase(Collection<?> modifiedInstances) {
        ResourceRoleModel roleModel = getEditedEntity();
        String roleDatabaseId = roleModel.getCustomProperties().get("databaseId");
        ResourceRoleEntity roleEntity;
        if (!Strings.isNullOrEmpty(roleDatabaseId)) {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.load(ResourceRoleEntity.class).id(roleEntityId).one();
        } else {
            roleEntity = metadata.create(ResourceRoleEntity.class);
        }
        roleEntity = getDataContext().merge(roleEntity);
        roleEntity.setName(roleModel.getName());
        roleEntity.setCode(roleModel.getCode());
        roleEntity.setDescription(roleModel.getDescription());
        roleEntity.setScopes(roleModel.getScopes());

        boolean roleModelModified = modifiedInstances.stream()
                .anyMatch(entity -> entity instanceof ResourceRoleModel);

        if (roleModelModified) {
            roleEntity = getDataContext().merge(roleEntity);
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
            policyEntity = getDataContext().merge(policyEntity);
            policyEntity.setType(policyModel.getTypeId());
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

    private Stream<Action> getCreatePolicyActions() {
        return resourcePoliciesTable.getActions()
                .stream()
                .filter(action -> action.getId().contains("Policy"));
    }

    private boolean isDatabaseSource() {
        return RoleSource.DATABASE.equals(getEditedEntity().getSource());
    }

    private boolean isGraphQLEnabled() {
        try {
            // TODO: 11.08.2022 is it applicable?
            Class.forName("io.jmix.graphql.security.GraphQLAuthorizedUrlsProvider");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    private DataContext getDataContext() {
        return getViewData().getDataContext();
    }
}
