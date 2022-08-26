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

package component_container

import component_container.view.ComponentContainerView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ComponentContainerTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerScreenBasePackages("component_container.view")
    }

    def "VerticalLayout.getChildren() returns own components"() {
        when: "Open the view"
        def view = openScreen(ComponentContainerView.class)

        then:
        noExceptionThrown()

        and: "getChildren() returns own components"
        view.vbox.getComponentCount() == 3
    }

    def "Details implements ComponentContainer"() {
        when: "Open the view"
        def view = openScreen(ComponentContainerView.class)
        def details = view.details

        then:
        noExceptionThrown()

        and: "getContent() returns own components"
        details.getContent().count() == 2

        and: "ComponentContainer methods implemented"
        details.getOwnComponents().size() == 2
        details.getComponents().size() == 4
    }

    def "Accordion implements ComponentContainer"() {
        when: "Open the view"
        def view = openScreen(ComponentContainerView.class)
        def details = view.accordion

        then:
        noExceptionThrown()

        and: "getChildren() returns own components"
        details.getChildren().count() == 2

        and: "ComponentContainer methods implemented"
        details.getOwnComponents().size() == 2
        details.getComponents().size() == 13
    }
}
