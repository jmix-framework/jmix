package ${project_rootPackage}.test_support

import ${project_rootPackage}.${project_classPrefix}Application
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration
import io.jmix.flowui.testassist.UiTest
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.View
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@UiTest
@SpringBootTest(classes = [${project_classPrefix}Application::class, FlowuiTestAssistConfiguration::class])
abstract class UiIntegrationTest {

    @Autowired
    lateinit var viewNavigators: ViewNavigators

    /**
     * Returns instance of currently navigated view.
     */
    protected fun <T : View<*>> getCurrentView(): T {
        return UiTestUtils.getCurrentView()
    }

    /**
     * Returns a component defined in the screen by the component id.
     * Throws an exception if not found.
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> findComponent(view: View<*>, componentId: String): T {
        val component = UiComponentUtils.findComponent(view, componentId)
        Assertions.assertTrue(component.isPresent)
        return component.get() as T
    }
}