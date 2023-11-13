/*
 * Copyright 2022 Haulmont.
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

package actions

import io.jmix.flowui.Actions
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.entitypicker.EntityClearAction
import io.jmix.flowui.action.entitypicker.EntityLookupAction
import io.jmix.flowui.action.entitypicker.EntityOpenAction
import io.jmix.flowui.component.combobox.EntityComboBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ActionTest extends FlowuiTestSpecification {

    @Autowired
    Actions actions

    @Autowired
    UiComponents uiComponents

    def "removeAllActions method correctly removes all actions"() {
        def entityComboBox = uiComponents.create(EntityComboBox)

        when:
        entityComboBox.addAction(actions.create(EntityOpenAction.ID))
        entityComboBox.addAction(actions.create(EntityLookupAction.ID))
        entityComboBox.addAction(actions.create(EntityClearAction.ID))

        then:
        entityComboBox.getActions().size() == 3

        when:
        entityComboBox.removeAllActions()

        then:
        noExceptionThrown()
        entityComboBox.getActions().isEmpty()
    }
}
