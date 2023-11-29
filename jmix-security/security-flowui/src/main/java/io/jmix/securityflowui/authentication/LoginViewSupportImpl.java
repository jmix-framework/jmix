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

package io.jmix.securityflowui.authentication;

import com.vaadin.flow.server.VaadinServletRequest;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.login.AbstractLoginViewSupport;
import io.jmix.flowui.login.AuthDetails;
import io.jmix.security.model.SecurityScope;
import io.jmix.securityflowui.accesscontext.UiLoginToUiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link io.jmix.flowui.login.LoginViewSupport} implementation for the security-flowui module
 */
@Component("flowui_LoginViewSupport")
public class LoginViewSupportImpl extends AbstractLoginViewSupport {

    private static final Logger log = LoggerFactory.getLogger(LoginViewSupportImpl.class);

    protected void checkLoginToUi(AuthDetails authDetails, Authentication authentication) {
        Authentication currentAuthentication = SecurityContextHelper.getAuthentication();

        UiLoginToUiContext loginToUiContext = new UiLoginToUiContext();
        try {
            SecurityContextHelper.setAuthentication(authentication);
            accessManager.applyRegisteredConstraints(loginToUiContext);
        } finally {
            SecurityContextHelper.setAuthentication(currentAuthentication);
        }

        if (!loginToUiContext.isPermitted()) {
            log.warn("Attempt of login to UI for user '{}' without '{}' permission", authDetails.getUsername(),
                    loginToUiContext.getName());
            throw new AccessDeniedException("specific", loginToUiContext.getName());
        }
    }

    protected Authentication createAuthenticationToken(String username, String password,
                                                       @Nullable Locale locale,
                                                       @Nullable TimeZone timeZone) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        VaadinServletRequest request = VaadinServletRequest.getCurrent();

        ClientDetails clientDetails = ClientDetails.builder()
                .locale(locale != null ? locale : getDefaultLocale())
                .scope(SecurityScope.UI)
                .sessionId(request.getSession().getId())
                .timeZone(timeZone == null ? getDeviceTimeZone() : timeZone)
                .build();

        authenticationToken.setDetails(clientDetails);

        return authenticationToken;
    }

}
