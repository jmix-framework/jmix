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

package component.button;

import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.entitypicker.EntityClearAction
import io.jmix.flowui.action.entitypicker.EntityLookupAction
import io.jmix.flowui.action.entitypicker.EntityOpenAction
import io.jmix.flowui.action.list.AddAction
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.kit.action.Action
import io.jmix.flowui.kit.component.button.JmixButton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.spec.FlowuiTestSpecification;

@SpringBootTest
public class JmixButtonTest extends FlowuiTestSpecification {

    @Autowired
    Actions actions

    @Autowired
    UiComponents uiComponents

    def "setAction method copy icon from Action"() {
        def addButton = uiComponents.create(JmixButton);
        def editButton = uiComponents.create(JmixButton);
        def removeButton = uiComponents.create(JmixButton);

        def addAction = actions.create(AddAction.ID)

        when:
        addButton.setAction(addAction)
        editButton.setAction(addAction)
        removeButton.setAction(addAction)

        then:
        addButton.getIcon() != null
        editButton.getIcon() != null
        removeButton.getIcon() != null

        addButton.getIcon().getElement().getAttribute("icon") ==
                addAction.getIcon().getElement().getAttribute("icon");
        editButton.getIcon().getElement().getAttribute("icon") ==
                addAction.getIcon().getElement().getAttribute("icon");
        removeButton.getIcon().getElement().getAttribute("icon") ==
                addAction.getIcon().getElement().getAttribute("icon");

        addButton.getIcon() != editButton.getIcon()
        addButton.getIcon() != removeButton.getIcon()
        editButton.getIcon() != removeButton.getIcon()
    }
}
