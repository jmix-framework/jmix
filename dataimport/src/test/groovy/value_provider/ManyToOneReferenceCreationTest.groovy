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

import com.google.common.collect.ImmutableMap
import io.jmix.core.FetchPlan
import io.jmix.core.common.util.ParamsMap
import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping
import io.jmix.dataimport.extractor.data.ImportedDataItem
import io.jmix.dataimport.extractor.data.ImportedObject
import io.jmix.dataimport.property.populator.EntityPropertiesPopulator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Customer
import test_support.entity.CustomerGrade
import test_support.entity.Order

class ManyToOneReferenceCreationTest extends DataImportSpec {

    @Autowired
    protected EntityPropertiesPopulator entityPropertiesPopulator


    def 'test reference creation using data from imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("customer", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .withDataFieldName("customer")
                        .addSimplePropertyMapping("name", "name")
                        .addSimplePropertyMapping("email", "email")
                        .addSimplePropertyMapping("grade", "grade")
                        .lookupByAllSimpleProperties()
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        def customerImportedObject = new ImportedObject()
        customerImportedObject.setRawValues(ImmutableMap.of("name", "John Dow",
                "email", "j.dow@mail.com",
                "grade", "Bronze"))
        importedDataItem.addRawValue('customer', customerImportedObject)

        when: 'entity properties populated'

        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 1

        def createdCustomer = entityInfo.createdReferences[0].createdObject as Customer

        order.customer == createdCustomer
        checkCustomer(createdCustomer, 'John Dow', 'j.dow@mail.com', CustomerGrade.BRONZE)
    }

    def 'test reference creation using data imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("customer", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .addSimplePropertyMapping("name", "Customer Name")
                        .addSimplePropertyMapping("email", "Customer Email")
                        .addSimplePropertyMapping("grade", "Customer Grade")
                        .lookupByAllSimpleProperties()
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('Customer Name', "John Dow")
        importedDataItem.addRawValue('Customer Email', "j.dow@mail.com")
        importedDataItem.addRawValue('Customer Grade', "Bronze")

        when: 'entity properties populated'

        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 1

        def createdCustomer = entityInfo.createdReferences[0].createdObject as Customer
        order.customer == createdCustomer
        checkCustomer(createdCustomer, 'John Dow', 'j.dow@mail.com', CustomerGrade.BRONZE)
    }

    def 'test ignore not existing many-to-one reference using data from imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addReferencePropertyMapping("customer", "Customer Name", 'name', ReferenceImportPolicy.IGNORE_IF_MISSING)
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('Customer Name', 'John Dow')

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 0
        order.customer == null
    }

    def 'test ignore not existing many-to-one reference using data from imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder('customer', ReferenceImportPolicy.IGNORE_IF_MISSING)
                        .withDataFieldName('customer')
                        .addSimplePropertyMapping('name', 'name')
                        .withLookupPropertyNames('name')
                        .build())
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('customer', createImportedObject(ParamsMap.of("name", 'John Dow')))

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 0
        order.customer == null
    }

    def 'test load existing reference using data from imported item'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addReferencePropertyMapping("customer", "Customer Name", "name", ReferenceImportPolicy.IGNORE_IF_MISSING)
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue('Customer Name', 'Parker Leighton')

        def customer = loadCustomer('Parker Leighton', FetchPlan.BASE)

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 0
        order.customer == customer
    }

    def 'test load existing reference from separate imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder('customer', ReferenceImportPolicy.IGNORE_IF_MISSING)
                        .withDataFieldName('customer')
                        .addSimplePropertyMapping('name', 'name')
                        .lookupByAllSimpleProperties()
                        .build())
                .build()

        def importedDataItem = new ImportedDataItem()
        def customerImportedObject = new ImportedObject()
        customerImportedObject.addRawValue("name", "Parker Leighton")
        importedDataItem.addRawValue('customer', customerImportedObject)

        def customer = loadCustomer('Parker Leighton', FetchPlan.BASE)

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPropertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 0
        order.customer == customer
    }
}
