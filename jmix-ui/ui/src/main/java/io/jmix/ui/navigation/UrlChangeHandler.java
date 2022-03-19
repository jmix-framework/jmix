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

package io.jmix.ui.navigation;

import com.vaadin.server.Page;
import com.vaadin.spring.annotation.UIScope;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.ui.*;
import io.jmix.ui.accesscontext.UiShowScreenContext;
import io.jmix.ui.component.CloseOriginType;
import io.jmix.ui.component.RootWindow;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.navigation.NavigationFilter.AccessCheckResult;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.ControllerUtils;
import io.jmix.ui.util.OperationResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@UIScope
@Component("ui_UrlChangeHandler")
public class UrlChangeHandler implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(UrlChangeHandler.class);

    @Autowired
    protected Messages messages;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected UrlTools urlTools;

    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected List<NavigationFilter> navigationFilters;

    @Autowired
    protected AccessManager accessManager;

    protected AppUI ui;

    protected HistoryNavigator historyNavigator;
    protected ScreenNavigator screenNavigator;

    protected RedirectHandler redirectHandler;

    @Autowired
    public void setAppUi(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public void afterPropertiesSet() {
        historyNavigator = applicationContext.getBean(HistoryNavigator.class, ui, this);
        screenNavigator = applicationContext.getBean(ScreenNavigator.class, ui);
    }

    public void handleUrlChange(Page.PopStateEvent event) {
        if (notSuitableMode()) {
            log.debug("UrlChangeHandler is disabled for '{}' URL handling mode", uiProperties.getUrlHandlingMode());
            return;
        }

        int hashIdx = event.getUri().indexOf("#");
        NavigationState requestedState = hashIdx < 0
                ? NavigationState.EMPTY
                : urlTools.parseState(event.getUri().substring(hashIdx + 1));

        if (requestedState == null) {
            log.debug("Unable to handle requested state: '{}'", Page.getCurrent().getUriFragment());
            reloadApp();
            return;
        }

        __handleUrlChange(requestedState);
    }

    public ScreenNavigator getScreenNavigator() {
        return screenNavigator;
    }

    @Nullable
    public RedirectHandler getRedirectHandler() {
        return redirectHandler;
    }

    public void setRedirectHandler(RedirectHandler redirectHandler) {
        this.redirectHandler = redirectHandler;
    }

    protected void __handleUrlChange(NavigationState requestedState) {
        boolean historyNavHandled = historyNavigator.handleHistoryNavigation(requestedState);
        if (!historyNavHandled) {
            screenNavigator.handleScreenNavigation(requestedState);
        }
    }

    @Nullable
    public Screen getActiveScreen() {
        Iterator<Screen> dialogsIterator = getOpenedScreens().getDialogScreens().iterator();
        if (dialogsIterator.hasNext()) {
            return dialogsIterator.next();
        }

        Iterator<Screen> screensIterator = getOpenedScreens().getCurrentBreadcrumbs().iterator();
        if (screensIterator.hasNext()) {
            return screensIterator.next();
        }

        return getOpenedScreens().getRootScreenOrNull();
    }

    @Nullable
    public Screen findActiveScreenByState(NavigationState requestedState) {
        Screen screen = findScreenByState(getOpenedScreens().getActiveScreens(), requestedState);

        if (screen == null && isCurrentRootState(requestedState)) {
            screen = ui.getScreens().getOpenedScreens().getRootScreenOrNull();
        }

        return screen;
    }

    public void restoreState() {
        if (notSuitableMode()) {
            log.debug("UrlChangeHandler is disabled for '{}' URL handling mode", uiProperties.getUrlHandlingMode());
            return;
        }

        NavigationState currentState = urlTools.parseState(ui.getPage().getUriFragment());

        if (currentState == null
                || currentState == NavigationState.EMPTY) {
            RootWindow topLevelWindow = ui.getTopLevelWindow();
            if (topLevelWindow instanceof WindowImpl) {
                NavigationState topScreenState = ((WindowImpl) topLevelWindow).getResolvedState();

                urlTools.replaceState(topScreenState.asRoute(), ui);
            }
        }
    }

    public boolean shouldRedirect(WindowInfo windowInfo) {
        if (ui.hasAuthenticatedSession()) {
            return false;
        }

        boolean allowAnonymousAccess = uiProperties.isAllowAnonymousAccess();


        UiShowScreenContext showScreenContext = new UiShowScreenContext(windowInfo.getId());
        accessManager.applyRegisteredConstraints(showScreenContext);

        return !allowAnonymousAccess || !showScreenContext.isPermitted();
    }

    public void redirect(NavigationState navigationState) {
        String loginScreenId = uiProperties.getLoginScreenId();

        Screen loginScreen = ui.getScreens().create(loginScreenId, OpenMode.ROOT);

        loginScreen.show();

        RedirectHandler redirectHandler = applicationContext.getBean(RedirectHandler.class, ui);
        redirectHandler.schedule(navigationState);

        setRedirectHandler(redirectHandler);
    }

    public boolean isPermittedToNavigate(NavigationState requestedState, WindowInfo windowInfo) {
        UiShowScreenContext showScreenContext = new UiShowScreenContext(windowInfo.getId());
        accessManager.applyRegisteredConstraints(showScreenContext);

        if (!showScreenContext.isPermitted()) {
            revertNavigationState();

            throw new AccessDeniedException("screen", windowInfo.getId());
        }

        NavigationFilter.AccessCheckResult navigationAllowed = navigationAllowed(requestedState);
        if (navigationAllowed.isRejected()) {
            if (isNotEmpty(navigationAllowed.getMessage())) {
                showNotification(navigationAllowed.getMessage());
            }

            revertNavigationState();

            return false;
        }

        return true;
    }

    public void showNotification(String msg) {
        ui.getNotifications()
                .create(Notifications.NotificationType.TRAY)
                .withCaption(msg)
                .show();
    }

    public void revertNavigationState() {
        Screen screen = findActiveScreenByState(ui.getHistory().getNow());
        if (screen == null) {
            screen = getActiveScreen();
        }

        urlTools.replaceState(getResolvedState(screen).asRoute(), ui);
    }

    public NavigationState getResolvedState(@Nullable Screen screen) {
        if (screen == null) {
            return NavigationState.EMPTY;
        }
        NavigationState resolvedState = ((WindowImpl) screen.getWindow()).getResolvedState();
        return resolvedState != null
                ? resolvedState
                : NavigationState.EMPTY;
    }

    public NavigationFilter.AccessCheckResult navigationAllowed(NavigationState requestedState) {
        NavigationState currentState = ui.getHistory().getNow();

        for (NavigationFilter filter : navigationFilters) {
            AccessCheckResult accessCheckResult = filter.allowed(currentState, requestedState);
            if (accessCheckResult.isRejected()) {
                return accessCheckResult;
            }
        }

        return AccessCheckResult.allowed();
    }

    public boolean isEmptyState(@Nullable NavigationState requestedState) {
        return requestedState == null || requestedState == NavigationState.EMPTY;
    }

    public boolean isRootRoute(@Nullable WindowInfo windowInfo) {
        return windowInfo != null
                && windowInfo.getRouteDefinition().isRoot();
    }

    public boolean isRootState(@Nullable NavigationState requestedState) {
        if (requestedState == null) {
            return false;
        }
        return isNotEmpty(requestedState.getRoot())
                && isEmpty(requestedState.getStateMark())
                && isEmpty(requestedState.getNestedRoute());
    }

    protected boolean isCurrentRootState(NavigationState requestedState) {
        if (!isRootState(requestedState)) {
            return false;
        }

        Screen rootScreen = ui.getScreens().getOpenedScreens().getRootScreenOrNull();
        if (rootScreen == null) {
            return false;
        }

        RouteDefinition routeDefinition = UiControllerUtils.getScreenContext(rootScreen)
                .getWindowInfo()
                .getRouteDefinition();

        return routeDefinition != null
                && routeDefinition.isRoot()
                && StringUtils.equals(routeDefinition.getPath(), requestedState.getRoot());
    }

    protected void reloadApp() {
        String url = ControllerUtils.getLocationWithoutParams() + "?restartApp";
        ui.getPage().open(url, "_self");
    }

    protected String getStateMark(Screen screen) {
        WindowImpl windowImpl = (WindowImpl) screen.getWindow();
        NavigationState resolvedState = windowImpl.getResolvedState();
        return resolvedState != null
                ? resolvedState.getStateMark()
                : NavigationState.EMPTY.getStateMark();
    }

    @Nullable
    protected Screen findScreenByState(NavigationState requestedState) {
        return findScreenByState(getOpenedScreens().getAll(), requestedState);
    }

    @Nullable
    protected Screen findScreenByState(Collection<Screen> screens, NavigationState requestedState) {
        return screens.stream()
                .filter(s -> Objects.equals(requestedState.getStateMark(), getStateMark(s)))
                .findFirst()
                .orElse(null);
    }

    protected void selectScreen(@Nullable Screen screen) {
        if (screen == null) {
            return;
        }

        for (Screens.WindowStack windowStack : getOpenedScreens().getWorkAreaStacks()) {
            Iterator<Screen> breadCrumbs = windowStack.getBreadcrumbs().iterator();
            if (breadCrumbs.hasNext()
                    && breadCrumbs.next() == screen) {

                windowStack.select();
                return;
            }
        }
    }

    protected boolean notSuitableMode() {
        return UrlHandlingMode.URL_ROUTES != uiProperties.getUrlHandlingMode();
    }

    protected Screens.OpenedScreens getOpenedScreens() {
        return ui.getScreens().getOpenedScreens();
    }

    // Copied from WebAppWorkArea

    public boolean isNotCloseable(Window window) {
        if (!window.isCloseable()) {
            return true;
        }

        if (uiProperties.isDefaultScreenCanBeClosed()) {
            return false;
        }

        return ((WindowImpl) window).isDefaultScreenWindow();
    }

    protected boolean closeWindowStack(Screens.WindowStack windowStack) {
        boolean closed = true;

        for (Screen screen : windowStack.getBreadcrumbs()) {
            if (isNotCloseable(screen.getWindow())
                    || isWindowClosePrevented(screen.getWindow())) {
                closed = false;

                windowStack.select();

                break;
            }

            OperationResult closeResult = screen.close(FrameOwner.WINDOW_CLOSE_ACTION);
            if (closeResult.getStatus() != OperationResult.Status.SUCCESS) {
                closed = false;

                windowStack.select();

                break;
            }
        }
        return closed;
    }

    protected boolean isWindowClosePrevented(Window window) {
        Window.BeforeCloseEvent event = new Window.BeforeCloseEvent(window, CloseOriginType.CLOSE_BUTTON);

        ((WindowImpl) window).fireBeforeClose(event);

        return event.isClosePrevented();
    }
}
