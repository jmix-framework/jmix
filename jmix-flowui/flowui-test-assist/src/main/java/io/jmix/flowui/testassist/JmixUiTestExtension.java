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

package io.jmix.flowui.testassist;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinServletContextInitializer;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.backgroundtask.BackgroundTaskManager;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.testassist.dialog.OpenedDialogs;
import io.jmix.flowui.testassist.notification.OpenedNotifications;
import io.jmix.flowui.testassist.vaadin.TestServletContext;
import io.jmix.flowui.testassist.vaadin.TestSpringServlet;
import io.jmix.flowui.testassist.vaadin.TestVaadinRequest;
import io.jmix.flowui.testassist.vaadin.TestVaadinSession;
import io.jmix.flowui.testassist.view.initial.InitialView;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import jakarta.servlet.ServletException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField;
import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;

/**
 * Extension starts Vaadin Flow before each test and configures view packages and authentication.
 * <p>
 * For instance:
 * <pre>
 * &#64;SpringBootTest(classes = {TestAssistApplication.class, FlowuiTestAssistConfiguration.class})
 * public class UserViewsTest {
 *
 *     &#64;RegisterExtension
 *     private JmixUiTestExtension extension = new JmixUiTestExtension();
 *
 *     &#64;Autowired
 *     private ViewNavigators viewNavigators;
 *
 *     &#64;Test
 *     public void navigateToUserListView() {
 *         viewNavigators.view(UserListView.class)
 *                 .navigate();
 *
 *         UserListView view = UiTestUtils.getCurrentView();
 *
 *         CollectionContainer&lt;User&gt; usersDc = ViewControllerUtils.getViewData(view)
 *                 .getContainer("usersDc");
 *
 *         Assertions.assertTrue(usersDc.getItems().size() &gt; 0);
 *     }
 * }
 * </pre>
 * For annotation-based approach use {@link UiTest} annotation to configure the extension.
 */
public class JmixUiTestExtension implements TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

    private static final String APP_ID = "testJmixUiAppId";

    public static final String WINDOW_NAME = "windowName";

    protected String[] viewBasePackages;

    // saving session and UI to avoid it be GC'ed
    protected VaadinSession vaadinSession;
    protected UI ui;

    protected UiTestAuthenticator uiTestAuthenticator;
    protected Class<? extends View> initialView;

    /**
     * @return view base packages or {@code null} if not set
     */
    @Nullable
    public String[] getViewBasePackages() {
        return viewBasePackages;
    }

    /**
     * Sets view base packages. Views under these packages will be available in test.
     * <p>
     * Note that depending on the test's configuration, all application views may be available.
     *
     * @param viewBasePackages view base packages
     * @return current instance of extension
     */
    public JmixUiTestExtension withViewBasePackages(@Nullable String... viewBasePackages) {
        this.viewBasePackages = viewBasePackages;
        return this;
    }

    /**
     * @return authenticator or {@code null} if not set
     */
    @Nullable
    public UiTestAuthenticator getTestAuthenticator() {
        return uiTestAuthenticator;
    }

    /**
     * Sets authentication management provider that will be used in tests before/after each test.
     * <p>
     * Provided authenticator will override a bean implementing {@link UiTestAuthenticator} for the test class.
     *
     * @param uiTestAuthenticator authenticator to set
     * @return current instance of extension
     * @see UiTestAuthenticator
     */
    public JmixUiTestExtension withTestAuthenticator(@Nullable UiTestAuthenticator uiTestAuthenticator) {
        this.uiTestAuthenticator = uiTestAuthenticator;
        return this;
    }

    /**
     * @return initial view or {@code null} if not set
     */
    @Nullable
    public Class<? extends View> getInitialView() {
        return initialView;
    }

    /**
     * Sets an initial view that will be opened before each test.
     * <p>
     * Note that for application tests, by default, the Main View class specified in the application properties will
     * be used. If it does not exist, the {@link InitialView} will be used instead.
     *
     * @param initialView the view to set
     * @return current instance of extension
     */
    public JmixUiTestExtension withInitialView(@Nullable Class<? extends View> initialView) {
        this.initialView = initialView;
        return this;
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        initViewBasePackages(context);
        initAuthentication(context);
        initInitialView(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        setupAuthentication(context);
        setupVaadin(context);
        registerViewBasePackages(context);

        navigateToInitialView(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        removeAuthentication(context);
        clearViewBasePackages(context);
        closeOpenedNotifications(context);
        closeOpenedDialogs(context);
    }

    protected void setupVaadin(ExtensionContext context) {
        ApplicationContext applicationContext = getApplicationContext(context);

        SpringServlet springServlet = new TestSpringServlet(applicationContext, true);

        VaadinServletContextInitializer contextInitializer =
                applicationContext.getBean(VaadinServletContextInitializer.class, applicationContext);
        try {
            // We create custom servlet context to disable dev server via attribute (see DevModeServletContextListener).
            // Also, custom TestServletContext enables adding ServletContextListener from
            // VaadinServletContextInitializer.
            TestServletContext servletContext = new TestServletContext();
            servletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
            servletContext.setInitParameter(InitParameters.SERVLET_PARAMETER_PRODUCTION_MODE, "true");

            MockServletConfig mockServletConfig = new MockServletConfig(servletContext);

            // Vaadin adds its own CompositeServletContextListener that contains other listeners:
            // LookupInitializerListener, RouteServletContextListener, DevModeServletContextListener, etc.
            contextInitializer.onStartup(mockServletConfig.getServletContext());

            // Fire event for composite listener from VaadinServletContextInitializer
            servletContext.fireServletContextInitialized();

            springServlet.init(mockServletConfig);
        } catch (ServletException e) {
            throw new IllegalStateException(String.format("Cannot init %s", TestSpringServlet.class.getName()), e);
        }

        VaadinService.setCurrent(springServlet.getService());

        vaadinSession = new TestVaadinSession(springServlet.getService());
        vaadinSession.setAttribute(BackgroundTaskManager.class, new BackgroundTaskManager());
        vaadinSession.setAttribute(UiEventsManager.class, new UiEventsManager());
        VaadinSession.setCurrent(vaadinSession);

        vaadinSession.setConfiguration(springServlet.getService().getDeploymentConfiguration());

        TestVaadinRequest request = new TestVaadinRequest(springServlet.getService());
        CurrentInstance.set(VaadinRequest.class, request);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ui = new UI();
        ui.getInternals().setSession(vaadinSession);

        // ExtendedClientDetails is not available since we don't have client-side here.
        // Mock and stub method to return the same window name (something like a tab's id in browser for Vaadin).
        ExtendedClientDetails clientDetails = Mockito.mock(ExtendedClientDetails.class);
        Mockito.when(clientDetails.getWindowName()).thenReturn(WINDOW_NAME);
        ui.getInternals().setExtendedClientDetails(clientDetails);

        ui.doInit(request, 1, APP_ID);
        UI.setCurrent(ui);
    }

    protected void registerViewBasePackages(ExtensionContext context) {
        ApplicationContext applicationContext = getApplicationContext(context);

        String[] viewBasePackages = getViewBasePackagesToRegister(context);

        List<ViewControllersConfiguration> result;

        if (ArrayUtils.isNotEmpty(viewBasePackages)) {
            // Setup custom View configuration
            result = new ArrayList<>(2);
            result.add(createViewControllersConfiguration(applicationContext, Arrays.asList(viewBasePackages)));
        } else {
            // Setup default View configurations
            Collection<ViewControllersConfiguration> configurations =
                    applicationContext.getBeansOfType(ViewControllersConfiguration.class)
                            .values();

            result = new ArrayList<>(configurations);
        }

        result.add(createViewControllersConfiguration(applicationContext, List.of(initialView.getPackageName())));

        setViewControllersConfigurations(context, result);

        List<String> viewPackages = new ArrayList<>();

        result.forEach(config -> viewPackages.addAll(config.getBasePackages()));

        registerViewRoutes(viewPackages, context);
    }

    protected ViewControllersConfiguration createViewControllersConfiguration(ApplicationContext applicationContext,
                                                                              List<String> viewBasePackages) {
        AnnotationScanMetadataReaderFactory metadataReaderFactory =
                applicationContext.getBean(AnnotationScanMetadataReaderFactory.class);

        ViewControllersConfiguration configuration =
                new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(configuration);
        configuration.setBasePackages(viewBasePackages);

        return configuration;
    }

    protected void registerViewRoutes(List<String> viewBasePackages, ExtensionContext context) {
        if (CollectionUtils.isEmpty(viewBasePackages)) {
            return;
        }

        ApplicationContext applicationContext = getApplicationContext(context);

        List<ViewInfo> viewInfos = applicationContext.getBean(ViewRegistry.class)
                .getViewInfos().stream()
                .filter(info -> isClassInPackages(info.getControllerClass().getPackageName(), viewBasePackages))
                .toList();

        for (ViewInfo view : viewInfos) {
            Class<? extends View> controllerClass = view.getControllerClass();
            Route route = controllerClass.getAnnotation(Route.class);
            if (route == null) {
                continue;
            }

            RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
            if (Strings.isNullOrEmpty(route.value())) {
                continue;
            }

            // Provides ability to overriding Views like in ViewRegistry#registerRoute()
            if (routeConfiguration.isPathAvailable(route.value())) {
                routeConfiguration.removeRoute(route.value());
            }

            routeConfiguration.setRoute(route.value(), controllerClass,
                    getParentChain(route, getDefaultParentChain(context)));
        }
    }

    private void navigateToInitialView(ExtensionContext context) {
        getApplicationContext(context).getBean(ViewNavigationSupport.class)
                .navigate(initialView);
    }

    /* CAUTION! Copied from ViewRegistry#getParentChain() */
    protected List<Class<? extends RouterLayout>> getParentChain(Route route,
                                                                 List<Class<? extends RouterLayout>> defaultChain) {
        Class<? extends RouterLayout> layout = route.layout();
        if (DefaultMainViewParent.class.isAssignableFrom(layout)) {
            return defaultChain;
        }

        // UI is the default route parent, so no need to add it explicitly
        return UI.class == layout ? Collections.emptyList() : List.of(layout);
    }

    /* CAUTION! Copied from ViewRegistry#getDefaultParentChain() */
    @SuppressWarnings("unchecked")
    protected List<Class<? extends RouterLayout>> getDefaultParentChain(ExtensionContext context) {
        String mainViewId = getApplicationContext(context).getBean(UiProperties.class)
                .getMainViewId();
        Optional<ViewInfo> mainViewInfo = getApplicationContext(context).getBean(ViewRegistry.class)
                .findViewInfo(mainViewId);

        if (mainViewInfo.isPresent()) {
            Class<? extends View<?>> controllerClass = mainViewInfo.get().getControllerClass();
            if (RouterLayout.class.isAssignableFrom(controllerClass)) {
                return List.of(((Class<? extends RouterLayout>) controllerClass));
            }
        }

        return Collections.emptyList();
    }

    protected boolean isClassInPackages(String classPackage, List<String> viewBasePackages) {
        return viewBasePackages.stream().anyMatch(classPackage::startsWith);
    }

    protected void setupAuthentication(ExtensionContext context) {
        if (uiTestAuthenticator != null) {
            uiTestAuthenticator.setupAuthentication(getApplicationContext(context));
        } else {
            getApplicationContext(context).getBean(SystemAuthenticator.class).begin();
        }
    }

    protected void removeAuthentication(ExtensionContext context) {
        if (uiTestAuthenticator != null) {
            uiTestAuthenticator.removeAuthentication(getApplicationContext(context));
        } else {
            getApplicationContext(context).getBean(SystemAuthenticator.class).end();
        }
    }

    protected void clearViewBasePackages(ExtensionContext context) {
        try {
            Field configurationsField = getDeclaredField(ViewRegistry.class,
                    "configurations", true);

            ViewRegistry viewRegistry = getApplicationContext(context).getBean(ViewRegistry.class);
            configurationsField.set(viewRegistry, new ArrayList<>());

            getDeclaredField(ViewRegistry.class, "initialized", true)
                    .set(viewRegistry, false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot clear view packages", e);
        }
    }

    protected void closeOpenedNotifications(ExtensionContext context) {
        getApplicationContext(context).getBean(OpenedNotifications.class).closeOpenedNotifications();
    }

    protected void closeOpenedDialogs(ExtensionContext context) {
        getApplicationContext(context).getBean(OpenedDialogs.class).closeOpenedDialogs();
    }

    @Nullable
    protected UiTestAuthenticator getTestAuthenticatorFromAnnotation(ExtensionContext context) {
        Optional<UiTest> annotationOpt =
                AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class);
        if (annotationOpt.isEmpty()) {
            return null;
        }

        Class<? extends UiTestAuthenticator> authenticatorClass = annotationOpt.get().authenticator();
        if (UiTest.DefaultUiTestAuthenticator.class.isAssignableFrom(authenticatorClass)) {
            return null;
        }

        try {
            Constructor<? extends UiTestAuthenticator> constructor = authenticatorClass.getConstructor();
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Cannot instantiate " +
                    UiTestAuthenticator.class.getSimpleName(), e);
        }
    }

    @Nullable
    protected Class<? extends View> getInitialViewFromAnnotation(ExtensionContext context) {
        Class<? extends View> viewClass = AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class)
                .<Class<? extends View>>map(UiTest::initialView)
                .orElse(null);

        // Return null if default value is returned
        if (InitialView.class.equals(viewClass)) {
            return null;
        }

        if (viewClass != null) {
            AnnotationSupport.findAnnotation(viewClass, ViewController.class)
                    .orElseThrow(() -> new IllegalStateException("View must contain "
                            + ViewController.class.getSimpleName() + " annotation"));

            AnnotationSupport.findAnnotation(viewClass, Route.class)
                    .orElseThrow(() -> new IllegalStateException("View must contain "
                            + Route.class.getSimpleName() + " annotation"));
        }

        return viewClass;
    }

    protected String[] getViewBasePackagesToRegister(ExtensionContext context) {
        String[] viewBasePackages = {};
        if (ArrayUtils.isNotEmpty(this.viewBasePackages)) {
            viewBasePackages = this.viewBasePackages;
        } else {
            Optional<UiTest> annotationOpt =
                    AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class);
            if (annotationOpt.isPresent()) {
                viewBasePackages = annotationOpt.get().viewBasePackages();
            }
        }
        return viewBasePackages;
    }

    protected void setViewControllersConfigurations(ExtensionContext context,
                                                    List<ViewControllersConfiguration> configurations) {
        try {
            Field configurationsField = getDeclaredField(ViewRegistry.class,
                    "configurations", true);

            ViewRegistry viewRegistry = getApplicationContext(context).getBean(ViewRegistry.class);

            configurationsField.set(viewRegistry, configurations);

            getDeclaredField(ViewRegistry.class, "initialized", true)
                    .set(viewRegistry, false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set packages to register views", e);
        }
    }

    protected ApplicationContext getApplicationContext(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context);
    }

    protected Class<? extends View> getDefaultInitialViewClass(ExtensionContext context) {
        String mainViewId = getApplicationContext(context).getBean(UiProperties.class).getMainViewId();
        if (!Strings.isNullOrEmpty(mainViewId)) {
            Optional<ViewInfo> viewInfo = getApplicationContext(context).getBean(ViewRegistry.class)
                    .findViewInfo(mainViewId);
            if (viewInfo.isPresent()) {
                return viewInfo.get().getControllerClass();
            }
        }
        return InitialView.class;
    }

    private void initViewBasePackages(ExtensionContext context) {
        if (ArrayUtils.isEmpty(viewBasePackages)) {
            getUiTestAnnotation(context).ifPresent(uiTest -> this.viewBasePackages = uiTest.viewBasePackages());
        }
    }

    private void initAuthentication(ExtensionContext context) {
        if (uiTestAuthenticator == null) {
            uiTestAuthenticator = getTestAuthenticatorFromAnnotation(context);
        }
        if (uiTestAuthenticator == null) {
            uiTestAuthenticator = getApplicationContext(context).getBeanProvider(UiTestAuthenticator.class)
                    .getIfAvailable();
        }
    }

    private void initInitialView(ExtensionContext context) {
        if (initialView == null) {
            initialView = getInitialViewFromAnnotation(context);
        }
        if (initialView == null) {
            initialView = getDefaultInitialViewClass(context);
        }
    }

    private static Optional<UiTest> getUiTestAnnotation(ExtensionContext context) {
        return AnnotationSupport.findAnnotation(context.getTestClass(), UiTest.class);
    }
}
