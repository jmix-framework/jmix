/*
 * Copyright 2026 Haulmont.
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

package io.jmix.securityflowui.authentication;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.user.PasswordChangeRequiredSupport;
import io.jmix.securityflowui.view.changepassword.ChangePasswordView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Opens a forced {@link ChangePasswordView} dialog on the main view if the currently logged in user is required
 * to change the password at the next logon.
 * <p>
 * Activates only when the user entity has a field marked with
 * {@link io.jmix.security.user.PasswordChangeRequired @PasswordChangeRequired} and the value is {@code true}.
 * Otherwise this listener does nothing.
 *
 * @see PasswordChangeRequiredSupport
 */
@Component("sec_PasswordChangeRequiredViewListener")
public class PasswordChangeRequiredViewListener {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected PasswordChangeRequiredSupport passwordChangeRequiredSupport;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected ViewRegistry viewRegistry;

    @EventListener
    public void onViewOpened(ViewOpenedEvent event) {
        View<?> view = event.getSource();
        if (!isMainOrDefaultView(view)) {
            return;
        }

        UserDetails user = currentAuthentication.getUser();
        if (!passwordChangeRequiredSupport.isPasswordChangeRequired(user)) {
            return;
        }

        openForcedChangePasswordDialog(view, user.getUsername());
    }

    protected boolean isMainOrDefaultView(View<?> view) {
        Class<?> viewClass = view.getClass();
        if (isViewOfId(viewClass, uiProperties.getMainViewId())) {
            return true;
        }

        String defaultViewId = uiProperties.getDefaultViewId();
        return !Strings.isNullOrEmpty(defaultViewId) && isViewOfId(viewClass, defaultViewId);
    }

    protected boolean isViewOfId(Class<?> viewClass, String viewId) {
        try {
            ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
            return viewInfo.getControllerClass().isAssignableFrom(viewClass);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    protected void openForcedChangePasswordDialog(View<?> origin, String username) {
        DialogWindow<ChangePasswordView> dialog = dialogWindows.view(origin, ChangePasswordView.class)
                .build();

        ChangePasswordView view = dialog.getView();
        view.setUsername(username);
        view.setCurrentPasswordRequired(false);
        view.setForced(true);

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.addAfterCloseListener(closeEvent -> {
            if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                UI.getCurrent().access(() -> openForcedChangePasswordDialog(origin, username));
            }
        });

        dialog.open();
    }
}
