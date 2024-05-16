/*
 * Copyright 2024 Haulmont.
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

package autowire

import autowire.view.ClickNotifierDependencyInjectorView
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ClickNotifierDependencyInjectorTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("autowire.view")
    }

    def "Autowire #clickEvent"() {
        when: "ElementDependencyInjectorView is opened"
        def elementDependencyInjectorView = navigateToView ClickNotifierDependencyInjectorView

        then: "#clickEvent listener should be assigned to the corresponding button"
        def currentButton = elementDependencyInjectorView."${clickEvent}Id" as JmixButton
        currentButton != null
        currentButton.click()
        currentButton.text == "${clickEvent} performed"

        where:
        clickEvent << ["clickListener", "singleClickListener", "doubleClickListener", "defaultClickListener"]
    }
}
