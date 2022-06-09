package io.jmix.flowui.sys;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.jmix.flowui.exception.UiExceptionHandlers;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenInfo;
import io.jmix.flowui.screen.ScreenRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("flowui_JmixServiceInitListener")
public class JmixServiceInitListener implements VaadinServiceInitListener, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(JmixServiceInitListener.class);

    protected ApplicationContext applicationContext;
    protected UiExceptionHandlers uiExceptionHandlers;
    protected ScreenRegistry screenRegistry;

    public JmixServiceInitListener(UiExceptionHandlers uiExceptionHandlers,
                                   ScreenRegistry screenRegistry) {
        this.uiExceptionHandlers = uiExceptionHandlers;
        this.screenRegistry = screenRegistry;
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
        registerScreenRoutes();
    }

    protected void onUIInitEvent(UIInitEvent uiInitEvent) {
        UI ui = uiInitEvent.getUI();
        // retrieve ExtendedClientDetails to be cached
        ui.getPage().retrieveExtendedClientDetails(extendedClientDetails -> {});
    }

    protected void onSessionInitEvent(SessionInitEvent event) {
        event.getSession().setErrorHandler(uiExceptionHandlers);
    }

    protected void registerScreenRoutes() {
        RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();

        for (ScreenInfo screenInfo : screenRegistry.getScreens()) {
            Class<? extends Screen<?>> controllerClass = screenInfo.getControllerClass();
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
}
