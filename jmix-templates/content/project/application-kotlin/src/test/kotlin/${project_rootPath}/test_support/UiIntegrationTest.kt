package ${project_rootPackage}.test_support

import ${project_rootPackage}.${project_classPrefix}Application
import io.jmix.ui.Screens
import io.jmix.ui.screen.Screen
import io.jmix.ui.testassist.UiTestAssistConfiguration
import io.jmix.ui.testassist.junit.UiTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@UiTest(authenticatedUser = "admin", mainScreenId = "MainScreen", screenBasePackages = ["${project_rootPackage}.screen"])
@ContextConfiguration(classes = [${project_classPrefix}Application::class, UiTestAssistConfiguration::class])
@Suppress("UNCHECKED_CAST")
abstract class UiIntegrationTest {

    /**
     * Screens instance to be used in tests
     */
    lateinit var screens: Screens

    @BeforeEach
    fun setUpScreens(screens: Screens) {
        this.screens = screens
        screens.removeAll()
    }

    /**
     * Returns an opened screen by its class.
     * Throws an exception if not found.
     */
    protected fun <T> findOpenScreen(screenClass: Class<T>): T {
        val screen = screens.openedScreens.activeScreens.stream()
            .filter { it: Screen -> screenClass.isAssignableFrom(it.javaClass) }
            .findFirst()
            .orElseThrow()
        Assertions.assertThat(screen)
            .isInstanceOf(screenClass)
        return screen as T
    }

    /**
     * Returns a component defined in the screen by the component id.
     * Throws an exception if not found.
     */
    protected fun <T> findComponent(screen: Screen, componentId: String): T {
        val component = screen.window.getComponent(componentId)
        Assertions.assertThat(component).isNotNull
        return component as T
    }
}