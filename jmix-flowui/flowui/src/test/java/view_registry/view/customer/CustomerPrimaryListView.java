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

package view_registry.view.customer;

import io.jmix.flowui.view.PrimaryListView;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import test_support.entity.sales.Customer;

@PrimaryListView(Customer.class)
@ViewController(CustomerPrimaryListView.VIEW_ID)
public class CustomerPrimaryListView extends StandardListView<Customer> {

    public static final String VIEW_ID = "customer-primary-list-view";
}
