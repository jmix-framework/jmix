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

package xml_inheritance


import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import xml_inheritance.view.XmlInheritanceExtBaseTestView

@SpringBootTest
class XmlInheritanceTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerScreenBasePackages("xml_inheritance.view")
    }

    def "ext:index in extended view"() {
        when: "Create view that extends another one"
        def view = (XmlInheritanceExtBaseTestView) openScreen(XmlInheritanceExtBaseTestView)

        then: "Components that use ext:index should change their position"

        def hboxNew = view.hboxAddNew
        hboxNew.getComponentAt(0).id.get() == "new1"

        def hboxUp = view.hboxMoveBaseToUp
        hboxUp.getComponentAt(2).id.get() == "up1"

        def hboxDown = view.hboxMoveBaseToDown
        hboxDown.getComponentAt(1).id.get() == "down3"
    }

    def "title from extended view is used"() {
        when: "Create view that extends another one"
        def view = (XmlInheritanceExtBaseTestView) openScreen(XmlInheritanceExtBaseTestView)

        then: "Title from extended view is used"
        view.pageTitle == "Extended title"
    }
}
