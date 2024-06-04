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

package component.generic_component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.icon.VaadinIcon
import component.generic_component.view.GenericComponentTestView
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.lang.Nullable
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class GenericComponentTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component.generic_component.view")
    }

    def "Open view with generic components"() {
        def view = navigateToView(GenericComponentTestView)

        when:
        def button = view.button

        then:
        button instanceof JmixButton
        button.text == "Button"
        getIconAttribute(button.icon) == getIconAttribute(VaadinIcon.PLUS.create())

        when:
        def propertyFilter = view.propertyFilter

        then:
        propertyFilter instanceof PropertyFilter
        propertyFilter.property == "name"
        propertyFilter.operation == PropertyFilter.Operation.CONTAINS
        propertyFilter.dataLoader == view.productsDl
        propertyFilter.parameterName == "productName"
        propertyFilter.label == "Property Filter"
        propertyFilter.tabIndex == 2

        when:
        def testComponent = view.testComponent

        then:
        testComponent.stringsList == List.of("a", "b", "c")
        testComponent.stringsSet == Set.of("a", "b", "c")
        testComponent.stringsArray == new String[]{"a", "b", "c"}
        testComponent.strings == new String[]{"a", "b", "c"}
        testComponent.dataContainer == view.productsDc
    }

    @Nullable
    private static String getIconAttribute(Component icon) {
        return icon != null ? icon.element.getAttribute("icon") : null
    }
}
