/*
 * Copyright (c) 2020 Haulmont.
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

package facet.lookup_screen.screen;

import io.jmix.core.Metadata;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.LookupScreenFacet;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;
import test_support.entity.sales.screen.OrderBrowse;

import java.util.Collection;

@UiController
@UiDescriptor("lookup-screen-facet-test-screen.xml")
public class LookupScreenFacetTestScreen extends Screen {

    @Autowired
    public Metadata metadata;

    @Autowired
    public CollectionContainer<Order> ordersDc;

    @Autowired
    public EntityPicker<Order> orderPicker;
    @Autowired
    public Table<Order> ordersTable;
    @Autowired
    public Button button;

    @Autowired
    public Action action;

    @Autowired
    public LookupScreenFacet<Order, OrderBrowse> lookupScreen;
    @Autowired
    public LookupScreenFacet<Order, OrderBrowse> tableLookupScreen;
    @Autowired
    public LookupScreenFacet<Order, OrderBrowse> fieldLookupScreen;

    @Subscribe
    public void onInit(InitEvent event) {
        Order order = metadata.create(Order.class);
        order.setNumber("Test order");

        orderPicker.setValue(order);
    }

    @Install(to = "lookupScreen", subject = "selectHandler")
    public void onLookupSelect(Collection<Order> selected) {
    }

    @Install(to = "lookupScreen", subject = "selectValidator")
    public boolean validateSelection(LookupScreen.ValidationContext<Order> selected) {
        return true;
    }

    @Install(to = "lookupScreen", subject = "transformation")
    public Collection<Order> transformLookupSelection(Collection<Order> selected) {
        return selected;
    }
}
