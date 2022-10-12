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

package io.jmix.securityflowui.view.changepassword;

import com.google.common.base.Strings;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.ViewValidation;
import io.jmix.securityflowui.password.PasswordValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import java.util.Objects;

@ViewController("changePasswordView")
@ViewDescriptor("change-password-view.xml")
@DialogMode(width = "AUTO", height = "AUTO")
public class ChangePasswordView extends StandardView {

    @ViewComponent
    protected PasswordField passwordField;
    @ViewComponent
    protected PasswordField confirmPasswordField;
    @ViewComponent
    protected PasswordField currentPasswordField;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UserManager userManager;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected PasswordValidation passwordValidation;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected MetadataTools metadataTools;

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
     */
    public void setUsername(String username) {
        this.username = username;
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
     */
    public void setCurrentPasswordRequired(boolean required) {
        currentPasswordField.setVisible(required);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Preconditions.checkNotNullArgument(username,
                messageBundle.getMessage("changePasswordView.emptyUsernameMessage"));
        user = userRepository.loadUserByUsername(username);
    }

    @Override
    public String getPageTitle() {
        return username != null
                ? String.format(messageBundle.formatMessage("changePasswordView.title", username))
                : super.getPageTitle();
    }

    @Subscribe("saveAction")
    public void onSaveActionPerformed(ActionPerformedEvent event) {
        if (!validate()) {
            return;
        }

        changePassword(username, getPassword(), null)
                .then(() -> {
                    notifications.create(messageBundle.getMessage("changePasswordView.passwordChanged"))
                            .withType(Notifications.Type.SUCCESS)
                            .withPosition(Notification.Position.MIDDLE)
                            .show();
                    close(StandardOutcome.SAVE);
                }).otherwise(() -> {
                    viewValidation.showValidationErrors(ValidationErrors.of(
                            messageBundle.getMessage("changePasswordView.currentPasswordWarning")));
                });

        close(StandardOutcome.SAVE);
    }

    @Subscribe("closeAction")
    public void onCloseActionPerformed(ActionPerformedEvent event) {
        closeWithDefaultAction();
    }

    protected boolean validate() {
        ValidationErrors errors = validatePassword(passwordField, confirmPasswordField, currentPasswordField);

        if (errors.isEmpty()) {
            return true;
        } else {
            viewValidation.showValidationErrors(errors);
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
                errors.add(passwordField, messageBundle.getMessage("changePasswordView.currentPasswordRequired"));
            } else if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                // if current password is wrong
                errors.add(currentPasswordField, messageBundle.getMessage("changePasswordView.wrongCurrentPassword"));
            } else if (Objects.equals(password, currentPassword)) {
                errors.add(passwordField, messageBundle.getMessage("changePasswordView.currentPasswordWarning"));
            }
        }
        if (Strings.isNullOrEmpty(password)) {
            errors.add(passwordField, messageBundle.getMessage("changePasswordView.passwordRequired"));
        }
        if (Strings.isNullOrEmpty(confirmPassword)) {
            errors.add(passwordField, messageBundle.getMessage("changePasswordView.confirmPasswordRequired"));
        }
        if (!Strings.isNullOrEmpty(password)
                && !Strings.isNullOrEmpty(confirmPassword)
                && !Objects.equals(password, confirmPassword)) {
            errors.add(confirmPasswordField, messageBundle.getMessage("changePasswordView.passwordsDoNotMatch"));
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
}
