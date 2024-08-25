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

package component.entitycombobox;

import component.entitycombobox.view.orderline.EntityComboBoxOrderLineDetailTestView;
import component.entitycombobox.view.orderline.EntityComboBoxOrderLineListTestView;
import component.entitycombobox.view.product.EntityComboBoxProductDetailTestView;
import component.entitycombobox.view.product.EntityComboBoxProductListTestView;
import component.standarddetailview.view.BlankTestView;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiTestConfiguration;
import test_support.entity.sales.OrderLine;
import test_support.entity.sales.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@UiTest(viewBasePackages = {"component.entitycombobox.view", "component.standarddetailview.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class EntityComboBoxTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private ViewNavigationSupport navigationSupport;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DialogWindows dialogWindows;

    @BeforeEach
    public void beforeEach() {
        setupData();
    }

    @AfterEach
    public void afterEach() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE");
        jdbcTemplate.execute("delete from TEST_PRODUCT");
    }

    @Test
    @DisplayName("The changed selected item should be updated after EntityOpenAction without exceptions")
    public void changedSelectedItemShouldBeUpdatedAfterEntityOpenActionWithoutExceptions() {
        var origin = navigateTo(BlankTestView.class);

        viewNavigators.view(origin, EntityComboBoxOrderLineListTestView.class)
                .navigate();

        // Create OrderLine
        EntityComboBoxOrderLineListTestView orderLineListView = UiTestUtils.getCurrentView();
        orderLineListView.createButton.click();

        // Open Product lookup view
        EntityComboBoxOrderLineDetailTestView orderLineDetailView = UiTestUtils.getCurrentView();
        orderLineDetailView.productFieldEntityLookup.execute();

        // Select first product
        EntityComboBoxProductListTestView productListView = getCurrentDialogView();
        Product product = productListView.productsDc.getItems().get(0);
        productListView.productsDataGrid.select(product);
        productListView.selectButton.click();

        // ValueChangeEvent must occur
        Assertions.assertEquals(1, orderLineDetailView.productValueChangeCount);

        // Change Product using EntityOpenAction.
        // No exceptions must be thrown. Updated value is tracked only in UI,
        // so we cannot check value at the server-side.
        Assertions.assertDoesNotThrow(() -> {
            orderLineDetailView.productFieldEntityOpen.execute();
            EntityComboBoxProductDetailTestView productDetailView = getCurrentDialogView();
            productDetailView.nameField.setValue("New value");
            productDetailView.saveAndCloseButton.click();
            orderLineDetailView.save();
        });
    }

    @Test
    @DisplayName("Clear EntityComboBox value using EntityClearAction")
    public void clearEntityComboBoxValueUsingEntityClearAction() {
        var origin = navigateTo(BlankTestView.class);

        viewNavigators.view(origin, EntityComboBoxOrderLineListTestView.class)
                .navigate();

        // Edit OrderLine
        EntityComboBoxOrderLineListTestView orderLineListView = UiTestUtils.getCurrentView();
        orderLineListView.editFirstItem();

        EntityComboBoxOrderLineDetailTestView orderLineDetailView = UiTestUtils.getCurrentView();

        // Check that OrderLine has a product
        Assertions.assertNotNull(orderLineDetailView.productField.getValue());

        // Clear value using EntityClearAction
        Assertions.assertDoesNotThrow(() -> orderLineDetailView.productFieldEntityClear.execute());
        Assertions.assertNull(orderLineDetailView.productField.getValue());

        orderLineDetailView.closeWithSave();
    }

    @Test
    @DisplayName("Clear EntityComboBox value using ValueClearAction")
    public void clearEntityComboBoxValueUsingValueClearAction() {
        var origin = navigateTo(BlankTestView.class);

        viewNavigators.view(origin, EntityComboBoxOrderLineListTestView.class)
                .navigate();

        // Edit OrderLine
        EntityComboBoxOrderLineListTestView orderLineListView = UiTestUtils.getCurrentView();
        orderLineListView.editFirstItem();

        EntityComboBoxOrderLineDetailTestView orderLineDetailView = UiTestUtils.getCurrentView();

        // Check that OrderLine has a product
        Assertions.assertNotNull(orderLineDetailView.productField.getValue());

        // Clear value using ValueClearAction
        Assertions.assertDoesNotThrow(() -> orderLineDetailView.productFieldValueClear.execute());
        Assertions.assertNull(orderLineDetailView.productField.getValue());

        orderLineDetailView.closeWithSave();
    }

    private void setupData() {
        List<Product> products = new ArrayList<>(2);
        IntStream.of(1, 2).forEach(i -> {
            Product product = dataManager.create(Product.class);
            product.setName("Product " + i);
            products.add(product);
        });
        OrderLine orderLine = dataManager.create(OrderLine.class);
        orderLine.setProduct(products.get(0));
        dataManager.save(new SaveContext().saving(products.toArray()).saving(orderLine));
    }

    private <T extends View<?>> T navigateTo(Class<T> view) {
        navigationSupport.navigate(view);
        return UiTestUtils.getCurrentView();
    }

    @SuppressWarnings("unchecked")
    private <T extends View<?>> T getCurrentDialogView() {
        List<View<?>> dialogs = dialogWindows.getOpenedDialogWindows().getDialogs();
        return (T) dialogs.get(dialogs.size() - 1);
    }
}
