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

import com.vaadin.server.*;
import io.jmix.core.AccessManager;
import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.security.model.SecurityScope;
import io.jmix.securityui.accesscontext.UiLoginToUiContext;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiProperties;
import io.jmix.ui.deviceinfo.DeviceInfo;
import io.jmix.ui.deviceinfo.DeviceInfoProvider;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

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

    private static final Logger log = LoggerFactory.getLogger(LoginScreenAuthenticationSupport.class);

    protected AuthenticationManager authenticationManager;

    protected UiProperties uiProperties;
    protected CoreProperties coreProperties;
    protected ScreenBuilders screenBuilders;
    protected AccessManager accessManager;
    protected Messages messages;
    protected DeviceInfoProvider deviceInfoProvider;
    protected RememberMeServices rememberMeServices;
    protected ApplicationEventPublisher applicationEventPublisher;

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

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setDeviceInfoProvider(DeviceInfoProvider deviceInfoProvider) {
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Autowired
    public void setRememberMeServices(RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }

    @Autowired(required = false)
    public void setAuthenticationStrategy(SessionAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
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
                        authDetails.getLocale(),
                        authDetails.getTimeZone())
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

        checkLoginToUi(authDetails, authentication);

        SecurityContextHelper.setAuthentication(authentication);
        rememberMeServices.loginSuccess(request, response, authentication);
        applicationEventPublisher.publishEvent(
                new InteractiveAuthenticationSuccessEvent(authentication, this.getClass()));

        showMainScreen(frameOwner);
    }

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
            throw new BadCredentialsException(messages.getMessage(LoginScreenAuthenticationSupport.class,
                    "badCredentials"));
        }
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

    protected Authentication createAuthenticationToken(String username, String password,
                                                       @Nullable Locale locale,
                                                       @Nullable TimeZone timeZone) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        ClientDetails clientDetails = ClientDetails.builder()
                .locale(locale != null ? locale : getDefaultLocale())
                .scope(SecurityScope.UI)
                .timeZone(timeZone == null ? getDeviceTimeZone() : timeZone)
                .build();

        authenticationToken.setDetails(clientDetails);

        return authenticationToken;
    }

    protected Locale getDefaultLocale() {
        Collection<Locale> localeMap = coreProperties.getAvailableLocales().values();
        return localeMap.iterator().next();
    }

    @Nullable
    protected TimeZone getDeviceTimeZone() {
        DeviceInfo deviceInfo = deviceInfoProvider.getDeviceInfo();
        return deviceInfo != null ? deviceInfo.getTimeZone() : null;
    }
}
