/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securityui.screen.changepassword;

import com.google.common.base.Strings;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import io.jmix.ui.util.OperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@UiController("ChangePasswordDialog")
@UiDescriptor("change-password-dialog.xml")
public class ChangePasswordDialog extends Screen {

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UserManager userManager;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected PasswordField passwordField;
    @Autowired
    protected PasswordField confirmPasswordField;
    @Autowired
    protected PasswordField currentPasswordField;

    protected UserDetails user;

    /**
     * @return user for which should be changed password
     */
    public UserDetails getUser() {
        return user;
    }

    /**
     * Sets user for which should be changed password.
     *
     * @param user user
     * @return current instance of dialog
     */
    public ChangePasswordDialog withUser(UserDetails user) {
        this.user = user;
        return this;
    }

    /**
     * @return {@code true} if a user should enter the current password
     */
    public boolean isCurrentPasswordRequired() {
        return currentPasswordField.isVisible();
    }

    /**
     * Sets whether a user should enter the current password.
     * <p>
     * Default value is {@code false}.
     *
     * @param required required option
     * @return current instance of dialog
     */
    public ChangePasswordDialog withCurrentPasswordRequired(boolean required) {
        currentPasswordField.setVisible(required);
        return this;
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        Preconditions.checkNotNullArgument(user, "Dialog cannot be opened without user");

        getWindow().setCaption(String.format(
                messageBundle.getMessage("ChangePasswordDialog.captionWithUserName"), user.getUsername()));
    }

    @Subscribe("okBtn")
    protected void onOkBtnClick(Button.ClickEvent event) {
        if (!validate()) {
            return;
        }

        changePassword(user.getUsername(), getPassword(), getCurrentPassword())
                .then(() -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.HUMANIZED)
                            .withCaption(messageBundle.getMessage("ChangePasswordDialog.passwordChanged"))
                            .show();
                    close(StandardOutcome.COMMIT);
                }).otherwise(() -> {
                    if (currentPasswordField.isVisible()) {
                        notifications.create()
                                .withType(Notifications.NotificationType.ERROR)
                                .withCaption(messageBundle.getMessage("ChangePasswordDialog.wrongCurrentPassword"))
                                .show();
                    } else {
                        notifications.create()
                                .withType(Notifications.NotificationType.WARNING)
                                .withCaption(messageBundle.getMessage("ChangePasswordDialog.currentPasswordWarning"))
                                .show();
                    }
                });
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(Button.ClickEvent event) {
        close(StandardOutcome.DISCARD);
    }

    protected boolean validate() {
        ValidationErrors errors = validatePassword(passwordField, confirmPasswordField, currentPasswordField);

        if (errors.isEmpty()) {
            return true;
        } else {
            screenValidation.showValidationErrors(this, errors);
            return false;
        }
    }

    protected ValidationErrors validatePassword(PasswordField passwordField,
                                                PasswordField confirmPasswordField,
                                                PasswordField currentPasswordField) {
        ValidationErrors errors = new ValidationErrors();

        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        String currentPassword = currentPasswordField.getValue();

        if (Strings.isNullOrEmpty(password)) {
            errors.add(passwordField, messageBundle.getMessage("ChangePasswordDialog.passwordRequired"));
        }
        if (Strings.isNullOrEmpty(confirmPassword)) {
            errors.add(passwordField, messageBundle.getMessage("ChangePasswordDialog.confirmPasswordRequired"));
        }
        if (!Strings.isNullOrEmpty(password)
                && !Strings.isNullOrEmpty(confirmPassword)
                && !Objects.equals(password, confirmPassword)) {
            errors.add(confirmPasswordField, messageBundle.getMessage("ChangePasswordDialog.passwordsDoNotMatch"));
        }
        if (currentPasswordField.isVisible()) {
            if (Strings.isNullOrEmpty(currentPassword)) {
                errors.add(passwordField, messageBundle.getMessage("ChangePasswordDialog.currentPasswordRequired"));
            } else if (Objects.equals(password, currentPassword)) {
                errors.add(passwordField, messageBundle.getMessage("ChangePasswordDialog.currentPasswordWarning"));
            }
        }

        if (errors.isEmpty()) {
            return ValidationErrors.none();
        }
        return errors;
    }

    protected OperationResult changePassword(String username, String password, @Nullable String currentPassword) {
        try {
            userManager.changePassword(username, currentPassword, password);
        } catch (PasswordNotMatchException e) {
            return OperationResult.fail();
        }
        return OperationResult.success();
    }

    @Nullable
    protected String getPassword() {
        return passwordField.getValue();
    }

    @Nullable
    protected String getConfirmPassword() {
        return confirmPasswordField.getValue();
    }

    @Nullable
    protected String getCurrentPassword() {
        return currentPasswordField.getValue();
    }
}
