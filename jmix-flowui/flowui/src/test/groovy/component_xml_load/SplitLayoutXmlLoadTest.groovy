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

package component_xml_load

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.splitlayout.SplitLayout
import component_xml_load.screen.BrokenSplitView
import component_xml_load.screen.BrokenSplitWithI18nView
import component_xml_load.screen.ContainerView
import component_xml_load.screen.SplitI18nView
import io.jmix.core.DataManager
import io.jmix.flowui.component.scroller.JmixScroller
import io.jmix.flowui.exception.GuiDevelopmentException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.spec.FlowuiTestSpecification
import tools.jackson.databind.node.ObjectNode

@SpringBootTest
class SplitLayoutXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load splitLayout container from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "SplitLayout attributes will be loaded"
        verifyAll(containerView.splitLayoutId) {
            id.get() == "splitLayoutId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            orientation == SplitLayout.Orientation.HORIZONTAL
            themeNames.containsAll(["small", "minimal"])
            visible
            width == "100px"
            getOwnComponent("splitPrimaryChild") instanceof JmixScroller
            getOwnComponent("splitSecondaryChild") instanceof VerticalLayout
            ownComponents.size() == 2
            i18n.separator == "separatorString"
            (element.getPropertyRaw("i18n") as ObjectNode).get("separator").asText() == "separatorString"
        }
    }

    def "Load splitLayout i18n from XML on top of the localized defaults"() {
        when: "Open the SplitI18nView"
        def view = navigateToView(SplitI18nView.class)

        then: "A split without i18n keeps the localized default"
        view.noI18nSplit.i18n.separator == "Resize separator"

        and: "An empty i18n element keeps the localized default"
        view.emptyI18nSplit.i18n.separator == "Resize separator"
        (view.emptyI18nSplit.element.getPropertyRaw("i18n") as ObjectNode).get("separator").asText() ==
                "Resize separator"

        and: "A message key is resolved from the view message group"
        view.localizedI18nSplit.i18n.separator == "Localized separator"
        (view.localizedI18nSplit.element.getPropertyRaw("i18n") as ObjectNode).get("separator").asText() ==
                "Localized separator"
    }

    def "Throw broken splitLayout content exception"() {
        when: "Open the ContainerView with brokenSplit"
        navigateToView(BrokenSplitView.class)

        then: "Throw GuiDevelopmentException"
        def e = thrown GuiDevelopmentException
        e.message == "Split 'Optional[brokenSplit]' must contain only two children"
    }

    def "Do not count the i18n element as a splitLayout child"() {
        when: "Open the view with a split that has one component and an i18n element"
        navigateToView(BrokenSplitWithI18nView.class)

        then: "Throw GuiDevelopmentException"
        def e = thrown GuiDevelopmentException
        e.message == "Split 'Optional[brokenSplitWithI18n]' must contain only two children"
    }
}
