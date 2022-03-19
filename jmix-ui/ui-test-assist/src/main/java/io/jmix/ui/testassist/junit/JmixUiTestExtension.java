/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.testassist.junit;

import com.google.common.base.Strings;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.ui.*;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.AppCookies;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.ui.testassist.ui.TestConnectorTracker;
import io.jmix.ui.testassist.ui.TestVaadinRequest;
import io.jmix.ui.testassist.ui.TestVaadinSession;
import io.jmix.ui.theme.ThemeConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField;

/**
 * Extension starts Vaadin UI before each test and configures screen packages,
 * main screen id, username to perform authentication. For instance:
 * <pre>
 * &#64;ExtendWith(SpringExtension.class)
 * &#64;ContextConfiguration(classes = {DemoApplication.class, UiTestAssistConfiguration.class})
 * public class UserBrowseTest {
 *
 *     &#64;RegisterExtension
 *     protected JmixUiTestExtension uiTestExtension = new JmixUiTestExtension()
 *             .withAuthenticatedUser("admin")
 *             .withScreenBasePackages("com.company.demo.screen")
 *             .withMainScreenId("MainScreen");
 *
 *     &#64;Test
 *     protected void openUserBrowse(Screens screens) {
 *         UserBrowse screen = screens.create(UserBrowse.class);
 *         screen.show();
 *     }
 * }
 * </pre>
 * {@link Screens} bean can be obtained from method parameters or via {@link ApplicationContext#getBean(Class)}.
 *
 * @see UiTest
 */
public class JmixUiTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(JmixUiTestExtension.class);

    private static final Set<Class<?>> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(
            Screens.class
    ));

    public static final String APP_UI = "appUI";
    public static final String SCREEN_PACKAGES = "screenPackages";

    protected String authenticatedUser;
    protected String mainScreenId;
    protected String[] screenBasePackages;

    /**
     * @return username or {@code null} if not set
     */
    @Nullable
    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * Sets username that should be authenticated before each test. If username is not set, authentication will be
     * performed by system user.
     *
     * @param username username
     * @return current instance
     */
    public JmixUiTestExtension withAuthenticatedUser(@Nullable String username) {
        authenticatedUser = username;
        return this;
    }

    /**
     * @return main screen id or {@code null} if not set
     */
    @Nullable
    public String getMainScreenId() {
        return mainScreenId;
    }

    /**
     * Sets main screen id that should be opened before each test. The screen with given id should be placed under
     * the package provided by {@link #withScreenBasePackages(String...)}.
     * <p>
     * If main screen id is not set, the {@link UiProperties#getMainScreenId()} will be used.
     *
     * @param mainScreenId main screen id
     * @return current instance
     */
    public JmixUiTestExtension withMainScreenId(@Nullable String mainScreenId) {
        this.mainScreenId = mainScreenId;
        return this;
    }

    /**
     * @return screen packages or {@code null} if not set
     */
    @Nullable
    public String[] getScreenBasePackages() {
        return screenBasePackages;
    }

    /**
     * Sets screen packages. Screens under these packages will be available in test.
     * If packages are not set, all application screens will be available depending on
     * the test's configuration.
     *
     * @param screenBasePackages screen packages
     * @return current instance
     */
    public JmixUiTestExtension withScreenBasePackages(@Nullable String... screenBasePackages) {
        this.screenBasePackages = screenBasePackages;
        return this;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        setupAuthentication(context);
        setupVaadinUi(context);
        registerScreenBasePackages(context);
        openMainScreen(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        removeAuthentication(context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return SUPPORTED_PARAMETERS.contains(parameterType);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        AppUI appUI = getAppUI(extensionContext);
        if (Screens.class.equals(parameterType)) {
            return appUI.getScreens();
        }

        throw new ParameterResolutionException("Cannot resolve parameter " + parameterType);
    }

    protected void setupAuthentication(ExtensionContext context) {
        String authenticatedUser = null;
        if (!Strings.isNullOrEmpty(this.authenticatedUser)) {
            authenticatedUser = this.authenticatedUser;
        } else {
            Optional<UiTest> jmixUiTestAnnotationOpt = AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class);
            if (jmixUiTestAnnotationOpt.isPresent()) {
                authenticatedUser = jmixUiTestAnnotationOpt.get().authenticatedUser();
            }
        }

        SystemAuthenticator systemAuthenticator = getApplicationContext(context).getBean(SystemAuthenticator.class);
        if (!Strings.isNullOrEmpty(authenticatedUser)) {
            systemAuthenticator.begin(authenticatedUser);
        } else {
            systemAuthenticator.begin();
        }
    }

    protected void removeAuthentication(ExtensionContext context) {
        SystemAuthenticator systemAuthenticator = getApplicationContext(context).getBean(SystemAuthenticator.class);
        systemAuthenticator.end();
    }

    protected void setupVaadinUi(ExtensionContext context) {
        ApplicationContext applicationContext = getApplicationContext(context);

        AutowireCapableBeanFactory injectFactory = applicationContext.getAutowireCapableBeanFactory();

        JmixApp app = new JmixApp();
        try {
            getDeclaredField(App.class, "themeConstants", true)
                    .set(app, new ThemeConstants(Collections.emptyMap()));
            getDeclaredField(App.class, "cookies", true)
                    .set(app, new AppCookies());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot initialize " + JmixApp.class.getName(), e);
        }

        TestVaadinSession vaadinSession = new TestVaadinSession(new WebBrowser(), Locale.ENGLISH);

        vaadinSession.setAttribute(App.class, app);
        vaadinSession.setAttribute("ui_App", app);

        VaadinSession.setCurrent(vaadinSession);

        injectFactory.autowireBean(app);

        AppUI vaadinUi = getAppUI(context);
        if (vaadinUi == null) {
            vaadinUi = applicationContext.getBean(AppUI.class);
            getStore(context).put(APP_UI, vaadinUi);
        }

        UI.setCurrent(vaadinUi);

        TestConnectorTracker connectorTracker = new TestConnectorTracker(vaadinUi);
        try {
            getDeclaredField(UI.class, "connectorTracker", true)
                    .set(vaadinUi, connectorTracker);
            getDeclaredField(UI.class, "session", true)
                    .set(vaadinUi, vaadinSession);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot initialize " + UI.class.getName(), e);
        }

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        TestVaadinRequest vaadinRequest = new TestVaadinRequest();
        vaadinUi.getPage().init(vaadinRequest);

        try {
            Method method = ReflectionUtils.findMethod(AppUI.class, "init", VaadinRequest.class);
            method.setAccessible(true);
            method.invoke(vaadinUi, vaadinRequest);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Cannot invoke init method of " + AppUI.class.getName(), e);
        }
    }

    protected ApplicationContext getApplicationContext(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context);
    }

    protected ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE);
    }

    protected AppUI getAppUI(ExtensionContext context) {
        return (AppUI) getStore(context).get(APP_UI);
    }

    protected void registerScreenBasePackages(ExtensionContext context) {
        String[] storedScreenBasePackages = getStore(context).get(SCREEN_PACKAGES, String[].class);
        if (ArrayUtils.isNotEmpty(storedScreenBasePackages)) {
            return;
        }

        String[] screenBasePackages = {};
        if (ArrayUtils.isNotEmpty(this.screenBasePackages)) {
            screenBasePackages = this.screenBasePackages;
        } else {
            Optional<UiTest> jmixUiTestAnnotationOpt = AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class);
            if (jmixUiTestAnnotationOpt.isPresent()) {
                screenBasePackages = jmixUiTestAnnotationOpt.get().screenBasePackages();
            }
        }

        if (ArrayUtils.isEmpty(screenBasePackages)) {
            return;
        }

        ApplicationContext applicationContext = getApplicationContext(context);

        AnnotationScanMetadataReaderFactory metadataReaderFactory = applicationContext.getBean(AnnotationScanMetadataReaderFactory.class);
        WindowConfig windowConfig = applicationContext.getBean(WindowConfig.class);

        UiControllersConfiguration configuration = new UiControllersConfiguration(applicationContext, metadataReaderFactory);

        AutowireCapableBeanFactory injector = applicationContext.getAutowireCapableBeanFactory();
        injector.autowireBean(configuration);

        configuration.setBasePackages(Arrays.asList(screenBasePackages));

        try {
            Field configurationsField = getDeclaredField(WindowConfig.class, "configurations", true);
            //noinspection unchecked
            Collection<UiControllersConfiguration> configurations = (Collection<UiControllersConfiguration>)
                    configurationsField.get(windowConfig);

            List<UiControllersConfiguration> modifiedConfigurations = new ArrayList<>(configurations);
            modifiedConfigurations.add(configuration);

            configurationsField.set(windowConfig, modifiedConfigurations);

            getDeclaredField(WindowConfig.class, "initialized", true)
                    .set(windowConfig, false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot register screen packages", e);
        }

        getStore(context).put(SCREEN_PACKAGES, screenBasePackages);
    }

    protected void openMainScreen(ExtensionContext context) {
        String mainScreenId = null;

        if (!Strings.isNullOrEmpty(this.mainScreenId)) {
            mainScreenId = this.mainScreenId;
        } else {
            Optional<UiTest> jmixUiTestAnnotationOpt = AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class);
            if (jmixUiTestAnnotationOpt.isPresent()) {
                mainScreenId = jmixUiTestAnnotationOpt.get().mainScreenId();
            }
        }

        AppUI appUI = getAppUI(context);

        if (Strings.isNullOrEmpty(mainScreenId)) {
            appUI.getApp().createTopLevelWindow();
        } else {
            appUI.getScreens()
                    .create(mainScreenId, OpenMode.ROOT)
                    .show();
        }
    }
}
