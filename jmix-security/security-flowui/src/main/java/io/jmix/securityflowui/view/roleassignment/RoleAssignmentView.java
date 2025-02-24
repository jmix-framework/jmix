package io.jmix.securityflowui.view.roleassignment;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.*;
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
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentModel;
import io.jmix.security.role.assignment.RoleAssignmentPersistence;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "sec/roleassignment/:username", layout = DefaultMainViewParent.class)
@ViewController("roleAssignmentView")
@ViewDescriptor("role-assignment-view.xml")
public class RoleAssignmentView extends StandardView {

    @ViewComponent
    private DataGrid<RoleAssignmentModel> resourceRoleAssignmentsTable;
    @ViewComponent
    private DataGrid<RoleAssignmentModel> rowLevelRoleAssignmentsTable;

    @ViewComponent
    private CollectionContainer<RoleAssignmentModel> rowLevelRoleAssignmentsDc;
    @ViewComponent
    private CollectionContainer<RoleAssignmentModel> resourceRoleAssignmentsDc;
    @ViewComponent
    private CollectionLoader<RoleAssignmentModel> rowLevelRoleAssignmentsDl;
    @ViewComponent
    private CollectionLoader<RoleAssignmentModel> resourceRoleAssignmentsDl;

    @Autowired
    private ResourceRoleRepository resourceRoleRepository;
    @Autowired
    private RowLevelRoleRepository rowLevelRoleRepository;

    @Autowired(required = false)
    private RoleAssignmentPersistence roleAssignmentPersistence;
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
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    private UserDetails user;

    @Supply(to = "resourceRoleAssignmentsTable.roleName", subject = "renderer")
    protected Renderer<RoleAssignmentModel> resourceRoleAssignmentsTableRoleNameRenderer() {
        return new TextRenderer<>(roleAssignmentEntity -> {
            BaseRole role = resourceRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
            return role != null ? role.getName() : StringUtils.EMPTY;
        });
    }

    @Supply(to = "resourceRoleAssignmentsTable.roleScopes", subject = "renderer")
    protected Renderer<RoleAssignmentModel> resourceRoleAssignmentsTableRoleScopesRenderer() {
        return new TextRenderer<>(roleAssignmentEntity -> {
            ResourceRole role = resourceRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
            return role != null
                    ? String.join(", ", role.getScopes())
                    : StringUtils.EMPTY;
        });
    }

    @Supply(to = "rowLevelRoleAssignmentsTable.roleName", subject = "renderer")
    protected Renderer<RoleAssignmentModel> rowLevelRoleAssignmentsTableRoleNameRenderer() {
        return new TextRenderer<>(roleAssignmentEntity -> {
            BaseRole role = rowLevelRoleRepository.findRoleByCode(roleAssignmentEntity.getRoleCode());
            return role != null ? role.getName() : StringUtils.EMPTY;
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        findUser(event.getRouteParameters());
        super.beforeEnter(event);
    }

    @Override
    protected void processBeforeEnterInternal(BeforeEnterEvent event) {
        super.processBeforeEnterInternal(event);

        findUser(event.getRouteParameters());
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadData();
    }

    @Install(to = "resourceRoleAssignmentsDl", target = Target.DATA_LOADER)
    protected List<RoleAssignmentModel> resourceRoleAssignmentsDlLoadDelegate(LoadContext<RoleAssignmentModel> loadContext) {
        return getRoleAssignmentPersistence().loadRoleAssignments(user.getUsername(), RoleAssignmentRoleType.RESOURCE);
    }

    @Install(to = "rowLevelRoleAssignmentsDl", target = Target.DATA_LOADER)
    protected List<RoleAssignmentModel> rowLevelRoleAssignmentsDlLoadDelegate(LoadContext<RoleAssignmentModel> loadContext) {
        return getRoleAssignmentPersistence().loadRoleAssignments(user.getUsername(), RoleAssignmentRoleType.ROW_LEVEL);
    }

    private void findUser(RouteParameters routeParameters) {
        String username = routeParameters.get("username")
                .orElseThrow(() -> new IllegalStateException("Username not found"));

        String decodedUsername = urlParamSerializer.deserialize(String.class, username);

        user = userRepository.loadUserByUsername(decodedUsername);
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
            resourceRoleAssignmentsDl.load();
            rowLevelRoleAssignmentsDl.load();
        }
    }

    @Install(to = "resourceRoleAssignmentsTable.addResourceRole", subject = "transformation")
    private Collection<RoleAssignmentModel> resourceRoleTransformation(Collection<ResourceRoleModel> roleModels) {
        Collection<String> assignedRoleCodes = resourceRoleAssignmentsDc.getItems().stream()
                .map(RoleAssignmentModel::getRoleCode)
                .collect(Collectors.toSet());

        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentModel assignmentModel = metadata.create(RoleAssignmentModel.class);
                    assignmentModel.setRoleCode(roleModel.getCode());
                    assignmentModel.setUsername(user.getUsername());
                    assignmentModel.setRoleType(RoleAssignmentRoleType.RESOURCE);
                    return assignmentModel;
                })
                .collect(Collectors.toList());
    }

    @Install(to = "rowLevelRoleAssignmentsTable.addRowLevelRole", subject = "transformation")
    private Collection<RoleAssignmentModel> rowLevelRoleTransformation(Collection<RowLevelRoleModel> roleModels) {
        Collection<String> assignedRoleCodes = rowLevelRoleAssignmentsDc.getItems().stream()
                .map(RoleAssignmentModel::getRoleCode)
                .collect(Collectors.toSet());

        return roleModels.stream()
                .filter(roleModel -> !assignedRoleCodes.contains(roleModel.getCode()))
                .map(roleModel -> {
                    RoleAssignmentModel assignmentModel = metadata.create(RoleAssignmentModel.class);
                    assignmentModel.setRoleCode(roleModel.getCode());
                    assignmentModel.setUsername(user.getUsername());
                    assignmentModel.setRoleType(RoleAssignmentRoleType.ROW_LEVEL);
                    return assignmentModel;
                })
                .collect(Collectors.toList());
    }

    @Subscribe("resourceRoleAssignmentsTable.remove")
    public void onResourceRoleAssignmentsTableRemove(ActionPerformedEvent event) {
        Set<RoleAssignmentModel> selected = resourceRoleAssignmentsTable.getSelectedItems();
        resourceRoleAssignmentsDc.getMutableItems().removeAll(selected);
        // do not immediately remove role assignments but do that
        // only when role-assignment-screen is saved
        DataContext dataContext = getDataContext();
        selected.forEach(dataContext::remove);
    }

    @Subscribe("rowLevelRoleAssignmentsTable.remove")
    public void onRowLevelRoleAssignmentsTableRemove(ActionPerformedEvent event) {
        Set<RoleAssignmentModel> selected = rowLevelRoleAssignmentsTable.getSelectedItems();
        rowLevelRoleAssignmentsDc.getMutableItems().removeAll(selected);
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

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(final SaveContext saveContext) {
        getRoleAssignmentPersistence().save(
                saveContext.getEntitiesToSave().getAll(RoleAssignmentModel.class),
                saveContext.getEntitiesToRemove().getAll(RoleAssignmentModel.class)
        );
        return Set.of();
    }

    protected RoleAssignmentPersistence getRoleAssignmentPersistence() {
        if (roleAssignmentPersistence == null) {
            throw new IllegalStateException("RoleAssignmentPersistence is not available");
        }
        return roleAssignmentPersistence;
    }
}