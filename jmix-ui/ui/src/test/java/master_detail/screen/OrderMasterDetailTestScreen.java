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

package master_detail.screen;

import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.MasterDetailScreen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import test_support.entity.sales.Order;

@UiController
@UiDescriptor("order-master-detail-test-screen.xml")
@LookupComponent("table")
public class OrderMasterDetailTestScreen extends MasterDetailScreen<Order> {

    @Subscribe
    protected void onInitEntity(InitEntityEvent<Order> event) {
        event.getEntity().setNumber("New number");
    }

    public Order getEditedOrder() {
        return getEditContainer().getItem();
    }
}
