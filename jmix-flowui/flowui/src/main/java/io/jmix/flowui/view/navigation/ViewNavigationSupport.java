package io.jmix.flowui.view.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;

@org.springframework.stereotype.Component("flowui_ViewNavigationSupport")
public class ViewNavigationSupport {

    protected ViewRegistry viewRegistry;

    public ViewNavigationSupport(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
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

    public void navigate(String viewId) {
        navigate(viewId, RouteParameters.empty(), QueryParameters.empty());
    }

    public void navigate(String viewId, RouteParameters routeParameters) {
        navigate(viewId, routeParameters, QueryParameters.empty());
    }

    public void navigate(String viewId,
                         RouteParameters routeParameters,
                         QueryParameters queryParameters) {

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        navigate(viewInfo.getControllerClass(), routeParameters, queryParameters);
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
