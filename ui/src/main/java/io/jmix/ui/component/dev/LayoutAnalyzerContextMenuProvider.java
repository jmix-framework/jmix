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

package io.jmix.ui.component.dev;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.MenuBar;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import io.jmix.ui.app.core.dev.LayoutAnalyzer;
import io.jmix.ui.app.core.dev.LayoutAnalyzerScreen;
import io.jmix.ui.app.core.dev.LayoutTip;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.addon.contextmenu.ContextMenu;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static io.jmix.ui.app.core.dev.LayoutAnalyzerScreen.TIPS_LIST_PARAM;

@Component(LayoutAnalyzerContextMenuProvider.NAME)
public class LayoutAnalyzerContextMenuProvider {

    public static final String NAME = "jmix_LayoutAnalyzerContextMenuProvider";

    @Inject
    protected Messages messages;
    @Inject
    protected UiProperties properties;

    public void initContextMenu(Screen screen, io.jmix.ui.component.Component contextMenuTarget) {
        if (properties.isLayoutAnalyzerEnabled()) {
            ContextMenu contextMenu = new ContextMenu(contextMenuTarget.unwrap(AbstractComponent.class), true);
            MenuBar.MenuItem menuItem = contextMenu.addItem(messages.getMessage("actions.analyzeLayout"), c -> {
                LayoutAnalyzer analyzer = new LayoutAnalyzer();

                List<LayoutTip> tipsList = analyzer.analyze(screen);

                if (tipsList.isEmpty()) {
                    Notifications notifications = UiControllerUtils.getScreenContext(screen)
                            .getNotifications();

                    notifications.create(Notifications.NotificationType.HUMANIZED)
                            .withCaption("No layout problems found")
                            .show();
                } else {
                    Screens screens = UiControllerUtils.getScreenContext(screen)
                            .getScreens();

                    LayoutAnalyzerScreen layoutAnalyzerScreen =
                            screens.create(
                                    LayoutAnalyzerScreen.class,
                                    OpenMode.DIALOG,
                                    new MapScreenOptions(
                                            ParamsMap.of(TIPS_LIST_PARAM, tipsList)));

                    layoutAnalyzerScreen.show();
                }
            });
            menuItem.setStyleName("c-cm-item");
        }
    }
}
