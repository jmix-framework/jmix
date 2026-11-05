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

package component.genericfilter.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.Order;

/**
 * Filter components bound to data loaders whose base condition is declared in XML: an {@code or} group,
 * or an {@code and} group for comparison.
 */
@Route(value = "filter-or-base-condition-view")
@ViewController("FilterOrBaseConditionTestView")
@ViewDescriptor("filter-or-base-condition-view.xml")
public class FilterOrBaseConditionTestView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;
    @ViewComponent
    public GroupFilter groupFilter;
    @ViewComponent
    public PropertyFilter<String> numberFilter;
    @ViewComponent
    public PropertyFilter<Object> amountFilter;
    @ViewComponent
    public JpqlFilter<Void> jpqlFilter;
    @ViewComponent
    public DataGrid<Order> gridOrdersGrid;
    @ViewComponent
    public GenericFilter gridGenericFilter;
    @ViewComponent
    public DataGrid<Order> gridGfOrdersGrid;
    @ViewComponent
    public GenericFilter andGenericFilter;
    @ViewComponent
    public PropertyFilter<String> andNumberFilter;

    @ViewComponent
    public CollectionLoader<Order> gfOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> groupOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> propertyOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> jpqlOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> gridOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> gridGfOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> andGfOrdersDl;
    @ViewComponent
    public CollectionLoader<Order> andPropertyOrdersDl;
}
