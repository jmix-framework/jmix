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

import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import icon_xml_load.view.IconsView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class IconXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerScreenBasePackages("icon_xml_load.view")
    }

    def "Load button's icon as VaadinIcon enum"() {
        when: "Open the IconsView"
        def view = openScreen(IconsView.class)

        then:
        view.buttonEnumIcon.icon.element.getAttribute("icon")
                == VaadinIcon.MENU.create().element.getAttribute("icon")
    }

    def "Load button's icon as full vaadin-icon definition"() {
        when: "Open the IconsView"
        def view = openScreen(IconsView.class)

        then:
        view.buttonVaadinIcon.icon.element.getAttribute("icon")
                == new Icon("vaadin", "menu").element.getAttribute("icon")
    }

    def "Load button's icon as full lumo-icon definition"() {
        when: "Open the IconsView"
        def view = openScreen(IconsView.class)

        then:
        view.buttonLumoIcon.icon.element.getAttribute("icon")
                == new Icon("lumo", "menu").element.getAttribute("icon")
    }

    def "Load action's icon as VaadinIcon enum"() {
        when: "Open the IconsView"
        def view = openScreen(IconsView.class)

        then:
        view.actionEnumIcon.icon.element.getAttribute("icon")
                == VaadinIcon.MENU.create().element.getAttribute("icon")
    }

    def "Load action's icon as full vaadin-icon definition"() {
        when: "Open the IconsView"
        def view = openScreen(IconsView.class)

        then:
        view.actionVaadinIcon.icon.element.getAttribute("icon")
                == new Icon("vaadin", "menu").element.getAttribute("icon")
    }

    def "Load action's icon as full lumo-icon definition"() {
        when: "Open the IconsView"
        def view = openScreen(IconsView.class)

        then:
        view.actionLumoIcon.icon.element.getAttribute("icon")
                == new Icon("lumo", "menu").element.getAttribute("icon")
    }
}
