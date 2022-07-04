package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.MessageTools;
import io.jmix.flowui.action.binder.ActionBinders;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.View.InitEvent;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
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

import static io.jmix.flowui.view.UiControllerUtils.getPackage;


@Component("flowui_ViewSupport")
public class ViewSupport {

    private static final Logger log = LoggerFactory.getLogger(ViewSupport.class);

    protected ApplicationContext applicationContext;
    protected ViewXmlLoader viewXmlLoader;
    protected UiControllerDependencyManager dependencyManager;
    protected ViewRegistry viewRegistry;
    protected ViewNavigationSupport navigationSupport;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setViewXmlLoader(ViewXmlLoader viewXmlLoader) {
        this.viewXmlLoader = viewXmlLoader;
    }

    @Autowired
    public void setDependencyManager(UiControllerDependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    @Autowired
    public void setViewRegistry(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Autowired
    public void setNavigationSupport(ViewNavigationSupport navigationSupport) {
        this.navigationSupport = navigationSupport;
    }

    public void initView(View<?> view) {
        log.debug("Init view: " + view);

        UiControllerUtils.setViewData(view, applicationContext.getBean(ViewData.class));

        ActionBinders actionBinders = applicationContext.getBean(ActionBinders.class);
        ViewActions actions = applicationContext.getBean(ViewActions.class, actionBinders.binder(view));
        UiControllerUtils.setViewActions(view, actions);

        UiControllerUtils.setViewFacets(view, applicationContext.getBean(ViewFacets.class, view));

        String viewId = getInferredViewId(view);
        view.setId(viewId);

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);

        ComponentLoaderContext componentLoaderContext = createComponentLoaderContext();

        componentLoaderContext.setFullFrameId(viewInfo.getId());
        componentLoaderContext.setCurrentFrameId(viewInfo.getId());
        // TODO: gg, consider messageGroup attribute
        componentLoaderContext.setMessageGroup(getPackage(viewInfo.getControllerClass()));
        componentLoaderContext.setView(view);
        componentLoaderContext.setViewActions(actions);

        Element element = loadViewXml(viewInfo);
        if (element != null) {
            loadMessageGroup(element)
                    .ifPresent(componentLoaderContext::setMessageGroup);
            loadWindowFromXml(element, view, componentLoaderContext);
        }

        UiControllerDependencyManager dependencyManager =
                applicationContext.getBean(UiControllerDependencyManager.class);
        dependencyManager.inject(view);

        componentLoaderContext.executeInitTasks();

        fireViewInitEvent(view);
    }

    public void registerBackNavigation(Class<? extends View> viewClass,
                                       Class<? extends View> backNavigationTarget) {
        registerBackNavigation(UI.getCurrent(), viewClass, backNavigationTarget);
    }

    public void registerBackNavigation(UI ui,
                                       Class<? extends View> viewClass,
                                       Class<? extends View> backNavigationTarget) {
        retrieveExtendedClientDetails(ui, details ->
                registerBackNavigation(ui.getSession(), details.getWindowName(),
                        viewClass, backNavigationTarget));
    }

    protected void registerBackNavigation(VaadinSession session, String windowName,
                                          Class<? extends View> viewClass,
                                          Class<? extends View> backNavigationTarget) {
        log.debug(String.format("Register back navigation for '%s' with back target '%s'",
                viewClass, backNavigationTarget));

        BackNavigationTargets targets = session.getAttribute(BackNavigationTargets.class);
        if (targets == null) {
            targets = new BackNavigationTargets();
        }

        targets.put(windowName, new Pair<>(viewClass, backNavigationTarget));
        session.setAttribute(BackNavigationTargets.class, targets);
    }

    public void unregisterBackNavigation(View<?> view) {
        UI ui = view.getUI().orElse(UI.getCurrent());
        unregisterBackNavigation(ui, view.getClass());
    }

    public void unregisterBackNavigation(Class<? extends View> viewClass) {
        unregisterBackNavigation(UI.getCurrent(), viewClass);
    }

    public void unregisterBackNavigation(UI ui, Class<? extends View> viewClass) {
        log.debug(String.format("Unregister back navigation for '%s'", viewClass));

        if (!hasBackNavigationTarget(ui, viewClass)) {
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
     * Gets localized page title from the view.
     *
     * @param view view to get localized page title
     * @return localized page title or message key if not found or empty string if message key is not defined
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedPageTitle(View<?> view) {
        String title = UiControllerUtils.getPageTitle(view);

        if (!Strings.isNullOrEmpty(title)) {
            MessageTools messageTools = applicationContext.getBean(MessageTools.class);
            String messagesGroup = UiControllerUtils.getPackage(view.getClass());

            String viewId = view.getId().orElse("");
            if (!Strings.isNullOrEmpty(viewId)) {
                ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
                Element element = loadViewXml(viewInfo);
                if (element != null) {
                    messagesGroup = loadMessageGroup(element).orElse(messagesGroup);
                }
            }

            if (!title.contains(MessageTools.MARK)) {
                return messageTools.loadString(messagesGroup, MessageTools.MARK + title);
            }

            return messageTools.loadString(messagesGroup, title);
        }

        String viewId = view.getId().orElse("");
        if (Strings.isNullOrEmpty(viewId)) {
            return "";
        }

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        Element element = loadViewXml(viewInfo);
        if (element != null) {
            MessageTools messageTools = applicationContext.getBean(MessageTools.class);

            title = element.attributeValue("title");
            if (Strings.isNullOrEmpty(title)) {
                return "";
            }

            String messagesGroup = loadMessageGroup(element)
                    .orElse(UiControllerUtils.getPackage(view.getClass()));

            return messageTools.loadString(messagesGroup, title);
        }

        return "";
    }

    public void close(View<?> view) {
        UI ui = view.getUI().orElse(UI.getCurrent());
        close(ui, view);
    }

    public void close(UI ui, View<?> view) {
        log.debug("Closing view: " + view);

        // Check if a back navigation target for the given view class is registered
        // for any window name before we obtain ExtendedClientDetails
        if (hasBackNavigationTarget(ui, view.getClass())) {
            navigateToBackTarget(ui, view);
        } else {
            navigateToParentLayout(view);
        }
    }

    protected boolean hasBackNavigationTarget(UI ui, Class<? extends View> viewClass) {
        VaadinSession vaadinSession = ui.getSession();
        BackNavigationTargets targets = vaadinSession.getAttribute(BackNavigationTargets.class);

        return targets != null && targets.values().stream()
                .allMatch(pair -> pair.getFirst().equals(viewClass));
    }

    protected void navigateToBackTarget(UI ui, View<?> view) {
        retrieveExtendedClientDetails(ui, details ->
                navigateToBackTarget(ui.getSession(), details.getWindowName(), view));
    }

    protected void navigateToBackTarget(VaadinSession session, String windowName, View<?> view) {
        BackNavigationTargets targets = session.getAttribute(BackNavigationTargets.class);
        if (targets != null && targets.containsKey(windowName)
                && targets.get(windowName).getFirst().equals(view.getClass())) {
            Class<? extends View> backTarget = targets.get(windowName).getSecond();
            targets.remove(windowName);
            navigationSupport.navigate(backTarget);
        } else {
            navigateToParentLayout(view);
        }
    }

    protected void navigateToParentLayout(View<?> view) {
        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        List<RouteData> routes = routeConfiguration.getAvailableRoutes();

        findRouteData(view.getClass(), routes)
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

    protected String getInferredViewId(View<?> view) {
        Class<? extends View> viewClass = view.getClass();

        UiController uiController = viewClass.getAnnotation(UiController.class);
        if (uiController == null) {
            throw new IllegalArgumentException("No @" + UiController.class.getSimpleName() +
                    " annotation for class " + viewClass);
        }

        return UiDescriptorUtils.getInferredViewId(uiController, viewClass);
    }

    protected void fireViewInitEvent(View<?> view) {
        UiControllerUtils.fireEvent(view, new InitEvent(view));
    }

    @Nullable
    protected Element loadViewXml(ViewInfo viewInfo) {
        Optional<String> templatePath = viewInfo.getTemplatePath();
        return templatePath.map(s -> viewXmlLoader.load(s)).orElse(null);
    }

    protected Optional<String> loadMessageGroup(Element element) {
        String messageGroup = element.attributeValue("messagesGroup");
        return Optional.ofNullable(messageGroup);
    }

    protected ComponentLoaderContext createComponentLoaderContext() {
        return new ComponentLoaderContext();
    }

    protected void loadWindowFromXml(Element element, View<?> view, ComponentLoaderContext context) {
        LayoutLoader layoutLoader = applicationContext.getBean(LayoutLoader.class, context);
        ComponentLoader<View<?>> viewLoader = layoutLoader.createViewContent(view, element);

        viewLoader.loadComponent();
    }

    // maps window.name to (view class, back navigation target)
    @SuppressWarnings("rawtypes")
    protected static class BackNavigationTargets
            extends HashMap<String, Pair<Class<? extends View>, Class<? extends View>>> {
    }
}
