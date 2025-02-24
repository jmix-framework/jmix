/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.internal.UIInternalUpdater;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.NodeProperties;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.internal.ErrorStateRenderer;
import com.vaadin.flow.router.internal.ErrorTargetEntry;
import com.vaadin.flow.router.internal.HasUrlParameterFormat;
import com.vaadin.flow.router.internal.PathUtil;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import com.vaadin.flow.spring.annotation.SpringComponent;
import elemental.json.JsonValue;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.builder.ViewOpeningContext;
import io.jmix.tabbedmode.navigation.RedirectHandler;
import io.jmix.tabbedmode.view.ViewOpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SpringComponent
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixUI extends UI {

//    public static final String STORAGE_KEY = "jmixUi";

    private static final Logger log = LoggerFactory.getLogger(JmixUI.class);

    private int uiId = -1;

    private boolean navigationInProgress = false;

//    protected String jmixUiId = null;

    protected final Views views;

    protected View<?> topLevelView;

    protected UIInternalUpdater internalsHandler;

    protected RedirectHandler redirectHandler;

    @Autowired
    public JmixUI(Views views) {
        this(views, new UIInternalUpdater() {
        });
    }

    protected JmixUI(Views views, UIInternalUpdater internalsHandler) {
        super(internalsHandler);

        this.internalsHandler = internalsHandler;
        this.views = views;
        this.redirectHandler = new RedirectHandler(this, views);
    }

    @Override
    public int getUIId() {
        return uiId;
    }

    static final String SERVER_CONNECTED = "this.serverConnected($0)";

    private NavigationState clientViewNavigationState;

    private String forwardToClientUrl = null;

    private boolean firstNavigation = true;

    @Nullable
    public static JmixUI getCurrent() {
        return (JmixUI) UI.getCurrent();
    }

    @Override
    public String getForwardToClientUrl() {
        return forwardToClientUrl;
    }

    @Override
    public void doInit(VaadinRequest request, int uiId, String appId) {
        if (this.uiId != -1) {
            String message = "This UI instance is already initialized (as UI id "
                    + this.uiId
                    + ") and can therefore not be initialized again (as UI id "
                    + uiId + "). ";

            if (getSession() != null
                    && !getSession().equals(VaadinSession.getCurrent())) {
                message += "Furthermore, it is already attached to another VaadinSession. ";
            }
            message += "Please make sure you are not accidentally reusing an old UI instance.";

            throw new IllegalStateException(message);
        }
        this.uiId = uiId;

        // TODO: gg, wait until fetched
        /*WebStorage.getItem(WebStorage.Storage.SESSION_STORAGE, TabbedUI.STORAGE_KEY, value -> {
            if (value == null) {
                jmixUiId = UUID.randomUUID().toString();
                WebStorage.setItem(this, WebStorage.Storage.SESSION_STORAGE, TabbedUI.STORAGE_KEY, jmixUiId);
            } else {
                jmixUiId = value;
            }
        });*/

        getInternals().setFullAppId(appId);

        if (this.isNavigationSupported()) {
            // Create flow reference for the client outlet element
            wrapperElement = new Element(getInternals().getContainerTag());

            // Connect server with client
            getElement().getStateProvider().appendVirtualChild(
                    getElement().getNode(), wrapperElement,
                    NodeProperties.INJECT_BY_ID, appId);

            getEventBus().addListener(JmixBrowserLeaveNavigationEvent.class,
                    this::leaveNavigation);
            getEventBus().addListener(JmixBrowserNavigateEvent.class,
                    this::browserNavigate);

        }

        // Add any dependencies from the UI class
        getInternals().addComponentDependencies(getClass());

        // Call the init overridden by the application developer
        init(request);
    }

    @Override
    public <T, C extends Component & HasUrlParameter<T>> Optional<C> navigate(Class<? extends C> navigationTarget,
                                                                              T parameter,
                                                                              QueryParameters queryParameters) {
        log.debug("navigate: {}; parameter: {}; queryParameters: {}", navigationTarget, parameter, queryParameters);
        // TODO: gg, override
//        return super.navigate(navigationTarget, parameter, queryParameters);
        RouteParameters parameters = HasUrlParameterFormat.getParameters(parameter);
        return navigate(navigationTarget, parameters, queryParameters);
    }

    @Override
    public <T extends Component> Optional<T> navigate(Class<? extends T> navigationTarget,
                                                      QueryParameters queryParameters) {
        log.debug("navigate: '{}'; queryParameters: '{}'", navigationTarget, queryParameters);
        // TODO: gg, override
//        return super.navigate(navigationTarget, queryParameters);
        return navigate(navigationTarget, RouteParameters.empty(), queryParameters);
    }

    @Override
    public <C extends Component> Optional<C> navigate(Class<? extends C> navigationTarget,
                                                      RouteParameters routeParameter,
                                                      QueryParameters queryParameters) {
        log.debug("navigate: '{}'; routeParameter: '{}'; queryParameters: '{}'", navigationTarget, routeParameter, queryParameters);
        // TODO: gg, override
//        return super.navigate(navigationTarget, routeParameter, queryParameters);
        RouteConfiguration configuration = RouteConfiguration
                .forRegistry(getInternals().getRouter().getRegistry());
        String url = configuration.getUrl(navigationTarget, routeParameter);
        navigate(url, queryParameters);

        return (Optional<C>) findCurrentNavigationTarget(navigationTarget);
    }

    // TODO: gg, duplicate
    private <T extends Component> Optional<T> findCurrentNavigationTarget(
            Class<T> navigationTarget) {
        List<HasElement> activeRouterTargetsChain = getInternals()
                .getActiveRouterTargetsChain();
        for (HasElement element : activeRouterTargetsChain) {
            if (navigationTarget.isAssignableFrom(element.getClass())) {
                return Optional.of((T) element);
            }
        }
        return Optional.empty();
    }

    @Override
    public void navigate(String locationString, QueryParameters queryParameters) {
        log.debug("navigate: '{}'; queryParameters: '{}'", locationString, queryParameters);

        Objects.requireNonNull(locationString, "Location must not be null");
        Objects.requireNonNull(queryParameters,
                "Query parameters must not be null");
        Location location = new Location(locationString, queryParameters);

        // There is an in-progress navigation or there are no changes,
        // prevent looping
        if (navigationInProgress
                || (getInternals().hasLastHandledLocation() && sameLocation(
                getInternals().getLastHandledLocation(), location))) {
            return;
        }

        navigationInProgress = true;
        try {
            Optional<NavigationState> navigationState = getInternals()
                    .getRouter().resolveNavigationTarget(location);

            if (navigationState.isPresent()) {
                // Navigation can be done in server side without extra
                // round-trip
                handleNavigation(location, navigationState.get(),
                        NavigationTrigger.UI_NAVIGATE);
                if (getForwardToClientUrl() != null) {
                    // Server is forwarding to a client route from a
                    // BeforeEnter.
                    navigateToClient(getForwardToClientUrl());
                }
            } else {
                // Server cannot resolve navigation, let client-side to
                // handle it.
                navigateToClient(location.getPathWithQueryParameters());
            }
        } finally {
            navigationInProgress = false;
        }
    }

    @DomEvent(BrowserLeaveNavigationEvent.EVENT_NAME)
    public static class JmixBrowserLeaveNavigationEvent extends BrowserLeaveNavigationEvent {

        public static final String EVENT_NAME = "ui-leave-navigation";

        private final String route;
        private final String query;

        /**
         * Creates a new event instance.
         *
         * @param route the route the user is navigating to.
         * @param query the query string the user is navigating to.
         */
        public JmixBrowserLeaveNavigationEvent(UI source, boolean fromClient,
                                               @EventData("route") String route,
                                               @EventData("query") String query) {
            super(source, fromClient, route, query);
            this.route = route;
            this.query = query;
        }
    }

    @DomEvent(BrowserNavigateEvent.EVENT_NAME)
    public static class JmixBrowserNavigateEvent extends BrowserNavigateEvent {

        public static final String EVENT_NAME = "ui-navigate";

        private final String route;
        private final String query;
        private final String appShellTitle;
        private final JsonValue historyState;
        private final String trigger;

        public JmixBrowserNavigateEvent(UI source, boolean fromClient,
                                        @EventData("route") String route,
                                        @EventData("query") String query,
                                        @EventData("appShellTitle") String appShellTitle,
                                        @EventData("historyState") JsonValue historyState,
                                        @EventData("trigger") String trigger) {
            super(source, fromClient, route, query, appShellTitle, historyState, trigger);
            this.route = route;
            this.query = query;
            this.appShellTitle = appShellTitle;
            this.historyState = historyState;
            this.trigger = trigger;
        }

        @Override
        public String toString() {
            return "JmixBrowserNavigateEvent{" +
                    "route='" + route + '\'' +
                    ", query='" + query + '\'' +
                    ", appShellTitle='" + appShellTitle + '\'' +
                    ", historyState=" + historyState +
                    ", trigger='" + trigger + '\'' +
                    '}';
        }
    }

    @Override
    @Deprecated
    public void connectClient(String flowRoutePath, String flowRouteQuery,
                              String appShellTitle, JsonValue historyState, String trigger) {
        browserNavigate(new JmixBrowserNavigateEvent(this, false, flowRoutePath,
                flowRouteQuery, appShellTitle, historyState, trigger));
    }

    /**
     * Connect a client with the server side UI. This method is invoked each
     * time client router navigates to a server route.
     *
     * @param event the event from the browser
     */
//    @Override
    public void browserNavigate(JmixBrowserNavigateEvent event) {
        // first load, page refresh
//        super.browserNavigate(event);
        log.debug("browser navigate: " + event);
        if (event.appShellTitle != null && !event.appShellTitle.isEmpty()) {
            getInternals().setAppShellTitle(event.appShellTitle);
        }

        final String trimmedRoute = PathUtil.trimPath(event.route);
        final Location location = new Location(trimmedRoute,
                QueryParameters.fromString(event.query));
        NavigationTrigger navigationTrigger;
        if (event.trigger.isEmpty()) {
            navigationTrigger = NavigationTrigger.PAGE_LOAD;
        } else if (event.trigger.equalsIgnoreCase("link")) {
            navigationTrigger = NavigationTrigger.ROUTER_LINK;
        } else if (event.trigger.equalsIgnoreCase("client")) {
            navigationTrigger = NavigationTrigger.CLIENT_SIDE;
        } else {
            navigationTrigger = NavigationTrigger.HISTORY;
        }
        if (firstNavigation) {
            firstNavigation = false;
            getPage().getHistory().setHistoryStateChangeHandler(
                    e -> renderViewForRoute(e.getLocation(), e.getTrigger()));

            if (getInternals().getActiveRouterTargetsChain().isEmpty()) {
                // Render the route unless it was rendered eagerly
                renderViewForRoute(location, navigationTrigger);
            }
        } else {
            History.HistoryStateChangeHandler handler = getPage().getHistory()
                    .getHistoryStateChangeHandler();
            handler.onHistoryStateChange(
                    new History.HistoryStateChangeEvent(getPage().getHistory(),
                            event.historyState, location, navigationTrigger));
        }

        // true if the target is client-view and the push mode is disable
        if (getForwardToClientUrl() != null) {
            navigateToClient(getForwardToClientUrl());
            acknowledgeClient();
        } else if (isPostponed()) {
            serverPaused();
        } else {
            // acknowledge client, but cancel if session not open
            serverConnected(
                    !getSession().getState().equals(VaadinSessionState.OPEN));
            replaceStateIfDiffersAndNoReplacePending(event.route, location);
        }
    }

    /**
     * Do a history replaceState if the trimmed route differs from the event
     * route and there is no pending replaceState command.
     *
     * @param route    the event.route
     * @param location the location with the trimmed route
     */
    private void replaceStateIfDiffersAndNoReplacePending(String route,
                                                          Location location) {
        boolean locationChanged = !location.getPath().equals(route)
                && route.startsWith("/")
                && !location.getPath().equals(route.substring(1));
        boolean containsPendingReplace = !getInternals()
                .containsPendingJavascript("window.history.replaceState")
                && !getInternals().containsPendingJavascript(
                "'vaadin-navigate', { detail: { state: $0, url: $1, replace: true } }");
        if (locationChanged && containsPendingReplace) {
            // See InternalRedirectHandler invoked via Router.
            getPage().getHistory().replaceState(null, location);
        }
    }

    @Override
    @Deprecated
    public void leaveNavigation(String route, String query) {
        leaveNavigation(
                new JmixBrowserLeaveNavigationEvent(this, false, route, query));
    }

    /**
     * Check that the view can be leave. This method is invoked when the client
     * router tries to navigate to a client route while the current route is a
     * server route.
     * <p>
     * This is only called when client route navigates from a server to a client
     * view.
     *
     * @param event the event from the browser
     */
    public void leaveNavigation(JmixBrowserLeaveNavigationEvent event) {
        navigateToPlaceholder(new Location(PathUtil.trimPath(event.route),
                QueryParameters.fromString(event.query)));

        // Inform the client whether the navigation should be postponed
        if (isPostponed()) {
            cancelClient();
        } else {
            acknowledgeClient();
        }
    }

    private void acknowledgeClient() {
        serverConnected(false);
    }

    private void cancelClient() {
        serverConnected(true);
    }

    private void serverPaused() {
        wrapperElement.executeJs("this.serverPaused()");
    }

    private void serverConnected(boolean cancel) {
        wrapperElement.executeJs(SERVER_CONNECTED, cancel);
    }

    private void navigateToPlaceholder(Location location) {
        if (clientViewNavigationState == null) {
            clientViewNavigationState = new NavigationStateBuilder(
                    getInternals().getRouter())
                    .withTarget(ClientViewPlaceholder.class).build();
        }
        // Passing the `clientViewLocation` to make sure that the navigation
        // events contain the correct location that we are navigating to.
        handleNavigation(location, clientViewNavigationState,
                NavigationTrigger.CLIENT_SIDE);
    }

    private void renderViewForRoute(Location location,
                                    NavigationTrigger trigger) {
        log.debug("renderViewForRoute: " + location.getPath() + "; trigger: " + trigger);
        if (!shouldHandleNavigation(location)) {
            return;
        }
        getInternals().setLastHandledNavigation(location);
        Optional<NavigationState> navigationState = getInternals().getRouter()
                .resolveNavigationTarget(location);
        if (navigationState.isPresent()) {
            // There is a valid route in flow.
            handleNavigation(location, navigationState.get(), trigger);
        } else {
            // When route does not exist, try to navigate to current route
            // in order to check if current view can be left before showing
            // the error page
            navigateToPlaceholder(location);

            if (!isPostponed()) {
                // Route does not exist, and current view does not prevent
                // navigation thus an error page is shown
                handleErrorNavigation(location);
            }

        }
    }

    private boolean shouldHandleNavigation(Location location) {
        return !getInternals().hasLastHandledLocation()
                || !sameLocation(getInternals().getLastHandledLocation(),
                location);
    }

    private boolean sameLocation(Location oldLocation, Location newLocation) {
        return PathUtil.trimPath(newLocation.getPathWithQueryParameters())
                .equals(PathUtil
                        .trimPath(oldLocation.getPathWithQueryParameters()));
    }

    private void handleNavigation(Location location,
                                  NavigationState navigationState, NavigationTrigger trigger) {
        log.debug("handleNavigation: '{}; trigger: '{}", location.getPath(), trigger);
        try {
            // Jmix API
            Class<? extends Component> navigationTarget = navigationState.getNavigationTarget();
            // Don't throw exception for placeholder, just skip it
            if (UI.ClientViewPlaceholder.class.isAssignableFrom(navigationTarget)) {
                return;
            }

            if (!View.class.isAssignableFrom(navigationTarget)) {
                throw new IllegalArgumentException("navigationTarget '%s' is not a %s"
                        .formatted(navigationTarget.getName(), View.class.getSimpleName()));
            }

            //noinspection unchecked
            Class<? extends View<?>> viewClass = (Class<? extends View<?>>) navigationTarget;
            ViewOpenMode openMode = inferOpenMode(viewClass);

            if (!ViewOpenMode.ROOT.equals(openMode)
                    && topLevelView == null) {
                renderTopLevelView();
            }

            View<?> view = views.create(viewClass);

            views.open(this, ViewOpeningContext.create(view, openMode)
                    .withRouteParameters(navigationState.getRouteParameters())
                    .withQueryParameters(location.getQueryParameters())
                    .withCheckMultipleOpen(true));

        } catch (Exception exception) {
            handleExceptionNavigation(location, exception);
        } finally {
            getInternals().clearLastHandledNavigation();
        }
    }

    protected ViewOpenMode inferOpenMode(Class<? extends View<?>> viewClass) {
        Route route = viewClass.getAnnotation(Route.class);
        if (route == null) {
            return ViewOpenMode.NEW_TAB;
        }

        if (route.layout().equals(UI.class)) {
            return ViewOpenMode.ROOT;
        }

        return ViewOpenMode.NEW_TAB;
    }

    private boolean handleExceptionNavigation(Location location,
                                              Exception exception) {
        Optional<ErrorTargetEntry> maybeLookupResult = getInternals()
                .getRouter().getErrorNavigationTarget(exception);
        if (maybeLookupResult.isPresent()) {
            ErrorTargetEntry lookupResult = maybeLookupResult.get();

            ErrorParameter<?> errorParameter = new ErrorParameter<>(
                    lookupResult.getHandledExceptionType(), exception,
                    exception.getMessage());
            ErrorStateRenderer errorStateRenderer = new ErrorStateRenderer(
                    new NavigationStateBuilder(getInternals().getRouter())
                            .withTarget(lookupResult.getNavigationTarget())
                            .build());

            ErrorNavigationEvent errorNavigationEvent = new ErrorNavigationEvent(
                    getInternals().getRouter(), location, this,
                    NavigationTrigger.CLIENT_SIDE, errorParameter);

            errorStateRenderer.handle(errorNavigationEvent);
        } else {
            throw new RuntimeException(exception);
        }
        return isPostponed();
    }

    private boolean isPostponed() {
        return getInternals().getContinueNavigationAction() != null;
    }

    private void handleErrorNavigation(Location location) {
        NavigationState errorNavigationState = getInternals().getRouter()
                .resolveRouteNotFoundNavigationTarget()
                .orElse(getDefaultNavigationError());
        ErrorStateRenderer errorStateRenderer = new ErrorStateRenderer(
                errorNavigationState);
        NotFoundException notFoundException = new NotFoundException(
                "Couldn't find route for '" + location.getPath() + "'");
        ErrorParameter<NotFoundException> errorParameter = new ErrorParameter<>(
                NotFoundException.class, notFoundException);
        ErrorNavigationEvent errorNavigationEvent = new ErrorNavigationEvent(
                getInternals().getRouter(), location, this,
                NavigationTrigger.CLIENT_SIDE, errorParameter);
        errorStateRenderer.handle(errorNavigationEvent);
    }

    private NavigationState getDefaultNavigationError() {
        return new NavigationStateBuilder(getInternals().getRouter())
                .withTarget(RouteNotFoundError.class).build();
    }

    /* Jmix API */

    public RedirectHandler getRedirectHandler() {
        return redirectHandler;
    }

    public View<?> getTopLevelView() {
        if (topLevelView == null) {
            throw new IllegalStateException("UI topLevelView is null");
        }

        return topLevelView;
    }

    public Optional<View<?>> getTopLevelViewOptional() {
        return Optional.ofNullable(topLevelView);
    }

    public void setTopLevelView(@Nullable View<?> topLevelView) {
        if (this.topLevelView != topLevelView) {
            HasElement oldRoot = this.topLevelView;
            this.topLevelView = topLevelView;

            // Probably 'wrapperElement' contains placeholder
            if (oldRoot == null
                    && wrapperElement.getChildren().findAny().isPresent()) {
               wrapperElement.removeAllChildren();
            }

            internalsHandler.updateRoot(this, oldRoot, topLevelView);
        }
    }

    @Override
    public View<?> getCurrentView() {
        return views.getCurrentView(this);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
    }

    @Override
    public void removeRouterLayoutContent(HasElement oldContent) {
        super.removeRouterLayoutContent(oldContent);
    }

    protected void renderTopLevelView() {
        String topLevelViewId = inferTopLevelWindowId();
        View<?> topLevelView = views.create(topLevelViewId);
        views.open(topLevelView, ViewOpenMode.ROOT);
    }

    protected String inferTopLevelWindowId() {
        return isAnonymousAuthentication()
                ? views.getLoginViewId()
                : views.getMainViewId();
    }

    protected boolean isAnonymousAuthentication() {
        Authentication authentication = SecurityContextHelper.getAuthentication();
        return authentication == null ||
                authentication instanceof AnonymousAuthenticationToken;
    }
}
