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

package component_xml_load.screen;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.scroller.JmixScroller;
import io.jmix.flowui.component.splitlayout.JmixSplitLayout;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.Order;

@Route(value = "container-view")
@ViewController("ContainerView")
@ViewDescriptor("container-view.xml")
public class ContainerView extends StandardView {

    @ViewComponent
    public InstanceContainer<Order> orderDc;

    public void loadData() {
        getViewData().loadAll();
    }

    @ViewComponent
    public VerticalLayout vboxId;

    @ViewComponent
    public HorizontalLayout hboxId;

    @ViewComponent
    public Accordion accordionId;

    @ViewComponent
    public AccordionPanel accordionPanelId;

    @ViewComponent
    public AccordionPanel anotherAccordionPanelId;

    @ViewComponent
    public FormLayout formLayoutId;

    @ViewComponent
    public JmixScroller scrollerId;

    @ViewComponent
    public JmixSplitLayout splitLayoutId;

    @ViewComponent
    public Tabs tabsId;
}
