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

package extractor.data

import io.jmix.core.Resources
import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.exception.ImportException
import io.jmix.dataimport.extractor.data.ImportedObject
import io.jmix.dataimport.extractor.data.ImportedObjectList
import io.jmix.dataimport.extractor.data.impl.JsonDataExtractor
import org.apache.commons.compress.utils.CharsetNames
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Customer
import test_support.entity.Product

class JsonDataExtractorTest extends DataImportSpec {

    @Autowired
    protected JsonDataExtractor jsonDataExtractor

    @Autowired
    protected Resources resources;

    def 'test item indices'() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/customers_and_addresses.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)
        def importedDataItems = importedData.items

        then:
        importedDataItems.size() == 3
        for (i in 0..<importedDataItems.size()) {
            def importedItem = importedDataItems[i]
            importedItem.itemIndex == i + 1
        }
    }

    def "test read imported data from byte array"() {
        given:
        def jsonString = resources.getResourceAsString("test_support/input_data_files/json/customers_and_addresses.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, jsonString.getBytes(CharsetNames.UTF_8))

        then:
        importedData.items.size() == 3
    }

    def "test imported data if root node is an object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/one_product.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'special', 'price']

        importedData.items.size() == 1
        def firstProduct = importedData.items[0]
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('name') == 'Outback Power Nano-Carbon Battery 12V'
        firstProduct.getRawValue('special') == 'Yes'
        firstProduct.getRawValue('price') == '6.25'
    }

    def "test imported data if root node is an array"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/array_of_products.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'special', 'price']

        importedData.items.size() == 2
        def firstProduct = importedData.items[0]
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('name') == 'Outback Power Nano-Carbon Battery 12V'
        firstProduct.getRawValue('special') == 'Yes'
        firstProduct.getRawValue('price') == '6.25'

        def secondProduct = importedData.items[1]
        secondProduct.rawValues.size() == 3
        secondProduct.getRawValue('name') == 'Fullriver Sealed Battery 6V'
        secondProduct.getRawValue('special') == 'No'
        secondProduct.getRawValue('price') == '5.1'
    }

    def "test empty imported list object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/customers_and_orders.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'orders']

        def firstCustomer = importedData.items[0]
        def emptyOrders = firstCustomer.getRawValue('orders') as ImportedObjectList
        emptyOrders.dataFieldName == 'orders'
        emptyOrders.importedObjects.size() == 0
    }

    def "test non-empty imported list object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/customers_and_orders.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'orders']

        def importedCustomerItem = importedData.items[1]
        def emptyOrders = (ImportedObjectList) importedCustomerItem.getRawValue('orders')
        emptyOrders.dataFieldName == 'orders'
        emptyOrders.importedObjects.size() == 1

        def importedOrderObject = emptyOrders.importedObjects[0]
        importedOrderObject.dataFieldName == null
        importedOrderObject.getRawValue("number") == '#001'
        importedOrderObject.getRawValue("amount") == '50.5'
        importedOrderObject.getRawValue("date") == '12/02/2021 12:00'
    }

    def "test null values"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/customers_and_orders.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'orders']

        def firstCustomer = importedData.items[2]
        firstCustomer.getRawValue('email') == null
        firstCustomer.getRawValue('orders') == null
    }

    def "test empty imported object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/customers_and_addresses.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON);

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'email', 'defaultAddress']

        importedData.items.size() == 3
        def customerItem = importedData.items[0]
        customerItem.rawValues.size() == 3
        customerItem.getRawValue('name') == 'Parker Leighton'
        customerItem.getRawValue('email') == 'leighton@mail.com'
        def defaultAddress = (ImportedObject) customerItem.getRawValue('defaultAddress')
        defaultAddress.dataFieldName == 'defaultAddress'
        defaultAddress.rawValues.size() == 0
    }

    def "test non-empty imported object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/json/customers_and_addresses.json")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.JSON)

        when: 'imported data extracted'
        def importedData = jsonDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'email', 'defaultAddress']

        importedData.items.size() == 3
        def customerItem = importedData.items[1]
        customerItem.rawValues.size() == 3
        customerItem.getRawValue('name') == 'Shelby Robinson'
        customerItem.getRawValue('email') == 'robinson@mail.com'
        def defaultAddress = (ImportedObject) customerItem.getRawValue('defaultAddress')
        defaultAddress.dataFieldName == 'defaultAddress'
        defaultAddress.rawValues.size() == 2
        defaultAddress.getRawValue('name') == 'Home'
        defaultAddress.getRawValue('fullAddress') == 'Samara'
    }

    def "test invalid json byte array"() {
        given:
        String jsonString = "invalid json string"

        when: 'imported data extracted'
        jsonDataExtractor.extract(null, jsonString.getBytes())

        then:
        thrown ImportException
    }

    def "test invalid json input stream"() {
        given:
        String jsonString = "invalid json string"

        when: 'imported data extracted'
        jsonDataExtractor.extract(null, new ByteArrayInputStream(jsonString.getBytes()))

        then:
        thrown ImportException
    }
}
