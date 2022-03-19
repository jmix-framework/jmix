/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.web.app.main;

import com.haulmont.cuba.core.global.Messages;
import com.vaadin.server.WebBrowser;
import io.jmix.ui.*;
import io.jmix.ui.component.*;
import io.jmix.ui.component.dev.LayoutAnalyzerContextMenuProvider;
import io.jmix.ui.component.mainwindow.AppMenu;
import io.jmix.ui.component.mainwindow.SideMenu;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import io.jmix.ui.widget.JmixCollapsibleMenuLayoutExtension;
import io.jmix.ui.widget.JmixCssActionsLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

import static io.jmix.ui.component.ComponentsHelper.setStyleName;

/**
 * Base class for a controller of application Main screen.
 */
@Route(path = "main", root = true)
@UiDescriptor("main-screen.xml")
@UiController("main")
public class MainScreen extends Screen implements Window.HasWorkArea, Window.HasUserIndicator {

    public static final String SIDEMENU_COLLAPSED_STATE = "sidemenuCollapsed";
    public static final String SIDEMENU_COLLAPSED_STYLENAME = "collapsed";

    protected static final String APP_LOGO_IMAGE = "application.logoImage";

    public MainScreen() {
        addInitListener(this::initComponents);
    }

    protected void initComponents(@SuppressWarnings("unused") InitEvent e) {
        initLogoImage();
        initFtsField();
        initUserIndicator();
        initMenu();
        initLayoutAnalyzerContextMenu();
    }

    protected void initUserIndicator() {
        UserIndicator userIndicator = getUserIndicator();
        if (userIndicator != null) {
            boolean authenticated = AppUI.getCurrent().hasAuthenticatedSession();
            userIndicator.setVisible(authenticated);
        }
    }

    protected void initLogoImage() {
        Image logoImage = getLogoImage();
        String logoImagePath = getApplicationContext().getBean(Messages.class)
                .getMainMessage(APP_LOGO_IMAGE);

        if (logoImage != null
                && StringUtils.isNotBlank(logoImagePath)
                && !APP_LOGO_IMAGE.equals(logoImagePath)) {
            logoImage.setSource(ThemeResource.class).setPath(logoImagePath);
        }
    }

    protected void initFtsField() {
        // TODO fts
        /*FtsField ftsField = getFtsField();
        if (ftsField != null && !FtsConfigHelper.getEnabled()) {
            ftsField.setVisible(false);
        }*/
    }

    protected void initLayoutAnalyzerContextMenu() {
        Image logoImage = getLogoImage();
        if (logoImage != null) {
            LayoutAnalyzerContextMenuProvider laContextMenuProvider =
                    getApplicationContext().getBean(LayoutAnalyzerContextMenuProvider.class);
            laContextMenuProvider.initContextMenu(this, logoImage);
        }
    }

    protected void initMenu() {
        Component menu = getAppMenu();
        if (menu == null) {
            menu = getSideMenu();
        }

        if (menu != null) {
            ((Component.Focusable) menu).focus();
        }

        initCollapsibleMenu();
    }

    protected void initCollapsibleMenu() {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        if (sideMenuContainer instanceof CssLayout) {
            Component sideMenuLayout = getWindow().getComponent("horizontalWrap");
            if (sideMenuLayout instanceof  CssLayout) {
                sideMenuLayout.withUnwrapped(JmixCssActionsLayout.class, JmixCollapsibleMenuLayoutExtension::new);
            }

            if (isMobileDevice()) {
                setSideMenuCollapsed(true);
            } else {
                String menuCollapsedCookie = App.getInstance()
                        .getCookieValue(SIDEMENU_COLLAPSED_STATE);

                boolean menuCollapsed = Boolean.parseBoolean(menuCollapsedCookie);

                setSideMenuCollapsed(menuCollapsed);
            }

            initCollapseMenuControls();
        }
    }

    protected void initCollapseMenuControls() {
        Button collapseMenuButton = getCollapseMenuButton();
        if (collapseMenuButton != null) {
            collapseMenuButton.addClickListener(event ->
                    setSideMenuCollapsed(!isMenuCollapsed()));
        }

        Button settingsButton = getSettingsButton();
        if (settingsButton != null) {
            settingsButton.addClickListener(event ->
                    openSettingsScreen());
        }

        Button loginButton = getLoginButton();
        if (loginButton != null) {
            loginButton.addClickListener(event ->
                    openLoginScreen());
        }
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        Screens screens = UiControllerUtils.getScreenContext(this)
                .getScreens();
        ScreenTools screenTools = getApplicationContext().getBean(ScreenTools.class);
        screenTools.openDefaultScreen(screens);
        screenTools.handleRedirect();
    }

    @Nullable
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }

    @Nullable
    @Override
    public UserIndicator getUserIndicator() {
        return (UserIndicator) getWindow().getComponent("userIndicator");
    }

    @Nullable
    protected Button getCollapseMenuButton() {
        return (Button) getWindow().getComponent("collapseMenuButton");
    }

    @Nullable
    protected Button getSettingsButton() {
        return (Button) getWindow().getComponent("settingsButton");
    }

    @Nullable
    protected Button getLoginButton() {
        return (Button) getWindow().getComponent("loginButton");
    }

    @Nullable
    protected Image getLogoImage() {
        return (Image) getWindow().getComponent("logoImage");
    }

    // TODO fts
    /*@Nullable
    protected FtsField getFtsField() {
        return (FtsField) getWindow().getComponent("ftsField");
    }*/

    @Nullable
    protected AppMenu getAppMenu() {
        return (AppMenu) getWindow().getComponent("appMenu");
    }

    @Nullable
    protected SideMenu getSideMenu() {
        return (SideMenu) getWindow().getComponent("sideMenu");
    }

    @Nullable
    protected Component getTitleBar() {
        return getWindow().getComponent("titleBar");
    }

    protected void openLoginScreen() {
        String loginScreenId = getApplicationContext().getBean(UiProperties.class).getLoginScreenId();

        UiControllerUtils.getScreenContext(this)
                .getScreens()
                .create(loginScreenId, OpenMode.ROOT)
                .show();
    }

    protected void openSettingsScreen() {
        UiControllerUtils.getScreenContext(this)
                .getScreens()
                .create("settings", OpenMode.NEW_TAB)
                .show();
    }

    protected void setSideMenuCollapsed(boolean collapsed) {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        CssLayout sideMenuPanel = (CssLayout) getWindow().getComponent("sideMenuPanel");
        Button collapseMenuButton = getCollapseMenuButton();

        setStyleName(sideMenuContainer, SIDEMENU_COLLAPSED_STYLENAME, collapsed);
        setStyleName(sideMenuPanel, SIDEMENU_COLLAPSED_STYLENAME, collapsed);

        if (collapseMenuButton != null) {
            Messages messages = getApplicationContext().getBean(Messages.class);
            if (collapsed) {
                collapseMenuButton.setCaption(messages.getMainMessage("menuExpandGlyph"));
                collapseMenuButton.setDescription(messages.getMainMessage("sideMenuExpand"));
            } else {
                collapseMenuButton.setCaption(messages.getMainMessage("menuCollapseGlyph"));
                collapseMenuButton.setDescription(messages.getMainMessage("sideMenuCollapse"));
            }
        }

        App.getInstance()
                .addCookie(SIDEMENU_COLLAPSED_STATE, String.valueOf(collapsed));
    }

    protected boolean isMenuCollapsed() {
        CssLayout sideMenuPanel = (CssLayout) getWindow().getComponent("sideMenuPanel");
        return sideMenuPanel != null
                && sideMenuPanel.getStyleName() != null
                && sideMenuPanel.getStyleName().contains(SIDEMENU_COLLAPSED_STYLENAME);
    }

    protected boolean isMobileDevice() {
        WebBrowser browser = AppUI.getCurrent()
                .getPage()
                .getWebBrowser();

        return browser.getScreenWidth() < 500
                || browser.getScreenHeight() < 800;
    }
}
