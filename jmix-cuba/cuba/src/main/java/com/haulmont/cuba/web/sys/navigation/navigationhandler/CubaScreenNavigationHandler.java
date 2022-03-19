/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.web.sys.navigation.navigationhandler;

import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.core.JmixOrder;
import io.jmix.ui.AppUI;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.navigation.navigationhandler.ScreenNavigationHandler;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Order(JmixOrder.LOWEST_PRECEDENCE - 35)
public class CubaScreenNavigationHandler extends ScreenNavigationHandler {

    @Override
    protected Screen doCreateEditor(WindowInfo windowInfo, AppUI ui, OpenMode openMode, Map<String, Object> options) {
        return isLegacyScreen(windowInfo.getControllerClass())
                ? ui.getScreens().create(windowInfo.getId(), openMode, new MapScreenOptions(options))
                : super.doCreateEditor(windowInfo, ui, openMode, options);
    }

    protected boolean isLegacyScreen(Class<? extends FrameOwner> controllerClass) {
        return LegacyFrame.class.isAssignableFrom(controllerClass);
    }
}
