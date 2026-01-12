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

import component_xml_load.screen.DrawerLayoutView
import io.jmix.flowui.kit.component.drawerlayout.DrawerMode
import io.jmix.flowui.kit.component.drawerlayout.DrawerPlacement
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DrawerLayoutXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load DrawerLayout attributes"() {
        when: "Open the view"
        def view = navigateToView(DrawerLayoutView)

        then: "Check all attributes"
        verifyAll(view.drawerLayoutAttributes) {
            id.get() == "drawerLayoutAttributes"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            !closeOnModalityCurtainClick
            !displayAsOverlayOnSmallScreen
            style.get("color") == "red"
            drawerHorizontalMaxSize == "10em"
            drawerHorizontalMinSize == "8em"
            drawerHorizontalSize == "9em"
            drawerMode == DrawerMode.PUSH
            drawerPlacement == DrawerPlacement.TOP
            drawerVerticalMaxSize == "10em"
            drawerVerticalMinSize == "8em"
            drawerVerticalSize == "9em"
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
