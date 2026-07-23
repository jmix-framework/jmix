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

package component.entitypicker

import component.entitypicker.view.EntityPickerTestView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.OrderLine
import test_support.entity.sales.Product
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class EntityPickerValueChangeTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component.entitypicker.view")
        setupData()
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE")
        jdbcTemplate.execute("delete from TEST_PRODUCT")
    }

    def "value change events via setValueFromClient must be marked as from client"() {
        given: "An EntityPicker opened in a view"
        navigateToView(EntityPickerTestView)
        def view = UiTestUtils.currentView as EntityPickerTestView

        def product = dataManager.load(Product).all().list().get(0)

        and: "A listener recording the fromClient flag of every value change event"
        def fromClientFlags = []
        view.productField.addValueChangeListener({ event -> fromClientFlags << event.fromClient })

        when: "User selects a value (LookupSelectAction) and then clears it (ValueClearAction)"
        view.productField.setValueFromClient(product)
        view.productField.setValueFromClient(null)

        then: "An event was fired and every event is client-originated"
        !fromClientFlags.isEmpty()
        fromClientFlags.every { it }
    }

    private void setupData() {
        def products = new ArrayList<Product>(2)
        [1, 2].each { i ->
            def product = dataManager.create(Product)
            product.name = "Product " + i
            products.add(product)
        }
        def orderLine = dataManager.create(OrderLine)
        orderLine.product = products.get(0)
        dataManager.save(new SaveContext().saving(products.toArray()).saving(orderLine))
    }
}
