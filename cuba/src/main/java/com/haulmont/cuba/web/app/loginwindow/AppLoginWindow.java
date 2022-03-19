/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.web.app.loginwindow;

import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.haulmont.cuba.web.security.AuthInfo;
import io.jmix.core.CoreProperties;
import io.jmix.core.security.ClientDetails;
import io.jmix.ui.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ThemeResource;
import io.jmix.securityui.authentication.AuthDetails;
import io.jmix.securityui.authentication.LoginScreenSupport;
import io.jmix.ui.security.UiLoginProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;

/**
 * Legacy base class for a controller of application Login window.
 *
 * @see LoginScreen
 */
public class AppLoginWindow extends AbstractWindow implements Window.TopLevelWindow {

    protected static final ThreadLocal<AuthInfo> authInfoThreadLocal = new ThreadLocal<>();

    @Inject
    protected UiLoginProperties loginProperties;
    @Inject
    protected CoreProperties coreProperties;
    @Inject
    protected CubaProperties cubaProperties;
    @Inject
    protected UiProperties uiProperties;

    @Inject
    protected LoginScreenSupport loginScreenSupport;

    @Inject
    protected JmixApp app;

    @Inject
    protected Image logoImage;

    @Inject
    protected TextField<String> loginField;

    @Inject
    protected CheckBox rememberMeCheckBox;

    @Inject
    protected PasswordField passwordField;

    @Inject
    protected LookupField<Locale> localesSelect;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        loginField.focus();

        initPoweredByLink();

        initLogoImage();

        initDefaultCredentials();

        initLocales();

        initRememberMe();

        initRememberMeLocalesBox();
    }

    protected void initPoweredByLink() {
        Component poweredByLink = getComponent("poweredByLink");
        if (poweredByLink != null) {
            poweredByLink.setVisible(cubaProperties.isPoweredByLinkVisible());
        }
    }

    protected void initLocales() {
        localesSelect.setOptionsMap(messageTools.getAvailableLocalesMap());
        localesSelect.setValue(app.getLocale());

        boolean localeSelectVisible = cubaProperties.isLocaleSelectVisible();
        localesSelect.setVisible(localeSelectVisible);

        // if old layout is used
        Component localesSelectLabel = getComponent("localesSelectLabel");
        if (localesSelectLabel != null) {
            localesSelectLabel.setVisible(localeSelectVisible);
        }

        localesSelect.addValueChangeListener(e -> {
            Locale selectedLocale = e.getValue();

            app.setLocale(selectedLocale);

            authInfoThreadLocal.set(new AuthInfo(loginField.getValue(), passwordField.getValue(),
                    rememberMeCheckBox.getValue()));
            try {
                app.createTopLevelWindow();
            } finally {
                authInfoThreadLocal.set(null);
            }
        });
    }

    protected void initLogoImage() {
        String loginLogoImagePath = messages.getMainMessage("loginWindow.logoImage", app.getLocale());
        if (StringUtils.isBlank(loginLogoImagePath) || "loginWindow.logoImage".equals(loginLogoImagePath)) {
            logoImage.setVisible(false);
        } else {
            logoImage.setSource(ThemeResource.class).setPath(loginLogoImagePath);
        }
    }

    protected void initRememberMe() {
        if (!cubaProperties.isRememberMeEnabled()) {
            rememberMeCheckBox.setValue(false);
            rememberMeCheckBox.setVisible(false);
        }
    }

    protected void initRememberMeLocalesBox() {
        Component rememberLocalesBox = getComponent("rememberLocalesBox");
        if (rememberLocalesBox != null) {
            rememberLocalesBox.setVisible(rememberMeCheckBox.isVisible() || localesSelect.isVisible());
        }
    }

    protected void initDefaultCredentials() {
        AuthInfo authInfo = authInfoThreadLocal.get();
        if (authInfo != null) {
            loginField.setValue(authInfo.getLogin());
            passwordField.setValue(authInfo.getPassword());
            rememberMeCheckBox.setValue(authInfo.getRememberMe());

            localesSelect.focus();

            authInfoThreadLocal.set(null);

            return;
        }

        String defaultUser = loginProperties.getDefaultUsername();
        if (!StringUtils.isBlank(defaultUser) && !"<disabled>".equals(defaultUser)) {
            loginField.setValue(defaultUser);
        } else {
            loginField.setValue("");
        }

        String defaultPassw = loginProperties.getDefaultPassword();
        if (!StringUtils.isBlank(defaultPassw) && !"<disabled>".equals(defaultPassw)) {
            passwordField.setValue(defaultPassw);
        } else {
            passwordField.setValue("");
        }
    }

    public void login() {
        doLogin();
    }

    protected void doLogin() {
        String login = loginField.getValue();
        String password = passwordField.getValue() != null ? passwordField.getValue() : "";

        if (StringUtils.isEmpty(login) || StringUtils.isEmpty(password)) {
            showNotification(messages.getMainMessage("loginWindow.emptyLoginOrPassword"), NotificationType.WARNING);
            return;
        }

        try {
            Locale selectedLocale = localesSelect.getValue();
            app.setLocale(selectedLocale);

            Authentication authentication = loginScreenSupport.authenticate(
                    AuthDetails.of(login, password)
                            .withLocale(selectedLocale)
                            .withRememberMe(rememberMeCheckBox.isChecked()), this);

            onSuccessfulAuthentication(authentication);
        } catch (BadCredentialsException | DisabledException e) {
            showNotification(messages.getMainMessage("loginFailed"), e.getMessage(), NotificationType.ERROR);
        }
    }

    protected void onSuccessfulAuthentication(Authentication authentication) {
        if (cubaProperties.isLocaleSelectVisible()) {
            ClientDetails clientDetails = (ClientDetails) authentication.getDetails();
            app.addCookie(App.COOKIE_LOCALE, clientDetails.getLocale().toLanguageTag());
        }
    }
}
