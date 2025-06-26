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
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.monitoring.ViewLifeCycle;
import io.jmix.flowui.sys.autowire.AutowireManager;
import io.jmix.flowui.sys.autowire.ViewAutowireContext;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.View.InitEvent;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.ComponentLoaderContext;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.jmix.flowui.monitoring.UiMonitoring.startTimerSample;
import static io.jmix.flowui.monitoring.UiMonitoring.stopViewTimerSample;
import static io.jmix.flowui.view.ViewControllerUtils.getPackage;

/**
 * Provides support and utility methods for managing views within the application. This class
 * handles view initialization, backward navigation management, localized title handling, and
 * lifecycle events for views, among other responsibilities.
 */
@Internal
@Component("flowui_ViewSupport")
public class ViewSupport {

    private static final Logger log = LoggerFactory.getLogger(ViewSupport.class);

    protected ApplicationContext applicationContext;
    protected ViewXmlLoader viewXmlLoader;
    protected ViewRegistry viewRegistry;
    protected ViewNavigationSupport navigationSupport;
    protected CurrentAuthentication currentAuthentication;
    protected AutowireManager autowireManager;
    protected RouteSupport routeSupport;
    protected MeterRegistry meterRegistry;

    protected Map<String, String> titleCache = new ConcurrentHashMap<>();

    public ViewSupport(ApplicationContext applicationContext,
                       ViewXmlLoader viewXmlLoader,
                       ViewRegistry viewRegistry,
                       ViewNavigationSupport navigationSupport,
                       CurrentAuthentication currentAuthentication,
                       AutowireManager autowireManager,
                       RouteSupport routeSupport,
                       MeterRegistry meterRegistry) {
        this.applicationContext = applicationContext;
        this.viewXmlLoader = viewXmlLoader;
        this.viewRegistry = viewRegistry;
        this.navigationSupport = navigationSupport;
        this.currentAuthentication = currentAuthentication;
        this.autowireManager = autowireManager;
        this.routeSupport = routeSupport;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Initializes and sets up the given {@link View}.
     * Performs tasks such as setting IDs, loading XML configurations, injecting dependencies,
     * and triggering relevant lifecycle events for the view.
     *
     * @param view the view instance to initialize
     */
    public void initView(View<?> view) {
        log.debug("Init view: " + view);

        Timer.Sample createSample = startTimerSample(meterRegistry);

        String viewId = getInferredViewId(view);
        view.setId(viewId);

        ViewControllerUtils.setViewData(view, applicationContext.getBean(ViewData.class));

        ViewActions actions = applicationContext.getBean(ViewActions.class, view);
        ViewControllerUtils.setViewActions(view, actions);

        ViewControllerUtils.setViewFacets(view, applicationContext.getBean(ViewFacets.class, view));

        stopViewTimerSample(createSample, meterRegistry, ViewLifeCycle.CREATE, viewId);

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);

        Timer.Sample loadSample = startTimerSample(meterRegistry);

        ComponentLoaderContext componentLoaderContext = createComponentLoaderContext();

        componentLoaderContext.setFullOriginId(viewInfo.getId());
        componentLoaderContext.setMessageGroup(getPackage(viewInfo.getControllerClass()));
        componentLoaderContext.setView(view);
        componentLoaderContext.setActionsHolder(actions);

        Element element = loadViewXml(viewInfo);
        if (element != null) {
            loadMessageGroup(element)
                    .ifPresent(componentLoaderContext::setMessageGroup);
            loadWindowFromXml(element, view, componentLoaderContext);
        }

        stopViewTimerSample(loadSample, meterRegistry, ViewLifeCycle.LOAD, viewId);

        // Pre InitTasks must be executed before DependencyManager
        // invocation to have precedence over @Subscribe methods
        componentLoaderContext.executePreInitTasks();

        Timer.Sample injectSample = startTimerSample(meterRegistry);

        ViewAutowireContext viewAutowireContext = new ViewAutowireContext(view);
        autowireManager.autowire(viewAutowireContext);

        stopViewTimerSample(injectSample, meterRegistry, ViewLifeCycle.INJECT, viewId);

        // perform injection for the nested fragments
        componentLoaderContext.executeAutowireTasks();

        Timer.Sample initSample = startTimerSample(meterRegistry);

        fireViewInitEvent(view);

        stopViewTimerSample(initSample, meterRegistry, ViewLifeCycle.INIT, viewId);

        // InitTasks must be executed after View.InitEvent
        // in case something was replaced, e.g. actions
        componentLoaderContext.executeInitTasks();
    }

    /**
     * Registers a backward navigation target for a specific view class.
     * This allows the application to support navigation actions such as returning
     * to a previous view.
     *
     * @param viewClass the type of the view for which backward navigation is being registered
     * @param url       the {@link URL} used as the navigation target for this view
     */
    public void registerBackwardNavigation(Class<? extends View> viewClass, URL url) {
        registerBackwardNavigation(UI.getCurrent(), viewClass, url);
    }

    /**
     * Registers backward navigation for a specific view in the given UI.
     * This enables the application to support navigation actions, such as navigating
     * back to a previous view, by associating the view with a specific URL.
     *
     * @param ui        the {@link UI} instance to register the backward navigation for
     * @param viewClass the class of the view to associate with backward navigation
     * @param url       the {@link URL} that will be used as the navigation target for the view
     */
    public void registerBackwardNavigation(UI ui, Class<? extends View> viewClass, URL url) {
        retrieveExtendedClientDetails(ui, details ->
                registerBackwardNavigation(ui.getSession(), details.getWindowName(),
                        viewClass, url));
    }

    protected void registerBackwardNavigation(VaadinSession session, String windowName,
                                              Class<? extends View> viewClass, URL url) {
        log.debug("Register backward navigation for '{}' with back url '{}'", viewClass, url);

        BackwardNavigationTargets targets = session.getAttribute(BackwardNavigationTargets.class);
        if (targets == null) {
            targets = new BackwardNavigationTargets();
        }

        targets.put(windowName, new Pair<>(viewClass, url));
        session.setAttribute(BackwardNavigationTargets.class, targets);
    }

    /**
     * Unregisters backward navigation for the specified view.
     * This removes the association between the view and any previously
     * registered backward navigation targets, thus disabling the ability
     * to navigate back to the view via backward navigation.
     *
     * @param view the {@link View} instance for which backward navigation is to be unregistered
     */
    public void unregisterBackwardNavigation(View<?> view) {
        UI ui = view.getUI().orElse(UI.getCurrent());
        unregisterBackwardNavigation(ui, view.getClass());
    }

    /**
     * Unregisters backward navigation for the specified view class.
     * This removes the association between the view class and any previously
     * registered backward navigation targets, disabling backward navigation
     * for that particular view class.
     *
     * @param viewClass the class of the {@link View} for which backward navigation
     *                  is to be unregistered
     */
    public void unregisterBackwardNavigation(Class<? extends View> viewClass) {
        unregisterBackwardNavigation(UI.getCurrent(), viewClass);
    }

    /**
     * Unregisters backward navigation for the specified UI and view class.
     * This removes the association between the view class and any previously
     * registered backward navigation targets for the provided {@link UI},
     * thereby disabling the ability to navigate back to this view class
     * in the given UI context.
     *
     * @param ui        the {@link UI} instance for which the backward navigation
     *                  is to be unregistered
     * @param viewClass the class of the {@link View} for which backward navigation
     *                  is to be unregistered
     */
    public void unregisterBackwardNavigation(UI ui, Class<? extends View> viewClass) {
        log.debug("Attempt to unregister backward navigation for '{}'", viewClass);

        if (!hasBackwardNavigationTarget(ui, viewClass)) {
            return;
        }

        retrieveExtendedClientDetails(ui, details ->
                unregisterBackwardNavigation(ui.getSession(), details.getWindowName()));
    }

    protected void unregisterBackwardNavigation(VaadinSession session, String windowName) {
        log.debug("Unregister backward navigation for '{}'", windowName);

        BackwardNavigationTargets targets = session.getAttribute(BackwardNavigationTargets.class);
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

    /**
     * Closes the specified {@link View}.
     *
     * @param view the {@link View} instance to be closed
     */
    public void close(View<?> view) {
        close(view, QueryParameters.empty());
    }

    /**
     * Closes the specified {@link View} with the specified {@link QueryParameters}.
     *
     * @param view         the {@link View} instance to be closed
     * @param returnParams the {@link QueryParameters} to be passed upon closing the view
     */
    public void close(View<?> view, QueryParameters returnParams) {
        UI ui = view.getUI().orElse(UI.getCurrent());
        close(ui, view, returnParams);
    }

    /**
     * Closes the specified {@link View} in the context of the provided {@link UI}.
     *
     * @param ui   the {@link UI} instance
     * @param view the {@link View} instance to be closed
     */
    public void close(UI ui, View<?> view) {
        close(ui, view, QueryParameters.empty());
    }

    /**
     * Closes the specified {@link View} within the context of the given {@link UI}.
     *
     * @param ui           the {@link UI} instance in which the {@link View} exists
     * @param view         the {@link View} instance to be closed
     * @param returnParams the {@link QueryParameters} to be used upon closing the view
     */
    public void close(UI ui, View<?> view, QueryParameters returnParams) {
        log.debug("Closing view: " + view);

        // Check if a backward navigation target for the given view class is registered
        // for any window name before we obtain ExtendedClientDetails
        if (hasBackwardNavigationTarget(ui, view.getClass())) {
            doBackwardNavigation(ui, view, returnParams);
        } else {
            navigateToParentLayout(view, returnParams);
        }
    }

    protected boolean hasBackwardNavigationTarget(UI ui, Class<? extends View> viewClass) {
        VaadinSession vaadinSession = ui.getSession();
        BackwardNavigationTargets targets = vaadinSession.getAttribute(BackwardNavigationTargets.class);

        return targets != null && targets.values().stream()
                .anyMatch(pair -> pair.getFirst().equals(viewClass));
    }

    protected void doBackwardNavigation(UI ui, View<?> view, QueryParameters returnParams) {
        retrieveExtendedClientDetails(ui, details ->
                doBackwardNavigation(ui, details.getWindowName(), view, returnParams));
    }

    protected void doBackwardNavigation(UI ui, String windowName, View<?> view, QueryParameters returnParams) {
        log.debug("Perform backward navigation for '{}'", view.getClass());

        BackwardNavigationTargets targets = ui.getSession().getAttribute(BackwardNavigationTargets.class);
        if (targets != null && targets.containsKey(windowName)
                && targets.get(windowName).getFirst().equals(view.getClass())) {
            URL url = targets.get(windowName).getSecond();
            targets.remove(windowName);
            ui.navigate(
                    routeSupport.resolveLocationString(url),
                    routeSupport.mergeQueryParameters(routeSupport.resolveQueryParameters(url), returnParams)
            );
        } else {
            navigateToParentLayout(view, returnParams);
        }
    }

    /**
     * Navigates to the parent layout of the specified view, if a parent layout exists.
     *
     * @param view         the {@link View} instance whose parent layout needs to be navigated to
     * @param returnParams the {@link QueryParameters} to be passed during the navigation
     */
    public void navigateToParentLayout(View<?> view, QueryParameters returnParams) {
        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        List<RouteData> routes = routeConfiguration.getAvailableRoutes();

        findRouteData(view.getClass(), routes)
                .ifPresent(routeData -> {
                    Class<? extends RouterLayout> parentLayout = routeData.getParentLayout();
                    findRouteData(parentLayout, routes)
                            .ifPresent(data ->
                                    navigationSupport.navigate(data.getNavigationTarget(),
                                            RouteParameters.empty(), returnParams));
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

        log.debug("Loading view with '{}' ID from '{}' path", viewInfo.getId(), templatePath.orElse(""));

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

    // maps window.name to (view class, backward navigation target)
    @SuppressWarnings("rawtypes")
    protected static class BackwardNavigationTargets
            extends HashMap<String, Pair<Class<? extends View>, URL>> {
    }
}
