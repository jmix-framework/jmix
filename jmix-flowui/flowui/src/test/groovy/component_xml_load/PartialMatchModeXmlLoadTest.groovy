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

import com.vaadin.flow.component.combobox.ComboBoxBase
import com.vaadin.flow.component.combobox.PartialMatchMode
import component_xml_load.screen.PartialMatchModeView
import io.jmix.flowui.component.UiComponentUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PartialMatchModeXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load #componentId partialMatchMode from XML"() {
        when: "Open the PartialMatchModeView"
        def view = navigateToView(PartialMatchModeView)

        then: "#componentId partialMatchMode will be loaded"
        def component = UiComponentUtils.findComponent(view, componentId).orElseThrow() as ComboBoxBase
        component.partialMatchMode == expectedMode

        where:
        componentId                 | expectedMode
        "comboBox"                  | PartialMatchMode.FIRST_MATCH
        "entityComboBox"            | PartialMatchMode.ONLY_MATCH
        "multiSelectComboBox"       | PartialMatchMode.FIRST_MATCH
        "multiSelectComboBoxPicker" | PartialMatchMode.ONLY_MATCH
    }
}
