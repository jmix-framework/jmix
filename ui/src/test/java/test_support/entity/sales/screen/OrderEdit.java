/*
 * Copyright 2020 Haulmont.
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

package test_support.entity.sales.screen;

import io.jmix.ui.component.DateField;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Order;

import java.math.BigDecimal;
import java.util.Date;

@UiController("test_Order.edit")
@UiDescriptor("order-edit.xml")
@EditedEntityContainer("orderDc")
public class OrderEdit extends StandardEditor<Order> {

    @Autowired
    protected Field<Customer> customerField;
    @Autowired
    protected Field<Customer> customerFieldWithOptions;
    @Autowired
    protected EntityPicker<Customer> customerPicker;
    @Autowired
    protected EntityComboBox<Customer> customerComboBox;
    @Autowired
    protected TextField<String> numberField;
    @Autowired
    protected DateField<Date> dateField;
    @Autowired
    protected TextField<BigDecimal> amountField;
}
