/*
 * Copyright 2025 Haulmont.
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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.core.usersubstitution.event.UserSubstitutionsChangedEvent;
import io.jmix.flowui.Actions;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.security.SubstituteUserAction;
import io.jmix.flowui.action.usermenu.UserMenuAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.usermenu.UserMenu;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem.HasSubMenu;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.View;
import io.jmix.securityflowui.SecurityUiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * An action that, depending on the number of substituted users, either opens a lookup view
 * to select a substituted user or shows a submenu with the substituted users.
 */
@ActionType(UserMenuSubstituteUserAction.ID)
public class UserMenuSubstituteUserAction extends UserMenuAction<UserMenuSubstituteUserAction, UserMenu>
        implements ApplicationListener<UserSubstitutionsChangedEvent> {

    public static final String ID = "sec_userMenuSubstituteUser";
    public static final String DEFAULT_VIEW = "sec_SubstituteUserView";

    protected Icons icons;
    protected Actions actions;
    protected Dialogs dialogs;
    protected Messages messages;
    protected DialogWindows dialogWindows;
    protected MetadataTools metadataTools;
    protected ApplicationContext applicationContext;
    protected UserSubstitutionManager substitutionManager;
    protected CurrentUserSubstitution currentUserSubstitution;

    protected List<UserDetails> currentSubstitutedUsers;
    protected int maxSubstitutions;

    protected final Map<String, UserMenuItem> menuItems = new HashMap<>(3);
    protected HasSubMenu.SubMenu subMenu;

    protected Registration attachRegistration;
    protected Registration detachRegistration;

    public UserMenuSubstituteUserAction() {
        this(ID);
    }

    public UserMenuSubstituteUserAction(String id) {
        super(id);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;

        this.text = messages.getMessage("actions.userMenu.SubstituteUser");
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.USER_MENU_SUBSTITUTE_USER_ACTION);
        }
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setCurrentUserSubstitution(CurrentUserSubstitution currentUserSubstitution) {
        this.currentUserSubstitution = currentUserSubstitution;
    }

    @Autowired
    public void setSubstitutionManager(UserSubstitutionManager substitutionManager) {
        this.substitutionManager = substitutionManager;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setSecurityUiProperties(SecurityUiProperties securityUiProperties) {
        maxSubstitutions = securityUiProperties.getSubstituteUserActionMaxSubstitutions();
    }

    /**
     * Returns the maximum number of users that can be substituted by the currently authenticated user,
     * which shows a submenu instead of opening a lookup view.
     *
     * @return the maximum number of users that can be substituted by the currently authenticated user,
     * which shows a submenu instead of opening a lookup view.
     */
    public int getMaxSubstitutions() {
        return maxSubstitutions;
    }

    /**
     * Sets the maximum number of users that can be substituted by the currently authenticated user,
     * which shows a submenu instead of opening a lookup view.
     *
     * @param maxSubstitutions number of users until a lookup view is used to select a substitution
     */
    public void setMaxSubstitutions(int maxSubstitutions) {
        this.maxSubstitutions = maxSubstitutions;
    }

    @Override
    protected void setTargetInternal(@Nullable UserMenu target) {
        super.setTargetInternal(target);

        unbindListeners();

        if (target != null) {
            bindListeners(target);
        }
    }

    protected void unbindListeners() {
        if (attachRegistration != null) {
            attachRegistration.remove();
            attachRegistration = null;
        }

        if (detachRegistration != null) {
            detachRegistration.remove();
            detachRegistration = null;
        }
    }

    protected void bindListeners(UserMenu target) {
        attachRegistration = target.addAttachListener(this::onTargetAttach);
        detachRegistration = target.addDetachListener(this::onTargetDetach);
    }

    protected void onTargetAttach(AttachEvent attachEvent) {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            context.addApplicationListener(this);
        }
    }

    protected void onTargetDetach(DetachEvent detachEvent) {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            context.removeApplicationListener(this);
        }
    }

    @Override
    public void refreshState() {
        updateSubstitutedUsers();
        updateMenuItem();

        super.refreshState();
    }

    protected void updateSubstitutedUsers() {
        if (target != null && menuItem != null) {
            currentSubstitutedUsers = substitutionManager.getCurrentSubstitutedUsers();
        } else {
            currentSubstitutedUsers = Collections.emptyList();
        }
    }

    protected void updateMenuItem() {
        if (menuItem == null
                || currentSubstitutedUsers.isEmpty()
                || currentSubstitutedUsers.size() > maxSubstitutions) {
            removeSubMenu();

            return;
        }

        if (subMenu == null) {
            subMenu = createSubMenu();
        } else {
            subMenu.removeAll();
        }

        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        UserMenuItem authenticatedUserMenuItem = createSubMenuItem(authenticatedUser);
        authenticatedUserMenuItem.addThemeName("authenticated-user");
        menuItems.put(authenticatedUser.getUsername(), authenticatedUserMenuItem);

        for (UserDetails user : currentSubstitutedUsers) {
            menuItems.put(user.getUsername(), createSubMenuItem(user));
        }

        updateState(currentUserSubstitution.getEffectiveUser().getUsername());
    }

    protected HasSubMenu.SubMenu createSubMenu() {
        if (!(menuItem instanceof HasSubMenu hasSubMenu)) {
            throw new IllegalStateException("%s does not implement %s"
                    .formatted(menuItem, HasSubMenu.class.getSimpleName()));
        }

        return hasSubMenu.getSubMenu();
    }

    protected UserMenuItem createSubMenuItem(UserDetails user) {
        String itemId = "%s_%sUserMenuItem".formatted(ID, user.getUsername());
        TextUserMenuItem item = subMenu.addTextItem(itemId, generateUserName(user), __ -> {
            updateState(user.getUsername());
            substituteUser(user);
        });
        item.setCheckable(true);

        return item;
    }

    protected void substituteUser(UserDetails newUser) {
        UserDetails prevUser = currentUserSubstitution.getEffectiveUser();
        if (prevUser.equals(newUser)) {
            return;
        }

        dialogs.createOptionDialog()
                .withHeader(messages.getMessage("dialogs.substitutionConfirmation.header"))
                .withText(messages.formatMessage("", "dialogs.substitutionConfirmation.text",
                        metadataTools.getInstanceName(newUser)))
                .withActions(
                        ((SubstituteUserAction) actions.create(SubstituteUserAction.ID))
                                .withUsers(prevUser, newUser)
                                .withCancelHandler(userDetails -> updateState(userDetails.getUsername()))
                                .withText(messages.getMessage("actions.Ok"))
                                .withIcon(icons.get(JmixFontIcon.DIALOG_OK))
                                .withVariant(ActionVariant.PRIMARY),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withHandler(__ -> updateState(prevUser.getUsername()))
                )
                .open();
    }

    protected void updateState(String username) {
        menuItems.forEach((key, menuItem) ->
                menuItem.setChecked(key.equals(username)));
    }

    protected void removeSubMenu() {
        if (subMenu != null) {
            subMenu.removeAll();
            subMenu = null;
        }
    }

    protected String generateUserName(UserDetails user) {
        if (EntityValues.isEntity(user)) {
            return metadataTools.getInstanceName(user);
        } else {
            return user.getUsername();
        }
    }

    @Override
    protected void setVisibleInternal(boolean visible) {
        super.setVisibleInternal(visible && !currentSubstitutedUsers.isEmpty());
    }

    @Override
    public void execute() {
        checkTarget();

        if (currentSubstitutedUsers.size() <= maxSubstitutions) {
            return;
        }

        View<?> origin = UiComponentUtils.getView(target);
        dialogWindows.view(origin, DEFAULT_VIEW)
                .open();
    }

    @Override
    public void onApplicationEvent(UserSubstitutionsChangedEvent event) {
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (Objects.equals(authenticatedUser.getUsername(), event.getSource())) {
            refreshState();
        }
    }
}
