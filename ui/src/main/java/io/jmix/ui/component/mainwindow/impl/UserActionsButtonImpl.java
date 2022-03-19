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

package io.jmix.ui.component.mainwindow.impl;

import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.component.mainwindow.UserActionsButton;
import io.jmix.ui.accesscontext.UiShowScreenContext;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.widget.JmixMenuBar;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class UserActionsButtonImpl extends AbstractComponent<JmixMenuBar> implements UserActionsButton {

    public static final String USERACTIONS_BUTTON_STYLENAME = "jmix-useractions-button";

    protected IconResolver iconResolver;
    protected Icons icons;
    protected Messages messages;
    protected AccessManager accessManager;

    protected Consumer<LoginHandlerContext> loginHandler;
    protected Consumer<LogoutHandlerContext> logoutHandler;

    public UserActionsButtonImpl() {
        component = new JmixMenuBar();
        component.addStyleName(USERACTIONS_BUTTON_STYLENAME);

        component.addAttachListener(event -> {
            UI ui = event.getConnector().getUI();

            if (ui instanceof AppUI) {
                initComponent((AppUI) ui);
            }
        });
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Override
    public void setLoginHandler(@Nullable Consumer<LoginHandlerContext> loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public void setLogoutHandler(@Nullable Consumer<LogoutHandlerContext> logoutHandler) {
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
        loginButton.setIcon(getIconResource(JmixIcon.SIGN_IN));
        loginButton.setVisible(!authenticated);
    }

    protected void initUserMenuButton(boolean authenticated) {
        MenuBar.MenuItem userMenuButton = component.addItem("");
        userMenuButton.setDescription(messages.getMessage("userActionsBtnDescription"));
        userMenuButton.setIcon(getIconResource(JmixIcon.USER));
        userMenuButton.setVisible(authenticated);

        UiShowScreenContext showScreenContext = new UiShowScreenContext("settings");
        accessManager.applyRegisteredConstraints(showScreenContext);

        if (showScreenContext.isPermitted()) {
            userMenuButton.addItem(messages.getMessage("settings"),
                    getIconResource(JmixIcon.GEAR), item -> openSettings());
        }

        userMenuButton.addItem(messages.getMessage("logoutBtnDescription"),
                getIconResource(JmixIcon.SIGN_OUT), item -> logout());
    }

    @Nullable
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

        String loginScreenId = applicationContext.getBean(UiProperties.class).getLoginScreenId();

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
        if (AppUI.getCurrent() != null) {
            Screen settingsScreen = AppUI.getCurrent().getScreens()
                    .create("settings", OpenMode.NEW_TAB);

            settingsScreen.show();
        }
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        component.addStyleName(USERACTIONS_BUTTON_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(
                super.getStyleName().replace(USERACTIONS_BUTTON_STYLENAME, ""));
    }
}
