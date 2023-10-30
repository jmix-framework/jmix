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

package main_view

import main_view.view.MainView
import main_view.view.SandboxView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MainViewTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("main_view.view")
    }

    def "initialLayout is added to the main view"() {
        when: "Open main view with initialLayout"
        def mainView = navigateToView(MainView)

        then: "initialLayout is loaded, its content can be injected and is added to the view content"
        mainView.getInitialLayout() != null
        mainView.textField != null
        mainView.getContent().getComponent(mainView.textField.getId().orElseThrow()) != null

        when: "a view is opened in the main view with initialLayout"
        navigateToView(SandboxView)

        then: "initialLayout is replaced with view content"
        !mainView.getContent().getComponents().contains(mainView.textField)
    }
}
