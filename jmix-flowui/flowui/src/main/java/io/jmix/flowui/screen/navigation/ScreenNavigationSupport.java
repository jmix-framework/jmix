package io.jmix.flowui.screen.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.screen.ScreenInfo;
import io.jmix.flowui.screen.ScreenRegistry;

@org.springframework.stereotype.Component("flowui_ScreenNavigationSupport")
public class ScreenNavigationSupport {

    protected ScreenRegistry screenRegistry;

    public ScreenNavigationSupport(ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
    }

    public void navigate(Class<? extends Component> navigationTarget) {
        navigate(navigationTarget, RouteParameters.empty(), QueryParameters.empty());
    }

    public void navigate(Class<? extends Component> navigationTarget,
                         RouteParameters routeParameters) {
        navigate(navigationTarget, routeParameters, QueryParameters.empty());
    }

    public void navigate(Class<? extends Component> navigationTarget,
                         RouteParameters routeParameters,
                         QueryParameters queryParameters) {

        String url = getRouteConfiguration().getUrl(navigationTarget, routeParameters);
        UI.getCurrent().navigate(url, queryParameters);
    }

    public void navigate(String screenId) {
        navigate(screenId, RouteParameters.empty(), QueryParameters.empty());
    }

    public void navigate(String screenId, RouteParameters routeParameters) {
        navigate(screenId, routeParameters, QueryParameters.empty());
    }

    public void navigate(String screenId,
                         RouteParameters routeParameters,
                         QueryParameters queryParameters) {

        ScreenInfo screenInfo = screenRegistry.getScreenInfo(screenId);
        navigate(screenInfo.getControllerClass(), routeParameters, queryParameters);
    }

    public <T, C extends Component & HasUrlParameter<T>> void navigate(Class<? extends C> navigationTarget,
                                                                       T parameter) {

        navigate(navigationTarget, parameter, QueryParameters.empty());
    }

    public <T, C extends Component & HasUrlParameter<T>> void navigate(Class<? extends C> navigationTarget,
                                                                      T parameter,
                                                                      QueryParameters queryParameters) {
        String url = getRouteConfiguration().getUrl(navigationTarget, parameter);
        UI.getCurrent().navigate(url, queryParameters);
    }

    protected RouteConfiguration getRouteConfiguration() {
        return RouteConfiguration.forSessionScope();
    }
}
