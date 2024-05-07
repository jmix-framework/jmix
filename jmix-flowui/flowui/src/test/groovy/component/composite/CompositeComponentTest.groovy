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

package component.composite

import com.vaadin.flow.component.Component
import component.composite.component.TestDataGridPanel
import component.composite.component.TestStepperField
import io.jmix.flowui.UiComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.lang.Nullable
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class CompositeComponentTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "Composite component without xml"() {

        def stepperField = uiComponents.create(TestStepperField)

        when:
        def content = stepperField.getContent()

        then:
        noExceptionThrown()
    }

    def "Composite component containing a DataGrid with MetaClass"() {
        def dataGridPanel = uiComponents.create(TestDataGridPanel)

        when:
        def content = dataGridPanel.getContent()

        then:
        noExceptionThrown()
    }

    @Nullable
    private static String getIconAttribute(Component icon) {
        return icon != null ? icon.element.getAttribute("icon") : null
    }
}
