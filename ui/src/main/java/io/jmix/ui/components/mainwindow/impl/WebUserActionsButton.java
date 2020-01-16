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

package io.jmix.ui.components.mainwindow.impl;

import io.jmix.core.ConfigInterfaces;
import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import io.jmix.core.Messages;
import io.jmix.core.security.Security;
import io.jmix.ui.AppUI;
import io.jmix.ui.WebConfig;
import io.jmix.ui.components.impl.WebAbstractComponent;
import io.jmix.ui.components.mainwindow.UserActionsButton;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.IconResolver;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.widgets.CubaMenuBar;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.function.Consumer;

public class WebUserActionsButton extends WebAbstractComponent<CubaMenuBar>
        implements UserActionsButton {

    public static final String USERACTIONS_BUTTON_STYLENAME = "c-useractions-button";

    protected IconResolver iconResolver;
    protected Icons icons;
    protected Messages messages;
    protected Security security;

    protected Consumer<LoginHandlerContext> loginHandler;
    protected Consumer<LogoutHandlerContext> logoutHandler;

    public WebUserActionsButton() {
        component = new CubaMenuBar();
        component.addStyleName(USERACTIONS_BUTTON_STYLENAME);

        component.addAttachListener(event -> {
            UI ui = event.getConnector().getUI();

            if (ui instanceof AppUI) {
                initComponent((AppUI) ui);
            }
        });
    }

    @Inject
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Inject
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Inject
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Inject
    public void setSecurity(Security security) {
        this.security = security;
    }

    @Override
    public void setLoginHandler(Consumer<LoginHandlerContext> loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public void setLogoutHandler(Consumer<LogoutHandlerContext> logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    protected void initComponent(AppUI ui) {
        boolean authenticated = ui.hasAuthenticatedSession();

        initLoginButton(authenticated);
        initUserMenuButton(authenticated);
    }

    protected void initLoginButton(boolean authenticated) {
        MenuBar.MenuItem loginButton = component.addItem("", item -> login());
        loginButton.setDescription(messages.getMessage("loginBtnDescription"));
        loginButton.setIcon(getIconResource(CubaIcon.SIGN_IN));
        loginButton.setVisible(!authenticated);
    }

    protected void initUserMenuButton(boolean authenticated) {
        MenuBar.MenuItem userMenuButton = component.addItem("");
        userMenuButton.setDescription(messages.getMessage("userActionsBtnDescription"));
        userMenuButton.setIcon(getIconResource(CubaIcon.USER));
        userMenuButton.setVisible(authenticated);

        if (security.isScreenPermitted("settings")) {
            userMenuButton.addItem(messages.getMessage("settings"),
                    getIconResource(CubaIcon.GEAR), item -> openSettings());
        }

        userMenuButton.addItem(messages.getMessage("logoutBtnDescription"),
                getIconResource(CubaIcon.SIGN_OUT), item -> logout());
    }

    protected Resource getIconResource(Icons.Icon icon) {
        return iconResolver.getIconResource(icons.get(icon));
    }

    protected void login() {
        if (loginHandler != null) {
            loginHandler.accept(new LoginHandlerContext(this));
        } else {
            defaultLogin();
        }
    }

    protected void logout() {
        if (logoutHandler != null) {
            logoutHandler.accept(new LogoutHandlerContext(this));
        } else {
            defaultLogout();
        }
    }

    protected void defaultLogin() {
        AppUI ui = ((AppUI) component.getUI());
        if (ui == null) {
            throw new IllegalStateException("Logout button is not attached to UI");
        }

        String loginScreenId = beanLocator.get(ConfigInterfaces.class)
                .getConfig(WebConfig.class)
                .getLoginScreenId();

        Screen loginScreen = ui.getScreens().create(loginScreenId, OpenMode.ROOT);

        loginScreen.show();
    }

    protected void defaultLogout() {
        AppUI ui = ((AppUI) component.getUI());
        if (ui == null) {
            throw new IllegalStateException("Logout button is not attached to UI");
        }
        ui.getApp().logout();
    }

    protected void openSettings() {
        Screen settingsScreen = AppUI.getCurrent().getScreens()
                .create("settings", OpenMode.NEW_TAB);

        settingsScreen.show();
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);

        component.addStyleName(USERACTIONS_BUTTON_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(
                super.getStyleName().replace(USERACTIONS_BUTTON_STYLENAME, ""));
    }
}
