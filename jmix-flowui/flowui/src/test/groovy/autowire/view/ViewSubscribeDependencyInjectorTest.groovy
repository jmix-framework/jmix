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
class ViewSubscribeDependencyInjectorTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("autowire.view")
    }

    def "Autowire #dataElement subscription into the view"() {
        when: "SubscribeDependencyInjectorView is opened"
        def view = navigateToView ViewSubscribeDependencyInjectorView

        then: "Markers are false"
        !(view.checkExecutedEvent "${dataElement}")

        when: "Events published"
        view.publishDataEvents()

        then: "The event listener logic will be executed"
        view.checkExecutedEvent "${dataElement}"

        where:
        dataElement << [
                "PreSaveEvent", "PostSaveEvent",
                "PreLoadEvent", "PostLoadEvent",
                "CollectionChangeEvent", "ItemChangeEvent", "ItemPropertyChangeEvent"
        ]
    }

    def "Autowire #component subscriptions into the view"() {
        when: "SubscribeDependencyInjectorView is opened"
        def view = navigateToView ViewSubscribeDependencyInjectorView

        then: "Markers are false"
        !(view.checkExecutedEvent "${component}")

        when: "Events published"
        view.publishComponentEvents()

        then: "The event listener logic will be executed"
        view.checkExecutedEvent "${component}"

        where:
        component << [
                "ActionPerformedEvent", "PropertyChangeEvent",
                "TypedValueChangeEvent", "ComponentValueChangeEvent", "SelectedChangeEvent",
                "HasActionComponent.ActionPerformedEvent", "DropdownButton.NestedElement.ClickEvent"
        ]
    }

    def "Autowire #viewEvent subscriptions into the view"() {
        when: "SubscribeDependencyInjectorView is opened"
        def view = navigateToView ViewSubscribeDependencyInjectorView

        then: "Events published"
        view.checkExecutedEvent "${viewEvent}"

        where:
        viewEvent << ["InitEvent", "BeforeShowEvent", "ReadyEvent"]
    }
}
