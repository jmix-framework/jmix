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
import io.jmix.flowui.view.navigation.RouteSupport;
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
import java.net.URL;
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
    protected RouteSupport routeSupport;

    protected Map<String, String> titleCache = new ConcurrentHashMap<>();

    public ViewSupport(ApplicationContext applicationContext,
                       ViewXmlLoader viewXmlLoader,
                       ViewRegistry viewRegistry,
                       ViewNavigationSupport navigationSupport,
                       CurrentAuthentication currentAuthentication,
                       ViewControllerDependencyManager dependencyManager,
                       RouteSupport routeSupport) {
        this.applicationContext = applicationContext;
        this.viewXmlLoader = viewXmlLoader;
        this.viewRegistry = viewRegistry;
        this.navigationSupport = navigationSupport;
        this.currentAuthentication = currentAuthentication;
        this.dependencyManager = dependencyManager;
        this.routeSupport = routeSupport;
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

    public void registerBackwardNavigation(Class<? extends View> viewClass, URL url) {
        registerBackwardNavigation(UI.getCurrent(), viewClass, url);
    }

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

    public void unregisterBackwardNavigation(View<?> view) {
        UI ui = view.getUI().orElse(UI.getCurrent());
        unregisterBackwardNavigation(ui, view.getClass());
    }

    public void unregisterBackwardNavigation(Class<? extends View> viewClass) {
        unregisterBackwardNavigation(UI.getCurrent(), viewClass);
    }

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

    public void close(View<?> view) {
        close(view, QueryParameters.empty());
    }

    public void close(View<?> view, QueryParameters returnParams) {
        UI ui = view.getUI().orElse(UI.getCurrent());
        close(ui, view, returnParams);
    }

    public void close(UI ui, View<?> view) {
        close(ui, view, QueryParameters.empty());
    }

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
                .allMatch(pair -> pair.getFirst().equals(viewClass));
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
