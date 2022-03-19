/*
 * Copyright 2021 Haulmont.
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

package date_interval.screen;

import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;

import java.util.Collection;

@UiController
@UiDescriptor("date-interval-test-screen.xml")
public class DateIntervalTestScreen extends Screen {

    @Autowired
    private CollectionContainer<Order> dateTimesDc;

    @Autowired
    public PropertyFilter dateFilter;

    public Collection<Order> getItems() {
        return dateTimesDc.getItems();
    }
}
