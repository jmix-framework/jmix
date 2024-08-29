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

package io.jmix.securityflowui.view.resetpassword;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.security.UserManager;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.view.resetpassword.model.UserPasswordValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ViewController("resetPasswordView")
@ViewDescriptor("reset-password-view.xml")
@DialogMode(width = "35em", resizable = true)
public class ResetPasswordView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordView.class);

    protected static final String RESET_PASSWORD_FIELD_CLASS_NAME = "reset-password-field";

    protected static final String PASSWORD_FIELD_ID = "passwordField";
    protected static final String BUTTON_ID = "copyButton";

    @Autowired
    protected UserManager userManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiAsyncTasks uiAsyncTasks;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UiComponents uiComponents;

    @Autowired(required = false)
    protected UiExportHelper uiExportHelper;

    @ViewComponent
    protected HorizontalLayout buttonPanel;
    @ViewComponent
    protected DataGrid<UserPasswordValue> passwordsDataGrid;
    @ViewComponent
    protected JmixButton closeBtn;

    @ViewComponent
    protected CollectionLoader<UserPasswordValue> userPasswordValueDl;

    protected JmixButton exportActionButton;

    protected Set<? extends UserDetails> users;

    @Subscribe
    public void onInit(InitEvent event) {
        initExportAction();
    }

    @Install(to = "passwordsDataGrid.username", subject = "tooltipGenerator")
    protected String passwordsDataGridUsernameTooltipGenerator(UserPasswordValue userPasswordValue) {
        return userPasswordValue.getUsername();
    }

    @Supply(to = "passwordsDataGrid.username", subject = "renderer")
    protected Renderer<UserPasswordValue> passwordsDataGridUsernameRenderer() {
        return new TextRenderer<>(UserPasswordValue::getUsername);
    }

    @Supply(to = "passwordsDataGrid.password", subject = "renderer")
    protected Renderer<UserPasswordValue> passwordsDataGridPasswordRenderer() {
        return new ComponentRenderer<>(this::layoutFactory, this::passwordFieldInitializer);
    }

    protected void initExportAction() {
        if (uiExportHelper == null) {
            return;
        }

        exportActionButton = uiComponents.create(JmixButton.class);
        exportActionButton.setText(messageBundle.getMessage("resetPasswordView.exportActionButton.text"));
        exportActionButton.setIcon(VaadinIcon.FILE_TABLE.create());
        exportActionButton.setVisible(false);
        exportActionButton.addClickListener(this::onExportBtnClick);

        buttonPanel.addComponentAtIndex(buttonPanel.indexOf(closeBtn), exportActionButton);
    }

    protected void onExportBtnClick(ClickEvent<Button> event) {
        uiExportHelper.exportDataGrid(passwordsDataGrid);
    }

    @Override
    public String getPageTitle() {
        return isSingleSelected()
                ? messageBundle.formatMessage("resetPasswordView.resetSinglePasswordTitle",
                users.iterator().next().getUsername())
                : messageBundle.getMessage("resetPasswordView.resetPasswordsTitle");
    }

    @SuppressWarnings("unchecked")
    @Install(to = "userPasswordValueDl", target = Target.DATA_LOADER)
    protected List<UserPasswordValue> userPasswordValuesDlLoadDelegate(LoadContext<UserPasswordValue> loadContext) {
        if (!passwordsDataGrid.isVisible()) {
            passwordsDataGrid.setVisible(true);

            if (exportActionButton != null) {
                exportActionButton.setVisible(true);
            }
        }

        return userManager.resetPasswords((Set<UserDetails>) users)
                .entrySet()
                .stream()
                .map(this::userPasswordValueMapper)
                .toList();
    }

    @Subscribe("generateBtn")
    protected void onGenerateBtnClick(ClickEvent<JmixButton> event) {
        userPasswordValueDl.load();
    }

    protected UserPasswordValue userPasswordValueMapper(Map.Entry<UserDetails, String> entry) {
        UserPasswordValue userPasswordValue = metadata.create(UserPasswordValue.class);
        userPasswordValue.setUsername(entry.getKey().getUsername());
        userPasswordValue.setPassword(entry.getValue());

        return userPasswordValue;
    }

    protected HorizontalLayout layoutFactory() {
        HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        JmixPasswordField passwordField = uiComponents.create(JmixPasswordField.class);
        passwordField.setId(PASSWORD_FIELD_ID);
        passwordField.addClassName(RESET_PASSWORD_FIELD_CLASS_NAME);
        passwordField.setReadOnly(true);

        JmixButton button = uiComponents.create(JmixButton.class);
        button.setId(BUTTON_ID);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
        button.setTooltipText(messageBundle.getMessage("resetPasswordView.copyButton.tooltip"));
        button.setIcon(VaadinIcon.COPY.create());

        layout.add(passwordField, button);

        return layout;
    }

    protected void passwordFieldInitializer(HorizontalLayout layout, UserPasswordValue userPasswordValue) {
        String password = userPasswordValue.getPassword();

        UiComponentUtils.findOwnComponent(layout, PASSWORD_FIELD_ID)
                .ifPresent(passwordField -> ((JmixPasswordField) passwordField).setValue(password));
        UiComponentUtils.findOwnComponent(layout, BUTTON_ID)
                .ifPresent(button -> copyButtonInitializer(((JmixButton) button), password));
    }

    protected void copyButtonInitializer(JmixButton button, String valueToCopy) {
        button.addClickListener(__ ->
                UiComponentUtils.copyToClipboard(button, valueToCopy)
                        .then(successResult -> {
                            applySuccessButtonStyles(button);

                            uiAsyncTasks.runnableConfigurer(() -> {
                                        try {
                                            // timer
                                            TimeUnit.SECONDS.sleep(2);
                                        } catch (InterruptedException e) {
                                            log.debug("{} exception in background task", e.getClass().getName(), e);
                                        }
                                    })
                                    .withResultHandler(() -> applyDefaultButtonStyles(button))
                                    .withTimeout(3, TimeUnit.SECONDS)
                                    .runAsync();
                        })
        );
    }

    protected void applySuccessButtonStyles(JmixButton button) {
        button.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        button.setIcon(VaadinIcon.CHECK.create());
    }

    protected void applyDefaultButtonStyles(JmixButton button) {
        button.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
        button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        button.setIcon(VaadinIcon.COPY.create());
    }

    protected boolean isSingleSelected() {
        return users != null && users.size() == 1;
    }

    public void setUsers(Set<? extends UserDetails> users) {
        this.users = users;

        if (isSingleSelected()) {
            passwordsDataGrid.setMinHeight("4em");
        }
    }
}
