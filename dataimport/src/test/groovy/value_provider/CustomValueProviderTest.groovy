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

import io.jmix.core.FetchPlan
import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.mapping.CustomPropertyMapping
import io.jmix.dataimport.extractor.data.ImportedDataItem
import io.jmix.dataimport.property.populator.EntityPropertiesPopulator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Customer
import test_support.entity.CustomerGrade
import test_support.entity.Order
import test_support.entity.Product

class CustomValueProviderTest extends DataImportSpec {

    @Autowired
    protected EntityPropertiesPopulator entityPropertiesPopulator

    def 'test custom value for simple property'() {
        given:
        def configuration = ImportConfiguration.builder(Product, InputDataFormat.XLSX)
                .addCustomPropertyMapping("price", customMappingContext -> {
                    def rawValue = customMappingContext.rawValues["Price"]
                    try {
                        def value = new BigDecimal(rawValue)
                        return value
                    } catch (Exception ex) {
                        return BigDecimal.ZERO
                    }
                })
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('Price', 'string')


        when: 'entity properties populated'

        def specialProduct = dataManager.create(Product)
        def entityInfo = entityPropertiesPopulator.populateProperties(specialProduct, configuration, importedDataItem)

        then:
        entityInfo.entity == specialProduct
        specialProduct.price == 0
    }


    def 'test custom value of reference property if raw value is string'() {
        given:
        def customValueFunction = customMappingContext -> {
            String customerName = customMappingContext.rawValues["customerName"]
            def customer = loadCustomer(customerName, FetchPlan.BASE) as Customer
            if (customer == null) {
                def newCustomer = dataManager.create(Customer)
                newCustomer.name = customerName
                return newCustomer
            }
        }
        def configuration = new ImportConfiguration(Order, InputDataFormat.XML)
                .addPropertyMapping(new CustomPropertyMapping("customer", customValueFunction))

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('customerName', 'John Dow')


        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        checkCustomer(order.customer, 'John Dow', null, null)
    }

    def 'test custom value of reference property if other raw values are taken from imported item'() {
        given:
        def configuration = new ImportConfiguration(Order, InputDataFormat.XML)
                .addPropertyMapping(new CustomPropertyMapping("customer", (customMappingContext) -> {
                    String customerName = customMappingContext.rawValues["customerName"]
                    def customer = loadCustomer(customerName, FetchPlan.BASE) as Customer
                    String email = customMappingContext.rawValues['customerEmail']
                    String grade = customMappingContext.rawValues['customerGrade']
                    if (customer == null) {
                        def newCustomer = dataManager.create(Customer)
                        newCustomer.name = customerName
                        newCustomer.email = email
                        newCustomer.grade = CustomerGrade.fromId(grade)
                        return newCustomer
                    }
                }))

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('customerName', 'John Dow')
        importedDataItem.addRawValue('customerEmail', 'j.dow@mail.com')
        importedDataItem.addRawValue('customerGrade', 'Bronze')


        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        checkCustomer(order.customer, 'John Dow', 'j.dow@mail.com', CustomerGrade.BRONZE)
    }

}
