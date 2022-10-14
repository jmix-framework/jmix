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
import component_xml_load.screen.ContainerView
import io.jmix.core.DataManager
import io.jmix.flowui.component.scroller.JmixScroller
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SplitLayoutXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")
    }

    def "Load splitLayout container from XML"() {
        when: "Open the ContainerView"
        def containerView = openScreen(ContainerView.class)

        then: "SplitLayout attributes will be loaded"
        verifyAll(containerView.splitLayoutId) {
            id.get() == "splitLayoutId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
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
        }
    }

    def "Throw broken splitLayout content exception"() {
        //TODO: kremnevda, fix forwarding JmixInternalServerError 12.05.2022
        /*when: "Open the ContainerView with brokenSplit"
        openScreen(BrokenSplitView.class)

        then: "Throw GuiDevelopmentException"
        def e = thrown GuiDevelopmentException
        e.message == "Split 'Optional[incorrectSplitLayoutId]' must contain only two children"*/
    }
}
