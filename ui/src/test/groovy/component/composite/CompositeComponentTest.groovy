/*
 * Copyright (c) 2008-2019 Haulmont.
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

package component.composite

import component.composite.appevent.TestAppEvent
import component.composite.component.TestCommentaryPanel
import component.composite.component.TestProgrammaticCommentaryPanel
import component.composite.component.TestStepperField
import component.composite.screen.CommentaryPanelTestScreen
import component.composite.screen.EventPanelTestScreen
import component.composite.screen.MultipleStepperFieldsTestScreen
import component.composite.screen.StepperFieldTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.AppUI
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Button
import io.jmix.ui.component.impl.WindowTestHelper
import io.jmix.ui.model.InstanceContainer
import io.jmix.ui.screen.StandardOutcome
import io.jmix.ui.screen.UiControllerUtils
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.OrderLine

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class CompositeComponentTest extends ScreenSpecification {

    @Autowired
    ScreenBuilders screenBuilders

    @Override
    void setup() {
        exportScreensPackages(['component.composite'])
    }

    def "Composite component as field in an editor screen and relative path to descriptor"() {
        def mainScreen = showTestMainScreen()

        def testScreen = screenBuilders.editor(OrderLine, mainScreen)
                .withScreenClass(StepperFieldTestScreen)
                .newEntity()
                .show()

        when:
        TestStepperField quantityField =
                testScreen.getWindow().getComponentNN("quantityField") as TestStepperField

        then: "quantityField is loaded"
        quantityField != null

        when: "Set value to item"
        InstanceContainer<OrderLine> lineDc =
                UiControllerUtils.getScreenData(testScreen).getContainer("lineDc")
        lineDc.item.quantity = 10

        then: "Composite component value is changed respectively"
        quantityField.value == 10

        when: "Set value to composite component"
        quantityField.value = 20

        then: "Item value is changed respectively"
        lineDc.item.quantity == 20
    }

    def "Composite component containing a DataGrid with MetaClass and full path to descriptor"() {
        showTestMainScreen()

        def testScreen = screens.create(CommentaryPanelTestScreen)
        testScreen.show()

        when:
        TestCommentaryPanel commentaryPanel =
                testScreen.getWindow().getComponentNN("commentaryPanel") as TestCommentaryPanel

        then: "commentaryPanel is loaded"
        commentaryPanel != null

        when:
        Button sendBtn = commentaryPanel.getSendButton()

        then: "Button has caption"
        sendBtn.getCaption() == "Send"
    }

    def "Composite component with programmatic creation of nested components"() {

        when:
        TestProgrammaticCommentaryPanel commentaryPanel = uiComponents.create(TestProgrammaticCommentaryPanel.NAME)

        then: "TestProgrammaticCommentaryPanel is created"
        commentaryPanel != null

        when:
        Button sendBtn = commentaryPanel.getSendButton()

        then: "Button has caption"
        sendBtn.caption == "Send"
    }

    def "Composite component handles ApplicationEvent fired by EventsMulticaster"() {
        showTestMainScreen()

        def screen = (EventPanelTestScreen) screens.create(EventPanelTestScreen)
        screen.show()

        when: "Fire app event via EventsMulticaster"
        AppUI.current.uiEventsMulticaster.multicastEvent(new TestAppEvent(screen))

        then: "Event should be handled by composite component"
        screen.testEventPanel.eventCounter == 1

        when: "Close screen and fire event again"
        screen.close(StandardOutcome.CLOSE)
        AppUI.current.uiEventsMulticaster.multicastEvent(new TestAppEvent(screen))

        then: "Component should unsubscribe and event should not be handled"
        screen.testEventPanel.eventCounter == 1
    }
    
    def "Multiple composite components of the same type have unique id for nested components"() {
        showTestMainScreen()

        def screen = (MultipleStepperFieldsTestScreen) screens.create(MultipleStepperFieldsTestScreen)
        screen.show()


        def window = screen.getWindow();

        expect: "2 StepperField"
        window.getOwnComponents().size() == 2

        and: "2 StepperField + 2 * (4 nested components) = 10"
        WindowTestHelper.getAllComponents(window).size() == 10
    }
}
