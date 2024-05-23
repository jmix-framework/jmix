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

package component.composite.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import io.jmix.flowui.component.accordion.JmixAccordionPanel;
import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.component.composite.CompositeDescriptor;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;

@CompositeDescriptor("test-composite-component.xml")
public class TestCompositeComponent extends CompositeComponent<VerticalLayout> {

    private JmixTabSheet tabSheet;
    private Tabs tabs;

    private JmixAccordionPanel accordionPanel1;
    private JmixAccordionPanel accordionPanel2;

    private DropdownButton dropdownButton;

    private TypedTextField<String> textField;

    public TestCompositeComponent() {
        addPostInitListener(this::onPostInit);
    }

    private void onPostInit(PostInitEvent event) {
        tabSheet = getInnerComponent("tabSheet");
        tabs = getInnerComponent("tabs");

        accordionPanel1 = getInnerComponent("accordionPanel1");
        accordionPanel2 = getInnerComponent("accordionPanel2");
        dropdownButton = getInnerComponent("dropdownButton");

        textField = getInnerComponent("textField");
    }

    public JmixTabSheet getTabSheet() {
        return tabSheet;
    }

    public Tabs getTabs() {
        return tabs;
    }

    public JmixAccordionPanel getAccordionPanel1() {
        return accordionPanel1;
    }

    public JmixAccordionPanel getAccordionPanel2() {
        return accordionPanel2;
    }

    public DropdownButton getDropdownButton() {
        return dropdownButton;
    }

    public TypedTextField<String> getTextField() {
        return textField;
    }
}
