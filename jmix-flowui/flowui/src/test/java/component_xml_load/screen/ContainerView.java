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
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ComponentId;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;
import test_support.entity.sales.Order;

@Route(value = "container-view")
@UiController("ContainerView")
@UiDescriptor("container-view.xml")
public class ContainerView extends StandardView {

    @ComponentId
    public InstanceContainer<Order> orderDc;

    public void loadData() {
        getViewData().loadAll();
    }

    @ComponentId
    public VerticalLayout vboxId;

    @ComponentId
    public HorizontalLayout hboxId;

    @ComponentId
    public Accordion accordionId;

    @ComponentId
    public AccordionPanel accordionPanelId;

    @ComponentId
    public AccordionPanel anotherAccordionPanelId;

    @ComponentId
    public FormLayout formLayoutId;

    @ComponentId
    public Scroller scrollerId;

    @ComponentId
    public SplitLayout splitLayoutId;

    @ComponentId
    public Tabs tabsId;
}
