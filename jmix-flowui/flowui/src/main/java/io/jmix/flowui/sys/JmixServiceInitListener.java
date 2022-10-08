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
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import io.jmix.core.CoreProperties;
import io.jmix.core.LocaleResolver;
import io.jmix.flowui.component.error.JmixInternalServerError;
import io.jmix.flowui.exception.UiExceptionHandlers;
import io.jmix.flowui.view.ViewRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

@Component("flowui_JmixServiceInitListener")
public class JmixServiceInitListener implements VaadinServiceInitListener, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(JmixServiceInitListener.class);

    protected ApplicationContext applicationContext;

    protected ViewRegistry viewRegistry;
    protected UiExceptionHandlers uiExceptionHandlers;
    protected CoreProperties coreProperties;

    protected AppCookies cookies;

    public JmixServiceInitListener(ViewRegistry viewRegistry,
                                   UiExceptionHandlers uiExceptionHandlers,
                                   CoreProperties coreProperties) {
        this.viewRegistry = viewRegistry;
        this.uiExceptionHandlers = uiExceptionHandlers;
        this.coreProperties = coreProperties;
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

    protected void onSessionInitEvent(SessionInitEvent event) {
        event.getSession().setErrorHandler(uiExceptionHandlers);

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

    protected AppCookies getCookies() {
        if (cookies == null) {
            cookies = new AppCookies();
        }
        return cookies;
    }
}
