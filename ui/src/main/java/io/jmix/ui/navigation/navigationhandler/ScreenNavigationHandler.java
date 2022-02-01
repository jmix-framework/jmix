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

import io.jmix.core.*;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.ui.AppUI;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.app.navigation.notfoundwindow.NotFoundScreen;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.navigation.*;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.ui.navigation.WebUrlRouting.NEW_ENTITY_ID;

@Component("ui_ScreenNavigationHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Order(JmixOrder.LOWEST_PRECEDENCE - 30)
public class ScreenNavigationHandler implements NavigationHandler {

    private static final Logger log = LoggerFactory.getLogger(ScreenNavigationHandler.class);

    protected static final int MAX_SUB_ROUTES = 2;

    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected AccessManager accessManager;

    @Override
    public boolean doHandle(NavigationState requestedState, AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();

        if (urlChangeHandler.isEmptyState(requestedState)
                || !isScreenChanged(requestedState, ui)) {
            return false;
        }

        String requestedRoute = requestedState.getNestedRoute();
        if (StringUtils.isEmpty(requestedRoute)) {
            log.info("Unable to handle state with empty route '{}'", requestedState);
            urlChangeHandler.revertNavigationState();

            return true;
        }

        String[] routeParts = {requestedRoute};
        if (windowConfig.findWindowInfoByRoute(requestedRoute) == null) {
            routeParts = requestedRoute.split("/");
        }

        if (routeParts.length > MAX_SUB_ROUTES) {
            log.info("Unable to perform navigation to requested state '{}'. Only {} sub routes are supported",
                    requestedRoute, MAX_SUB_ROUTES);
            urlChangeHandler.revertNavigationState();

            return true;
        }

        List<Pair<String, WindowInfo>> routeWindowInfos = Arrays.stream(routeParts)
                .map(subRoute -> new Pair<>(subRoute, windowConfig.findWindowInfoByRoute(subRoute)))
                .collect(Collectors.toList());

        for (Pair<String, WindowInfo> entry : routeWindowInfos) {
            WindowInfo routeWindowInfo = entry.getSecond();
            if (routeWindowInfo == null) {
                log.info("No registered screen found for route: '{}'", entry.getFirst());
                urlChangeHandler.revertNavigationState();

                handle404(entry.getFirst(), ui);

                return true;
            }

            if (urlChangeHandler.shouldRedirect(routeWindowInfo)) {
                urlChangeHandler.redirect(requestedState);
                return true;
            }

            if (urlChangeHandler.isRootRoute(routeWindowInfo)) {
                log.info("Unable navigate to '{}' as nested screen", routeWindowInfo.getId());
                urlChangeHandler.revertNavigationState();

                return true;
            }
        }

        return navigate(requestedState, ui, routeWindowInfos);
    }

    protected boolean navigate(NavigationState requestedState, AppUI ui, List<Pair<String, WindowInfo>> routeWindowInfos) {
        int subRouteIdx = 0;
        NavigationState currentState = ui.getHistory().getNow();

        for (Pair<String, WindowInfo> entry : routeWindowInfos) {
            String subRoute = entry.getFirst();

            if (skipSubRoute(requestedState, subRouteIdx, currentState, subRoute)) {
                subRouteIdx++;
                continue;
            }

            WindowInfo windowInfo = entry.getSecond();

            openScreen(requestedState, subRoute, windowInfo, ui);

            subRouteIdx++;
        }

        return true;
    }

    protected void handle404(String route, AppUI ui) {
        MapScreenOptions options = new MapScreenOptions(ParamsMap.of("requestedRoute", route));

        NotFoundScreen notFoundScreen = ui.getScreens()
                .create(NotFoundScreen.class, OpenMode.NEW_TAB, options);

        NavigationState state = new NavigationState(
                ui.getUrlRouting().getState().getRoot(),
                "",
                route,
                Collections.emptyMap());
        ((WindowImpl) notFoundScreen.getWindow())
                .setResolvedState(state);

        notFoundScreen.show();
    }

    protected boolean isScreenChanged(NavigationState requestedState, AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();

        if (urlChangeHandler.isEmptyState(requestedState)
                || urlChangeHandler.isRootState(requestedState)) {
            return false;
        }

        Screen currentScreen = urlChangeHandler.findActiveScreenByState(ui.getHistory().getNow());

        if (currentScreen == null) {
            Iterator<Screen> screensIterator = ui.getScreens()
                    .getOpenedScreens().getCurrentBreadcrumbs().iterator();
            currentScreen = screensIterator.hasNext()
                    ? screensIterator.next()
                    : null;

            if (currentScreen == null) {
                return true;
            }
        }

        NavigationState currentState = urlChangeHandler.getResolvedState(currentScreen);
        if (currentState == null) {
            return true;
        }

        return !Objects.equals(currentState.getStateMark(), requestedState.getStateMark())
                || !Objects.equals(currentState.getNestedRoute(), requestedState.getNestedRoute());
    }

    protected boolean skipSubRoute(NavigationState requestedState, int subRouteIdx, NavigationState currentState,
                                   String screenRoute) {
        if (!requestedState.asRoute().startsWith(currentState.asRoute() + '/')) {
            return false;
        }

        String[] currentRouteParts = currentState.getNestedRoute()
                .split("/");
        return subRouteIdx < currentRouteParts.length
                && currentRouteParts[subRouteIdx].equals(screenRoute);
    }

    protected void openScreen(NavigationState requestedState, String screenRoute, WindowInfo windowInfo, AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();

        if (!urlChangeHandler.isPermittedToNavigate(requestedState, windowInfo)) {
            return;
        }

        Screen screen = createScreen(requestedState, screenRoute, windowInfo, ui);

        if (screen == null) {
            log.info("Unable to open screen '{}' for requested route '{}'", windowInfo.getId(),
                    requestedState.getNestedRoute());

            urlChangeHandler.revertNavigationState();
            return;
        }

        if (requestedState.getNestedRoute().endsWith(screenRoute)) {
            Map<String, String> params = requestedState.getParams();
            if (MapUtils.isNotEmpty(params)) {
                UiControllerUtils.fireEvent(screen, UrlParamsChangedEvent.class,
                        new UrlParamsChangedEvent(screen, params));
            }

            ((WindowImpl) screen.getWindow())
                    .setResolvedState(requestedState);
        } else {
            ((WindowImpl) screen.getWindow())
                    .setResolvedState(getNestedScreenState(screenRoute, requestedState));
        }

        screen.show();
    }

    protected NavigationState getNestedScreenState(String screenRoute, NavigationState requestedState) {
        String nestedRoute = requestedState.getNestedRoute();
        String subRoute = screenRoute + '/';
        String nestedScreenRoute = nestedRoute.substring(0, nestedRoute.indexOf(subRoute) + subRoute.length() - 1);

        return new NavigationState(
                requestedState.getRoot(),
                "",
                nestedScreenRoute,
                Collections.emptyMap());
    }

    protected Screen createScreen(NavigationState requestedState, String screenRoute, WindowInfo windowInfo, AppUI ui) {
        Screen screen;

        if (isEditor(windowInfo)) {
            screen = createEditor(windowInfo, screenRoute, requestedState, ui);
        } else {
            OpenMode openMode = getScreenOpenMode(requestedState.getNestedRoute(), screenRoute, ui);
            screen = ui.getScreens().create(windowInfo.getId(), openMode);
        }

        return screen;
    }

    protected Screen createEditor(WindowInfo windowInfo, String screenRoute, NavigationState requestedState, AppUI ui) {
        Map<String, Object> options = createEditorScreenOptions(windowInfo, requestedState, ui);

        if (MapUtils.isEmpty(options)) {
            log.info("Unable to load entity for editor: '{}'. " +
                    "Subscribe for 'UrlParamsChangedEvent' to obtain its serialized id", windowInfo.getId());
        }

        OpenMode openMode = getScreenOpenMode(requestedState.getNestedRoute(), screenRoute, ui);
        Screen editor = doCreateEditor(windowInfo, ui, openMode, options);

        if (MapUtils.isNotEmpty(options)) {
            Object entity = options.get("item");
            //noinspection unchecked
            ((EditorScreen<Object>) editor).setEntityToEdit(entity);
        }

        return editor;
    }

    protected Screen doCreateEditor(WindowInfo windowInfo, AppUI ui, OpenMode openMode, Map<String, Object> options) {
        return ui.getScreens().create(windowInfo.getId(), openMode);
    }

    protected OpenMode getScreenOpenMode(String requestedRoute, String screenRoute, AppUI ui) {
        if (StringUtils.isEmpty(screenRoute)) {
            return OpenMode.NEW_TAB;
        }

        String currentRoute = ui.getHistory()
                .getNow()
                .getNestedRoute();

        return requestedRoute.startsWith(currentRoute + '/')
                ? OpenMode.THIS_TAB
                : OpenMode.NEW_TAB;
    }

    @Nullable
    protected Map<String, Object> createEditorScreenOptions(WindowInfo windowInfo, NavigationState requestedState, AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();

        String idParam = MapUtils.isNotEmpty(requestedState.getParams())
                // If no id was passed, open editor for creation
                ? requestedState.getParams().getOrDefault("id", NEW_ENTITY_ID)
                : NEW_ENTITY_ID;

        Class<?> entityClass = EditorTypeExtractor.extractEntityClass(windowInfo);
        if (entityClass == null) {
            return null;
        }

        MetaClass metaClass = metadata.getClass(entityClass);

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isViewPermitted()) {
            urlChangeHandler.revertNavigationState();
            throw new AccessDeniedException("entity", entityClass.getSimpleName(), "read");
        }

        if (NEW_ENTITY_ID.equals(idParam)) {
            if (!entityContext.isCreatePermitted()) {
                throw new AccessDeniedException("entity", entityClass.getSimpleName(), "create");
            }
            return ParamsMap.of("item", metadata.create(entityClass));
        }

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        if (primaryKeyProperty == null) {
            throw new IllegalStateException(String.format("Entity %s has no primary key", metaClass.getName()));
        }

        Class<?> idType = primaryKeyProperty.getJavaType();
        Object id = UrlIdSerializer.deserializeId(idType, idParam);

        LoadContext<?> ctx = new LoadContext(metaClass);
        ctx.setId(id);
        ctx.setFetchPlan(fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.INSTANCE_NAME));

        Object entity = dataManager.load(ctx);
        if (entity == null) {
            urlChangeHandler.revertNavigationState();
            throw new EntityAccessException(metaClass, id);
        }

        return ParamsMap.of("item", entity);
    }

    protected boolean isEditor(WindowInfo windowInfo) {
        return EditorScreen.class.isAssignableFrom(windowInfo.getControllerClass());
    }
}
