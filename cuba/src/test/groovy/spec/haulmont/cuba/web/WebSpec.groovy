/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.web

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.web.testsupport.WebTest

import com.haulmont.cuba.web.testsupport.ui.TestConnectorTracker
import com.haulmont.cuba.web.testsupport.ui.TestVaadinRequest
import com.haulmont.cuba.web.testsupport.ui.TestVaadinSession
import com.vaadin.server.VaadinSession
import com.vaadin.server.WebBrowser
import com.vaadin.ui.UI
import io.jmix.core.*
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.core.security.LoginPasswordCredentials
import io.jmix.core.security.UserSession
import io.jmix.core.security.UserSessionSource
import io.jmix.security.entity.User
import io.jmix.security.impl.StandardUserSession
import io.jmix.ui.*
import io.jmix.ui.model.DataComponents
import io.jmix.ui.sys.AppCookies
import io.jmix.ui.sys.ConnectionImpl
import io.jmix.ui.sys.UiControllersConfiguration
import io.jmix.ui.theme.ThemeConstants
import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField

@WebTest
class WebSpec extends Specification {

    @Inject
    Metadata metadata

    @Inject
    MetadataTools metadataTools

    @Inject
    FetchPlanRepository viewRepository

    @Inject
    EntityStates entityStates

    @Inject
    DataManager dataManager

    @Inject
    DataComponents dataComponents

    @Inject
    UiComponents uiComponents

    @Inject
    UserSessionSource sessionSource

    @Inject
    ApplicationContext applicationContext

    @Inject
    AnnotationScanMetadataReaderFactory metadataReaderFactory

    @Inject
    WindowConfig windowConfig

    AppUI vaadinUi

    @SuppressWarnings("GroovyAccessibility")
    void setup() {
        exportScreensPackages(['com.haulmont.cuba.web.app.main'])

        def credentials = new LoginPasswordCredentials('test', 'test', Locale.ENGLISH) {
            @Override
            Object getPrincipal() {
                def user = new User()

                user.login = 'test'
                user.password = 'test'
                user.name = 'test'

                user
            }
        }

        def session = new StandardUserSession(credentials)
        session.setAuthenticated(false)

        def injectFactory = applicationContext.getAutowireCapableBeanFactory()

        def app = new DefaultApp()
        app.themeConstants = new ThemeConstants([:])
        app.cookies = new AppCookies()

        def connection = new ConnectionImpl()
        // injectFactory.autowireBean(connection)

        app.connection = connection

        def vaadinSession = new TestVaadinSession(new WebBrowser(), Locale.ENGLISH)

        vaadinSession.setAttribute(App.class, app)
        vaadinSession.setAttribute(App.NAME, app)
        vaadinSession.setAttribute(Connection.class, connection)
        vaadinSession.setAttribute(Connection.NAME, connection)
        vaadinSession.setAttribute(UserSession.class, session)

        VaadinSession.setCurrent(vaadinSession)

        injectFactory.autowireBean(app)

        vaadinUi = new AppUI()
        injectFactory.autowireBean(vaadinUi)

        def connectorTracker = new TestConnectorTracker(vaadinUi)
        getDeclaredField(UI.class, "connectorTracker", true)
            .set(vaadinUi, connectorTracker)
        getDeclaredField(UI.class, "session", true)
            .set(vaadinUi, vaadinSession)

        UI.setCurrent(vaadinUi)

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(Mock(HttpServletRequest)))

        def vaadinRequest = new TestVaadinRequest()
        vaadinUi.getPage().init(vaadinRequest)
        vaadinUi.init(vaadinRequest)
    }

    void cleanup() {
        resetScreensConfig()

        UI.setCurrent(null)
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
