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

package component.entitypicker;

import component.entitypicker.view.EntityPickerTestView;
import component.standarddetailview.view.BlankTestView;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
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

@UiTest(viewBasePackages = {"component.entitypicker.view", "component.standarddetailview.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class EntityPickerTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private ViewNavigationSupport navigationSupport;

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
    @DisplayName("Set empty value from client should not cause unparseable validation error")
    public void setEmptyValueFromClientShouldNotCauseUnparseableValidationError() {
        var origin = navigateTo(BlankTestView.class);

        viewNavigators.view(origin, EntityPickerTestView.class)
                .navigate();

        EntityPickerTestView entityPickerTestView = UiTestUtils.getCurrentView();

        // Pre-fill value to check clearing
        Product product = dataManager.load(Product.class).all().list().get(0);
        entityPickerTestView.productField.setValue(product);

        // Simulate user action that sets an empty value
        entityPickerTestView.productField.setValueFromClient(null);

        // This should not have unparseable validation error
        Assertions.assertFalse(entityPickerTestView.productField.isInvalid());
        Assertions.assertNull(entityPickerTestView.productField.getErrorMessage());
    }

    @Test
    @DisplayName("Value change event should be fired when value is changed via setValueFromClient")
    public void setValueFromClientShouldFireValueChangeEvent() {
        var origin = navigateTo(BlankTestView.class);

        viewNavigators.view(origin, EntityPickerTestView.class)
                .navigate();

        EntityPickerTestView entityPickerTestView = UiTestUtils.getCurrentView();

        // Pre-fill value to check clearing
        Product product = dataManager.load(Product.class).all().list().get(0);
        entityPickerTestView.productField.setValue(product);

        final boolean[] eventFired = {false};
        entityPickerTestView.productField.addValueChangeListener(event -> eventFired[0] = true);

        // Simulate user action that sets an empty value (e.g., from EntityClearAction)
        entityPickerTestView.productField.setValueFromClient(null);

        Assertions.assertTrue(eventFired[0], "Value change event should be fired");
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

    protected <T extends View<?>> T navigateTo(Class<T> viewClass) {
        navigationSupport.navigate(viewClass);
        return UiTestUtils.getCurrentView();
    }
}
