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

package component.splitlayout

import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.splitlayout.JmixSplitLayout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import tools.jackson.databind.node.ObjectNode

@SpringBootTest
class SplitLayoutI18nTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "SplitLayout takes the splitter accessible name from messages"() {
        when: "A SplitLayout is created"
        def splitLayout = uiComponents.create(JmixSplitLayout)

        then: "The splitter accessible name is the localized default"
        splitLayout.i18n.separator == "Resize separator"

        and: "The localized default is sent to the client"
        (splitLayout.element.getPropertyRaw("i18n") as ObjectNode).get("separator").asText() == "Resize separator"
    }
}
