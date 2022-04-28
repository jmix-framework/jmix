package test_support.spec

import com.google.common.base.Strings
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.server.InitParameters
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.SpringServlet
import com.vaadin.flow.spring.VaadinServletContextInitializer
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.core.security.SystemAuthenticator
import io.jmix.flowui.screen.Screen
import io.jmix.flowui.screen.ScreenRegistry
import io.jmix.flowui.sys.UiControllersConfiguration
import org.apache.commons.lang3.ArrayUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockServletConfig
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification
import test_support.FlowuiTestConfiguration
import test_support.TestServletContext
import test_support.TestSpringServlet
import test_support.TestVaadinRequest
import test_support.TestVaadinSession

import javax.servlet.ServletException

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField
import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE

@ContextConfiguration(classes = [FlowuiTestConfiguration])
class FlowuiTestSpecification extends Specification {

    @Autowired
    ApplicationContext applicationContext

    // saving session and UI to avoid it be GC'ed
    protected VaadinSession vaadinSession
    protected UI ui

    void setup() {
        setupAuthentication()
        setupVaadinUi()
        registerScreenBasePackages()
    }

    void cleanup() {
        removeAuthentication()
    }

    protected void setupAuthentication() {
        SystemAuthenticator systemAuthenticator = applicationContext.getBean(SystemAuthenticator.class)
        systemAuthenticator.begin()
    }

    protected void removeAuthentication() {
        SystemAuthenticator systemAuthenticator = applicationContext.getBean(SystemAuthenticator.class)
        systemAuthenticator.end()
    }

    protected void setupVaadinUi() {
        SpringServlet springServlet = new TestSpringServlet(applicationContext, true)

        VaadinServletContextInitializer contextInitializer =
                applicationContext.getBean(VaadinServletContextInitializer.class, applicationContext)
        try {
            TestServletContext servletContext = new TestServletContext()
            servletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext)
            servletContext.setInitParameter(InitParameters.SERVLET_PARAMETER_ENABLE_DEV_SERVER, "false")

            MockServletConfig mockServletConfig = new MockServletConfig(servletContext)

            // fill servlet context with listeners from VaadinServletContextInitializer
            contextInitializer.onStartup(mockServletConfig.getServletContext())

            servletContext.fireServletContextInitialized()

            springServlet.init(mockServletConfig)
        } catch (ServletException e) {
            throw new IllegalStateException(String.format("Cannot init %s", TestSpringServlet.class.getName()), e)
        }

        VaadinService.setCurrent(springServlet.getService())

        vaadinSession = new TestVaadinSession(springServlet.getService())
        VaadinSession.setCurrent(vaadinSession)

        def request = new TestVaadinRequest(springServlet.getService())

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))

        ui = new UI()
        ui.getInternals().setSession(vaadinSession)
        ui.doInit(request, 1)
        UI.setCurrent(ui)
    }

    protected void registerScreenBasePackages(String[] screenBasePackages) {
        if (ArrayUtils.isEmpty(screenBasePackages)) {
            return
        }

        def metadataReaderFactory = applicationContext.getBean(AnnotationScanMetadataReaderFactory.class)
        ScreenRegistry windowConfig = applicationContext.getBean(ScreenRegistry.class)

        def configuration = new UiControllersConfiguration(applicationContext, metadataReaderFactory)

        def injector = applicationContext.getAutowireCapableBeanFactory()
        injector.autowireBean(configuration)

        configuration.setBasePackages(Arrays.asList(screenBasePackages))

        try {
            def configurationsField = getDeclaredField(ScreenRegistry.class,
                    "configurations", true)
            //noinspection unchecked
            def configurations = (Collection<UiControllersConfiguration>) configurationsField.get(windowConfig)

            def modifiedConfigurations = new ArrayList<>(configurations)
            modifiedConfigurations.add(configuration)

            configurationsField.set(windowConfig, modifiedConfigurations)

            getDeclaredField(ScreenRegistry.class, "initialized", true)
                    .set(windowConfig, false)
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot register screen packages", e)
        }

        registerScreenRoutes(screenBasePackages)
    }

    protected void registerScreenRoutes(String[] screenBasePackages) {
        if (ArrayUtils.isEmpty(screenBasePackages)) {
            return
        }

        def screenRegistry = applicationContext.getBean(ScreenRegistry.class)
        def screens = screenRegistry.getScreens()
        screens.forEach({
            Class<? extends Screen> controllerClass = it.getControllerClass()
            Route route = controllerClass.getAnnotation(Route.class)
            if (route == null) {
                return
            }

            RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope()
            if (Strings.isNullOrEmpty(route.value())
                    || routeConfiguration.isPathAvailable(route.value())) {
                return
            }

            routeConfiguration.setRoute(route.value(), controllerClass)
        })
    }
}
