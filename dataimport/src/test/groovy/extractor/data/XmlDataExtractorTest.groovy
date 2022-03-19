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
import io.jmix.dataimport.extractor.data.ImportedObject
import io.jmix.dataimport.extractor.data.ImportedObjectList
import io.jmix.dataimport.extractor.data.impl.XmlDataExtractor
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Customer
import test_support.entity.Product

class XmlDataExtractorTest extends DataImportSpec {

    @Autowired
    protected XmlDataExtractor xmlDataExtractor

    @Autowired
    protected Resources resources;

    def "test imported data if root node is an array"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/xml/list_of_products.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'special', 'price']

        importedData.items.size() == 2
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('name') == 'Outback Power Nano-Carbon Battery 12V'
        firstProduct.getRawValue('special') == 'Yes'
        firstProduct.getRawValue('price') == '6.25'

        def secondProduct = importedData.items[1]
        secondProduct.itemIndex == 2
        secondProduct.rawValues.size() == 3
        secondProduct.getRawValue('name') == 'Fullriver Sealed Battery 6V'
        secondProduct.getRawValue('special') == 'No'
        secondProduct.getRawValue('price') == '5.10'
    }

    def "test imported data if root node is an object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/xml/one_product.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'special', 'price']

        importedData.items.size() == 1
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('name') == 'Cotek Battery Charger'
        firstProduct.getRawValue('special') == 'No'
        firstProduct.getRawValue('price') == '30.10'
    }

    def "test non-empty imported list object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/xml/customers_with_orders.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'orders']

        def importedCustomerItem = importedData.items[1]
        def emptyOrders = (ImportedObjectList) importedCustomerItem.getRawValue('orders')
        emptyOrders.dataFieldName == 'orders'
        emptyOrders.importedObjects.size() == 1

        def importedOrderObject = emptyOrders.importedObjects[0]
        importedOrderObject.dataFieldName == 'order'
        importedOrderObject.getRawValue("number") == '#001'
        importedOrderObject.getRawValue("amount") == '50.5'
        importedOrderObject.getRawValue("date") == '12/02/2021 12:00'
    }

    def "test import several objects with the same tag as list"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/xml/customer_with_orders.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'order']

        def importedCustomerItem = importedData.items[0]
        def importedOrderList = (ImportedObjectList) importedCustomerItem.getRawValue('order')
        importedOrderList.importedObjects.size() == 2

        def firstImportedOrderObject = importedOrderList.importedObjects[0]
        firstImportedOrderObject.dataFieldName == 'order'
        firstImportedOrderObject.getRawValue("number") == '#001'
        firstImportedOrderObject.getRawValue("amount") == '50.5'
        firstImportedOrderObject.getRawValue("date") == '12/02/2021 12:00'

        def secondImportedOrderObject = importedOrderList.importedObjects[1]
        secondImportedOrderObject.dataFieldName == 'order'
        secondImportedOrderObject.getRawValue("number") == '#002'
        secondImportedOrderObject.getRawValue("amount") == '25'
        secondImportedOrderObject.getRawValue("date") == '12/05/2021 17:00'
    }

    def "test null values"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/xml/customers_with_orders.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'orders']

        def firstCustomer = importedData.items[2]
        firstCustomer.getRawValue('email') == null
        firstCustomer.getRawValue('orders') == null
    }

    def "test non-empty imported object"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/xml/customers_with_addresses.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Customer, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames == ['name', 'email', 'defaultAddress']

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

    def 'test read from byte array'() {
        given:
        def xmlString = resources.getResourceAsString("test_support/input_data_files/xml/one_product.xml")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.XML);

        when: 'imported data extracted'
        def importedData = xmlDataExtractor.extract(importConfiguration, xmlString.getBytes())

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['name', 'special', 'price']

        importedData.items.size() == 1
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('name') == 'Cotek Battery Charger'
        firstProduct.getRawValue('special') == 'No'
        firstProduct.getRawValue('price') == '30.10'
    }
}
