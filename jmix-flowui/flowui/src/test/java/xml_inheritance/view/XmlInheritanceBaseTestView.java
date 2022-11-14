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

package xml_inheritance.view;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.flowui.component.pagination.SimplePagination;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.OrderLine;

@ViewController
@ViewDescriptor("xml-inheritance-base-test-view.xml")
public class XmlInheritanceBaseTestView extends StandardView {

    @ViewComponent
    public HorizontalLayout hboxAddNew;

    @ViewComponent
    public HorizontalLayout hboxMoveBaseToUp;

    @ViewComponent
    public HorizontalLayout hboxMoveBaseToDown;

    @ViewComponent
    public InstanceContainer<OrderLine> lineDc;

}
