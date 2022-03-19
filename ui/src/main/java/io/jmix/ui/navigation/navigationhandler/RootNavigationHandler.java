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

package io.jmix.ui.navigation.navigationhandler;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.app.navigation.notfoundwindow.NotFoundScreen;
import io.jmix.ui.component.RootWindow;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.navigation.NavigationHandler;
import io.jmix.ui.navigation.NavigationState;
import io.jmix.ui.navigation.UrlChangeHandler;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component("ui_RootNavigationHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Order(JmixOrder.LOWEST_PRECEDENCE - 40)
public class RootNavigationHandler implements NavigationHandler {

    private static final Logger log = LoggerFactory.getLogger(RootNavigationHandler.class);

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Messages messages;

    @Override
    public boolean doHandle(NavigationState requestedState, AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();

        if (urlChangeHandler.isEmptyState(requestedState)) {
            urlChangeHandler.revertNavigationState();
            return false;
        }

        if (!rootChanged(requestedState, ui)) {
            return false;
        }

        String rootRoute = requestedState.getRoot();
        WindowInfo windowInfo = windowConfig.findWindowInfoByRoute(rootRoute);

        if (windowInfo == null) {
            log.info("No registered screen found for route: '{}'", rootRoute);

            urlChangeHandler.revertNavigationState();

            handle404(rootRoute, ui);

            return true;
        }

        if (urlChangeHandler.shouldRedirect(windowInfo)) {
            urlChangeHandler.redirect(requestedState);
            return true;
        }

        if (!urlChangeHandler.isPermittedToNavigate(requestedState, windowInfo)) {
            return true;
        }

        Screen screen = ui.getScreens().create(windowInfo.getId(), OpenMode.ROOT);

        boolean hasNestedRoute = StringUtils.isNotEmpty(requestedState.getNestedRoute());
        if (!hasNestedRoute
                && MapUtils.isNotEmpty(requestedState.getParams())) {
            UiControllerUtils.fireEvent(screen, UrlParamsChangedEvent.class,
                    new UrlParamsChangedEvent(screen, requestedState.getParams()));

            ((WindowImpl) screen.getWindow())
                    .setResolvedState(requestedState);
        }

        screen.show();

        return !hasNestedRoute;
    }

    protected boolean rootChanged(NavigationState requestedState, AppUI ui) {
        Screen rootScreen = ui.getScreens().getOpenedScreens()
                .getRootScreenOrNull();

        if (rootScreen == null) {
            return false;
        }

        String rootRoute = ((WindowImpl) rootScreen.getWindow())
                .getResolvedState()
                .getRoot();

        return !StringUtils.equals(rootRoute, requestedState.getRoot());
    }

    protected void handle404(String route, AppUI ui) {
        RootWindow topWindow = ui.getTopLevelWindow();
        Screen rootScreen = topWindow != null ? topWindow.getFrameOwner() : null;

        if (rootScreen instanceof Window.HasWorkArea) {
            MapScreenOptions options = new MapScreenOptions(
                    ParamsMap.of("requestedRoute", route));

            ui.getScreens()
                    .create(NotFoundScreen.class, OpenMode.NEW_TAB, options)
                    .show();
        } else {
            ui.getNotifications()
                    .create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.formatMessage("", "navigation.screenNotFound", route))
                    .show();
        }
    }
}
