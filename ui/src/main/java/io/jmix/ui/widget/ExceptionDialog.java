/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.widget;

import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.*;
import io.jmix.core.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.*;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.context.UiShowExceptionDetailsContext;
import io.jmix.ui.sys.ControllerUtils;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.jmix.ui.action.Action.Status;
import static io.jmix.ui.action.DialogAction.Type;

/**
 * This dialog can be used by exception handlers to show an information about error.
 */
public class ExceptionDialog extends JmixWindow {
    private static final Logger log = LoggerFactory.getLogger(ExceptionDialog.class);

    protected VerticalLayout mainLayout;

    protected TextArea stackTraceTextArea;

    protected Button copyButton;

    protected Button showStackTraceButton;

    protected boolean isStackTraceVisible = false;

    protected Map<String, Object> additionalExceptionReportBinding = null;

    protected Messages messages;

//    todo exception report service
//    protected ExceptionReportService reportService = AppBeans.get(ExceptionReportService.NAME);

    protected WindowConfig windowConfig;

    protected UiProperties properties;

    protected CurrentAuthentication currentAuthentication;

    protected TimeSource timeSource;

    protected AccessManager accessManager;

    public ExceptionDialog(Throwable throwable, ApplicationContext applicationContext) {
        this(throwable, null, null, applicationContext);
    }

    public ExceptionDialog(Throwable throwable, @Nullable String caption, @Nullable String message, ApplicationContext applicationContext) {
        messages = applicationContext.getBean(Messages.class);
        windowConfig = applicationContext.getBean(WindowConfig.class);
        properties = applicationContext.getBean(UiProperties.class);
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        timeSource = applicationContext.getBean(TimeSource.class);
        accessManager = applicationContext.getBean(AccessManager.class);

        AppUI ui = AppUI.getCurrent();

        String closeShortcut = properties.getCloseShortcut();
        KeyCombination closeCombination = KeyCombination.create(closeShortcut);

        com.vaadin.event.ShortcutAction closeShortcutAction = new com.vaadin.event.ShortcutAction(
                "closeShortcutAction",
                closeCombination.getKey().getCode(),
                KeyCombination.Modifier.codes(closeCombination.getModifiers())
        );

        addActionHandler(new com.vaadin.event.Action.Handler() {
            @Override
            public com.vaadin.event.Action[] getActions(Object target, Object sender) {
                return new com.vaadin.event.Action[]{closeShortcutAction};
            }

            @Override
            public void handleAction(com.vaadin.event.Action action, Object sender, Object target) {
                if (Objects.equals(action, closeShortcutAction)) {
                    close();
                }
            }
        });

        setCaption(caption != null ? caption : messages.getMessage("exceptionDialog.caption"));

        ThemeConstants theme = ui.getApp().getThemeConstants();
        setWidth(theme.get("cuba.web.ExceptionDialog.width"));
        center();

        final String text = message != null ? message : getText(throwable);
        Throwable exception = removeRemoteException(throwable);
        final String stackTrace = getStackTrace(exception);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        TextArea textArea = new TextArea();
        textArea.setHeight(theme.get("cuba.web.ExceptionDialog.textArea.height"));
        textArea.setWidth(100, Sizeable.Unit.PERCENTAGE);

        boolean showExceptionDetails = false;
        if (currentAuthentication.isSet()) {
            UiShowExceptionDetailsContext showExceptionDetailsContext = new UiShowExceptionDetailsContext();
                    accessManager.applyRegisteredConstraints(new UiShowExceptionDetailsContext());
            showExceptionDetails = showExceptionDetailsContext.isPermitted();
        }

        if (showExceptionDetails) {
            textArea.setValue(text);
        } else {
            textArea.setValue(messages.getMessage("exceptionDialog.contactAdmin"));
        }
        textArea.setReadOnly(true);

        mainLayout.addComponent(textArea);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(false);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");
        mainLayout.addComponent(buttonsLayout);

        Button closeButton = new JmixButton(messages.getMessage("exceptionDialog.closeBtn"));
        closeButton.addClickListener(event ->
                this.close()
        );
        buttonsLayout.addComponent(closeButton);

        showStackTraceButton = new JmixButton(messages.getMessage("exceptionDialog.showStackTrace"));
        showStackTraceButton.addClickListener(event ->
                setStackTraceVisible(!isStackTraceVisible)
        );
        buttonsLayout.addComponent(showStackTraceButton);
        showStackTraceButton.setVisible(showExceptionDetails);

        Label spacer = new Label();
        buttonsLayout.addComponent(spacer);
        buttonsLayout.setExpandRatio(spacer, 1);

        String logContentClassName = "c-exception-dialog-log-content";
        String copyLogContentClassName = logContentClassName + "-" + UUID.randomUUID();

//        todo JmixCopyButtonExtension
        /*if (JmixCopyButtonExtension.browserSupportCopy()) {
            copyButton = new JmixButton(messages.getMainMessage("exceptionDialog.copyStackTrace"));
            copyButton.setVisible(false);
            JmixCopyButtonExtension copyExtension = JmixCopyButtonExtension.copyWith(copyButton, copyLogContentClassName);
            copyExtension.addCopyListener(event ->
                    Notification.show(messages.getMessage(
                            event.isSuccess() ? "exceptionDialog.copingSuccessful" : "exceptionDialog.copingFailed"),
                            Notification.Type.TRAY_NOTIFICATION));
            buttonsLayout.addComponent(copyButton);
        }*/

        if (currentAuthentication.isSet()) {
            if (!StringUtils.isBlank(properties.getSupportEmail())) {
                Button reportButton = new JmixButton(messages.getMessage("exceptionDialog.reportBtn"));
                reportButton.addClickListener(event -> {
                    sendSupportEmail(text, stackTrace);
                    reportButton.setEnabled(false);
                });
                buttonsLayout.addComponent(reportButton);

                if (ui.isTestMode()) {
                    reportButton.setJTestId("errorReportButton");
                }
            }
        }

        Button logoutButton = new JmixButton(messages.getMessage("exceptionDialog.logout"));
        logoutButton.addClickListener(event ->
                logoutPrompt()
        );
        buttonsLayout.addComponent(logoutButton);

        stackTraceTextArea = new TextArea();
        stackTraceTextArea.setSizeFull();
        stackTraceTextArea.setWordWrap(false);
        stackTraceTextArea.setValue(stackTrace);
        stackTraceTextArea.setStyleName(logContentClassName);
        stackTraceTextArea.addStyleName(copyLogContentClassName);
        stackTraceTextArea.setReadOnly(true);

        setContent(mainLayout);
        setResizable(false);

        if (ui.isTestMode()) {
            setJTestId("exceptionDialog");

            closeButton.setJTestId("closeButton");
            if (copyButton != null) {
                copyButton.setJTestId("copyStackTraceButton");
            }
            showStackTraceButton.setJTestId("showStackTraceButton");
            stackTraceTextArea.setJTestId("stackTraceTextArea");
            logoutButton.setJTestId("logoutButton");
        }

        if (ui.isPerformanceTestMode()) {
            setId(ui.getTestIdManager().getTestId("exceptionDialog"));
        }
    }

    protected String getStackTrace(Throwable throwable) {
        return ExceptionUtils.getStackTrace(throwable);
    }

    protected Throwable removeRemoteException(Throwable throwable) {
        // todo do we need this ?
        /*if (throwable instanceof RemoteException) {
            RemoteException re = (RemoteException) throwable;
            for (int i = re.getCauses().size() - 1; i >= 0; i--) {
                if (re.getCauses().get(i).getThrowable() != null) {
                    return re.getCauses().get(i).getThrowable();
                }
            }
        }*/
        return throwable;
    }

    protected String getText(Throwable rootCause) {
        StringBuilder msg = new StringBuilder();
        // todo RemoteException
        /*if (rootCause instanceof RemoteException) {
            RemoteException re = (RemoteException) rootCause;
            if (!re.getCauses().isEmpty()) {
                RemoteException.Cause cause = re.getCauses().get(re.getCauses().size() - 1);
                if (cause.getThrowable() != null) {
                    rootCause = cause.getThrowable();
                } else {
                    // root cause is not supported by client
                    String className = cause.getClassName();
                    if (className != null && className.indexOf('.') > 0) {
                        className = className.substring(className.lastIndexOf('.') + 1);
                    }
                    msg.append(className).append(": ").append(cause.getMessage());
                }
            }
        }*/

        if (msg.length() == 0) {
            msg.append(rootCause.getClass().getSimpleName());
            if (!StringUtils.isBlank(rootCause.getMessage())) {
                msg.append(": ").append(rootCause.getMessage());
            }

            if (rootCause instanceof DevelopmentException) {
                Map<String, Object> params = new LinkedHashMap<>();
                if (rootCause instanceof GuiDevelopmentException) {
                    GuiDevelopmentException guiDevException = (GuiDevelopmentException) rootCause;
                    ComponentLoader.Context context = guiDevException.getContext();
                    // todo composite component
                    /*if (context instanceof CompositeComponentContext) {
                        Class<?> componentClass = ((CompositeComponentContext) context).getComponentClass();
                        params.put("Component Class", componentClass);
                        CompositeDescriptor template = componentClass.getAnnotation(CompositeDescriptor.class);
                        if (template != null) {
                            params.put("XML descriptor", template.value());
                        }
                    } else */
                    if (guiDevException.getFrameId() != null) {
                        String frameId = guiDevException.getFrameId();
                        params.put("Frame ID", frameId);
                        try {
                            params.put("XML descriptor",
                                    windowConfig.getWindowInfo(frameId).getTemplate());
                        } catch (Exception e) {
                            params.put("XML descriptor", "not found for " + frameId);
                        }
                    }
                }
                params.putAll(((DevelopmentException) rootCause).getParams());

                if (!params.isEmpty()) {
                    msg.append("\n\n");
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        msg.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                }
            }
        }
        return msg.toString();
    }

    public void setStackTraceVisible(boolean visible) {
        isStackTraceVisible = visible;

        ThemeConstants theme = App.getInstance().getThemeConstants();
        if (visible) {
            if (copyButton != null) {
                copyButton.setVisible(true);
            }

            showStackTraceButton.setCaption(messages.getMessage("exceptionDialog.hideStackTrace"));

            mainLayout.addComponent(stackTraceTextArea);
            mainLayout.setExpandRatio(stackTraceTextArea, 1.0f);
            mainLayout.setHeight(100, Sizeable.Unit.PERCENTAGE);

            setWidth(theme.get("cuba.web.ExceptionDialog.expanded.width"));
            setHeight(theme.get("cuba.web.ExceptionDialog.expanded.height"));

            setResizable(true);
            center();
            stackTraceTextArea.focus();
            stackTraceTextArea.setCursorPosition(0);
        } else {
            if (copyButton != null) {
                copyButton.setVisible(false);
            }

            showStackTraceButton.setCaption(messages.getMessage("exceptionDialog.showStackTrace"));

            mainLayout.setHeight(-1, Sizeable.Unit.PIXELS);
            mainLayout.removeComponent(stackTraceTextArea);

            setWidth(theme.get("cuba.web.ExceptionDialog.width"));
            setHeight(-1, Sizeable.Unit.PERCENTAGE);

            setResizable(false);
            center();

            setWindowMode(WindowMode.NORMAL);
        }
    }

    public void sendSupportEmail(String message, String stackTrace) {
        // todo exception report
        /*try {
            User user = userSessionSource.getUserSession().getUser();
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeSource.currentTimestamp());

            Map<String, Object> binding = new HashMap<>();
            binding.put("timestamp", date);
            binding.put("errorMessage", message);
            binding.put("stacktrace", stackTrace);
            binding.put("systemId", clientConfig.getSystemID());
            binding.put("userLogin", user.getLogin());

            if (MapUtils.isNotEmpty(additionalExceptionReportBinding)) {
                binding.putAll(additionalExceptionReportBinding);
            }

            reportService.sendExceptionReport(clientConfig.getSupportEmail(), MapUtils.unmodifiableMap(binding));

            Notification.show(messages.getMainMessage("exceptionDialog.emailSent"));
        } catch (Throwable e) {
            log.error("Error sending exception report", e);
            Notification.show(messages.getMainMessage("exceptionDialog.emailSendingErr"));
        }*/
    }

    protected void logoutPrompt() {
        Dialogs dialogs = ((AppUI) getUI()).getDialogs();

        dialogs.createOptionDialog()
                .withCaption(messages.getMessage("exceptionDialog.logoutCaption"))
                .withMessage(messages.getMessage("exceptionDialog.logoutMessage"))
                .withActions(
                        new BaseAction("close")
                                .withCaption(messages.getMessage("closeApplication"))
                                .withIcon("icons/ok.png")
                                .withHandler(event -> forceLogout()),
                        new DialogAction(Type.CANCEL, Status.PRIMARY)
                )
                .show();
    }

    protected void forceLogout() {
        AppUI ui = (AppUI) getUI();

        //todo MG logout
        // App app = ui.getApp();
        try {
//            Connection connection = app.getConnection();
//            if (connection.isConnected()) {
//                connection.logout();
//            }
        } catch (Exception e) {
            log.warn("Exception on forced logout", e);
        } finally {
            // always restart UI
            String url = ControllerUtils.getLocationWithoutParams() + "?restartApp";

            ui.getPage().open(url, "_self");
        }
    }

    public void setAdditionalExceptionReportBinding(Map<String, Object> binding) {
        additionalExceptionReportBinding = binding;
    }

    public Map<String, Object> getAdditionalExceptionReportBinding() {
        return additionalExceptionReportBinding;
    }
}