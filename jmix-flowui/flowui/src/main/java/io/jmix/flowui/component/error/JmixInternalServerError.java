package io.jmix.flowui.component.error;

import com.vaadin.flow.router.*;
import com.vaadin.flow.server.ErrorEvent;
import io.jmix.flowui.exception.UiExceptionHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

public class JmixInternalServerError extends InternalServerError {

    private static final Logger log = LoggerFactory.getLogger(JmixInternalServerError.class);

    protected UiExceptionHandlers uiExceptionHandlers;

    public JmixInternalServerError(UiExceptionHandlers uiExceptionHandlers) {
        this.uiExceptionHandlers = uiExceptionHandlers;
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        forwardToPreviousView(event);

        uiExceptionHandlers.error(new ErrorEvent(parameter.getException()));

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    protected void forwardToPreviousView(BeforeEnterEvent event) {
        Location location = event.getLocation();
        if (location.getSegments().size() > 1) {
            event.forwardTo(location.getFirstSegment());
        } else {
            RouteConfiguration.forSessionScope().getRoute(location.getPath())
                    .ifPresentOrElse(
                            viewClass -> navigateToParentLayout(viewClass, event),
                            () -> log.info("Cannot navigate to the parent layout"));
        }
    }

    protected void navigateToParentLayout(Class<?> viewClass, BeforeEnterEvent event) {
        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        List<RouteData> routes = routeConfiguration.getAvailableRoutes();

        RouteData parentRouteData = findRouteData(viewClass, routes)
                .flatMap(routeData -> findRouteData(routeData.getParentLayout(), routes))
                .orElse(null);

        if (parentRouteData != null) {
            String redirectUrl = routeConfiguration.getUrl(parentRouteData.getNavigationTarget());
            event.forwardTo(redirectUrl);
            return;
        }

        log.info("Cannot navigate to the parent layout {}", viewClass.getName());
    }

    protected Optional<RouteData> findRouteData(Class<?> target, List<RouteData> routes) {
        return routes.stream()
                .filter(routeData -> target.equals(routeData.getNavigationTarget()))
                .findFirst();
    }
}
