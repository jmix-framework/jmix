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

package component.tabsheet

import component.tabsheet.view.TabSheetTestView
import io.jmix.flowui.component.UiComponentUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class TabSheetTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component.tabsheet")
    }

    def "Load TabSheet with lazy tabs"() {
        when: "Open screen with TabSheet and some its tabs are not loaded because they are lazy"
        def screen = navigateToView(TabSheetTestView)

        then:

        screen.tab1Span != null
        screen.tab2Button == null
        screen.tab3Checkbox != null

        screen.tabSheet.findComponent("tab2Button").isEmpty()
        screen.tabSheet.setSelectedTab(screen.tab2)
        screen.tabSheet.findComponent("tab2Button").isPresent()

        screen.tabSheet.findComponent("tab4Span").isEmpty()
        screen.tabSheet.setSelectedTab(screen.tab4)
        screen.tabSheet.findComponent("tab4Span").isPresent()
    }

    def "Selected tab content is attached to the view after navigation"() {
        when: "Open a view with TabSheet"
        def screen = navigateToView(TabSheetTestView)

        then: "Content of the selected tab is attached to the view"
        UiComponentUtils.findView(screen.tab1Span) == screen
    }

    def "Selected tab content is attached to the view after switching tabs without navigation"() {
        given: "An open view with a TabSheet"
        def screen = navigateToView(TabSheetTestView)

        when: "Switching to another tab programmatically, with no navigation happening"
        screen.tabSheet.setSelectedIndex(2)

        then: "Content of the newly selected tab is attached to the view"
        UiComponentUtils.findView(screen.tab3Checkbox) == screen
    }
}
