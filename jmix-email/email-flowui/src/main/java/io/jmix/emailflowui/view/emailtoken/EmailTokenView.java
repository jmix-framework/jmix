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

package io.jmix.emailflowui.view.emailtoken;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.email.EmailConnectionTester;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2AuthorizationCodeFlow;
import io.jmix.email.authentication.OAuth2DeviceCodeFlow;
import io.jmix.email.authentication.OAuth2DeviceCodeSession;
import io.jmix.email.entity.RefreshToken;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.util.Base64;

@Route(value = "email/token", layout = DefaultMainViewParent.class)
@ViewController(id = "email_tokenView")
@ViewDescriptor(path = "email-token-view.xml")
public class EmailTokenView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(EmailTokenView.class);

    protected static final int DEVICE_CODE_POLL_INTERVAL_MS = 2000;
    // Covers the connect (20 s) and read (60 s) timeouts of the underlying transport.
    protected static final int TEST_CONNECTION_TIMEOUT_SEC = 90;

    // The stored token value must never reach the page as a whole: browsers offer to save the
    // content of password inputs, and any real value in the DOM is exposed to the client.
    // Only a short suffix is rendered so that tokens can be distinguished for diagnostics.
    protected static final String STORED_TOKEN_PLACEHOLDER = "••••••••••••••••";
    protected static final int TOKEN_SUFFIX_LENGTH = 4;

    @ViewComponent
    protected TypedTextField<String> refreshTokenValueField;
    @ViewComponent
    protected JmixButton deviceCodeConnectButton;
    @ViewComponent
    protected JmixButton authCodeConnectButton;

    @ViewComponent
    protected InstanceLoader<RefreshToken> refreshTokenDl;

    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected EmailRefreshTokenManager emailRefreshTokenManager;
    @Autowired
    protected EmailConnectionTester emailConnectionTester;
    @Autowired
    protected Messages messages;
    @Autowired
    protected EmailerProperties emailerProperties;
    @Autowired
    protected ObjectProvider<OAuth2DeviceCodeFlow> deviceCodeFlows;
    @Autowired
    protected ObjectProvider<OAuth2AuthorizationCodeFlow> authorizationCodeFlows;

    @Subscribe
    public void onInit(final InitEvent event) {
        deviceCodeConnectButton.setEnabled(deviceCodeFlows.getIfAvailable() != null);
        deviceCodeConnectButton.setTooltipText(messages.getMessage(getClass(), "deviceCodeConnectButton.tooltip"));
        authCodeConnectButton.setEnabled(authorizationCodeFlows.getIfAvailable() != null);
        authCodeConnectButton.setTooltipText(messages.getMessage(getClass(), "authCodeConnectButton.tooltip"));
    }

    @Subscribe("deviceCodeConnectButton")
    public void onDeviceCodeConnectButtonClick(final ClickEvent<JmixButton> event) {
        OAuth2DeviceCodeSession session;
        try {
            session = deviceCodeFlows.getObject().start();
        } catch (Exception e) {
            log.error("Unable to start the device code flow", e);
            showConnectionFailedNotification(e.getMessage());
            return;
        }
        if (session.getStatus() == OAuth2DeviceCodeSession.Status.FAILED) {
            showConnectionFailedNotification(session.getErrorMessage());
            return;
        }
        openDeviceCodeDialog(session);
    }

    @Subscribe("authCodeConnectButton")
    public void onAuthCodeConnectButtonClick(final ClickEvent<JmixButton> event) {
        String state = generateState();
        String redirectUri = resolveRedirectUri();

        String authorizationUrl;
        try {
            authorizationUrl = authorizationCodeFlows.getObject().buildAuthorizationUrl(redirectUri, state);
        } catch (Exception e) {
            log.error("Unable to build the authorization URL", e);
            showConnectionFailedNotification(e.getMessage());
            return;
        }

        VaadinSession.getCurrent().setAttribute(EmailOAuth2CallbackView.OAUTH2_STATE_ATTRIBUTE, state);
        VaadinSession.getCurrent().setAttribute(EmailOAuth2CallbackView.OAUTH2_REDIRECT_URI_ATTRIBUTE, redirectUri);

        UI.getCurrent().getPage().setLocation(authorizationUrl);
    }

    @Subscribe("updateRefreshTokenAction")
    public void onUpdateRefreshTokenAction(final ActionPerformedEvent event) {
        dialogs.createInputDialog(this)
                .withParameters(InputParameter.stringParameter("tokenValue")
                        .withLabel(messages.getMessage(getClass(), "onUpdateRefreshTokenAction.dialog.tokenValue.label")))
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        String tokenValue = closeEvent.getValue("tokenValue");
                        if (StringUtils.isNotBlank(tokenValue)) {
                            emailRefreshTokenManager.storeRefreshTokenValue(tokenValue);
                            refreshTokenDl.load();
                            notifications.create(messages.getMessage(getClass(), "tokenUpdatedNotification.text"))
                                    .show();
                        }
                    }
                })
                .withHeader(messages.getMessage(getClass(), "updateRefreshTokenDialog.header"))
                .open();
    }

    @Subscribe("testConnectionButton")
    public void onTestConnectionButtonClick(final ClickEvent<JmixButton> event) {
        dialogs.createBackgroundTaskDialog(createTestConnectionTask())
                .withHeader(messages.getMessage(getClass(), "testConnectionDialog.header"))
                .withText(messages.getMessage(getClass(), "testConnectionDialog.text"))
                .withCancelAllowed(true)
                .open();
    }

    protected BackgroundTask<Integer, Void> createTestConnectionTask() {
        return new BackgroundTask<>(TEST_CONNECTION_TIMEOUT_SEC, this) {
            @Override
            public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
                emailConnectionTester.testConnection();
                return null;
            }

            @Override
            public void done(Void result) {
                notifications.create(messages.getMessage(EmailTokenView.class,
                                "testConnectionSuccessNotification.text"))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }

            @Override
            public boolean handleException(Exception ex) {
                log.warn("Mail server connection test failed", ex);
                notifications.create(messages.formatMessage(EmailTokenView.class,
                                "testConnectionFailedNotification.text", ExceptionUtils.getRootCauseMessage(ex)))
                        .withType(Notifications.Type.ERROR)
                        .show();
                return true;
            }

            @Override
            public boolean handleTimeoutException() {
                notifications.create(messages.getMessage(EmailTokenView.class,
                                "testConnectionTimeoutNotification.text"))
                        .withType(Notifications.Type.ERROR)
                        .show();
                return true;
            }
        };
    }

    @Subscribe(id = "refreshTokenDc", target = Target.DATA_CONTAINER)
    public void onRefreshTokenDcItemChange(final InstanceContainer.ItemChangeEvent<RefreshToken> event) {
        refreshTokenValueField.setValue(event.getItem() != null
                ? STORED_TOKEN_PLACEHOLDER + tokenSuffix(event.getItem().getTokenValue())
                : "");
    }

    /**
     * Returns the last characters of the token for diagnostic identification. Nothing is revealed
     * for short values so that a manually entered test value is not half-exposed.
     */
    protected String tokenSuffix(String tokenValue) {
        return tokenValue.length() > TOKEN_SUFFIX_LENGTH * 4
                ? tokenValue.substring(tokenValue.length() - TOKEN_SUFFIX_LENGTH)
                : "";
    }

    @Install(to = "refreshTokenDl", target = Target.DATA_LOADER)
    private RefreshToken refreshTokenDlLoadDelegate(final LoadContext<RefreshToken> loadContext) {
        return emailRefreshTokenManager.loadRefreshToken();
    }

    protected void openDeviceCodeDialog(OAuth2DeviceCodeSession session) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messages.getMessage(getClass(), "deviceCodeDialog.header"));
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);

        Span instruction = new Span(messages.getMessage(getClass(), "deviceCodeDialog.instruction"));
        Anchor verificationLink = new Anchor(StringUtils.defaultString(session.getVerificationUri()),
                StringUtils.defaultString(session.getVerificationUri()));
        verificationLink.setTarget(AnchorTarget.BLANK);
        H3 userCode = new H3(StringUtils.defaultString(session.getUserCode()));
        Span status = new Span(messages.getMessage(getClass(), "deviceCodeDialog.pending"));

        VerticalLayout layout = new VerticalLayout(instruction, verificationLink, userCode, status);
        layout.setPadding(false);
        dialog.add(layout);

        Button closeButton = new Button(messages.getMessage(getClass(), "deviceCodeDialog.closeButton.text"),
                clickEvent -> dialog.close());
        dialog.getFooter().add(closeButton);

        UI ui = UI.getCurrent();
        int previousPollInterval = ui.getPollInterval();
        ui.setPollInterval(DEVICE_CODE_POLL_INTERVAL_MS);
        Registration pollRegistration = ui.addPollListener(pollEvent -> onDeviceCodePoll(session, status, dialog));
        dialog.addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened()) {
                pollRegistration.remove();
                ui.setPollInterval(previousPollInterval);
            }
        });

        dialog.open();
    }

    protected void onDeviceCodePoll(OAuth2DeviceCodeSession session, Span status, Dialog dialog) {
        switch (session.getStatus()) {
            case COMPLETED -> {
                dialog.close();
                notifications.create(messages.getMessage(getClass(), "deviceCodeCompletedNotification.text"))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
                refreshTokenDl.load();
            }
            case FAILED -> status.setText(messages.formatMessage(getClass(), "deviceCodeDialog.failed",
                    StringUtils.defaultString(session.getErrorMessage())));
            default -> {
            }
        }
    }

    protected void showConnectionFailedNotification(String errorMessage) {
        notifications.create(messages.formatMessage(getClass(), "connectionFailedNotification.text",
                        StringUtils.defaultString(errorMessage)))
                .withType(Notifications.Type.ERROR)
                .show();
    }

    protected String generateState() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the redirect URI configured by 'jmix.email.oauth2.redirect-uri' or derives it
     * from the current request and the callback view route.
     */
    protected String resolveRedirectUri() {
        String configured = emailerProperties.getOAuth2().getRedirectUri();
        if (StringUtils.isNotBlank(configured)) {
            return configured;
        }

        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        String route = RouteConfiguration.forSessionScope().getUrl(EmailOAuth2CallbackView.class);

        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        int defaultPort = "https".equals(request.getScheme()) ? 443 : 80;
        if (port > 0 && port != defaultPort) {
            sb.append(':').append(port);
        }
        String contextPath = request.getContextPath();
        if (StringUtils.isNotEmpty(contextPath)) {
            sb.append(contextPath);
        }
        if (!route.startsWith("/")) {
            sb.append('/');
        }
        sb.append(route);
        return sb.toString();
    }
}
