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

package autowire.fragment

import autowire.fragment.view.AutowireFragmentLifecycleEventsHostView
import autowire.fragment.view.AutowireFragmentLifecycleEventsProgrammaticHostView
import io.jmix.flowui.Fragments
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FragmentLifecycleEventsDependencyInjectorTest extends FlowuiTestSpecification {

    @Autowired
    Fragments fragments

    @Override
    void setup() {
        registerViewBasePackages("autowire.fragment.view")
    }

    def "Autowire lifecycle event listeners for fragments in the correct order using XML loading"() {
        when: "Host view will be opened"
        def view = navigateToView AutowireFragmentLifecycleEventsHostView

        then: "Lifecycle event listeners must be fired in the correct order"
        def events = view.fragment.executedEvents
        events == [
                "Fragment.ReadyEvent",
                "Host.InitEvent", "Fragment.Host.InitEvent",
                "Host.BeforeShowEvent", "Fragment.Host.BeforeShowEvent",
                "Host.ReadyEvent", "Fragment.Host.ReadyEvent"
        ]
    }

    def "Autowire lifecycle event listeners for fragments in the correct order using programmatic definition"() {
        when: "Host view will be opened"
        def view = navigateToView AutowireFragmentLifecycleEventsProgrammaticHostView

        then: "Lifecycle event listeners must be fired in the correct order"
        def events = view.fragment.executedEvents
        // Fragment.Host.InitEvent is skipped because the fragment is created on Host.InitEvent
        events == [
                "Host.InitEvent",
                "Fragment.ReadyEvent",
                "Host.BeforeShowEvent", "Fragment.Host.BeforeShowEvent",
                "Host.ReadyEvent", "Fragment.Host.ReadyEvent"
        ]
    }
}
