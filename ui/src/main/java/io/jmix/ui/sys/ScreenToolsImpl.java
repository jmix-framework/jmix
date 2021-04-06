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

import io.jmix.core.Metadata;
import io.jmix.ui.*;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.navigation.EditorTypeExtractor;
import io.jmix.ui.navigation.RedirectHandler;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.settings.UserSettingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ui_ScreenTools")
public class ScreenToolsImpl implements ScreenTools {

    private static final Logger log = LoggerFactory.getLogger(ScreenToolsImpl.class);

    protected UiProperties uiProperties;
    protected Metadata metadata;
    protected WindowConfig windowConfig;
    protected UserSettingService userSettingService;

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setWindowConfig(WindowConfig windowConfig) {
        this.windowConfig = windowConfig;
    }

    @Autowired(required = false)
    public void setUserSettingService(UserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }

    @Override
    public void openDefaultScreen(Screens screens) {
        String defaultScreenId = uiProperties.getDefaultScreenId();
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

        setDefaultScreenWindow(window);

        if (!uiProperties.isDefaultScreenCanBeClosed()) {
            window.setCloseable(false);
        }
    }

    protected void setDefaultScreenWindow(Window window) {
        ((WindowImpl) window).setDefaultScreenWindow(true);
    }

    protected Object getEntityToEdit(String screenId) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);
        Class<?> entityClass = EditorTypeExtractor.extractEntityClass(windowInfo);

        if (entityClass == null) {
            throw new UnsupportedOperationException(
                    String.format("Unable to open default screen '%s'. Failed to determine editor entity type",
                            screenId));
        }

        return metadata.create(entityClass);
    }

    @Override
    public void handleRedirect() {
        AppUI ui = AppUI.getCurrent();
        if (ui != null) {
            RedirectHandler redirectHandler = ui.getUrlChangeHandler().getRedirectHandler();
            if (redirectHandler != null
                    && redirectHandler.scheduled()) {
                redirectHandler.redirect();
            }
        }
    }
}
