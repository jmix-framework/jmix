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

package view_root_component

import com.vaadin.flow.component.html.Div
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import view_root_component.view.ViewRootComponentView

@SpringBootTest
class ViewTootComponentTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("view_root_component")
    }

    def "any HasComponents component can be view root component"() {
        when:
        def view = navigateToView(ViewRootComponentView)

        then:
        noExceptionThrown()

        when:
        def content = view.getContent()

        then:
        !content.isEnabled()
        content.getClassName() == "foo-bar"
        content.getMinHeight() == "10em"
        content.getHeight() == "15em"
        content.getMaxHeight() == "20em"
        content.getMinWidth() == "10em"
        content.getWidth() == "15em"
        content.getMaxWidth() == "20em"
    }
}
