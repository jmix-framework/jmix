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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.security.UserManager;
import io.jmix.core.security.event.UserPasswordResetEvent;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundTaskHandler;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.view.resetpassword.model.UserPasswordValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
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
    protected DataManager dataManager;
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    protected UiAsyncTasks uiAsyncTasks;
    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UiComponents uiComponents;

    @Autowired(required = false)
    protected UiExportHelper uiExportHelper;

    @ViewComponent
    protected VerticalLayout resetPasswordLayout;
    @ViewComponent
    protected JmixButton generateBtn;
    @ViewComponent
    protected HorizontalLayout buttonPanel;
    @ViewComponent
    protected DataGrid<UserPasswordValue> passwordsDataGrid;
    @ViewComponent
    protected JmixButton closeBtn;

    @ViewComponent
    protected VerticalLayout progressBarLayout;
    @ViewComponent
    protected JmixButton generationCancelBtn;
    @ViewComponent
    protected ProgressBar resetProgressBar;
    @ViewComponent
    protected Span progressBarLabel;
    @ViewComponent
    protected Span progressSpan;

    @ViewComponent
    protected CollectionContainer<UserPasswordValue> userPasswordValueDc;

    protected BackgroundTaskHandler<List<UserPasswordValue>> generationTaskHandler;

    protected JmixButton exportActionButton;

    protected Set<? extends UserDetails> users;
    protected Dialog cancelDialog;

    @Subscribe
    public void onInit(InitEvent event) {
        initExportAction();
    }

    @Subscribe
    public void onBeforeClose(final BeforeCloseEvent event) {
        if (generationTaskHandler != null && generationTaskHandler.isAlive()
                && !event.closedWith(StandardOutcome.DISCARD)) {
            UnknownOperationResult result = new UnknownOperationResult();

            cancelDialog = dialogs.createOptionDialog()
                    .withHeader(messageBundle.getMessage("resetPasswordView.cancelDialog.title"))
                    .withText(messageBundle.getMessage("resetPasswordView.cancelDialog.message"))
                    .withActions(
                            new DialogAction(DialogAction.Type.YES)
                                    .withHandler(__ -> result.resume(close(StandardOutcome.DISCARD))),
                            new DialogAction(DialogAction.Type.NO)
                                    .withHandler(__ -> result.fail())
                                    .withVariant(ActionVariant.PRIMARY)
                    )
                    .open();

            event.preventClose(result);
        }
    }

    @Supply(to = "passwordsDataGrid.password", subject = "renderer")
    protected Renderer<UserPasswordValue> passwordsDataGridPasswordRenderer() {
        return new ComponentRenderer<>(this::passwordLayoutFactory, this::passwordFieldInitializer);
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

    @Subscribe("generateBtn")
    protected void onGenerateBtnClick(ClickEvent<JmixButton> event) {
        configureComponentsBeforeGeneration();

        generationTaskHandler = backgroundWorker.handle(createBackgroundTask());
        generationTaskHandler.execute();
    }

    protected void configureComponentsBeforeGeneration() {
        resetPasswordLayout.setVisible(false);
        progressBarLayout.setVisible(true);

        generateBtn.setEnabled(false);
        generationCancelBtn.setEnabled(true);

        progressBarLabel.setText(messageBundle.getMessage("resetPasswordView.progressBarLabel.text"));
        resetProgressBar.setValue(0);
        resetProgressBar.removeThemeVariants(ProgressBarVariant.LUMO_ERROR);
        progressSpan.setText("%s/%s".formatted(0, users.size()));
    }

    protected void configureComponentOnProgress(Integer progress) {
        resetProgressBar.setValue(progress);
        progressSpan.setText("%s/%s".formatted(progress, users.size()));
    }

    protected void configureComponentsAfterCanceling() {
        generationCancelBtn.setEnabled(false);
        generateBtn.setEnabled(true);

        resetProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
        progressBarLabel.setText(messageBundle.getMessage("resetPasswordView.progressBarLabel.cancelText"));
    }

    protected void configureComponentsAfterGeneration() {
        passwordsDataGrid.setVisible(true);
        progressBarLayout.setVisible(false);

        if (exportActionButton != null) {
            exportActionButton.setVisible(true);
        }
    }

    @Subscribe("generationCancelBtn")
    protected void onGenerationCancelBtnClick(ClickEvent<JmixButton> event) {
        cancelGenerationProcess();
        configureComponentsAfterCanceling();
    }

    protected BackgroundTask<Integer, List<UserPasswordValue>> createBackgroundTask() {
        return new ResetPasswordBackgroundTask();
    }

    protected UserPasswordValue createPasswordValue(String username, String password) {
        UserPasswordValue userPasswordValue = dataManager.create(UserPasswordValue.class);
        userPasswordValue.setUsername(username);
        userPasswordValue.setPassword(password);

        return userPasswordValue;
    }

    protected HorizontalLayout passwordLayoutFactory() {
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
                UiComponentUtils.copyToClipboard(valueToCopy)
                        .then(successResult -> {
                            applyCopyButtonSuccessStyles(button);
                            runAsync(() -> applyCopyButtonDefaultStyles(button));
                        })
        );
    }

    protected void applyCopyButtonSuccessStyles(JmixButton button) {
        button.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        button.setIcon(VaadinIcon.CHECK.create());
    }

    protected void applyCopyButtonDefaultStyles(JmixButton button) {
        button.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
        button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        button.setIcon(VaadinIcon.COPY.create());
    }

    protected void runAsync(Runnable runnable) {
        uiAsyncTasks.runnableConfigurer(() -> {
                    try {
                        // timer
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        log.debug("{} exception in background task", e.getClass().getName(), e);
                    }
                })
                .withResultHandler(runnable)
                .withTimeout(3, TimeUnit.SECONDS)
                .runAsync();
    }

    protected void cancelGenerationProcess() {
        if (generationTaskHandler != null && generationTaskHandler.isAlive()) {
            generationTaskHandler.cancel();
        }
    }

    protected boolean isSingleSelected() {
        return users != null && users.size() == 1;
    }

    public void setUsers(Set<? extends UserDetails> users) {
        this.users = users;

        resetProgressBar.setMax(users.size());
    }

    protected class ResetPasswordBackgroundTask extends BackgroundTask<Integer, List<UserPasswordValue>> {

        protected final Map<String, String> usernamePasswordMap = new LinkedHashMap<>();
        protected final SaveContext saveContext = new SaveContext();

        public ResetPasswordBackgroundTask() {
            super(30, ResetPasswordView.this);
        }

        @Override
        public List<UserPasswordValue> run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            ArrayList<UserPasswordValue> result = new ArrayList<>(users.size());
            int i = 0;

            for (UserDetails userDetails : users) {
                Pair<UserDetails, String> userPassword = userManager.resetPasswordWithoutSave(userDetails);
                String username = userPassword.getFirst().getUsername();
                String password = userPassword.getSecond();

                usernamePasswordMap.put(username, password);
                saveContext.saving(userPassword.getFirst());
                result.add(createPasswordValue(username, password));

                taskLifeCycle.publish(++i);
            }

            return result;
        }

        @Override
        public void progress(List<Integer> changes) {
            Integer lastElement = changes.get(changes.size() - 1);
            configureComponentOnProgress(lastElement);
        }

        @Override
        public void done(List<UserPasswordValue> result) {
            if (cancelDialog != null) {
                cancelDialog.close();
            }

            dataManager.save(saveContext);

            //noinspection unchecked
            userManager.resetRememberMe((Collection<UserDetails>) users);
            applicationEventPublisher.publishEvent(new UserPasswordResetEvent(usernamePasswordMap));

            userPasswordValueDc.setItems(result);
            configureComponentsAfterGeneration();
        }
    }
}
