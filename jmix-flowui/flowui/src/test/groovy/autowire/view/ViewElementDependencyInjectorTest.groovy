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

package autowire.view

import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ViewElementDependencyInjectorTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("autowire.view")
    }

    def "Autowire #element annotated with ViewComponent into the view"() {
        when: "ElementDependencyInjectorView is opened"
        def elementDependencyInjectorView = navigateToView ViewElementDependencyInjectorView

        then: "#element should be autowired"
        elementDependencyInjectorView."$element" != null

        where:
        element << [
                "component",
                "instanceDc", "collectionDc", "keyValueCollectionDc",
                "instanceDl", "collectionDl", "keyValueCollectionDl",
                "dataContext",
                "viewAction", "componentAction",
                "facet_1", "facet_2",
                "messageBundle"
        ]
    }

    def "Tab autowiring when TabSheet is placed in component container"() {
        when: "ElementDependencyInjectorView is opened"
        def elementDependencyInjectorView = navigateToView ViewElementDependencyInjectorView

        then: "Tab fields should not be null"
        verifyAll(elementDependencyInjectorView) {
            tabSheet != null
            tabSheetTab1 != null
            tabSheetTab2 != null
            tabSheetTab1.id.orElse(null) == "tab1"
            tabSheetTab2.id.orElse(null) == "tab2"
        }
    }
}
