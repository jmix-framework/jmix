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

package io.jmix.emailflowui.view.emailtoken;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.Messages;
import io.jmix.email.authentication.OAuth2AuthorizationCodeFlow;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Receives the OAuth2 authorization code flow callback initiated from {@link EmailTokenView}.
 * The view route must be registered as a redirect URI of the OAuth client.
 */
@Route(value = "email/oauth2/callback", layout = DefaultMainViewParent.class)
@ViewController(id = "email_oauth2CallbackView")
@ViewDescriptor(path = "email-oauth2-callback-view.xml")
public class EmailOAuth2CallbackView extends StandardView {

    public static final String OAUTH2_STATE_ATTRIBUTE = "jmix_email_oauth2_state";
    public static final String OAUTH2_REDIRECT_URI_ATTRIBUTE = "jmix_email_oauth2_redirect_uri";

    private static final Logger log = LoggerFactory.getLogger(EmailOAuth2CallbackView.class);

    @ViewComponent
    protected Span statusLabel;
    @ViewComponent
    protected JmixButton openTokenViewButton;

    @Autowired
    protected ObjectProvider<OAuth2AuthorizationCodeFlow> authorizationCodeFlows;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Messages messages;

    protected QueryParameters queryParameters = QueryParameters.empty();

    @Subscribe
    public void onQueryParametersChange(final QueryParametersChangeEvent event) {
        queryParameters = event.getQueryParameters();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        processCallback();
        openTokenViewButton.setVisible(true);
    }

    @Subscribe("openTokenViewButton")
    public void onOpenTokenViewButtonClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, EmailTokenView.class).navigate();
    }

    protected void processCallback() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        String expectedState = (String) vaadinSession.getAttribute(OAUTH2_STATE_ATTRIBUTE);
        String redirectUri = (String) vaadinSession.getAttribute(OAUTH2_REDIRECT_URI_ATTRIBUTE);
        vaadinSession.setAttribute(OAUTH2_STATE_ATTRIBUTE, null);
        vaadinSession.setAttribute(OAUTH2_REDIRECT_URI_ATTRIBUTE, null);

        Map<String, List<String>> parameters = queryParameters.getParameters();

        String error = getFirstParameter(parameters, "error");
        if (error != null) {
            String description = getFirstParameter(parameters, "error_description");
            showFailure(description != null ? error + ": " + description : error);
            return;
        }

        String code = getFirstParameter(parameters, "code");
        String state = getFirstParameter(parameters, "state");
        if (code == null || state == null) {
            statusLabel.setText(messages.getMessage(getClass(), "callback.missingParameters"));
            return;
        }
        if (expectedState == null || !expectedState.equals(state) || redirectUri == null) {
            log.warn("OAuth2 callback state validation failed");
            statusLabel.setText(messages.getMessage(getClass(), "callback.invalidState"));
            return;
        }

        OAuth2AuthorizationCodeFlow flow = authorizationCodeFlows.getIfAvailable();
        if (flow == null) {
            statusLabel.setText(messages.getMessage(getClass(), "callback.flowUnavailable"));
            return;
        }

        try {
            flow.completeAuthorization(code, redirectUri);
            statusLabel.setText(messages.getMessage(getClass(), "callback.success"));
        } catch (Exception e) {
            log.error("Failed to complete the mailbox connection", e);
            showFailure(rootMessage(e));
        }
    }

    protected void showFailure(String errorMessage) {
        statusLabel.setText(messages.formatMessage(getClass(), "callback.error",
                StringUtils.defaultString(errorMessage)));
    }

    @Nullable
    protected String getFirstParameter(Map<String, List<String>> parameters, String name) {
        List<String> values = parameters.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    protected String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }
}
