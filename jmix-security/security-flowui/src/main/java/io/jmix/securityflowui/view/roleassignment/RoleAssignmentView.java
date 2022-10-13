package io.jmix.securityflowui.view.roleassignment;

import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.Notifications.Type;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.*;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.model.ResourceRoleModel;
import io.jmix.securityflowui.model.RowLevelRoleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "sec/roleassignment/:username", layout = DefaultMainViewParent.class)
@ViewController("roleAssignmentView")
@ViewDescriptor("role-assignment-view.xml")
public class RoleAssignmentView extends StandardView {

    @ViewComponent
    private DataGrid<RoleAssignmentEntity> resourceRoleAssignmentsTable;
    @ViewComponent
    private DataGrid<RoleAssignmentEntity> rowLevelRoleAssignmentsTable;

    @ViewComponent
    private CollectionContainer<RoleAssignmentEntity> rowLevelRoleAssignmentEntitiesDc;
    @ViewComponent
    private CollectionContainer<RoleAssignmentEntity> resourceRoleAssignmentEntitiesDc;
    @ViewComponent
    private CollectionLoader<RoleAssignmentEntity> rowLevelRoleAssignmentEntitiesDl;
    @ViewComponent
    private CollectionLoader<RoleAssignmentEntity> resourceRoleAssignmentEntitiesDl;

    @Autowired
    private ResourceRoleRepository resourceRoleRepository;
    @Autowired
    private RowLevelRoleRepository rowLevelRoleRepository;

    @Autowired
    private Metadata metadata;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Notifications notifications;

    private UserDetails user;

    @Subscribe
    public void onInit(InitEvent event) {
        addColumns();
    }

    private void addColumns() {
        resourceRoleAssignmentsTable.addColumn(
                (ValueProvider<RoleAssignmentEntity, String>) roleAssignmentEntity -> {
                    BaseRole role = resourceRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
                    return role != null ? role.getName() : "";
                }).setHeader(messageBundle.getMessage("column.roleName.header"));

        resourceRoleAssignmentsTable.addColumn(
                (ValueProvider<RoleAssignmentEntity, String>) roleAssignmentEntity -> {
                    ResourceRole role = resourceRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
                    return role != null ? String.join(", ", role.getScopes()) : "";
                }).setHeader(messageBundle.getMessage("column.roleScopes.header"));


        rowLevelRoleAssignmentsTable.addColumn(
                (ValueProvider<RoleAssignmentEntity, String>) roleAssignmentEntity -> {
                    BaseRole role = rowLevelRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
                    return role != null ? role.getName() : "";
                }).setHeader(messageBundle.getMessage("column.roleName.header"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        findUser(event.getRouteParameters());
        super.beforeEnter(event);
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadData();
    }

    private void findUser(RouteParameters routeParameters) {
        String username = routeParameters.get("username")
                .orElseThrow(() -> new IllegalStateException("Username not found"));

        user = userRepository.loadUserByUsername(username);
    }

    private void preventUnsavedChanges(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (action instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) action).isCheckForUnsavedChanges()
                && getDataContext().hasChanges()) {
            UnknownOperationResult result = new UnknownOperationResult();

            getViewValidation().showUnsavedChangesDialog(this)
                    .onDiscard(() -> result.resume(close(StandardOutcome.DISCARD)))
                    .onCancel(result::fail);

            event.preventClose(result);
        }
    }

    private void loadData() {
        if (entityStates.isNew(user)) {
            notifications.create(messageBundle.getMessage("error.newUser"))
                    .withType(Type.ERROR)
                    .show();
        } else {
            resourceRoleAssignmentEntitiesDl.setParameter("username", user.getUsername());
            resourceRoleAssignmentEntitiesDl.setParameter("roleType", RoleAssignmentRoleType.RESOURCE);
            resourceRoleAssignmentEntitiesDl.load();

            rowLevelRoleAssignmentEntitiesDl.setParameter("username", user.getUsername());
            rowLevelRoleAssignmentEntitiesDl.setParameter("roleType", RoleAssignmentRoleType.ROW_LEVEL);
            rowLevelRoleAssignmentEntitiesDl.load();
        }
    }

    @Install(to = "resourceRoleAssignmentsTable.addResourceRole", subject = "transformation")
    private Collection<RoleAssignmentEntity> resourceRoleTransformation(Collection<ResourceRoleModel> roleModels) {
        Collection<String> assignedRoleCodes = resourceRoleAssignmentEntitiesDc.getItems().stream()
                .map(RoleAssignmentEntity::getRoleCode)
                .collect(Collectors.toSet());

        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
                    roleAssignmentEntity.setRoleCode(roleModel.getCode());
                    roleAssignmentEntity.setUsername(user.getUsername());
                    roleAssignmentEntity.setRoleType(RoleAssignmentRoleType.RESOURCE);
                    return roleAssignmentEntity;
                })
                .collect(Collectors.toList());
    }

    @Install(to = "rowLevelRoleAssignmentsTable.addRowLevelRole", subject = "transformation")
    private Collection<RoleAssignmentEntity> rowLevelRoleTransformation(Collection<RowLevelRoleModel> roleModels) {
        Collection<String> assignedRoleCodes = rowLevelRoleAssignmentEntitiesDc.getItems().stream()
                .map(RoleAssignmentEntity::getRoleCode)
                .collect(Collectors.toSet());

        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
                    roleAssignmentEntity.setRoleCode(roleModel.getCode());
                    roleAssignmentEntity.setUsername(user.getUsername());
                    roleAssignmentEntity.setRoleType(RoleAssignmentRoleType.ROW_LEVEL);
                    return roleAssignmentEntity;
                })
                .collect(Collectors.toList());
    }

    @Subscribe("resourceRoleAssignmentsTable.remove")
    public void onResourceRoleAssignmentsTableRemove(ActionPerformedEvent event) {
        Set<RoleAssignmentEntity> selected = resourceRoleAssignmentsTable.getSelectedItems();
        resourceRoleAssignmentEntitiesDc.getMutableItems().removeAll(selected);
        // do not immediately remove role assignments but do that
        // only when role-assignment-screen is saved
        DataContext dataContext = getDataContext();
        selected.forEach(dataContext::remove);
    }

    @Subscribe("rowLevelRoleAssignmentsTable.remove")
    public void onRowLevelRoleAssignmentsTableRemove(ActionPerformedEvent event) {
        Set<RoleAssignmentEntity> selected = rowLevelRoleAssignmentsTable.getSelectedItems();
        rowLevelRoleAssignmentEntitiesDc.getMutableItems().removeAll(selected);
        // do not immediately remove role assignments but do that
        // only when role-assignment-screen is saved
        DataContext dataContext = getDataContext();
        selected.forEach(dataContext::remove);
    }

    @Subscribe("saveAction")
    public void onSaveActionPerformed(ActionPerformedEvent event) {
        getDataContext().save();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("closeAction")
    public void onCloseActionPerformed(ActionPerformedEvent event) {
        closeWithDefaultAction();
    }

    @Override
    public String getPageTitle() {
        return user != null
                ? messageBundle.formatMessage("roleAssignmentView.title", metadataTools.getInstanceName(user))
                : messageBundle.getMessage("roleAssignmentView.defaultTitle");
    }

    private DataContext getDataContext() {
        return getViewData().getDataContext();
    }

    private ViewValidation getViewValidation() {
        return getApplicationContext().getBean(ViewValidation.class);
    }
}