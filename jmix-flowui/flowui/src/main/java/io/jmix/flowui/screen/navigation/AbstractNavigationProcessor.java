package io.jmix.flowui.screen.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.sys.ScreenSupport;

public abstract class AbstractNavigationProcessor<N extends ScreenNavigator> {

    protected ScreenSupport screenSupport;
    protected ScreenRegistry screenRegistry;
    protected ScreenNavigationSupport navigationSupport;

    protected AbstractNavigationProcessor(ScreenSupport screenSupport,
                                          ScreenRegistry screenRegistry,
                                          ScreenNavigationSupport navigationSupport) {
        this.screenSupport = screenSupport;
        this.screenRegistry = screenRegistry;
        this.navigationSupport = navigationSupport;
    }

    public void processNavigation(N navigator) {
        Class<? extends Screen> screenClass = getScreenClass(navigator);
        RouteParameters routeParameters = getRouteParameters(navigator);
        QueryParameters queryParameters = getQueryParameters(navigator);

        navigator.getBackNavigationTarget().ifPresent(target ->
                screenSupport.registerBackNavigation(screenClass, target));

        navigationSupport.navigate(screenClass, routeParameters, queryParameters);
    }

    protected Class<? extends Screen> getScreenClass(N navigator) {
        if (navigator.getScreenId().isPresent()) {
            String screenId = navigator.getScreenId().get();
            return screenRegistry.getScreenInfo(screenId).getControllerClass();
        } else if (navigator.getScreenClass().isPresent()) {
            return navigator.getScreenClass().get();
        } else {
            return inferScreenClass(navigator);
        }
    }

    protected abstract Class<? extends Screen> inferScreenClass(N navigator);

    protected RouteParameters getRouteParameters(N navigator) {
        return navigator.getRouteParameters().orElse(RouteParameters.empty());
    }

    protected QueryParameters getQueryParameters(N navigator) {
        return navigator.getQueryParameters().orElse(QueryParameters.empty());
    }
}
