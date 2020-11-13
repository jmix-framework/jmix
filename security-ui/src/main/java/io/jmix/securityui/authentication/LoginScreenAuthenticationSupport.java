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

package io.jmix.securityui.authentication;

import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import io.jmix.core.CoreProperties;
import io.jmix.core.security.ClientDetails;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiProperties;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.DEFAULT_PARAMETER;

/**
 * Class that provides authentication via {@link AuthenticationManager}. It is intended to use from login screen.
 * <p>
 * Example usage:
 * <pre>
 * &#64;Autowired
 * private LoginScreenAuthenticator authenticator;
 *
 * private void doLogin(String username, String password) {
 *     Authentication authentication = authenticationSupport.authenticate(
 *             AuthDetails.of(username, password)
 *                     .withLocale(localesField.getValue())
 *                     .withRememberMe(rememberMeCheckBox.isChecked()), this);
 * }
 *
 * </pre>
 *
 * @see AuthDetails
 */
@Component("ui_LoginScreenAuthenticationSupport")
public class LoginScreenAuthenticationSupport {

    protected AuthenticationManager authenticationManager;

    protected UiProperties uiProperties;
    protected CoreProperties coreProperties;
    protected ScreenBuilders screenBuilders;

    private SessionAuthenticationStrategy authenticationStrategy;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired(required = false)
    public void setAuthenticationStrategy(SessionAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    /**
     * Performs authentication via {@link AuthenticationManager} and uses
     * {@link UsernamePasswordAuthenticationToken} with credentials from {@link AuthDetails}.
     * <p>
     * If locale is not provided it will use the first locale from
     * {@link CoreProperties#getAvailableLocales()} list.
     * <p>
     * After successful authentication, there will be an attempt to open the main screen using
     * {@link UiProperties#getMainScreenId()} from {@link FrameOwner}
     * if it is not {@code null}.
     *
     * @param authDetails authentication details
     * @param frameOwner  invoking screen to open main screen
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if exception occurs while authentication process
     */
    public Authentication authenticate(AuthDetails authDetails,
                                       @Nullable FrameOwner frameOwner) throws AuthenticationException {
        Authentication authenticationToken = authenticationManager.authenticate(
                createAuthenticationToken(
                        authDetails.getUsername(),
                        authDetails.getPassword(),
                        authDetails.getLocale())
        );

        onSuccessfulAuthentication(authenticationToken, authDetails, frameOwner);

        return authenticationToken;
    }

    protected void onSuccessfulAuthentication(Authentication authentication,
                                              AuthDetails authDetails,
                                              @Nullable FrameOwner frameOwner) {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        VaadinServletResponse response = VaadinServletResponse.getCurrent();
        request.setAttribute(DEFAULT_PARAMETER, authDetails.isRememberMe());

        if (authenticationStrategy != null) {
            authenticationStrategy.onAuthentication(authentication, request, response);
        }

        showMainScreen(frameOwner);
    }

    protected void showMainScreen(@Nullable FrameOwner frameOwner) {
        if (frameOwner != null) {
            String mainScreenId = uiProperties.getMainScreenId();
            screenBuilders.screen(frameOwner)
                    .withScreenId(mainScreenId)
                    .withOpenMode(OpenMode.ROOT)
                    .build()
                    .show();
        }
    }

    protected Authentication createAuthenticationToken(String username, String password, @Nullable Locale locale) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        ClientDetails clientDetails = ClientDetails.builder()
                .locale(locale != null ? locale : getDefaultLocale())
                .build();

        authenticationToken.setDetails(clientDetails);

        return authenticationToken;
    }

    protected Locale getDefaultLocale() {
        Collection<Locale> localeMap = coreProperties.getAvailableLocales().values();
        return localeMap.iterator().next();
    }
}
