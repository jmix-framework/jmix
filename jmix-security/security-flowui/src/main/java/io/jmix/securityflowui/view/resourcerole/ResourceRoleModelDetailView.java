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

import com.google.common.collect.Sets;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RolePersistence;
import io.jmix.securityflowui.view.resourcepolicy.*;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "sec/resourcerolemodels/:code", layout = DefaultMainViewParent.class)
@ViewController("sec_ResourceRoleModel.detail")
@ViewDescriptor("resource-role-model-detail-view.xml")
@EditedEntityContainer("roleModelDc")
public class ResourceRoleModelDetailView extends StandardDetailView<ResourceRoleModel> {

    private static final Logger log = LoggerFactory.getLogger(ResourceRoleModelDetailView.class);

    public static final String ROUTE_PARAM_NAME = "code";

    @ViewComponent
    private TypedTextField<String> codeField;
    @ViewComponent
    private JmixCheckboxGroup<String> scopesField;
    @ViewComponent
    private DataGrid<ResourcePolicyModel> resourcePoliciesTable;
    @ViewComponent
    private DataGrid<ResourceRoleModel> childRolesTable;
    @ViewComponent
    private DropdownButton createDropdownButton;

    @ViewComponent
    private DataContext dataContext;
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
    @Autowired(required = false)
    private RolePersistence rolePersistence;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private ResourceRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired(required = false)
    private List<ResourcePolicyTypeProvider> resourcePolicyTypeProviders;

    @Subscribe
    public void onInit(InitEvent event) {
        initScopesField();
        initResourcePoliciesTable();
    }

    @Subscribe
    public void onInitNewEntity(InitEntityEvent<ResourceRoleModel> event) {
        codeField.setReadOnly(false);

        ResourceRoleModel entity = event.getEntity();
        entity.setSource(RoleSourceType.DATABASE);
        entity.setScopes(Sets.newHashSet(SecurityScope.UI));
    }

    @Override
    protected String getRouteParamName() {
        return ROUTE_PARAM_NAME;
    }

    @Override
    protected void setupEntityToEdit(ResourceRoleModel entityToEdit) {
        // do nothing
    }

    @Override
    protected void initExistingEntity(String serializedEntityCode) {
        String code = urlParamSerializer.deserialize(String.class, serializedEntityCode);
        ResourceRole roleByCode = roleRepository.findRoleByCode(code);

        ResourceRoleModel resourceRoleModel = roleModelConverter.createResourceRoleModel(roleByCode);

        childRolesDc.mute();
        childRolesDc.setItems(loadChildRoleModels(resourceRoleModel));
        childRolesDc.unmute();

        ResourceRoleModel merged = dataContext.merge(resourceRoleModel);
        roleModelDc.setItem(merged);
    }

    protected List<ResourceRoleModel> loadChildRoleModels(ResourceRoleModel editedRoleModel) {
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
        setupRoleReadOnlyMode(isDatabaseSource());
        initAdditionalResourcePolicyTypes();
    }

    private void initAdditionalResourcePolicyTypes() {
        if (resourcePolicyTypeProviders != null) {
            for (ResourcePolicyTypeProvider resourcePolicyTypeProvider : resourcePolicyTypeProviders) {
                BaseAction action = getCreatePolicyAction(resourcePolicyTypeProvider);
                createDropdownButton.addItem(action.getId(), action);
            }
        }
    }

    private BaseAction getCreatePolicyAction(ResourcePolicyTypeProvider resourcePolicyTypeProvider) {
        BaseAction action = new BaseAction(RandomStringUtils.randomAlphabetic(5)) {
            @Override
            public void actionPerform(Component component) {
                dialogWindows.view(ResourceRoleModelDetailView.this, resourcePolicyTypeProvider.getCreatePolicyViewClass())
                        .withAfterCloseListener(ResourceRoleModelDetailView.this::addPoliciesFromMultiplePoliciesView)
                        .open();
            }

            @Nullable
            @Override
            public String getText() {
                return resourcePolicyTypeProvider.getLocalizedPolicyName();
            }
        };
        return action;
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

    @Subscribe("childRolesTable.add")
    public void onChildRolesTableAdd(ActionPerformedEvent event) {
        DialogWindow<ResourceRoleModelLookupView> lookupDialog = dialogWindows.lookup(childRolesTable)
                .withViewClass(ResourceRoleModelLookupView.class)
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

    private void initResourcePoliciesTable() {
        resourcePoliciesTable.addColumn((ValueProvider<ResourcePolicyModel, String>) resourcePolicyModel ->
                        messageBundle.getMessage("roleAction." + resourcePolicyModel.getAction()))
                .setKey("action")
                .setHeader(messageTools.getPropertyCaption(resourcePoliciesDc.getEntityMetaClass(), "action"))
                .setSortable(true);
        if (isEffectColumnVisible()) {
            resourcePoliciesTable.addColumn((ValueProvider<ResourcePolicyModel, String>) resourcePolicyModel ->
                            messageBundle.getMessage("roleEffect." + resourcePolicyModel.getEffect()))
                    .setKey("effect")
                    .setHeader(messageTools.getPropertyCaption(resourcePoliciesDc.getEntityMetaClass(), "effect"))
                    .setSortable(true);
        }
    }

    private boolean isEffectColumnVisible() {
        if (resourcePolicyTypeProviders != null) {
            return resourcePolicyTypeProviders.stream()
                    .anyMatch(ResourcePolicyTypeProvider::isEffectColumnVisible);
        }
        return false;
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
                    ResourcePolicyModel mergedResourcePolicyModel = dataContext.merge(resourcePolicyModel);
                    resourcePoliciesDc.getMutableItems().add(mergedResourcePolicyModel);
                }
            }
        }
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
            case ResourcePolicyType.MENU:
                return MenuResourcePolicyModelDetailView.class;
            case ResourcePolicyType.SCREEN:
                return ViewResourcePolicyModelDetailView.class;
            case ResourcePolicyType.ENTITY:
                return EntityResourcePolicyModelDetailView.class;
            case ResourcePolicyType.ENTITY_ATTRIBUTE:
                return EntityAttributeResourcePolicyModelDetailView.class;
            case ResourcePolicyType.GRAPHQL:
                return GraphQLResourcePolicyModelDetailView.class;
            case ResourcePolicyType.SPECIFIC:
                return SpecificResourcePolicyModelDetailView.class;
        }

        if (resourcePolicyTypeProviders != null) {
            for (ResourcePolicyTypeProvider resourcePolicyTypeProvider : resourcePolicyTypeProviders) {
                if (resourcePolicyTypeProvider.supports(resourcePolicyModel.getType())) {
                    return resourcePolicyTypeProvider.getEditPolicyViewClass();
                }
            }
        }

        return ResourcePolicyModelDetailView.class;

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
