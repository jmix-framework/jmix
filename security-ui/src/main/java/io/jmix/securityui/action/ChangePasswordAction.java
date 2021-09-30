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

package io.jmix.securityui.action;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

@ActionType(ChangePasswordAction.ID)
public class ChangePasswordAction extends SecuredListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {

    public static final String ID = "changePassword";

    protected UiComponents uiComponents;
    // Set default caption only once
    protected boolean currentPasswordRequired = false;
    protected Messages messages;
    protected UserManager userManager;
    protected Notifications notifications;

    public ChangePasswordAction() {
        super(ID);
    }

    public ChangePasswordAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage("actions.changePassword");
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setCurrentPasswordRequired(boolean currentPasswordRequired) {
        this.currentPasswordRequired = currentPasswordRequired;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isEditPermitted()) {
            return false;
        }

        return super.isPermitted();
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

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("Target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("Target dataSource is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        Object editedEntity = target.getSingleSelected();
        if (editedEntity == null) {
            throw new IllegalStateException("There is not selected item in ChangePassword target");
        }

        UserDetails user = (UserDetails) editedEntity;

        buildAndShowChangePasswordDialog(user);
    }

    private void buildAndShowChangePasswordDialog(UserDetails user) {
        Dialogs.InputDialogBuilder builder = UiControllerUtils.getScreenContext(target.getFrame().getFrameOwner())
                .getDialogs()
                .createInputDialog(target.getFrame().getFrameOwner());

        builder.withCaption(String.format(messages.getMessage("changePasswordDialog.captionWithUserName"), user.getUsername()));

        PasswordField currentPasswField = uiComponents.create(PasswordField.class);
        currentPasswField.setCaption(messages.getMessage("changePasswordDialog.currentPassword"));
        currentPasswField.setWidthFull();

        PasswordField passwField = uiComponents.create(PasswordField.class);
        passwField.setCaption(messages.getMessage("changePasswordDialog.password"));
        passwField.setWidthFull();

        PasswordField confirmPasswField = uiComponents.create(PasswordField.class);
        confirmPasswField.setCaption(messages.getMessage("changePasswordDialog.confirmPassword"));
        confirmPasswField.setWidthFull();

        builder.withParameters(
                new InputParameter("password")
                        .withField(() -> passwField),
                new InputParameter("confirmPassword")
                        .withField(() -> confirmPasswField));

        if (currentPasswordRequired) {
            builder.withParameter(new InputParameter("currentPassword").withField(() -> currentPasswField));
        }

        String userName = user.getUsername();
        builder.withValidator(validationContext -> getValidationErrors(passwField, confirmPasswField, validationContext));
        InputDialog inputDialog = builder.build();
        builder.withActions(DialogActions.OK_CANCEL, result -> okButtonAction(userName, result));
        inputDialog.show();
    }

    private ValidationErrors getValidationErrors(PasswordField passwField, PasswordField confirmPasswField, InputDialog.ValidationContext validationContext) {
        String password = validationContext.getValue("password");
        String confirmPassword = validationContext.getValue("confirmPassword");
        ValidationErrors errors = new ValidationErrors();
        if (!Objects.equals(password, confirmPassword)) {
            errors.add(confirmPasswField, messages.getMessage("changePasswordDialog.passwordsDoNotMatch"));
        }
        if (Strings.isNullOrEmpty(password)) {
            errors.add(passwField, messages.getMessage("changePasswordDialog.passwordRequired"));
        }
        if (errors.isEmpty()) {
            return ValidationErrors.none();
        }
        return errors;
    }

    private void okButtonAction(String userName, InputDialog.InputDialogResult result) {
        if (result.getCloseActionType() == InputDialog.InputDialogResult.ActionType.OK) {
            String newPassword = result.getValue("password");
            String oldPassword = result.getValue("currentPassword");
            try {
                userManager.changePassword(userName, oldPassword, newPassword);
            } catch (PasswordNotMatchException e) {
                if (currentPasswordRequired) {
                    notifications.create()
                            .withType(Notifications.NotificationType.ERROR)
                            .withCaption(messages.getMessage("changePasswordDialog.wrongCurrentPassword"))
                            .show();
                } else {
                    notifications.create()
                            .withType(Notifications.NotificationType.WARNING)
                            .withCaption(messages.getMessage("changePasswordDialog.currentPasswordWarning"))
                            .show();

                }
                return;
            }
            notifications.create()
                    .withType(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("changePasswordDialog.passwordChanged"))
                    .show();
        }
    }
}
