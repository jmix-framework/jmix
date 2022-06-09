package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.MessageTools;
import io.jmix.flowui.action.binder.ActionBinders;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.screen.*;
import io.jmix.flowui.screen.Screen.InitEvent;
import io.jmix.flowui.screen.navigation.ScreenNavigationSupport;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.ComponentLoaderContext;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.flowui.screen.UiControllerUtils.getPackage;


@Component("flowui_ScreenSupport")
public class ScreenSupport {

    private static final Logger log = LoggerFactory.getLogger(ScreenSupport.class);

    protected ApplicationContext applicationContext;
    protected ScreenXmlLoader screenXmlLoader;
    protected UiControllerDependencyManager dependencyManager;
    protected ScreenRegistry screenRegistry;
    protected ScreenNavigationSupport navigationSupport;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setScreenXmlLoader(ScreenXmlLoader screenXmlLoader) {
        this.screenXmlLoader = screenXmlLoader;
    }

    @Autowired
    public void setDependencyManager(UiControllerDependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    @Autowired
    public void setScreenRegistry(ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
    }

    @Autowired
    public void setNavigationSupport(ScreenNavigationSupport navigationSupport) {
        this.navigationSupport = navigationSupport;
    }

    public void initScreen(Screen<?> screen) {
        log.debug("Init screen: " + screen);

        UiControllerUtils.setScreenData(screen, applicationContext.getBean(ScreenData.class));

        ActionBinders actionBinders = applicationContext.getBean(ActionBinders.class);
        ScreenActions actions = applicationContext.getBean(ScreenActions.class, actionBinders.binder(screen));
        UiControllerUtils.setScreenActions(screen, actions);

        UiControllerUtils.setScreenFacets(screen, applicationContext.getBean(ScreenFacets.class, screen));

        String screenId = getInferredScreenId(screen);
        screen.setId(screenId);

        ScreenInfo screenInfo = screenRegistry.getScreenInfo(screenId);

        ComponentLoaderContext componentLoaderContext = createComponentLoaderContext();

        componentLoaderContext.setFullFrameId(screenInfo.getId());
        componentLoaderContext.setCurrentFrameId(screenInfo.getId());
        // TODO: gg, consider messageGroup attribute
        componentLoaderContext.setMessageGroup(getPackage(screenInfo.getControllerClass()));
        componentLoaderContext.setScreen(screen);
        componentLoaderContext.setScreenActions(actions);

        Element element = loadScreenXml(screenInfo);
        if (element != null) {
            loadMessageGroup(element)
                    .ifPresent(componentLoaderContext::setMessageGroup);
            loadWindowFromXml(element, screen, componentLoaderContext);
        }

        UiControllerDependencyManager dependencyManager =
                applicationContext.getBean(UiControllerDependencyManager.class);
        dependencyManager.inject(screen);

        componentLoaderContext.executeInitTasks();

        fireScreenInitEvent(screen);
    }

    public void registerBackNavigation(Class<? extends Screen> screenClass,
                                       Class<? extends Screen> backNavigationTarget) {
        registerBackNavigation(UI.getCurrent(), screenClass, backNavigationTarget);
    }

    public void registerBackNavigation(UI ui,
                                       Class<? extends Screen> screenClass,
                                       Class<? extends Screen> backNavigationTarget) {
        retrieveExtendedClientDetails(ui, details ->
                registerBackNavigation(ui.getSession(), details.getWindowName(),
                        screenClass, backNavigationTarget));
    }

    protected void registerBackNavigation(VaadinSession session, String windowName,
                                          Class<? extends Screen> screenClass,
                                          Class<? extends Screen> backNavigationTarget) {
        log.debug(String.format("Register back navigation for '%s' with back target '%s'",
                screenClass, backNavigationTarget));

        BackNavigationTargets targets = session.getAttribute(BackNavigationTargets.class);
        if (targets == null) {
            targets = new BackNavigationTargets();
        }

        targets.put(windowName, new Pair<>(screenClass, backNavigationTarget));
        session.setAttribute(BackNavigationTargets.class, targets);
    }

    public void unregisterBackNavigation(Screen<?> screen) {
        UI ui = screen.getUI().orElse(UI.getCurrent());
        unregisterBackNavigation(ui, screen.getClass());
    }

    public void unregisterBackNavigation(Class<? extends Screen> screenClass) {
        unregisterBackNavigation(UI.getCurrent(), screenClass);
    }

    public void unregisterBackNavigation(UI ui, Class<? extends Screen> screenClass) {
        log.debug(String.format("Unregister back navigation for '%s'", screenClass));

        if (!hasBackNavigationTarget(ui, screenClass)) {
            return;
        }

        retrieveExtendedClientDetails(ui, details ->
                unregisterBackNavigation(ui.getSession(), details.getWindowName()));
    }

    protected void unregisterBackNavigation(VaadinSession session, String windowName) {
        BackNavigationTargets targets = session.getAttribute(BackNavigationTargets.class);
        if (targets != null) {
            targets.remove(windowName);
        }
    }

    /**
     * Gets localized page title from the screen.
     *
     * @param screen screen to get localized page title
     * @return localized page title or message key if not found or empty string if message key is not defined
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedPageTitle(Screen<?> screen) {
        String title = UiControllerUtils.getPageTitle(screen);

        if (!Strings.isNullOrEmpty(title)) {
            MessageTools messageTools = applicationContext.getBean(MessageTools.class);
            String messagesGroup = UiControllerUtils.getPackage(screen.getClass());

            String screenId = screen.getId().orElse("");
            if (!Strings.isNullOrEmpty(screenId)) {
                ScreenInfo screenInfo = screenRegistry.getScreenInfo(screenId);
                Element element = loadScreenXml(screenInfo);
                if (element != null) {
                    messagesGroup = loadMessageGroup(element).orElse(messagesGroup);
                }
            }

            if (!title.contains(MessageTools.MARK)) {
                return messageTools.loadString(messagesGroup, MessageTools.MARK + title);
            }

            return messageTools.loadString(messagesGroup, title);
        }

        String screenId = screen.getId().orElse("");
        if (Strings.isNullOrEmpty(screenId)) {
            return "";
        }

        ScreenInfo screenInfo = screenRegistry.getScreenInfo(screenId);
        Element element = loadScreenXml(screenInfo);
        if (element != null) {
            MessageTools messageTools = applicationContext.getBean(MessageTools.class);

            title = element.attributeValue("title");
            if (Strings.isNullOrEmpty(title)) {
                return "";
            }

            String messagesGroup = loadMessageGroup(element)
                    .orElse(UiControllerUtils.getPackage(screen.getClass()));

            return messageTools.loadString(messagesGroup, title);
        }

        return "";
    }

    public void close(Screen<?> screen) {
        UI ui = screen.getUI().orElse(UI.getCurrent());
        close(ui, screen);
    }

    public void close(UI ui, Screen<?> screen) {
        log.debug("Close screen: " + screen);

        // Check if a back navigation target for the given screen class is registered
        // for any window name before we obtain ExtendedClientDetails
        if (hasBackNavigationTarget(ui, screen.getClass())) {
            navigateToBackTarget(ui, screen);
        } else {
            navigateToParentLayout(screen);
        }
    }

    protected boolean hasBackNavigationTarget(UI ui, Class<? extends Screen> screenClass) {
        VaadinSession vaadinSession = ui.getSession();
        BackNavigationTargets targets = vaadinSession.getAttribute(BackNavigationTargets.class);

        return targets != null && targets.values().stream()
                .allMatch(pair -> pair.getFirst().equals(screenClass));
    }

    protected void navigateToBackTarget(UI ui, Screen<?> screen) {
        retrieveExtendedClientDetails(ui, details ->
                navigateToBackTarget(ui.getSession(), details.getWindowName(), screen));
    }

    protected void navigateToBackTarget(VaadinSession session, String windowName, Screen<?> screen) {
        BackNavigationTargets targets = session.getAttribute(BackNavigationTargets.class);
        if (targets != null && targets.containsKey(windowName)
                && targets.get(windowName).getFirst().equals(screen.getClass())) {
            Class<? extends Screen> backTarget = targets.get(windowName).getSecond();
            targets.remove(windowName);
            navigationSupport.navigate(backTarget);
        } else {
            navigateToParentLayout(screen);
        }
    }

    protected void navigateToParentLayout(Screen<?> screen) {
        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        List<RouteData> routes = routeConfiguration.getAvailableRoutes();

        findRouteData(screen.getClass(), routes)
                .ifPresent(routeData -> {
                    Class<? extends RouterLayout> parentLayout = routeData.getParentLayout();

                    findRouteData(parentLayout, routes)
                            .ifPresent(data ->
                                    navigationSupport.navigate(data.getNavigationTarget()));
                });
    }

    protected Optional<RouteData> findRouteData(Class<?> target, List<RouteData> routes) {
        return routes.stream()
                .filter(routeData ->
                        target.equals(routeData.getNavigationTarget()))
                .findFirst();
    }

    protected void retrieveExtendedClientDetails(UI ui,
                                                 Consumer<ExtendedClientDetails> details) {
        ui.getPage().retrieveExtendedClientDetails(details::accept);
    }

    protected String getInferredScreenId(Screen<?> screen) {
        Class<? extends Screen> screenClass = screen.getClass();

        UiController uiController = screenClass.getAnnotation(UiController.class);
        if (uiController == null) {
            throw new IllegalArgumentException("No @" + UiController.class.getSimpleName() +
                    " annotation for class " + screenClass);
        }

        return UiDescriptorUtils.getInferredScreenId(uiController, screenClass);
    }

    protected void fireScreenInitEvent(Screen<?> screen) {
        UiControllerUtils.fireEvent(screen, new InitEvent(screen));
    }

    @Nullable
    protected Element loadScreenXml(ScreenInfo screenInfo) {
        Optional<String> templatePath = screenInfo.getTemplatePath();
        return templatePath.map(s -> screenXmlLoader.load(s)).orElse(null);
    }

    protected Optional<String> loadMessageGroup(Element element) {
        String messageGroup = element.attributeValue("messagesGroup");
        return Optional.ofNullable(messageGroup);
    }

    protected ComponentLoaderContext createComponentLoaderContext() {
        return new ComponentLoaderContext();
    }

    protected void loadWindowFromXml(Element element, Screen<?> screen, ComponentLoaderContext context) {
        LayoutLoader layoutLoader = applicationContext.getBean(LayoutLoader.class, context);
        ComponentLoader<Screen<?>> screenLoader = layoutLoader.createScreenContent(screen, element);

        screenLoader.loadComponent();
    }

    // maps window.name to (screen class, back navigation target)
    protected static class BackNavigationTargets
            extends HashMap<String, Pair<Class<? extends Screen>, Class<? extends Screen>>> {
    }
}
