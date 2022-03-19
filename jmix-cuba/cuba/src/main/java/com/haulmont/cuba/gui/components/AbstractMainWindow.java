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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import io.jmix.ui.ScreenTools;
import io.jmix.ui.Screens;
import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Image;
import io.jmix.ui.component.ThemeResource;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.screen.Screen;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Legacy base class for a controller of application Main window.
 */
public class AbstractMainWindow extends AbstractTopLevelWindow
        implements Window.HasWorkArea, Window.HasUserIndicator, com.haulmont.cuba.gui.components.Window.HasFoldersPane {

    protected static final String APP_LOGO_IMAGE = "application.logoImage";

    @Autowired
    protected Screens screens;
    @Autowired
    protected ScreenTools screenTools;

    protected AppWorkArea workArea;
    protected UserIndicator userIndicator;
    protected FoldersPane foldersPane;

    public AbstractMainWindow() {
        addInitListener(this::initComponents);
    }

    protected void initComponents(Screen.InitEvent e) {
        workArea = getWorkArea();
        userIndicator = getUserIndicator();
        foldersPane = getFoldersPane();
    }

    @Override
    @Nullable
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getComponent("workArea");
    }

    @Override
    @Nullable
    public UserIndicator getUserIndicator() {
        return (UserIndicator) getComponent("userIndicator");
    }

    @Nullable
    @Override
    public FoldersPane getFoldersPane() {
        return (FoldersPane) getComponent("foldersPane");
    }

    protected void initLogoImage(Image logoImage) {
        String logoImagePath = messages.getMessage(APP_LOGO_IMAGE);
        if (logoImage != null
                && StringUtils.isNotBlank(logoImagePath)
                && !APP_LOGO_IMAGE.equals(logoImagePath)) {
            logoImage.setSource(ThemeResource.class).setPath(logoImagePath);
        }
    }

    // todo fts
//    protected void initFtsField(FtsField ftsField) {
//        if (ftsField != null && !FtsConfigHelper.getEnabled()) {
//            ftsField.setVisible(false);
//        }
//    }

    //todo layout context analyzer
//    protected void initLayoutAnalyzerContextMenu(Component contextMenuTarget) {
//        LayoutAnalyzerContextMenuProvider laContextMenuProvider =
//                getBeanLocator().get(LayoutAnalyzerContextMenuProvider.NAME);
//        laContextMenuProvider.initContextMenu(this, contextMenuTarget);
//    }

    //todo UserSubstitutionsChangedEvent
//    @Order(Events.LOWEST_CORE_PRECEDENCE - 100)
//    @EventListener
//    protected void onUserSubstitutionsChange(UserSubstitutionsChangedEvent event) {
//        UserIndicator userIndicator = getUserIndicator();
//        if (userIndicator != null) {
//            userIndicator.refreshUserSubstitutions();
//        }
//    }

    // todo UserRemovedEvent
//    @Order(Events.LOWEST_CORE_PRECEDENCE - 100)
//    @EventListener
//    protected void onUserRemove(UserRemovedEvent event) {
//        UserIndicator userIndicator = getUserIndicator();
//        if (userIndicator != null) {
//            userIndicator.refreshUserSubstitutions();
//        }
//    }

    @Override
    public void ready() {
        super.ready();

        screenTools.openDefaultScreen(screens);
        screenTools.handleRedirect();
    }
}
