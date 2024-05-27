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

package date_interval.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.Order;

import java.util.Collection;

@Route(value = "date-interval-test-view")
@ViewController
@ViewDescriptor("date-interval-test-view.xml")
public class DateIntervalTestView extends StandardView {

    @ViewComponent
    public CollectionContainer<Order> ordersDc;
    @ViewComponent
    public PropertyFilter<? super BaseDateInterval> dateFilter;

    public Collection<Order> getItems() {
        return ordersDc.getItems();
    }
}
