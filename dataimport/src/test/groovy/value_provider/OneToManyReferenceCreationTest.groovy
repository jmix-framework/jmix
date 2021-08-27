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

package value_provider

import io.jmix.core.common.util.ParamsMap
import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping
import io.jmix.dataimport.extractor.data.ImportedDataItem
import io.jmix.dataimport.extractor.data.ImportedObject
import io.jmix.dataimport.extractor.data.ImportedObjectList
import io.jmix.dataimport.property.populator.EntityPropertiesPopulator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Customer

class OneToManyReferenceCreationTest extends DataImportSpec {

    @Autowired
    protected EntityPropertiesPopulator entityPropertiesPopulator

    def 'test creation using data from imported object list'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("orders", ReferenceImportPolicy.CREATE)
                        .withDataFieldName('orders')
                        .addSimplePropertyMapping("orderNumber", "orderNumber")
                        .addSimplePropertyMapping("amount", "amount")
                        .addSimplePropertyMapping("date", "date")
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        def importedObjectList = new ImportedObjectList()
                .addImportedObject(createOrderObject('#001', '55.5', '12/06/2021 12:00'))
                .addImportedObject(createOrderObject('#002', '13', '25/06/2021 17:00'))
        importedDataItem.addRawValue("orders", importedObjectList)

        when: 'entity properties populated'

        def customer = dataManager.create(Customer)
        def entityInfo = entityPropertiesPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        entityInfo.createdReferences.size() == 2
        customer.orders.size() == 2
        checkOrder(customer.orders[0], '#001', '12/06/2021 12:00', 55.5)
        checkOrder(customer.orders[1], '#002', '25/06/2021 17:00', 13)
    }

    def 'test creation of nested one-to-many associations if each one has own imported object list'() {
        given:
        def orderLinesPropertyMapping = ReferenceMultiFieldPropertyMapping.builder("lines", ReferenceImportPolicy.CREATE)
                .withDataFieldName("lines")
                .addSimplePropertyMapping("quantity", "quantity")
                .addReferencePropertyMapping("product", 'productName', 'name', ReferenceImportPolicy.IGNORE_IF_MISSING)
                .build()

        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "name")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("orders", ReferenceImportPolicy.CREATE)
                        .withDataFieldName('orders')
                        .addSimplePropertyMapping("orderNumber", "orderNumber")
                        .addSimplePropertyMapping("amount", "amount")
                        .addSimplePropertyMapping("date", "date")
                        .addPropertyMapping(orderLinesPropertyMapping)
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        def importedObjectList = new ImportedObjectList()
                .addImportedObject(createOrderObject('#001', '20', '12/06/2021 12:00'))
        importedDataItem.setRawValues(ParamsMap.of("name", "John Dow",
                "orders", importedObjectList))

        when: 'entity properties populated'

        def customer = dataManager.create(Customer)
        def entityInfo = entityPropertiesPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.orders.size() == 1


        def firstOrder = customer.orders[0]
        checkOrder(firstOrder, '#001', '12/06/2021 12:00', 20)
        firstOrder.lines.size() == 1
        checkOrderLine(firstOrder.lines[0], 'Outback Power Nano-Carbon Battery 12V', 2)
    }

    def 'test creation of nested one-to-many associations without separate imported object list'() {
        given:
        def ordersPropertyMapping = ReferenceMultiFieldPropertyMapping.builder("orders", ReferenceImportPolicy.CREATE)
                .withDataFieldName('orderLines')
                .addSimplePropertyMapping("orderNumber", "orderNum")
                .addSimplePropertyMapping("amount", "orderAmount")
                .addSimplePropertyMapping("date", "orderDate")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("lines", ReferenceImportPolicy.CREATE)
                        .addSimplePropertyMapping("quantity", "quantity")
                        .addReferencePropertyMapping("product", "productName", "name", ReferenceImportPolicy.IGNORE_IF_MISSING)
                        .build())
                .build()


        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "name")
                .addPropertyMapping(ordersPropertyMapping)
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("name", "John Dow")

        def importedObjectList = new ImportedObjectList()
                .addImportedObject(createImportedObject(ParamsMap.of("orderNum", "#001",
                        "orderAmount", "30",
                        "orderDate", "12/06/2021 12:00",
                        "productName", "Outback Power Nano-Carbon Battery 12V",
                        "quantity", "2")))
                .addImportedObject(createImportedObject(ParamsMap.of("orderNum", "#002",
                        "orderAmount", "20",
                        "orderDate", "25/05/2021 12:00",
                        "productName", "Fullriver Sealed Battery 6V",
                        "quantity", "4")))
        importedDataItem.addRawValue("orderLines", importedObjectList)

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPropertiesPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.orders.size() == 2

        def firstOrder = customer.orders[0]
        checkOrder(firstOrder, '#001', '12/06/2021 12:00', 30)
        firstOrder.lines.size() == 1
        checkOrderLine(firstOrder.lines[0], 'Outback Power Nano-Carbon Battery 12V', 2)


        def secondOrder = customer.orders[1]
        checkOrder(secondOrder, '#002', '25/05/2021 12:00', 20)
        secondOrder.lines.size() == 1
        checkOrderLine(secondOrder.lines[0], 'Fullriver Sealed Battery 6V', 4)
    }

    def 'test creation of nested one-to-many associations if all data stores in imported data item'() {
        given:
        def ordersPropertyMapping = ReferenceMultiFieldPropertyMapping.builder("orders", ReferenceImportPolicy.CREATE)
                .addSimplePropertyMapping("orderNumber", "orderNum")
                .addSimplePropertyMapping("amount", "orderAmount")
                .addSimplePropertyMapping("date", "orderDate")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("lines", ReferenceImportPolicy.CREATE)
                        .addSimplePropertyMapping("quantity", "quantity")
                        .addReferencePropertyMapping("product", "productName", "name", ReferenceImportPolicy.IGNORE_IF_MISSING)
                        .build())
                .build()

        def importConfiguration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "name")
                .addPropertyMapping(ordersPropertyMapping)
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("name", "John Dow")
        importedDataItem.addRawValue("orderNum", "#001")
        importedDataItem.addRawValue("orderDate", "12/06/2021 12:00")
        importedDataItem.addRawValue("orderAmount", "20")
        importedDataItem.addRawValue("productName", "Outback Power Nano-Carbon Battery 12V")
        importedDataItem.addRawValue("quantity", "2")

        when: 'entity properties populated'

        def customer = dataManager.create(Customer)
        def entityInfo = entityPropertiesPopulator.populateProperties(customer, importConfiguration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.orders.size() == 1

        def firstOrder = customer.orders[0]
        checkOrder(firstOrder, '#001', '12/06/2021 12:00', 20)
        firstOrder.lines.size() == 1
        checkOrderLine(firstOrder.lines[0], 'Outback Power Nano-Carbon Battery 12V', 2)
    }

    def 'test duplicates in one-to-many association for imported entity'() {
        given:
        def orderLinesPropertyMapping = ReferenceMultiFieldPropertyMapping.builder("lines", ReferenceImportPolicy.CREATE)
                .addSimplePropertyMapping("quantity", "quantity")
                .addReferencePropertyMapping("product", "productName", "name", ReferenceImportPolicy.IGNORE_IF_MISSING)
                .build()

        def ordersPropertyMapping = ReferenceMultiFieldPropertyMapping.builder("orders", ReferenceImportPolicy.CREATE)
                .withDataFieldName('orderLines')
                .addSimplePropertyMapping("orderNumber", "orderNum")
                .addSimplePropertyMapping("amount", "orderAmount")
                .addSimplePropertyMapping("date", "orderDate")
                .addPropertyMapping(orderLinesPropertyMapping)
                .lookupByAllSimpleProperties()
                .build()


        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "name")
                .addPropertyMapping(ordersPropertyMapping)
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("name", "John Dow")

        def importedObjectList = new ImportedObjectList()
                .addImportedObject(createImportedObject(ParamsMap.of("orderNum", "#001",
                        "orderAmount", "50",
                        "orderDate", "12/06/2021 12:00",
                        "productName", "Outback Power Nano-Carbon Battery 12V",
                        "quantity", "2")))
                .addImportedObject(createImportedObject(ParamsMap.of("orderNum", "#001",
                        "orderAmount", "50",
                        "orderDate", "12/06/2021 12:00",
                        "productName", "Fullriver Sealed Battery 6V",
                        "quantity", "4")))
        importedDataItem.addRawValue("orderLines", importedObjectList)

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPropertiesPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.orders.size() == 1

        def order = customer.orders[0]
        checkOrder(order, '#001', '12/06/2021 12:00', 50)
        order.lines.size() == 2
        checkOrderLine(order.lines[0], 'Outback Power Nano-Carbon Battery 12V', 2)
        checkOrderLine(order.lines[1], 'Fullriver Sealed Battery 6V', 4)
    }

    protected ImportedObject createOrderObject(String orderNum, String amount, String date) {
        def orderObject = new ImportedObject()
        orderObject.setRawValues(ParamsMap.of("orderNumber", orderNum,
                "amount", amount,
                "date", date,
                "lines", createLinesList()))
        return orderObject
    }

    protected ImportedObjectList createLinesList() {
        return new ImportedObjectList()
                .addImportedObject(createImportedObject(
                        ParamsMap.of("productName", "Outback Power Nano-Carbon Battery 12V",
                                "quantity", "2")))
    }

}
