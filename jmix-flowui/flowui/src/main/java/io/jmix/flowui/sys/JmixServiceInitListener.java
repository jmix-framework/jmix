package io.jmix.flowui.sys;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.InternalServerError;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.internal.ErrorTargetEntry;
import com.vaadin.flow.server.*;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import io.jmix.flowui.component.error.JmixInternalServerError;
import io.jmix.flowui.exception.UiExceptionHandlers;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component("flowui_JmixServiceInitListener")
public class JmixServiceInitListener implements VaadinServiceInitListener, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(JmixServiceInitListener.class);

    protected ApplicationContext applicationContext;
    protected UiExceptionHandlers uiExceptionHandlers;
    protected ViewRegistry viewRegistry;

    public JmixServiceInitListener(UiExceptionHandlers uiExceptionHandlers,
                                   ViewRegistry viewRegistry) {
        this.uiExceptionHandlers = uiExceptionHandlers;
        this.viewRegistry = viewRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this::onSessionInitEvent);
        event.getSource().addUIInitListener(this::onUIInitEvent);

        // Vaadin scans only application packages by default. To enable scanning
        // Jmix packages, Vaadin provides @EnableVaadin() annotation, but it
        // should be defined only in one configuration as Spring cannot register bean with
        // the same name, see VaadinScanPackagesRegistrar#registerBeanDefinitions().
        // Register routes after route application scope is available.
        registerViewRoutes();

        registerInternalServiceError();
    }

    protected void onUIInitEvent(UIInitEvent uiInitEvent) {
        UI ui = uiInitEvent.getUI();
        // retrieve ExtendedClientDetails to be cached
        ui.getPage().retrieveExtendedClientDetails(extendedClientDetails -> {});
    }

    protected void onSessionInitEvent(SessionInitEvent event) {
        event.getSession().setErrorHandler(uiExceptionHandlers);
    }

    protected void registerViewRoutes() {
        RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();

        for (ViewInfo viewInfo : viewRegistry.getViewInfos()) {
            Class<? extends View<?>> controllerClass = viewInfo.getControllerClass();
            Route route = controllerClass.getAnnotation(Route.class);
            if (route == null) {
                continue;
            }

            if (routeConfiguration.isPathAvailable(route.value())
                    || routeConfiguration.isRouteRegistered(controllerClass)) {
                log.debug("Skipping route '{}' for class '{}' since it was already registered",
                        route.value(), controllerClass.getName());
                continue;
            }

            routeConfiguration.setRoute(route.value(), controllerClass);
        }
    }

    protected void registerInternalServiceError() {
        ApplicationRouteRegistry applicationRouteRegistry =
                ApplicationRouteRegistry.getInstance(VaadinService.getCurrent().getContext());

        Optional<ErrorTargetEntry> navigationTargetOpt = applicationRouteRegistry.
                getErrorNavigationTarget(new Exception());

        if (navigationTargetOpt.isPresent()) {
            ErrorTargetEntry errorTargetEntry = navigationTargetOpt.get();
            if (!errorTargetEntry.getNavigationTarget().equals(InternalServerError.class)) {
                log.debug("Internal server error handler is registered: "
                        + errorTargetEntry.getNavigationTarget().getName());
                return;
            }
        }

        applicationRouteRegistry.setErrorNavigationTargets(Collections.singleton(JmixInternalServerError.class));

        log.debug("Default internal server error handler is registered: " + JmixInternalServerError.class.getName());
    }
}
