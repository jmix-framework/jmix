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
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.global.UserSession
import com.haulmont.cuba.web.testsupport.TestSupport
import com.haulmont.cuba.web.testsupport.WebTest
import com.haulmont.cuba.web.testsupport.ui.TestConnectorTracker
import com.haulmont.cuba.web.testsupport.ui.TestVaadinRequest
import com.haulmont.cuba.web.testsupport.ui.TestVaadinSession
import com.vaadin.server.VaadinSession
import com.vaadin.server.WebBrowser
import com.vaadin.ui.UI
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlanRepository
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.ui.*
import io.jmix.ui.model.DataComponents
import io.jmix.ui.sys.AppCookies
import io.jmix.ui.sys.UiControllersConfiguration
import io.jmix.ui.theme.ThemeConstants
import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.http.HttpServletRequest

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField

@WebTest
class WebSpec extends Specification {

    @Autowired
    Metadata metadata

    @Autowired
    MetadataTools metadataTools

    @Autowired
    FetchPlanRepository viewRepository

    @Autowired
    EntityStates entityStates

    @Autowired
    DataManager dataManager

    @Autowired
    DataComponents dataComponents

    @Autowired
    UiComponents uiComponents

    @Autowired
    UserSessionSource sessionSource

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    AnnotationScanMetadataReaderFactory metadataReaderFactory

    @Autowired
    WindowConfig windowConfig

    AppUI vaadinUi

    @SuppressWarnings("GroovyAccessibility")
    void setup() {
        exportScreensPackages(['com.haulmont.cuba.web.app.main'])

        def session = new UserSession()
        session.setAuthenticated(false)

        TestSupport.setAuthenticationToSecurityContext()

        def injectFactory = applicationContext.getAutowireCapableBeanFactory()

        def app = new JmixApp()
        app.themeConstants = new ThemeConstants([:])
        app.cookies = new AppCookies()

        def vaadinSession = new TestVaadinSession(new WebBrowser(), Locale.ENGLISH)

        vaadinSession.setAttribute(App.class, app)
        vaadinSession.setAttribute(App.NAME, app)

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
