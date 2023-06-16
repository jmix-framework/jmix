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
import com.vaadin.flow.router.InternalServerError;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.internal.ErrorTargetEntry;
import com.vaadin.flow.server.*;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import io.jmix.core.CoreProperties;
import io.jmix.core.JmixModules;
import io.jmix.core.LocaleResolver;
import io.jmix.core.Resources;
import io.jmix.flowui.component.error.JmixInternalServerError;
import io.jmix.flowui.exception.UiExceptionHandlers;
import io.jmix.flowui.backgroundtask.BackgroundTaskManager;
import io.jmix.flowui.view.ViewRegistry;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("flowui_JmixServiceInitListener")
public class JmixServiceInitListener implements VaadinServiceInitListener, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(JmixServiceInitListener.class);

    public static final String IMPORT_STYLES_PROP = "jmix.ui.import-styles";
    protected static final String RESOURCE_PREFIX = "META-INF/resources/";

    protected ApplicationContext applicationContext;

    protected ViewRegistry viewRegistry;
    protected UiExceptionHandlers uiExceptionHandlers;
    protected CoreProperties coreProperties;
    protected JmixModules modules;
    protected Resources resources;

    protected AppCookies cookies;

    public JmixServiceInitListener(ViewRegistry viewRegistry,
                                   UiExceptionHandlers uiExceptionHandlers,
                                   CoreProperties coreProperties,
                                   JmixModules modules,
                                   Resources resources) {
        this.viewRegistry = viewRegistry;
        this.uiExceptionHandlers = uiExceptionHandlers;
        this.coreProperties = coreProperties;
        this.modules = modules;
        this.resources = resources;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this::onSessionInitEvent);
        event.getSource().addSessionDestroyListener(this::onSessionDestroyEvent);
        event.getSource().addUIInitListener(this::onUIInitEvent);

        event.addIndexHtmlRequestListener(this::modifyIndexHtmlResponse);

        // Vaadin scans only application packages by default. To enable scanning
        // Jmix packages, Vaadin provides @EnableVaadin() annotation, but it
        // should be defined only in one configuration as Spring cannot register bean with
        // the same name, see VaadinScanPackagesRegistrar#registerBeanDefinitions().
        // Register routes after route application scope is available.
        viewRegistry.setRouteConfiguration(RouteConfiguration.forApplicationScope());
        viewRegistry.registerViewRoutes();

        registerInternalServiceError();
    }

    protected void onUIInitEvent(UIInitEvent uiInitEvent) {
        UI ui = uiInitEvent.getUI();
        // retrieve ExtendedClientDetails to be cached
        ui.getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
        });
    }

    protected void onSessionDestroyEvent(SessionDestroyEvent event) {
        // VaadinSessionScope is not active here
        if (log.isTraceEnabled()) {
            log.trace("VaadinSession {} is destroyed", event.getSession());
        }
        VaadinSession session = event.getSession();
        session.getAttribute(BackgroundTaskManager.class).cleanupTasks();
    }

    protected void onSessionInitEvent(SessionInitEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("VaadinSession {} is initialized", event.getSession());
        }
        event.getSession().setErrorHandler(uiExceptionHandlers);
        event.getSession().setAttribute(BackgroundTaskManager.class, new BackgroundTaskManager());

        initCookieLocale(event.getSession());
    }

    protected void initCookieLocale(VaadinSession session) {
        String localeString = getCookies().getCookieValue(AppCookies.COOKIE_LOCALE);
        if (Strings.isNullOrEmpty(localeString)) {
            return;
        }
        Locale resolvedLocale = LocaleResolver.resolve(localeString);
        if (coreProperties.getAvailableLocales().contains(resolvedLocale)) {
            session.setLocale(resolvedLocale);
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

    protected void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        List<String> styles = modules.getPropertyValues(IMPORT_STYLES_PROP);
        if (styles.isEmpty()) {
            return;
        }

        Element head = response.getDocument().head();
        styles.forEach(path -> appendStyles(head, path));
    }

    protected void appendStyles(Element element, String path) {
        String fullPath = RESOURCE_PREFIX + (path.startsWith("/") ? path.substring(1) : path);
        String content = getContent(fullPath);
        if (Strings.isNullOrEmpty(content)) {
            return;
        }

        log.debug("Adding styles from '" + fullPath + "'");
        Element styleElement = createElement("style", content, "type", "text/css");
        element.appendChild(styleElement);
    }

    @Nullable
    protected String getContent(String path) {
        InputStream resourceStream = resources.getResourceAsStream(path);
        if (resourceStream == null) {
            return null;
        }

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(resourceStream, StandardCharsets.UTF_8)
        );
        return bufferedReader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    protected Element createElement(String tag, @Nullable String content, String... attrs) {
        Element element = new Element(Tag.valueOf(tag), "");
        if (content != null && !content.isEmpty()) {
            element.appendChild(new DataNode(content));
        }

        for (int i = 0; i < attrs.length - 1; i += 2) {
            element.attr(attrs[i], attrs[i + 1]);
        }

        return element;
    }

    protected AppCookies getCookies() {
        if (cookies == null) {
            cookies = new AppCookies();
        }
        return cookies;
    }
}
