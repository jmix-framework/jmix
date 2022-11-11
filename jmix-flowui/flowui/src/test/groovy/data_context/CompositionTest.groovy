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

package data_context


import io.jmix.core.DataManager
import io.jmix.core.EntitySerialization
import io.jmix.core.FetchPlans
import io.jmix.core.SaveContext
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import io.jmix.flowui.model.InstanceContainer
import io.jmix.flowui.model.InstanceLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.*
import test_support.spec.FlowuiTestSpecification

@SuppressWarnings("GroovyAssignabilityCheck")
@SpringBootTest
class CompositionTest extends FlowuiTestSpecification {

    @Autowired
    EntitySerialization entitySerialization
    @Autowired
    DataManager dataManager
    @Autowired
    FetchPlans fetchPlans
    @Autowired
    JdbcTemplate jdbc
    @Autowired
    DataComponents dataComponents

    private Customer customer1
    private Order order1
    private Product product11, product12
    private OrderLine orderLine11, orderLine12

    def saved, removed

    @Override
    void setup() {
        customer1 = dataManager.save(new Customer(name: 'customer-1', address: new Address()))
        order1 = dataManager.save(new Order(number: '111', customer: customer1, orderLines: []))
        product11 = dataManager.save(new Product(name: 'product-11', price: 100))
        product12 = dataManager.save(new Product(name: 'product-12', price: 200))
        orderLine11 = dataManager.save(new OrderLine(quantity: 10, order: order1, product: product11, params: []))
        orderLine12 = dataManager.save(new OrderLine(quantity: 20, order: order1, product: product11, params: []))
        order1.orderLines = [orderLine11, orderLine12]

        saved = []
        removed = []
    }

    def cleanup() {
        jdbc.update('delete from TEST_ORDER_LINE_PARAM')
        jdbc.update('delete from TEST_ORDER_LINE')
        jdbc.update('delete from TEST_ORDER')
        jdbc.update('delete from TEST_PRODUCT')
        jdbc.update('delete from TEST_CUSTOMER')
    }

    def "zero composition"() {

        def orderScreen = new OrderScreen()

        when:

        orderScreen.open(order1)

        then:

        orderScreen.orderCnt.item == order1

        when:

        orderScreen.orderCnt.item.number = '222'

        orderScreen.dataContext.dataManager = mockDataManager()

        orderScreen.dataContext.save()

        then:

        saved.size() == 1
    }

    def "one level of composition"() {
        def orderScreen = new OrderScreen()
        def orderLineScreen = new LineScreen()

        when:

        orderScreen.open(order1)

        then:

        orderScreen.orderCnt.item == order1
        orderScreen.linesCnt.items.size() == 2
        orderScreen.linesCnt.itemOrNull == null

        when: "open edit screen for orderLine11"

        orderScreen.linesCnt.item = orderLine11
        orderScreen.dataContext.dataManager = mockDataManager()
        orderLineScreen.open(orderScreen.linesCnt.item, orderScreen.dataContext)

        then:

        !orderScreen.dataContext.hasChanges()
        orderLineScreen.lineCnt.item == orderLine11
        orderLineScreen.lineCnt.item.quantity == 10

        when: "change orderLine11.quantity and save child context"

        orderLineScreen.lineCnt.item.quantity = 11

        def childContextStateBeforeSave = entitySerialization.toJson(orderLineScreen.dataContext.getAll())

        def modified = []
        orderLineScreen.dataContext.addPreSaveListener { e ->
            modified.addAll(e.modifiedInstances)
        }
        orderLineScreen.dataContext.save()

        def childContextStateAfterSave = entitySerialization.toJson(orderLineScreen.dataContext.getAll())

        then: "child context saved orderLine11 to parent"

        modified.contains(orderLine11)
        saved.isEmpty()
        orderScreen.linesCnt.item.quantity == 11

        and: "child context has no changes anymore"

        !orderLineScreen.dataContext.hasChanges()

        and: "child context is exactly the same as before save"

        childContextStateAfterSave == childContextStateBeforeSave

        when: "save parent context"

        orderScreen.dataContext.save()

        then: "orderLine11 saved to DataService"

        saved.size() == 1
        saved.contains(orderLine11)
        saved.find { it == orderLine11 }.quantity == 11
    }

    def "two levels of composition"() {

        def orderScreen = new OrderScreen()
        def orderLineScreen = new LineScreen()
        def productScreen = new ProductScreen()

        when:

        orderScreen.open(order1)
        orderScreen.dataContext.dataManager = mockDataManager()

        orderScreen.linesCnt.item = orderLine11
        orderLineScreen.open(orderScreen.linesCnt.item, orderScreen.dataContext)

        productScreen.open(orderScreen.linesCnt.item.product, orderLineScreen.dataContext)

        then:

        productScreen.productCnt.item == product11

        when:

        productScreen.productCnt.item.price = 101

        productScreen.dataContext.save()

        then:

        saved.isEmpty()

        when:

        orderLineScreen.lineCnt.item.quantity = 11
        orderLineScreen.dataContext.save()

        then:

        saved.isEmpty()

        when:

        orderScreen.dataContext.save()

        then:

        saved.size() == 2
        saved.contains(product11)
        saved.contains(orderLine11)
        saved.find { it == product11 }.price == 101
        saved.find { it == orderLine11 }.quantity == 11
    }

    def "one level of composition - repetitive edit"() {

        def orderScreen = new OrderScreen()
        def orderLineScreen = new LineScreen()

        orderScreen.open(order1)
        orderScreen.dataContext.dataManager = mockDataManager()

        orderScreen.linesCnt.item = orderLine11
        orderLineScreen.open(orderScreen.linesCnt.item, orderScreen.dataContext)
        orderLineScreen.lineCnt.item.quantity = 11
        orderLineScreen.dataContext.save()

        when: "open orderLineScreen second time"

        orderLineScreen.open(orderScreen.linesCnt.item, orderScreen.dataContext)

        then:

        saved.isEmpty()

        when:

        orderLineScreen.lineCnt.item.quantity = 12

        orderLineScreen.dataContext.save()
        orderScreen.dataContext.save()

        then:

        saved.size() == 1
        saved.find { it == orderLine11 }.quantity == 12
    }

    def "one level of composition - changed reference"() {

        def orderScreen = new OrderScreen()
        def orderLineScreen = new LineScreen()

        orderScreen.open(order1)
        orderScreen.dataContext.dataManager = mockDataManager()

        orderScreen.linesCnt.item = orderLine11
        orderLineScreen.open(orderScreen.linesCnt.item, orderScreen.dataContext)
        orderLineScreen.lineCnt.item.product = product12

        when:

        orderLineScreen.dataContext.save()
        orderScreen.dataContext.save()

        then:

        saved.size() == 1
        saved[0].product == product12
    }

    def "one level of composition - remove item"() {

        def orderScreen = new OrderScreen()
        def lineScreen = new LineScreen()

        orderScreen.open(order1)
        orderScreen.dataContext.dataManager = mockDataManager()

        orderScreen.linesCnt.item = orderLine11
        lineScreen.open(orderScreen.linesCnt.item, orderScreen.dataContext)

        when: "remove OrderLine in lineScreen"

        lineScreen.dataContext.remove(lineScreen.lineCnt.item)
        lineScreen.dataContext.save()

        then:

        saved.isEmpty()
        removed.isEmpty()

        when:

        orderScreen.dataContext.save()

        then:

        saved.size() == 1
        saved.contains(order1)
        removed.size() == 1
        removed.contains(orderLine11)
    }

    protected DataManager mockDataManager() {
        def mockDataManager = Mock(DataManager)
        mockDataManager.save(_ as SaveContext) >> { SaveContext cc ->
            saved.addAll(cc.entitiesToSave)
            removed.addAll(cc.entitiesToRemove)
            dataManager.save(cc)
        }
        return mockDataManager
    }

    class OrderScreen {
        DataContext dataContext
        InstanceContainer<Order> orderCnt
        CollectionContainer<OrderLine> linesCnt

        def open(Order order) {
            dataContext = dataComponents.createDataContext()
            orderCnt = dataComponents.createInstanceContainer(Order)
            linesCnt = dataComponents.createCollectionContainer(OrderLine)
            orderCnt.addItemChangeListener { e ->
                linesCnt.setItems(e.item.orderLines)
            }

            InstanceLoader orderLdr = dataComponents.createInstanceLoader()
            orderLdr.container = orderCnt
            orderLdr.dataContext = dataContext
            orderLdr.entityId = order.id
            orderLdr.fetchPlan = fetchPlans.builder(Order)
                    .addFetchPlan('_local')
                    .add('orderLines', { builder ->
                        builder.addFetchPlan('_local')
                                .add('product', '_local')
                                .add('params', '_local')
                    })
                    .build()
            orderLdr.load()
        }
    }

    class LineScreen {
        DataContext dataContext
        InstanceContainer<OrderLine> lineCnt

        def open(OrderLine orderLine, DataContext parentContext) {
            dataContext = dataComponents.createDataContext()
            if (parentContext != null) {
                dataContext.setParent(parentContext)
            }
            lineCnt = dataComponents.createInstanceContainer(OrderLine)

            if (!dataContext.contains(orderLine)) {
                InstanceLoader loader = dataComponents.createInstanceLoader()
                loader.container = lineCnt
                loader.dataContext = dataContext
                loader.entityId = orderLine.id
                loader.fetchPlan = fetchPlans.builder(OrderLine)
                        .addFetchPlan('_local')
                        .add('product', '_local')
                        .add('params', '_local')
                        .build()
                loader.load()
            } else {
                lineCnt.item = dataContext.find(OrderLine, orderLine.id)
            }
        }
    }

    class ProductScreen {
        DataContext dataContext
        InstanceContainer<Product> productCnt

        def open(Product product, DataContext parentContext) {
            dataContext = dataComponents.createDataContext()
            if (parentContext != null) {
                dataContext.setParent(parentContext)
            }
            productCnt = dataComponents.createInstanceContainer(Product)

            if (!dataContext.contains(product)) {
                InstanceLoader loader = dataComponents.createInstanceLoader()
                loader.container = productCnt
                loader.dataContext = dataContext
                loader.entityId = product.id
                loader.load()
            } else {
                productCnt.item = dataContext.find(Product, product.id)
            }
        }
    }
}
