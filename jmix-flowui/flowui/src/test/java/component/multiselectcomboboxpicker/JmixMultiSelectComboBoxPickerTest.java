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

package component.multiselectcomboboxpicker;

import com.vaadin.flow.data.provider.Query;
import component.multiselectcomboboxpicker.view.JmixMultiSelectComboBoxPickerOrderDetailTestView;
import component.multiselectcomboboxpicker.view.JmixMultiSelectComboBoxPickerOrderListTestView;
import component.standarddetailview.view.BlankTestView;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiTestConfiguration;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@UiTest(viewBasePackages = {"component.multiselectcomboboxpicker.view", "component.standarddetailview.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class JmixMultiSelectComboBoxPickerTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private ViewNavigationSupport navigationSupport;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private enum SetValueType {
        TYPED_VALUE, INCORRECT_TYPED_VALUE, VALUE
    }

    @BeforeEach
    public void beforeEach() {
        setupData();
    }

    @AfterEach
    public void afterEach() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE");
        jdbcTemplate.execute("delete from TEST_ORDER");
    }

    @ParameterizedTest
    @EnumSource(names = { "TYPED_VALUE", "INCORRECT_TYPED_VALUE", "VALUE"})
    @DisplayName("Set typed-value/value to JmixMultiSelectComboBoxPicker")
    public void setCorrectTypedValueToJmixCheckboxGroup(SetValueType setValueType) {
        var origin = navigateTo(BlankTestView.class);
        viewNavigators.view(origin, JmixMultiSelectComboBoxPickerOrderListTestView.class)
                .navigate();

        var orderListView = UiTestUtils.getCurrentView();

        // Create new order in detail view
        JmixButton createButton = UiTestUtils.getComponent(orderListView, "createButton");
        createButton.click();

        JmixMultiSelectComboBoxPickerOrderDetailTestView orderDetailView = UiTestUtils.getCurrentView();

        // JmixMultiSelectComboBoxPicker should have 3 options
        var items = orderDetailView.orderLinesField.getDataProvider()
                .fetch(new Query<>())
                .toList();

        Assertions.assertEquals(3, items.size());

        // Edit Order
        orderDetailView.numberField.setValue("2");
        var orderLineValue = orderDetailView.getOrderLineByDescription("1");
        if (setValueType == SetValueType.TYPED_VALUE) {
            orderDetailView.orderLinesField.setTypedValue(List.of(orderLineValue));
        } else if (setValueType == SetValueType.INCORRECT_TYPED_VALUE) {
            orderDetailView.orderLinesField.setTypedValue(Set.of(orderLineValue));
        } else {
            orderDetailView.orderLinesField.setValue(Set.of(orderLineValue));
        }

        // No exception thrown and JmixMultiSelectComboBoxPicker value contains item
        Assertions.assertTrue(orderDetailView.orderLinesField.getValue().contains(orderLineValue));

        // Save Order with 'selected' OrderLines. Reopen in detail view
        JmixButton saveAndCloseButton = UiTestUtils.getComponent(orderDetailView, "saveAndCloseButton");
        saveAndCloseButton.click();

        findOrderAndEdit(UiTestUtils.getCurrentView(), "2");

        orderDetailView = UiTestUtils.getCurrentView();

        // JmixMultiSelectComboBoxPicker value should have saved previously 'selected' OrderLine

        Assertions.assertTrue(orderDetailView.orderLinesField.getValue().contains(orderLineValue));
    }

    private static void findOrderAndEdit(JmixMultiSelectComboBoxPickerOrderListTestView orderListView, String number) {
        DataGrid<Order> ordersDataGrid = UiTestUtils.getComponent(orderListView, "ordersDataGrid");

        DataGridItems<Order> ordersDataGridItems = ordersDataGrid.getItems();
        Assertions.assertNotNull(ordersDataGridItems);

        Order order = ordersDataGridItems.getItems().stream()
                .filter(o -> o.getNumber().equals(number))
                .findFirst()
                .orElseThrow();

        ordersDataGrid.select(order);

        JmixButton editButton = UiTestUtils.getComponent(orderListView, "editButton");
        editButton.click();
    }

    private void setupData() {
        List<OrderLine> orderLines = new ArrayList<>();
        IntStream.of(1, 2, 3).forEach(i -> {
            var orderLine = dataManager.create(OrderLine.class);
            orderLine.setDescription(i + "");
            orderLines.add(orderLine);
        });

        var order = dataManager.create(Order.class);
        order.setNumber("1");
        order.setOrderLines(List.of(orderLines.get(0)));

        dataManager.save(new SaveContext().saving(order).saving(orderLines.toArray()));
    }

    private <T extends View<?>> T navigateTo(Class<T> view) {
        navigationSupport.navigate(view);
        return UiTestUtils.getCurrentView();
    }
}
