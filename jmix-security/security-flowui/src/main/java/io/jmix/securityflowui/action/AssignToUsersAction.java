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

package io.jmix.securityflowui.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.*;
import io.jmix.flowui.xml.layout.support.DataComponentsLoaderSupport;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentPersistence;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securityflowui.util.RoleAssignmentCandidatePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.securityflowui.util.PredicateUtils.combineRoleAssignmentPredicates;

@ActionType(AssignToUsersAction.ID)
public class AssignToUsersAction<E extends BaseRoleModel>
        extends SecuredListDataComponentAction<AssignToUsersAction<E>, E> {

    private static final Logger log = LoggerFactory.getLogger(AssignToUsersAction.class);

    public static final String ID = "sec_assignToUsers";

    protected static final String USERNAME_PROPERTY = "username";

    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;

    protected RoleAssignmentPersistence roleAssignmentPersistence;
    protected UserRepository userRepository;

    protected List<RoleAssignmentCandidatePredicate> roleAssignmentCandidatePredicates = Collections.emptyList();
    protected RoleAssignmentCandidatePredicate compositeRoleAssignmentCandidatePredicate;

    //TODO IVGA
    protected ResourceRoleRepository resourceRoleRepository;
    protected RowLevelRoleRepository rowLevelRoleRepository;

    protected E selectedItem;

    public AssignToUsersAction() {
        this(ID);
    }

    public AssignToUsersAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.USERS);
        this.compositeRoleAssignmentCandidatePredicate = combineRoleAssignmentPredicates(roleAssignmentCandidatePredicates);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired(required = false)
    public void setRoleAssignmentCandidatePredicates(List<RoleAssignmentCandidatePredicate> roleAssignmentCandidatePredicates) {
        this.roleAssignmentCandidatePredicates = roleAssignmentCandidatePredicates == null
                ? Collections.emptyList()
                : roleAssignmentCandidatePredicates;
    }

    @Autowired
    public void setResourceRoleRepository(ResourceRoleRepository resourceRoleRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
    }

    @Autowired
    public void setRowLevelRoleRepository(RowLevelRoleRepository rowLevelRoleRepository) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.assignToUsers");
        this.messages = messages;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired(required = false)
    public void setRoleAssignmentPersistence(RoleAssignmentPersistence roleAssignmentPersistence) {
        this.roleAssignmentPersistence = roleAssignmentPersistence;
    }

    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        selectedItem = target.getSingleSelectedItem();
        if (selectedItem == null) {
            String message = String.format(
                    "There is not selected item in %s target",
                    getClass().getSimpleName()
            );

            throw new IllegalStateException(message);
        }

        openDialog();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getSelectedItems().size() == 1;
    }

    protected void openDialog() {
        Class<?> userClass = userRepository.getSystemUser().getClass();

        if (!UserDetails.class.isAssignableFrom(userClass)) {
            String message = String.format(
                    "User class '%s' does not extend %s",
                    userClass.getSimpleName(),
                    UserDetails.class.getName()
            );

            throw new IllegalStateException(message);
        }

        String roleTenantId = selectedItem.getTenantId();


        Class<? extends BaseRoleModel> roleClass = selectedItem.getClass();
        BaseRole baseRole = null;
        if (ResourceRoleModel.class.isAssignableFrom(roleClass)) {
            baseRole = resourceRoleRepository.findRoleByCode(selectedItem.getCode());
        } else if (RowLevelRoleModel.class.isAssignableFrom(roleClass)) {
            baseRole = rowLevelRoleRepository.findRoleByCode(selectedItem.getCode());
        }

        final BaseRole finalBaseRole = baseRole;

        DialogWindow<View<?>> dialog = dialogWindows
                .lookup(UiComponentUtils.getView(((Component) target)), userClass)
                .withSelectHandler(this::selectHandler)
                .withSelectValidator(validationContext -> {
                    if (finalBaseRole == null) {
                        return true;
                    }
                    Collection<?> selectedItems = validationContext.getSelectedItems();
                    for (Object item : selectedItems) {
                        if (item instanceof UserDetails userDetails) {
                            boolean applicable = compositeRoleAssignmentCandidatePredicate.test(userDetails, finalBaseRole);
                            if (!applicable) {
                                log.info("[IVGA] Role '{}' can't be assigned to user '{}'", finalBaseRole.getName(), userDetails.getUsername());
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .withAfterCloseListener(this::showNotification)
                .build();

        View<?> view = dialog.getView();
        if (view instanceof MultiSelectLookupView) {
            ((MultiSelectLookupView) view).setLookupComponentMultiSelect(true);
        }

        DataLoader viewLoader = findViewLoader(view, userClass);
        configureViewLoader(viewLoader);

        dialog.open();
    }

    protected String getFakeTenantId(UserDetails userDetails) {
        return "NO_TENANT"; //todo ivga
    }

    protected void configureViewLoader(DataLoader loader) {
        List<String> excludedUsernames = getRoleAssignmentPersistence().getExcludedUsernames(selectedItem.getCode());

        loader.setCondition(PropertyCondition.notInList(USERNAME_PROPERTY, excludedUsernames).skipNullOrEmpty());
    }

    protected DataLoader findViewLoader(View<?> view, Class<?> userClass) {
        ViewData viewData = ViewControllerUtils.getViewData(view);

        List<Object> loaders = viewData
                .getLoaderIds()
                .stream()
                .filter(loaderId -> !loaderId.startsWith(DataComponentsLoaderSupport.GENERATED_PREFIX))
                .map(viewData::getLoader)
                .filter(loader -> ((DataLoader) loader).getContainer()
                        .getEntityMetaClass()
                        .getJavaClass()
                        .isAssignableFrom(userClass)
                )
                .collect(Collectors.toList());

        if (loaders.size() != 1) {
            String message = String.format("Impossible to find loader for '%s'", userClass);
            throw new IllegalStateException(message);
        } else {
            return (DataLoader) loaders.get(0);
        }
    }

    protected void selectHandler(Collection<?> userDetails) {
        List<RoleAssignment> roleAssignments = userDetails.stream()
                .map(user -> new RoleAssignment(
                        ((UserDetails) user).getUsername(),
                        selectedItem.getCode(),
                        getRoleType(selectedItem)
                ))
                .toList();
        getRoleAssignmentPersistence().save(roleAssignments);
    }

    protected void showNotification(DialogWindow.AfterCloseEvent<View<?>> viewAfterCloseEvent) {
        if (!viewAfterCloseEvent.closedWith(StandardOutcome.SELECT)) {
            return;
        }

        int selectedItemsCount = ((StandardListView<?>) viewAfterCloseEvent.getView())
                .getLookupComponent()
                .getSelectedItems()
                .size();

        String title = messages.getMessage(getClass(), "assignToUsersAction.afterCloseNotificationTitle");
        String message = messages.formatMessage(
                getClass(),
                "assignToUsersAction.afterCloseNotificationMessage",
                selectedItem.getName(), selectedItemsCount
        );

        notifications.create(title, message)
                .withType(Notifications.Type.SUCCESS)
                .show();
    }

    protected String getRoleType(E selectedItem) {
        if (selectedItem instanceof ResourceRoleModel) {
            return RoleAssignmentRoleType.RESOURCE;
        } else if (selectedItem instanceof RowLevelRoleModel) {
            return RoleAssignmentRoleType.ROW_LEVEL;
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Unknown type of role with name '%s'",
                            selectedItem.getName()
                    )
            );
        }
    }

    protected RoleAssignmentPersistence getRoleAssignmentPersistence() {
        if (roleAssignmentPersistence == null) {
            throw new IllegalStateException("RoleAssignmentPersistence is not available");
        }
        return roleAssignmentPersistence;
    }
}
