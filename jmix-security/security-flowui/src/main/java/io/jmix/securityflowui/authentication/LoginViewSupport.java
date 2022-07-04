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

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import io.jmix.core.AccessManager;
import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.FlowuiProperties;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.sys.AppCookies;
import io.jmix.flowui.sys.ExtendedClientDetailsProvider;
import io.jmix.security.model.SecurityScope;
import io.jmix.securityflowui.accesscontext.FlowuiLoginToUiContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.DEFAULT_PARAMETER;

/**
 * Class that provides authentication via {@link AuthenticationManager}. It is intended to use from login view.
 * <p>
 * Example usage:
 * <pre>
 * &#64;Autowired
 * private LoginViewSupport authenticator;
 *
 * private void doLogin(String username, String password) {
 *     loginViewSupport.authenticate(
 *         AuthDetails.of(event.getUsername(), event.getPassword())
 *     );
 * }
 * </pre>
 *
 * @see AuthDetails
 */
@SpringComponent("flowui_LoginViewSupport")
public class LoginViewSupport {

    private static final Logger log = LoggerFactory.getLogger(LoginViewSupport.class);

    protected AuthenticationManager authenticationManager;

    protected CoreProperties coreProperties;
    protected FlowuiProperties flowuiProperties;
    protected ViewNavigators viewNavigators;
    protected AccessManager accessManager;
    protected Messages messages;
    protected ExtendedClientDetailsProvider clientDetailsProvider;
    protected RememberMeServices rememberMeServices;
    protected ApplicationEventPublisher applicationEventPublisher;
    protected VaadinDefaultRequestCache requestCache;

    protected AppCookies cookies;

    private SessionAuthenticationStrategy authenticationStrategy;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Autowired
    public void setFlowuiProperties(FlowuiProperties flowuiProperties) {
        this.flowuiProperties = flowuiProperties;
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
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
    public void setRememberMeServices(RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setRequestCache(VaadinDefaultRequestCache requestCache) {
        this.requestCache = requestCache;
    }

    @Autowired
    public void setClientDetailsProvider(ExtendedClientDetailsProvider clientDetailsProvider) {
        this.clientDetailsProvider = clientDetailsProvider;
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
     * After successful authentication, there will be an attempt to open the main view.
     *
     * @param authDetails authentication details
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if exception occurs while authentication process
     */
    public Authentication authenticate(AuthDetails authDetails) throws AuthenticationException {
        Authentication authenticationToken = authenticationManager.authenticate(
                createAuthenticationToken(
                        authDetails.getUsername(),
                        authDetails.getPassword(),
                        authDetails.getLocale(),
                        authDetails.getTimeZone())
        );

        onSuccessfulAuthentication(authenticationToken, authDetails);

        return authenticationToken;
    }

    protected void onSuccessfulAuthentication(Authentication authentication,
                                              AuthDetails authDetails) {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        VaadinServletResponse response = VaadinServletResponse.getCurrent();
        request.setAttribute(DEFAULT_PARAMETER, authDetails.isRememberMe());

        if (authenticationStrategy != null) {
            authenticationStrategy.onAuthentication(authentication, request, response);
        }

        checkLoginToUi(authDetails, authentication);

        SecurityContextHelper.setAuthentication(authentication);
        rememberMeServices.loginSuccess(request, response, authentication);

        saveCookies(authDetails);

        applicationEventPublisher.publishEvent(
                new InteractiveAuthenticationSuccessEvent(authentication, this.getClass()));

        showInitialView(request, response);
    }

    protected void saveCookies(AuthDetails authDetails) {
        Locale locale = authDetails.getLocale();
        if (locale != null) {
            getCookies().addCookie(AppCookies.COOKIE_LOCALE, locale.toLanguageTag());
        } else {
            getCookies().removeCookie(AppCookies.COOKIE_LOCALE);
        }
    }

    protected void checkLoginToUi(AuthDetails authDetails, Authentication authentication) {
        Authentication currentAuthentication = SecurityContextHelper.getAuthentication();

        FlowuiLoginToUiContext loginToUiContext = new FlowuiLoginToUiContext();
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

    protected void showInitialView(VaadinServletRequest request, VaadinServletResponse response) {
        Location location = getRedirectLocation(request, response);
        if (location != null) {
            UI.getCurrent().navigate(location.getPath(), location.getQueryParameters());
        } else {
            String mainViewId = flowuiProperties.getMainViewId();
            viewNavigators.view(mainViewId)
                    .navigate();
        }
    }

    @Nullable
    protected Location getRedirectLocation(VaadinServletRequest request, VaadinServletResponse response) {
        HttpServletRequest httpServletRequest = request.getHttpServletRequest();
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            return null;
        }

        String redirectTarget = (String) session.getAttribute(ViewAccessChecker.SESSION_STORED_REDIRECT);
        if (redirectTarget != null) {
            return new Location(redirectTarget);
        }

        SavedRequest savedRequest = requestCache.getRequest(httpServletRequest, response);
        if (savedRequest != null) {
            return new Location(savedRequest.getRedirectUrl());
        }

        return null;
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

    protected Locale getDefaultLocale() {
        List<Locale> locales = coreProperties.getAvailableLocales();
        return locales.get(0);
    }

    @Nullable
    protected TimeZone getDeviceTimeZone() {
        ExtendedClientDetails clientDetails = clientDetailsProvider.getExtendedClientDetails();
        return clientDetails != null ? detectTimeZone(clientDetails) : null;
    }

    protected TimeZone detectTimeZone(ExtendedClientDetails details) {
        String timeZoneId = details.getTimeZoneId();
        if (!Strings.isNullOrEmpty(timeZoneId)) {
            return TimeZone.getTimeZone(timeZoneId);
        } else {
            int offset = details.getTimezoneOffset() / 1000 / 60;
            char sign = offset >= 0 ? '+' : '-';
            int absOffset = Math.abs(offset);

            String hours = StringUtils.leftPad(String.valueOf(absOffset / 60), 2, '0');
            String minutes = StringUtils.leftPad(String.valueOf(absOffset % 60), 2, '0');

            return TimeZone.getTimeZone("GMT" + sign + hours + minutes);
        }
    }

    protected AppCookies getCookies() {
        if (cookies == null) {
            cookies = new AppCookies();
        }

        return cookies;
    }
}
