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

package io.jmix.samples.ui.screen;

import io.jmix.core.Messages;
import io.jmix.ui.ScreenTools;
import io.jmix.ui.Screens;
import io.jmix.ui.component.*;
import io.jmix.ui.component.dev.LayoutAnalyzerContextMenuProvider;
import io.jmix.ui.component.mainwindow.Drawer;
import io.jmix.ui.component.mainwindow.SideMenu;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Base class for a controller of application Main screen.
 */
@UiDescriptor("main-screen.xml")
@UiController("main")
public class MainScreen extends Screen implements Window.HasWorkArea {

    protected static final String APP_LOGO_IMAGE = "application.logoImage";

    @Autowired
    private Image logoImage;
    @Autowired
    private Messages messages;
    @Autowired
    private ScreenTools screenTools;
    @Autowired
    private Screens screens;
    @Autowired
    private LayoutAnalyzerContextMenuProvider layoutAnalyzerContextMenuProvider;
    @Autowired
    private Drawer drawer;
    @Autowired
    private Button collapseDrawerButton;

    @Subscribe
    public void onInit(InitEvent event) {
        initLogoImage();
        initLayoutAnalyzerContextMenu();
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        screenTools.openDefaultScreen(screens);
    }

    protected void initLogoImage() {
        String logoImagePath = messages.getMessage(APP_LOGO_IMAGE);
        if (StringUtils.isNotBlank(logoImagePath)
                && !APP_LOGO_IMAGE.equals(logoImagePath)) {
            logoImage.setSource(ThemeResource.class).setPath(logoImagePath);
        }
    }

    protected void initLayoutAnalyzerContextMenu() {
        layoutAnalyzerContextMenuProvider.initContextMenu(this, logoImage);
    }

    @Nullable
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }

    @Subscribe("collapseDrawerButton")
    public void onCollapseDrawerButtonClick(Button.ClickEvent event) {
        drawer.toggle();
        if (drawer.isCollapsed()) {
            collapseDrawerButton.setIconFromSet(JmixIcon.CHEVRON_RIGHT);
        } else {
            collapseDrawerButton.setIconFromSet(JmixIcon.CHEVRON_LEFT);
        }
    }
}
