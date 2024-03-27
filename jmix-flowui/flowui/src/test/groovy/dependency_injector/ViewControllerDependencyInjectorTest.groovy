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

package dependency_injector


import dependency_injector.view.DependencyInjectorView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ViewControllerDependencyInjectorTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("dependency_injector.view")
    }

    def "Tab injection when TabSheet is placed in component container"() {
        when: "Open the DependencyInjectorView"
        def dependencyInjectorView = navigateToView(DependencyInjectorView.class)

        then: "Tab fields should be injected correctly"
        dependencyInjectorView.tabSheet != null
        dependencyInjectorView.tabSheetTab1 != null
        dependencyInjectorView.tabSheetTab1.id.get() == "tab1"
        dependencyInjectorView.tabSheetTab2 != null
        dependencyInjectorView.tabSheetTab2.id.get() == "tab2"
    }
}
