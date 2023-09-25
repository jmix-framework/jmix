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

package view_overriding

import com.vaadin.flow.component.UI
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import view_overriding.view_inheritor.ViewOverridingInheritor
import view_overriding.view.ViewOverridingTestView

@SpringBootTest
class ViewOverridingTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("view_overriding.view")
        registerViewBasePackages("view_overriding.view_inheritor")
    }

    def "Check overriding in navigation by id"() {
        when: "Open View and perform navigation to overridden View by ID"

        def view = navigateToView(ViewOverridingTestView)
        view.navigateToViewByIdBtn.click()

        then: "Current View should be an instance of inheritor"

        def currentView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        currentView instanceof ViewOverridingInheritor
    }

    def "Check overriding in navigation by class"() {
        when: "Open View and perform navigation to overridden View by Class"

        def view = navigateToView(ViewOverridingTestView)
        view.navigateToViewByClassBtn.click()

        then: "Current View should be an instance of inheritor"

        def currentView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        currentView instanceof ViewOverridingInheritor
    }
}
