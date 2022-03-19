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

package data_importer

import io.jmix.core.Resources
import io.jmix.dataimport.DataImporter
import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.ImportTransactionStrategy
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Order
import test_support.entity.OrderLine
import test_support.entity.Product

class DataImporterTest extends DataImportSpec {
    @Autowired
    protected DataImporter dataImporter
    @Autowired
    protected Resources resources

    def 'test successful import result'() {
        given:
        def importConfig = ImportConfiguration.builder(Product, InputDataFormat.XML)
                .addSimplePropertyMapping("name", "name")
                .addSimplePropertyMapping("price", "price")
                .addSimplePropertyMapping("special", "special")
                .withBooleanFormats("Yes", "No")
                .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_ENTITY)
                .build()

        def xmlContent = resources.getResourceAsStream("/test_support/input_data_files/xml/one_product.xml")

        when: 'data imported'
        def result = dataImporter.importData(importConfig, xmlContent)

        then:
        result.success
        result.importedEntityIds.size() == 1
        result.failedEntities.empty
    }

    def 'test failed import result'() {
        given:
        def importConfig = ImportConfiguration.builder(OrderLine, InputDataFormat.XML)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder('order', ReferenceImportPolicy.CREATE_IF_MISSING)
                        .addSimplePropertyMapping('orderNumber', 'orderNumber')
                        .addSimplePropertyMapping('date', 'orderDate')
                        .addSimplePropertyMapping('amount', 'orderAmount')
                        .lookupByAllSimpleProperties()
                        .build())
                .addReferencePropertyMapping('product', 'productName', 'name', ReferenceImportPolicy.IGNORE_IF_MISSING)
                .addSimplePropertyMapping("quantity", "quantity")
                .withDateFormat('dd/MM/yyyy HH:mm')
                .withTransactionStrategy(ImportTransactionStrategy.SINGLE_TRANSACTION)
                .build()
        InputStream xmlContent = resources.getResourceAsStream("/test_support/input_data_files/xml/order_lines.xml")

        when: 'data imported'
        def importResult = dataImporter.importData(importConfig, xmlContent)

        then:
        !importResult.success
        importResult.importedEntityIds.size() == 0
        importResult.failedEntities.size() == 0
        importResult.errorMessage != null
    }

    def 'test import with embeddable entity'() {
        given:
        def importConfig = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("deliveryDetails", ReferenceImportPolicy.CREATE)
                        .addSimplePropertyMapping("deliveryDate", "Delivery Date")
                        .addSimplePropertyMapping("fullAddress", "Delivery Address")
                        .build())
                .addReferencePropertyMapping('customer', 'Customer Name', 'name', ReferenceImportPolicy.IGNORE_IF_MISSING)
                .addSimplePropertyMapping('orderNumber', 'Order Num')
                .addSimplePropertyMapping('date', 'Order Date')
                .addSimplePropertyMapping('amount', 'Order Amount')
                .withDateFormat('dd/MM/yyyy HH:mm')
                .withTransactionStrategy(ImportTransactionStrategy.SINGLE_TRANSACTION)
                .build()
        InputStream xlsxContent = resources.getResourceAsStream("/test_support/input_data_files/xlsx/orders_with_delivery_details.xlsx")

        when: 'data imported'
        def importResult = dataImporter.importData(importConfig, xlsxContent)

        then:
        importResult.success
        importResult.importedEntityIds.size() == 2

        def firstOrder = loadEntity(Order, importResult.importedEntityIds[0], "order-full") as Order
        checkDeliveryDetails(firstOrder.deliveryDetails, '14/12/2020 12:00', 'Samara')

        def secondOrder = loadEntity(Order, importResult.importedEntityIds[1], "order-full") as Order
        checkDeliveryDetails(secondOrder.deliveryDetails, '14/12/2020 12:00', 'Samara')
    }
}
