package component_registration

import com.vaadin.flow.component.UI
import component_registration.screen.ComponentRegistrationTestScreen
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.UiComponents
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = ComponentsRegistrationTestConfiguration)
class ComponentRegistrationTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Autowired
    ViewNavigators screenNavigators

    void setup() {
        registerScreenBasePackages("component_registration.screen")
    }

    def "Check component replacement"() {
        when: """
              Component replacement has the following view:
                JmixButton <- TestFirstJmixButton <- TestSecondJmixButton
                JmixButton <- TestThirdJmixButton
              The 'TestThirdJmixButton' has the priority.
              """

        def jmixBtn = uiComponents.create(JmixButton)
        def firstBtn = uiComponents.create(TestFirstJmixButton)
        def secondBtn = uiComponents.create(TestSecondJmixButton)
        def thirdBtn = uiComponents.create(TestThirdJmixButton)

        then: "Created components should have types"

        jmixBtn.class == TestThirdJmixButton
        firstBtn.class == TestSecondJmixButton
        secondBtn.class == TestSecondJmixButton
        thirdBtn.class == TestThirdJmixButton
    }

    def "Override button loader"() {
        when: "Open screen with declarative button"

        screenNavigators.view(ComponentRegistrationTestScreen)
                .navigate()

        then: "Loaded button should have default text that is defined in the extended loader"

        def screen = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        screen instanceof ComponentRegistrationTestScreen

        ((ComponentRegistrationTestScreen) screen).button.text == ExtButtonLoader.DEFAULT_TEXT
    }
}
