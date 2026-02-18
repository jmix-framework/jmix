/*
 * Copyright 2026 Haulmont.
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

import component_xml_load.screen.SidePanelLayoutTestView
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelMode
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelPosition
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SidePanelLayoutXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load SidePanelLayout attributes"() {
        when: "Open the view"
        def view = navigateToView(SidePanelLayoutTestView)

        then: "Check all attributes"
        verifyAll(view.sidePanelLayoutAttributes) {
            id.get() == "sidePanelLayoutAttributes"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            !closeOnOutsideClick
            !displayAsOverlayOnSmallDevices
            style.get("color") == "red"
            sidePanelHorizontalMaxSize == "10em"
            sidePanelHorizontalMinSize == "8em"
            sidePanelHorizontalSize == "9em"
            sidePanelMode == SidePanelMode.PUSH
            sidePanelPosition == SidePanelPosition.TOP
            sidePanelVerticalMaxSize == "10em"
            sidePanelVerticalMinSize == "8em"
            sidePanelVerticalSize == "9em"
            height == "100px"
            maxHeight == "100px"
            maxWidth == "100px"
            minHeight == "100px"
            minWidth == "100px"
            !modal
            overlayAriaLabel == "overlayAriaLabel"
            visible
            width == "100px"
        }
    }
}
