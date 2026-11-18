/*
 * Copyright 2026 Haulmont.
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

package component_xml_load

import component_xml_load.screen.StepperBrokenView
import component_xml_load.screen.StepperView
import io.jmix.flowui.exception.GuiDevelopmentException
import io.jmix.flowui.kit.component.stepper.JmixStepper
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class StepperXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load stepper and its steps from XML"() {
        when: "Open the StepperView"
        def view = navigateToView(StepperView)

        then: "Stepper attributes are loaded"
        verifyAll(view.stepperId) {
            orientation == JmixStepper.Orientation.VERTICAL
            !validationEnabled
            element.themeList.containsAll(["small", "completed-success"])
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            width == "30em"
            height == "20em"
            steps == [view.firstStep, view.hiddenStep, view.emptyStep]
            selectedStep.is(view.firstStep)
        }

        and: "Step attributes and contents are loaded"
        verifyAll(view.firstStep) {
            label == "First"
            description == "First description"
            icon != null
            content.is(view.firstContent)
        }
        !view.hiddenStep.visible
        view.hiddenStep.content.is(view.hiddenContent)
        view.emptyStep.content == null
    }

    def "A step with more than one child component fails to load"() {
        when: "Open a view whose step has two children"
        navigateToView(StepperBrokenView)

        then:
        def e = thrown(GuiDevelopmentException)
        e.message == "Step 'brokenStep' must have no more than one child component"
    }
}
