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
import io.jmix.core.security.UserRepository;
import io.jmix.securityui.password.PasswordValidation;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.screen.*;
import io.jmix.ui.util.OperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import java.util.Objects;

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
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected PasswordValidation passwordValidation;
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordField passwordField;
    @Autowired
    protected PasswordField confirmPasswordField;
    @Autowired
    protected PasswordField currentPasswordField;

    protected String username;
    protected UserDetails user;

    /**
     * @return username for which should be changed password
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username for which should be changed password.
     *
     * @param username username
     * @return current instance of dialog
     */
    public ChangePasswordDialog withUsername(String username) {
        this.username = username;
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
        Preconditions.checkNotNullArgument(username, "Dialog cannot be opened without username");

        getWindow().setCaption(String.format(
                messageBundle.getMessage("ChangePasswordDialog.captionWithUserName"), username));

        user = userRepository.loadUserByUsername(username);

        getWindow().focusFirstComponent();
    }

    @Subscribe("okBtn")
    protected void onOkBtnClick(Button.ClickEvent event) {
        if (!validate()) {
            return;
        }

        changePassword(username, getPassword(), null)
                .then(() -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.HUMANIZED)
                            .withCaption(messageBundle.getMessage("ChangePasswordDialog.passwordChanged"))
                            .show();
                    close(StandardOutcome.COMMIT);
                }).otherwise(() -> {
                    screenValidation.showValidationErrors(this, ValidationErrors.of(
                            messageBundle.getMessage("ChangePasswordDialog.currentPasswordWarning")));
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

        if (currentPasswordField.isVisible()) {
            if (Strings.isNullOrEmpty(currentPassword)) {
                errors.add(passwordField, messageBundle.getMessage("ChangePasswordDialog.currentPasswordRequired"));
            } else if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                // if current password is wrong
                errors.add(currentPasswordField, messageBundle.getMessage("ChangePasswordDialog.wrongCurrentPassword"));
            } else if (Objects.equals(password, currentPassword)) {
                errors.add(passwordField, messageBundle.getMessage("ChangePasswordDialog.currentPasswordWarning"));
            }
        }
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

        for (String errorMessage : passwordValidation.validate(user, password)) {
            errors.add(errorMessage);
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
