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

package component_container.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.accordion.JmixAccordion;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;

@Route("component-container-view")
@ViewController
@ViewDescriptor("component-container-view.xml")
public class ComponentContainerView extends StandardView {

    @ViewComponent
    public VerticalLayout vbox;

    @ViewComponent
    public JmixDetails details;

    @ViewComponent
    public JmixAccordion accordion;

    @Subscribe("detailsDataGrid.customAction")
    public void onDetailsCustomAction(ActionPerformedEvent event) {

    }

    @Subscribe("accordionDataGrid.customAction")
    public void onAccordionCustomAction(ActionPerformedEvent event) {

    }

    @Subscribe("button1")
    public void onButton1Click(ClickEvent<Button> event) {

    }

    @Subscribe("button2")
    public void onButton2Click(ClickEvent<Button> event) {

    }
}
