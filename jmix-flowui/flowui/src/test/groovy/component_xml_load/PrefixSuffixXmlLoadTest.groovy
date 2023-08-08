/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.shared.HasPrefix
import com.vaadin.flow.component.shared.HasSuffix
import component_xml_load.screen.PrefixSuffixView
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PrefixSuffixXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load #hasPrefixAndSuffixComponent prefix and suffix from XML"() {
        when: "Open the PrefixSuffixView"
        def prefixSuffixView = navigateToView(PrefixSuffixView)

        then: "#hasPrefixAndSuffixComponent prefix and suffix components will be loaded"
        checkPrefixAndSuffix(prefixSuffixView."${hasPrefixAndSuffixComponent}" as Component)

        where:
        hasPrefixAndSuffixComponent << ["bigDecimalField", "button", "drawerToggle", "emailField", "entityPicker",
                                        "integerField", "multiValuePicker", "numberField", "passwordField", "textArea",
                                        "textField", "valuePicker", "tabSheet"]
    }

    def "Load #hasPrefixComponent prefix from XML"() {
        when: "Open the PrefixSuffixView"
        def prefixSuffixView = navigateToView(PrefixSuffixView)

        then: "#hasPrefixComponent prefix component will be loaded"
        checkPrefix(prefixSuffixView."${hasPrefixComponent}" as HasPrefix)

        where:
        hasPrefixComponent << ["comboBox", "datePicker", "entityComboBox", "select", "timePicker"]
    }

    boolean checkPrefixAndSuffix(Component component) {
        return checkPrefix(component as HasPrefix) && checkSuffix(component as HasSuffix)
    }

    boolean checkPrefix(HasPrefix component) {
        def prefixComponent = component.getPrefixComponent()

        prefixComponent instanceof Icon
                && prefixComponent.getId().get() == "prefixIcon"
                && prefixComponent.element.getAttribute("icon") == "vaadin:question-circle"
    }

    boolean checkSuffix(HasSuffix component) {
        def suffixComponent = component.getSuffixComponent()

        suffixComponent instanceof JmixButton
                && suffixComponent.getId().get() == "suffixButton"
                && (suffixComponent as JmixButton).text == "Suffix Button"
                && (suffixComponent as JmixButton).icon.element.getAttribute("icon") == "vaadin:pencil"
    }
}
