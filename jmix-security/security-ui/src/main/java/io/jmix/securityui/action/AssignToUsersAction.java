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

package io.jmix.securityui.action;

import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.UserRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.model.BaseRoleModel;
import io.jmix.securityui.model.ResourceRoleModel;
import io.jmix.securityui.model.RowLevelRoleModel;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Assign the role to selected users"
)
@ActionType(AssignToUsersAction.ID)
public class AssignToUsersAction extends SecuredListAction implements Action.ExecutableAction {

    public static final String ID = "assignToUsers";

    protected static final String ROLE_CODE_PROPERTY = "roleCode";
    protected static final String USERNAME_PROPERTY = "username";

    protected ScreenBuilders screenBuilders;
    protected Notifications notifications;
    protected Messages messages;

    protected DataManager dataManager;

    protected UserRepository userRepository;

    protected BaseRoleModel selectedItem;

    public AssignToUsersAction() {
        this(ID);
    }

    public AssignToUsersAction(String id) {
        super(id);
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.assignToUsers");
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

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.USERS);
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("Target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("Target items is null or does not implement EntityDataUnit");
        }

        if (!(target.getSingleSelected() instanceof BaseRoleModel)) {
            throw new IllegalStateException("Target item does not implement BaseRoleModel");
        }

        selectedItem = (BaseRoleModel) target.getSingleSelected();

        openDialog();
    }


    protected void openDialog() {
        Class<?> userClass = userRepository.getSystemUser().getClass();

        if (!UserDetails.class.isAssignableFrom(userClass)) {
            String message = String.format(
                    "User class '%s' does not extend %s",
                    userClass.getSimpleName(),
                    UserDetails.class.getName());

            throw new IllegalStateException(message);
        }

        Screen screen = screenBuilders.lookup(userClass, findParent())
                .withSelectHandler(this::selectHandler)
                .withOpenMode(OpenMode.DIALOG)
                .build();

        DataLoader loader = findScreenLoader(screen, userClass);
        configureScreenLoader(loader);

        screen.show();
    }

    protected void configureScreenLoader(DataLoader loader) {
        List<String> excludedUsernames = dataManager.load(RoleAssignmentEntity.class)
                .condition(PropertyCondition.equal(ROLE_CODE_PROPERTY, selectedItem.getCode()))
                .list()
                .stream()
                .map(RoleAssignmentEntity::getUsername)
                .collect(Collectors.toUnmodifiableList());

        loader.setCondition(PropertyCondition.notInList(USERNAME_PROPERTY, excludedUsernames));
    }

    protected DataLoader findScreenLoader(Screen view, Class<?> userClass) {
        ScreenData screenData = UiControllerUtils.getScreenData(view);

        List<Object> loaders = screenData
                .getLoaderIds()
                .stream()
                .filter(loaderId -> !loaderId.startsWith(ScreenDataXmlLoader.GENERATED_PREFIX))
                .map(screenData::getLoader)
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

        int selectedItemsCount = userDetails.size();
        showNotification(selectedItemsCount);
    }

    protected void showNotification(int selectedItemsCount) {
        String caption = messages.getMessage(getClass(), "assignToUsersAction.afterCloseNotificationTitle");
        String description = messages.formatMessage(
                getClass(),
                "assignToUsersAction.afterCloseNotificationMessage",
                selectedItem.getName(), selectedItemsCount
        );

        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withCaption(caption)
                .withDescription(description)
                .show();
    }

    protected Screen findParent() {
        Frame frame = target.getFrame();

        if (frame == null) {
            String message = String.format(
                    "A component '%s' is not attached to a screen",
                    target.getClass().getSimpleName()
            );

            throw new IllegalStateException(message);
        }

        return UiControllerUtils.getScreen(frame.getFrameOwner());
    }

    protected String getRoleType(BaseRoleModel selectedItem) {
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
