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
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.*;
import io.jmix.ui.app.navigation.notfoundwindow.NotFoundScreen;
import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.DialogWindow;
import io.jmix.ui.component.RootWindow;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.sys.ControllerUtils;
import io.jmix.ui.sys.UiDescriptorUtils;
import io.jmix.ui.widget.client.ui.AppUIConstants;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@UIScope
@Component("ui_UrlRouting")
public class WebUrlRouting implements UrlRouting {

    public static final String NEW_ENTITY_ID = "new";
    protected static final int MAX_NESTING = 2;

    private static final Logger log = LoggerFactory.getLogger(WebUrlRouting.class);

    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UrlTools urlTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Screens screens;

    protected AppUI ui;

    protected String lastHistoryOperation = AppUIConstants.HISTORY_PUSH_OP;

    @Autowired
    public void setAppUi(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public void pushState(Screen screen, Map<String, String> urlParams) {
        if (!checkConditions(screen, urlParams)) {
            return;
        }

        updateState(screen, urlParams, true);
    }

    @Override
    public void replaceState(Screen screen, Map<String, String> urlParams) {
        if (!checkConditions(screen, urlParams)) {
            return;
        }

        updateState(screen, urlParams, false);
    }

    @Override
    public NavigationState getState() {
        if (UrlHandlingMode.URL_ROUTES != uiProperties.getUrlHandlingMode()) {
            log.debug("UrlRouting is disabled for '{}' URL handling mode", uiProperties.getUrlHandlingMode());
            return NavigationState.EMPTY;
        }

        if (UrlTools.headless()) {
            log.debug("Unable to resolve navigation state in headless mode");
            return NavigationState.EMPTY;
        }

        return urlTools.parseState(Page.getCurrent().getLocation().getRawFragment());
    }

    @Override
    public RouteGenerator getRouteGenerator() {
        return new RouteGeneratorImpl();
    }

    protected void updateState(Screen screen, Map<String, String> urlParams, boolean pushState) {
        NavigationState currentState = getState();
        NavigationState newState = buildNavState(screen, urlParams);

        // do not push copy-pasted requested state to avoid double state pushing into browser history
        if (!pushState
                || externalNavigation(currentState, newState)
                || isNotFoundScreen(screen)) {
            urlTools.replaceState(newState.asRoute(), ui);

            lastHistoryOperation = AppUIConstants.HISTORY_REPLACE_OP;
        } else {
            urlTools.pushState(newState.asRoute(), ui);

            lastHistoryOperation = AppUIConstants.HISTORY_PUSH_OP;
        }

        ((WindowImpl) screen.getWindow()).setResolvedState(newState);

        if (pushState) {
            ui.getHistory().forward(newState);
        } else {
            ui.getHistory().replace(newState);
        }
    }

    protected NavigationState buildNavState(Screen screen, Map<String, String> urlParams) {
        NavigationState state;

        if (screen.getWindow() instanceof RootWindow) {
            state = new NavigationState(getRoute(screen), "", "", urlParams);
        } else {
            String rootRoute = getRoute(ui.getScreens().getOpenedScreens().getRootScreen());
            String stateMark = getStateMark(screen);

            String nestedRoute = buildNestedRoute(screen);
            Map<String, String> params = buildParams(screen, urlParams);

            state = new NavigationState(rootRoute, stateMark, nestedRoute, params);
        }

        return state;
    }

    protected String buildNestedRoute(Screen screen) {
        return screen.getWindow() instanceof DialogWindow
                ? buildDialogRoute(screen)
                : buildScreenRoute(screen);
    }

    protected String buildDialogRoute(Screen dialog) {
        RouteDefinition dialogRouteDefinition = getRouteDef(dialog);

        Iterator<Screen> currentTabScreens = ui.getScreens().getOpenedScreens().getCurrentBreadcrumbs().iterator();
        Screen currentScreen = currentTabScreens.hasNext()
                ? currentTabScreens.next()
                : null;
        String currentScreenRoute = currentScreen != null
                ? buildScreenRoute(currentScreen)
                : "";

        if (dialogRouteDefinition == null) {
            return currentScreenRoute;
        }
        String dialogRoute = dialogRouteDefinition.getPath();
        if (dialogRoute == null || dialogRoute.isEmpty()) {
            return currentScreenRoute;
        }

        String parentPrefix = dialogRouteDefinition.getParentPrefix();
        if (StringUtils.isNotEmpty(parentPrefix)
                && dialogRoute.startsWith(parentPrefix + '/')
                && currentScreenRoute.endsWith(parentPrefix)) {
            dialogRoute = dialogRoute.substring(parentPrefix.length() + 1);
        }

        return currentScreenRoute == null || currentScreenRoute.isEmpty()
                ? dialogRoute
                : currentScreenRoute + '/' + dialogRoute;
    }

    protected String buildScreenRoute(Screen screen) {
        List<Screen> screens = new ArrayList<>(ui.getScreens().getOpenedScreens().getCurrentBreadcrumbs());
        if (screens.isEmpty()
                || screens.get(0) != screen) {
            log.debug("Current breadcrumbs doesn't contain the given screen '{}'", screen.getId());
            return "";
        }

        Collections.reverse(screens);

        StringBuilder state = new StringBuilder();
        String prevSubRoute = null;

        for (int i = 0; i < screens.size() && i < MAX_NESTING; i++) {
            String subRoute = buildSubRoute(prevSubRoute, screens.get(i));

            if (StringUtils.isNotEmpty(state)
                    && StringUtils.isNotEmpty(subRoute)) {
                state.append('/');
            }
            state.append(subRoute);

            prevSubRoute = subRoute;
        }

        return state.toString();
    }

    protected String buildSubRoute(@Nullable String prevSubRoute, Screen screen) {
        String screenRoute = getRoute(screen);

        String parentPrefix = getParentPrefix(screen);
        if (StringUtils.isEmpty(parentPrefix)) {
            return nullToEmpty(screenRoute);
        }

        if (Objects.equals(prevSubRoute, parentPrefix)) {
            return nullToEmpty(screenRoute.replace(parentPrefix + "/", ""));
        } else {
            return nullToEmpty(screenRoute);
        }
    }

    protected Map<String, String> buildParams(Screen screen, Map<String, String> urlParams) {
        String route = getRoute(screen);

        if (StringUtils.isEmpty(route)
                && (isEditor(screen) || MapUtils.isNotEmpty(urlParams))) {
            log.debug("There's no route for screen \"{}\". URL params will be ignored", screen.getId());
            return Collections.emptyMap();
        }

        if (omitParams(screen)) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new LinkedHashMap<>();

        if (isEditor(screen)) {
            Object editedEntity = ((EditorScreen) screen).getEditedEntity();
            if (editedEntity != null) {
                if (!entityStates.isNew(editedEntity)) {
                    Object entityId = EntityValues.getId(editedEntity);
                    if (entityId != null) {
                        String serializedId = UrlIdSerializer.serializeId(entityId);
                        if (!"".equals(serializedId)) {
                            params.put("id", serializedId);
                        }
                    }
                }
            }
        }

        params.putAll(urlParams != null
                ? urlParams
                : Collections.emptyMap());

        return params;
    }

    @Nullable
    protected String getParentPrefix(Screen screen) {
        String parentPrefix = null;

        Route routeAnnotation = screen.getClass().getAnnotation(Route.class);
        if (routeAnnotation != null) {
            parentPrefix = routeAnnotation.parentPrefix();
        } else {
            RouteDefinition routeDef = getScreenContext(screen)
                    .getWindowInfo()
                    .getRouteDefinition();
            if (routeDef != null) {
                parentPrefix = routeDef.getParentPrefix();
            }
        }

        return parentPrefix;
    }

    protected boolean omitParams(Screen screen) {
        OpenMode openMode = screen.getWindow().getContext().getOpenMode();
        if (OpenMode.THIS_TAB != openMode) {
            return false;
        }

        return ui.getScreens().getOpenedScreens().getCurrentBreadcrumbs().size() > MAX_NESTING;
    }

    protected boolean isEditor(Screen screen) {
        return screen instanceof EditorScreen;
    }

    protected String getRoute(Screen screen) {
        RouteDefinition routeDef = getRouteDef(screen);

        return routeDef != null && StringUtils.isNotEmpty(routeDef.getPath())
                ? routeDef.getPath()
                : "";
    }

    @Nullable
    protected RouteDefinition getRouteDef(@Nullable Screen screen) {
        return screen == null
                ? null
                : getScreenContext(screen).getWindowInfo().getRouteDefinition();
    }

    protected String getStateMark(Screen screen) {
        WindowImpl windowImpl = (WindowImpl) screen.getWindow();
        NavigationState resolvedState = windowImpl.getResolvedState();
        return resolvedState != null
                ? resolvedState.getStateMark()
                : NavigationState.EMPTY.getStateMark();
    }

    protected boolean externalNavigation(@Nullable NavigationState currentState, NavigationState newState) {
        if (currentState == null) {
            return false;
        }

        boolean notInHistory = !ui.getHistory().has(currentState)
                || findActiveScreenByState(currentState) == null;

        boolean sameRoot = Objects.equals(currentState.getRoot(), newState.getRoot());

        String currentNested = currentState.getNestedRoute();
        String newNested = newState.getNestedRoute();
        boolean sameNestedRoute = Objects.equals(currentNested, newNested)
                || (StringUtils.isNotEmpty(currentNested) && StringUtils.isNotEmpty(newNested)
                && (currentNested.startsWith(newNested) || newNested.startsWith(currentNested + '/')));

        boolean sameParams = Objects.equals(currentState.getParamsString(), newState.getParamsString());

        return notInHistory && sameRoot && sameNestedRoute && sameParams;
    }

    @Nullable
    protected Screen findActiveScreenByState(NavigationState requestedState) {
        AppWorkArea workArea = screens.getConfiguredWorkAreaOrNull();

        return workArea != null
                ? findScreenByState(getOpenedScreens().getActiveScreens(), requestedState)
                : null;
    }

    protected Screens.OpenedScreens getOpenedScreens() {
        return ui.getScreens().getOpenedScreens();
    }

    @Nullable
    protected Screen findScreenByState(Collection<Screen> screens, NavigationState requestedState) {
        return screens.stream()
                .filter(s -> Objects.equals(requestedState.getStateMark(), getStateMark(s)))
                .findFirst().orElse(null);
    }

    protected boolean checkConditions(Screen screen, Map<String, String> urlParams) {
        if (UrlHandlingMode.URL_ROUTES != uiProperties.getUrlHandlingMode()) {
            log.debug("UrlRouting is disabled for '{}' URL handling mode", uiProperties.getUrlHandlingMode());
            return false;
        }

        checkNotNullArgument(screen, "Screen cannot be null");
        checkNotNullArgument(urlParams, "Parameters cannot be null");

        if (notAttachedToUi(screen)) {
            log.info("Ignore changing of URL for not attached screen '{}'", screen.getId());
            return false;
        }

        return true;
    }

    protected boolean notAttachedToUi(Screen screen) {
        boolean notAttached;

        Screens.OpenedScreens openedScreens = ui.getScreens().getOpenedScreens();

        if (screen.getWindow() instanceof RootWindow) {
            Screen rootScreen = openedScreens.getRootScreenOrNull();
            notAttached = rootScreen == null || rootScreen != screen;
        } else if (screen.getWindow() instanceof DialogWindow) {
            notAttached = !openedScreens.getDialogScreens()
                    .contains(screen);
        } else {
            notAttached = !openedScreens.getActiveScreens()
                    .contains(screen);
        }

        return notAttached;
    }

    @Override
    public String getLastHistoryOperation() {
        return lastHistoryOperation;
    }

    protected boolean isNotFoundScreen(Screen screen) {
        return screen instanceof NotFoundScreen;
    }

    protected class RouteGeneratorImpl implements RouteGenerator {

        @Override
        public String getRoute(String screenId, Map<String, String> urlParams) {
            checkNotEmptyString(screenId, "Screen id cannot be empty");
            checkNotNullArgument(urlParams, "URL params cannot be null");

            WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);

            RouteDefinition screenRouteDef = windowInfo.getRouteDefinition();
            if (screenRouteDef == null || StringUtils.isEmpty(screenRouteDef.getPath())) {
                throw new IllegalStateException(
                        String.format("Unable to generate route for screen '%s' - no registered route found", screenId));
            }

            StringBuilder routeBuilder = new StringBuilder(ControllerUtils.getLocationWithoutParams());

            if (screenRouteDef.isRoot()) {
                routeBuilder.append('#')
                        .append(screenRouteDef.getPath());
            } else {
                Screen rootScreen = getOpenedScreens().getRootScreenOrNull();
                if (rootScreen == null) {
                    throw new IllegalStateException("Unable to generate route for non-root screen when no root screen exists");
                }

                RouteDefinition rootScreenRouteDef = getScreenContext(rootScreen).getWindowInfo().getRouteDefinition();
                if (rootScreenRouteDef == null || StringUtils.isEmpty(rootScreenRouteDef.getPath())) {
                    throw new IllegalStateException(
                            String.format("Unable to generate route - no registered route found for root screen: '%s'", rootScreen));
                }

                routeBuilder.append('#')
                        .append(rootScreenRouteDef.getPath())
                        .append('/')
                        .append(screenRouteDef.getPath());
            }

            if (!urlParams.isEmpty()) {
                String paramsString = urlParams.entrySet()
                        .stream()
                        .map(param -> String.format("%s=%s", param.getKey(), param.getValue()))
                        .collect(Collectors.joining("&"));

                routeBuilder.append('?')
                        .append(paramsString);
            }

            return routeBuilder.toString();
        }

        @Override
        public String getRoute(Class<? extends Screen> screenClass, Map<String, String> urlParams) {
            checkNotNullArgument(screenClass, "Screen class cannot be null");
            checkNotNullArgument(urlParams, "URL params cannot be null");

            return getRoute(getScreenId(screenClass), urlParams);
        }

        @Override
        public String getEditorRoute(Object entity, Map<String, String> urlParams) {
            checkNotNullArgument(entity, "Entity cannot be null");
            checkNotNullArgument(urlParams, "URL params cannot be null");

            String editorId = windowConfig.getEditorScreenId(metadata.getClass(entity));
            Map<String, String> params = prepareEditorUrlParams(entity, urlParams);

            return getRoute(editorId, params);
        }

        @Override
        public String getEditorRoute(Object entity, String screenId, Map<String, String> urlParams) {
            checkNotNullArgument(entity, "Entity cannot be null");
            checkNotEmptyString(screenId, "Editor screen id cannot be empty");
            checkNotNullArgument(urlParams, "URL params cannot be null");

            Map<String, String> params = prepareEditorUrlParams(entity, urlParams);

            return getRoute(screenId, params);
        }

        @Override
        public String getEditorRoute(Object entity, Class<? extends Screen> screenClass, Map<String, String> urlParams) {
            checkNotNullArgument(entity, "Entity cannot be null");
            checkNotNullArgument(screenClass, "Editor screen id cannot be empty");
            checkNotNullArgument(urlParams, "URL params cannot be null");

            String screenId = getScreenId(screenClass);
            Map<String, String> params = prepareEditorUrlParams(entity, urlParams);

            return getRoute(screenId, params);
        }

        protected String getScreenId(Class<? extends Screen> screenClass) {
            UiController uiController = screenClass.getAnnotation(UiController.class);
            if (uiController == null) {
                throw new IllegalArgumentException("No @UiController annotation for " + screenClass);
            }
            return UiDescriptorUtils.getInferredScreenId(uiController, screenClass);
        }

        protected Map<String, String> prepareEditorUrlParams(Object entity, Map<String, String> urlParams) {
            if (EntityValues.getId(entity) == null) {
                throw new IllegalArgumentException("Unable to generate route for an entity without id: " + entity);
            }

            Map<String, String> params = new LinkedHashMap<>(1 + urlParams.size());
            if (entityStates.isNew(entity)) {
                params.put("id", NEW_ENTITY_ID);
            } else {
                params.put("id", UrlIdSerializer.serializeId(EntityValues.getId(entity)));
            }
            params.putAll(urlParams);

            return params;
        }
    }
}
