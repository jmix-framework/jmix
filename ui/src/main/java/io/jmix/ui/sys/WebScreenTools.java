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

package io.jmix.ui.sys;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.ui.*;
import io.jmix.ui.navigation.EditorTypeExtractor;
import io.jmix.ui.settings.UserSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(ScreenTools.NAME)
public class WebScreenTools implements ScreenTools {

    private static final Logger log = LoggerFactory.getLogger(WebScreenTools.class);

    /*@Autowired
    protected WebConfig webConfig;*/
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected UserSettingService userSettingService;

    @Override
    public void openDefaultScreen(Screens screens) {
        // todo db properties
        /*String defaultScreenId = webConfig.getDefaultScreenId();

        if (webConfig.getUserCanChooseDefaultScreen()) {
            String userDefaultScreen = userSettingService.loadSetting(ClientType.WEB, "userDefaultScreen");

            defaultScreenId = StringUtils.isEmpty(userDefaultScreen)
                    ? defaultScreenId
                    : userDefaultScreen;
        }

        if (StringUtils.isEmpty(defaultScreenId)) {
            return;
        }

        if (!windowConfig.hasWindow(defaultScreenId)) {
            log.info("Can't find default screen: {}", defaultScreenId);
            return;
        }

        Screen screen = screens.create(defaultScreenId, OpenMode.NEW_TAB);

        if (screen instanceof EditorScreen) {
            ((EditorScreen) screen).setEntityToEdit(getEntityToEdit(defaultScreenId));
        }

        screen.show();

        Window window = screen.getWindow();

        WebWindow webWindow;
        if (window instanceof Window.Wrapper) {
            webWindow = (WebWindow) ((Window.Wrapper) window).getWrappedWindow();
        } else {
            webWindow = (WebWindow) window;
        }
        webWindow.setDefaultScreenWindow(true);

        if (!uiProperties.isDefaultScreenCanBeClosed()) {
            window.setCloseable(false);
        }
        */
    }

    protected Entity getEntityToEdit(String screenId) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);
        Class<? extends Entity> entityClass = EditorTypeExtractor.extractEntityClass(windowInfo);

        if (entityClass == null) {
            throw new UnsupportedOperationException(
                    String.format("Unable to open default screen '%s'. Failed to determine editor entity type",
                            screenId));
        }

        return metadata.create(entityClass);
    }
}
