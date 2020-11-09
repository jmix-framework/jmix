/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.testassist.spec


import com.vaadin.server.VaadinSession
import com.vaadin.server.WebBrowser
import com.vaadin.ui.UI
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlanRepository
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.authentication.CoreAuthenticationToken
import io.jmix.core.security.impl.CoreUser
import io.jmix.ui.*
import io.jmix.ui.model.DataComponents
import io.jmix.ui.sys.AppCookies
import io.jmix.ui.sys.UiControllersConfiguration
import io.jmix.ui.testassist.UiTest
import io.jmix.ui.testassist.UiTestAssistProperties
import io.jmix.ui.testassist.ui.TestConnectorTracker
import io.jmix.ui.testassist.ui.TestVaadinRequest
import io.jmix.ui.testassist.ui.TestVaadinSession
import io.jmix.ui.theme.ThemeConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField

@UiTest
class UiTestAssistSpecification extends Specification {

    @Autowired
    Metadata metadata

    @Autowired
    MetadataTools metadataTools

    @Autowired
    FetchPlanRepository viewRepository

    @Autowired
    EntityStates entityStates

    @Autowired
    DataComponents dataComponents

    @Autowired
    UiComponents uiComponents

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    AnnotationScanMetadataReaderFactory metadataReaderFactory

    @Autowired
    WindowConfig windowConfig

    @Autowired
    AppUI vaadinUi

    @Autowired
    UiTestAssistProperties uiTestAssistProperties

    @Autowired(required = false)
    AuthenticationManager authenticationManager

    @SuppressWarnings("GroovyAccessibility")
    void setup() {
        setupSecurityContext()

        def injectFactory = applicationContext.getAutowireCapableBeanFactory()

        def app = new JmixApp()
        app.themeConstants = new ThemeConstants([:])
        app.cookies = new AppCookies()

        def vaadinSession = new TestVaadinSession(new WebBrowser(), Locale.ENGLISH)

        vaadinSession.setAttribute(App.class, app)
        vaadinSession.setAttribute('ui_App', app)

        VaadinSession.setCurrent(vaadinSession)

        injectFactory.autowireBean(app)

        UI.setCurrent(vaadinUi)

        def connectorTracker = new TestConnectorTracker(vaadinUi)
        getDeclaredField(UI.class, "connectorTracker", true)
                .set(vaadinUi, connectorTracker)
        getDeclaredField(UI.class, "session", true)
                .set(vaadinUi, vaadinSession)

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(Mock(HttpServletRequest)))

        def vaadinRequest = new TestVaadinRequest()
        vaadinUi.getPage().init(vaadinRequest)
        vaadinUi.init(vaadinRequest)
    }

    void cleanup() {
        resetScreensConfig()
        def extensions = vaadinUi.getExtensions().findAll()
        extensions.forEach() {
            vaadinUi.removeExtension(it)
        }
        def windows = vaadinUi.getWindows().findAll()
        windows.forEach() {
            vaadinUi.removeWindow(it)
        }
        UI.setCurrent(null)
    }

    void setupSecurityContext() {
        Authentication authentication = authenticationManager == null
                ? createCoreAuthentication()
                : createSecurityAuthentication()

        SecurityContextHelper.setAuthentication(authentication)
    }

    Authentication createCoreAuthentication() {
        CoreUser user = new CoreUser(
                uiTestAssistProperties.getUsername(),
                uiTestAssistProperties.getPassword(),
                uiTestAssistProperties.getUsername());

        CoreAuthenticationToken authentication = new CoreAuthenticationToken(user, Collections.emptyList());
        authentication.setLocale(Locale.US);
        return authentication
    }

    Authentication createSecurityAuthentication() {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        uiTestAssistProperties.getUsername(), uiTestAssistProperties.getPassword()))
        return authentication
    }

    protected void exportScreensPackages(List<String> packages) {
        def configuration = new UiControllersConfiguration(applicationContext, metadataReaderFactory)

        def injector = applicationContext.getAutowireCapableBeanFactory()
        injector.autowireBean(configuration)

        configuration.basePackages = packages

        windowConfig.configurations += [configuration]
        windowConfig.initialized = false
    }

    protected void resetScreensConfig() {
        windowConfig.configurations = []
        windowConfig.initialized = false
    }
}
