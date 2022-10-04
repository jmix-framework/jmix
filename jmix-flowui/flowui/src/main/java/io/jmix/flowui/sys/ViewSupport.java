/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.MessageTools;
import io.jmix.core.security.CurrentAuthentication;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.jmix.flowui.view.ViewControllerUtils.getPackage;


@Component("flowui_ViewSupport")
public class ViewSupport {

    private static final Logger log = LoggerFactory.getLogger(ViewSupport.class);

    protected ApplicationContext applicationContext;
    protected ViewXmlLoader viewXmlLoader;
    protected ViewRegistry viewRegistry;
    protected ViewNavigationSupport navigationSupport;
    protected CurrentAuthentication currentAuthentication;
    protected ViewControllerDependencyManager dependencyManager;

    protected Map<String, String> titleCache = new ConcurrentHashMap<>();

    public ViewSupport(ApplicationContext applicationContext,
                       ViewXmlLoader viewXmlLoader,
                       ViewRegistry viewRegistry,
                       ViewNavigationSupport navigationSupport,
                       CurrentAuthentication currentAuthentication,
                       ViewControllerDependencyManager dependencyManager) {
        this.applicationContext = applicationContext;
        this.viewXmlLoader = viewXmlLoader;
        this.viewRegistry = viewRegistry;
        this.navigationSupport = navigationSupport;
        this.currentAuthentication = currentAuthentication;
        this.dependencyManager = dependencyManager;
    }

    public void initView(View<?> view) {
        log.debug("Init view: " + view);

        ViewControllerUtils.setViewData(view, applicationContext.getBean(ViewData.class));

        ActionBinders actionBinders = applicationContext.getBean(ActionBinders.class);
        ViewActions actions = applicationContext.getBean(ViewActions.class, actionBinders.binder(view));
        ViewControllerUtils.setViewActions(view, actions);

        ViewControllerUtils.setViewFacets(view, applicationContext.getBean(ViewFacets.class, view));

        String viewId = getInferredViewId(view);
        view.setId(viewId);

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);

        ComponentLoaderContext componentLoaderContext = createComponentLoaderContext();

        componentLoaderContext.setFullFrameId(viewInfo.getId());
        componentLoaderContext.setCurrentFrameId(viewInfo.getId());
        componentLoaderContext.setMessageGroup(getPackage(viewInfo.getControllerClass()));
        componentLoaderContext.setView(view);
        componentLoaderContext.setViewActions(actions);

        Element element = loadViewXml(viewInfo);
        if (element != null) {
            loadMessageGroup(element)
                    .ifPresent(componentLoaderContext::setMessageGroup);
            loadWindowFromXml(element, view, componentLoaderContext);
        }

        // Pre InitTasks must be executed before DependencyManager
        // invocation to have precedence over @Subscribe methods
        componentLoaderContext.executePreInitTasks();

        ViewControllerDependencyManager dependencyManager =
                applicationContext.getBean(ViewControllerDependencyManager.class);
        dependencyManager.inject(view);

        fireViewInitEvent(view);

        // InitTasks must be executed after View.InitEvent
        // in case something was replaced, e.g. actions
        componentLoaderContext.executeInitTasks();
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
     * Gets localized title for the view.
     *
     * @param view view to get localized title for
     * @return localized page title or empty string. If possible, returns cached value.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(View<?> view) {
        return getLocalizedTitle(view, currentAuthentication.getLocale(), true);
    }

    /**
     * Gets localized title for the view.
     *
     * @param view   view to get localized title for
     * @param cached whether to use the cached value
     * @return localized page title or empty string.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(View<?> view, boolean cached) {
        return getLocalizedTitle(view, currentAuthentication.getLocale(), cached);
    }

    /**
     * Gets localized title for the view.
     *
     * @param view   view to get localized title for
     * @param locale the locale to get the title for
     * @return localized page title or empty string. If possible, returns cached value.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(View<?> view, Locale locale) {
        return getLocalizedTitle(view, locale, true);
    }

    /**
     * Gets localized title for the view.
     *
     * @param view   view to get localized title for
     * @param locale the locale to get the title for
     * @param cached whether to use the cached value
     * @return localized page title or empty string.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(View<?> view, Locale locale, boolean cached) {
        ViewInfo viewInfo = view.getId().map(viewId ->
                        viewRegistry.getViewInfo(viewId))
                .orElseThrow(() -> new IllegalStateException(
                        String.format("%s '%s' has no id",
                                View.class.getSimpleName(), view.getClass())));

        return getLocalizedTitle(viewInfo, locale, cached);
    }

    /**
     * Gets localized title for the view info.
     *
     * @param viewInfo view info to get localized title for
     * @return localized page title or empty string. If possible, returns cached value.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(ViewInfo viewInfo) {
        return getLocalizedTitle(viewInfo, currentAuthentication.getLocale(), true);
    }

    /**
     * Gets localized title for the view info.
     *
     * @param viewInfo view info to get localized title for
     * @param cached   whether to use the cached value
     * @return localized page title or empty string.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(ViewInfo viewInfo, boolean cached) {
        return getLocalizedTitle(viewInfo, currentAuthentication.getLocale(), cached);
    }

    /**
     * Gets localized title for the view info.
     *
     * @param viewInfo view info to get localized title for
     * @param locale   the locale to get the title for
     * @return localized page title or empty string. If possible, returns cached value.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(ViewInfo viewInfo, Locale locale) {
        return getLocalizedTitle(viewInfo, locale, true);
    }

    /**
     * Gets localized title for the view info.
     *
     * @param viewInfo view info to get the localized title for
     * @param locale   the locale to get the title for
     * @param cached   whether to use the cached value
     * @return localized page title or empty string.
     * @see PageTitle
     * @see HasDynamicTitle
     */
    public String getLocalizedTitle(ViewInfo viewInfo, Locale locale, boolean cached) {
        String key = getTitleCacheKey(viewInfo.getId(), locale);

        if (cached) {
            String title = titleCache.get(key);
            if (title != null) {
                return title;
            }
        }

        String title = getViewTitleValue(viewInfo);

        String localizedTitle = "";
        if (!Strings.isNullOrEmpty(title)) {
            MessageTools messageTools = applicationContext.getBean(MessageTools.class);
            String messagesGroup = getViewMessageGroup(viewInfo);
            localizedTitle = messageTools.loadString(messagesGroup, title);
        }

        cacheTitle(key, localizedTitle);
        return localizedTitle;
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

        ViewController viewController = viewClass.getAnnotation(ViewController.class);
        if (viewController == null) {
            throw new IllegalArgumentException("No @" + ViewController.class.getSimpleName() +
                    " annotation for class " + viewClass);
        }

        return ViewDescriptorUtils.getInferredViewId(viewController, viewClass);
    }

    protected void fireViewInitEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new InitEvent(view));
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

    @Nullable
    protected String getViewTitleValue(ViewInfo viewInfo) {
        String title = ViewControllerUtils.findAnnotation(viewInfo.getControllerClass(), PageTitle.class)
                .map(PageTitle::value)
                .orElse(null);

        // the PageTitle annotation must take precedence
        if (title == null) {
            Element element = loadViewXml(viewInfo);
            if (element != null) {
                return element.attributeValue("title");
            }
        }

        return title;
    }

    protected String getViewMessageGroup(ViewInfo viewInfo) {
        String messagesGroup = ViewControllerUtils.getPackage(viewInfo.getControllerClass());

        // XML value takes precedence because it's defined explicitly
        Element element = loadViewXml(viewInfo);
        if (element != null) {
            messagesGroup = loadMessageGroup(element).orElse(messagesGroup);
        }

        return messagesGroup;
    }

    protected String getTitleCacheKey(String id, Locale locale) {
        return id + locale;
    }

    protected void cacheTitle(String key, String value) {
        if (!titleCache.containsKey(key)) {
            titleCache.put(key, value);
        }
    }

    // maps window.name to (view class, back navigation target)
    @SuppressWarnings("rawtypes")
    protected static class BackNavigationTargets
            extends HashMap<String, Pair<Class<? extends View>, Class<? extends View>>> {
    }
}
