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

package component_trim

import component_trim.view.ComponentTrimView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ComponentTrimTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages "component_trim.view"
    }

    def "#hasTrimming trimming"() {
        given: "Opened view with #hasTrimming component"
        def view = navigateToView ComponentTrimView
        def hasTrimmingComponent = view."$hasTrimming"
        def valueForTrimming = "  value for trimming   "

        when: "Trimming is enabled for #hasTrimming"
        hasTrimmingComponent.value = valueForTrimming

        then: "The value will be trimmed"
        valueForTrimming.trim().equals hasTrimmingComponent.value

        when: "Trimming is disabled for #hasTrimming"
        hasTrimmingComponent.trimEnabled = false
        hasTrimmingComponent.setValue valueForTrimming

        then: "The value will not be trimmed"
        valueForTrimming.equals hasTrimmingComponent.value

        where:
        hasTrimming << ["textField", "textArea"]
    }
}
