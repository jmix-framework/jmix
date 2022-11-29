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
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.model.impl.ViewDataXmlLoader;
import io.jmix.flowui.view.*;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.model.BaseRoleModel;
import io.jmix.securityflowui.model.ResourceRoleModel;
import io.jmix.securityflowui.model.RowLevelRoleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ActionType(AssignToUsersAction.ID)
public class AssignToUsersAction<E extends BaseRoleModel>
        extends SecuredListDataComponentAction<AssignToUsersAction<E>, E> {

    public static final String ID = "assignToUsers";

    protected static final String ROLE_CODE_PROPERTY = "roleCode";
    protected static final String USERNAME_PROPERTY = "username";

    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;

    protected DataManager dataManager;

    protected UserRepository userRepository;

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

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.USERS);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.assignToUsers");
        this.messages = messages;
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        DialogWindow<View<?>> dialog = dialogWindows.lookup(findParent(), userClass)
                .withSelectHandler(this::selectHandler)
                .withAfterCloseListener(this::showNotification)
                .build();

        DataLoader viewLoader = findViewLoader(dialog.getView(), userClass);
        configureViewLoader(viewLoader);

        dialog.open();
    }

    protected void configureViewLoader(DataLoader loader) {
        List<String> excludedUsernames = dataManager.load(RoleAssignmentEntity.class)
                .condition(PropertyCondition.equal(ROLE_CODE_PROPERTY, selectedItem.getCode()))
                .list()
                .stream()
                .map(RoleAssignmentEntity::getUsername)
                .collect(Collectors.toUnmodifiableList());

        loader.setCondition(PropertyCondition.notInList(USERNAME_PROPERTY, excludedUsernames));
    }

    protected DataLoader findViewLoader(View<?> view, Class<?> userClass) {
        ViewData viewData = ViewControllerUtils.getViewData(view);

        List<Object> loaders = viewData
                .getLoaderIds()
                .stream()
                .filter(loaderId -> !loaderId.startsWith(ViewDataXmlLoader.GENERATED_PREFIX))
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
        SaveContext saveContext = new SaveContext();

        userDetails.stream()
                .map(user -> {
                    RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
                    roleAssignmentEntity.setRoleCode(selectedItem.getCode());
                    roleAssignmentEntity.setUsername(((UserDetails) user).getUsername());
                    roleAssignmentEntity.setRoleType(getRoleType(selectedItem));
                    return roleAssignmentEntity;
                })
                .forEach(saveContext::saving);

        dataManager.save(saveContext);
    }

    @SuppressWarnings("unchecked")
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

    protected View<?> findParent() {
        View<?> view = UiComponentUtils.findView((Component) target);

        if (view == null) {
            String message = String.format(
                    "A component '%s' is not attached to a view",
                    target.getClass().getSimpleName()
            );

            throw new IllegalStateException(message);
        }

        return view;
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
}
