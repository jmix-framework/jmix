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
import io.jmix.dataimport.property.populator.EntityPropertiesPopulator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.BonusCard
import test_support.entity.Customer
import test_support.entity.Order
import test_support.entity.PaymentType

class OneToOneReferenceCreationTest extends DataImportSpec {

    @Autowired
    protected EntityPropertiesPopulator entityPopulator

    def 'test creation of one-to-one association from imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "customerName")
                .addReferencePropertyMapping("bonusCard", "bonusCardNumber", "cardNumber", ReferenceImportPolicy.CREATE)
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("customerName", "John Dow")
        importedDataItem.addRawValue("bonusCardNumber", "12345-6789")

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        checkBonusCard(customer.bonusCard, '12345-6789', null, null)
    }

    def 'test creation of one-to-one association from separate imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "customerName")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("bonusCard", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .withDataFieldName("bonusCard")
                        .addSimplePropertyMapping("cardNumber", "cardNumber")
                        .addSimplePropertyMapping("isActive", "isActive")
                        .addSimplePropertyMapping("balance", "balance")
                        .lookupByAllSimpleProperties()
                        .build())
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.setRawValues(ParamsMap.of("customerName", "John Dow",
                "cardNumber", "12345-67890",
                "isActive", "True",
                "balance", "50"));

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        checkBonusCard(customer.bonusCard, '12345-67890', true, 50 as BigDecimal)
    }

    def 'test ignore not existing one-to-one association from separate imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "customerName")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("bonusCard", ReferenceImportPolicy.IGNORE_IF_MISSING)
                        .withDataFieldName("bonusCard")
                        .addSimplePropertyMapping("cardNumber", "cardNumber")
                        .addSimplePropertyMapping("isActive", "isActive")
                        .addSimplePropertyMapping("balance", "balance")
                        .lookupByAllSimpleProperties()
                        .build())
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("customerName", "John Dow")

        def bonusCardObject = new ImportedObject()
        bonusCardObject.setRawValues(ParamsMap.of("cardNumber", "12345-67890",
                "isActive", "True",
                "balance", "50"))
        importedDataItem.addRawValue("bonusCard", bonusCardObject);

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.bonusCard == null
    }

    def 'test ignore not existing one-to-one association from imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "customerName")
                .addReferencePropertyMapping("bonusCard", "bonusCardNumber", "cardNumber", ReferenceImportPolicy.IGNORE_IF_MISSING)
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("customerName", "John Dow")
        importedDataItem.addRawValue("bonusCardNumber", "12345-6789")

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.bonusCard == null
    }

    def 'test set existing one-to-one association from imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "customerName")
                .addReferencePropertyMapping("bonusCard", "bonusCardNumber", "cardNumber", ReferenceImportPolicy.IGNORE_IF_MISSING)
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("customerName", "John Dow")
        importedDataItem.addRawValue("bonusCardNumber", "12345-6789")

        def bonusCard = dataManager.create(BonusCard)
        bonusCard.cardNumber = '12345-6789'
        bonusCard = dataManager.save(bonusCard)

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.bonusCard != null
        customer.bonusCard == bonusCard
    }

    def 'test set existing existing one-to-one association from separate imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Customer, InputDataFormat.XLSX)
                .addSimplePropertyMapping("name", "customerName")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("bonusCard", ReferenceImportPolicy.IGNORE_IF_MISSING)
                        .withDataFieldName("bonusCard")
                        .addSimplePropertyMapping("cardNumber", "cardNumber")
                        .withLookupPropertyNames('cardNumber')
                        .build())
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.setRawValues(ParamsMap.of("customerName", "John Dow",
                "bonusCard", new ImportedObject()
                .addRawValue("cardNumber", "12345-67890")));

        def bonusCard = dataManager.create(BonusCard)
        bonusCard.cardNumber = '12345-67890'
        bonusCard = dataManager.save(bonusCard)

        when: 'entity properties populated'
        def customer = dataManager.create(Customer)
        def entityInfo = entityPopulator.populateProperties(customer, configuration, importedDataItem)

        then:
        entityInfo.entity == customer
        checkCustomer(customer, 'John Dow', null, null)
        customer.bonusCard != null
        customer.bonusCard == bonusCard
    }

    def 'test creation of nested one-to-one association from imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addSimplePropertyMapping("orderNumber", "orderNum")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("paymentDetails", ReferenceImportPolicy.CREATE)
                        .addSimplePropertyMapping("paymentType", "paymentType")
                        .addSimplePropertyMapping("date", "paymentDate")
                        .addSimplePropertyMapping("bonusAmount", "bonusAmount")
                        .addReferencePropertyMapping("bonusCard", "bonusCardNumber", "cardNumber", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("orderNum", "#001")
        importedDataItem.addRawValue("paymentType", "Cash")
        importedDataItem.addRawValue("paymentDate", "12/06/2021 12:00")
        importedDataItem.addRawValue("bonusCardNumber", "12345-67890")
        importedDataItem.addRawValue("bonusAmount", "0")

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        checkOrder(order, "#001", null, null)
        checkPaymentDetails(order.paymentDetails, '12/06/2021 12:00', PaymentType.CASH, '12345-67890', BigDecimal.ZERO)
    }

    def 'test creation of nested one-to-one association from separate imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addSimplePropertyMapping("orderNumber", "orderNum")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("paymentDetails", ReferenceImportPolicy.CREATE)
                        .withDataFieldName('paymentDetails')
                        .addSimplePropertyMapping("paymentType", "paymentType")
                        .addSimplePropertyMapping("date", "paymentDate")
                        .addSimplePropertyMapping("bonusAmount", "bonusAmount")
                        .addReferencePropertyMapping("bonusCard", "bonusCardNumber", "cardNumber", ReferenceImportPolicy.CREATE)
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()
        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("orderNum", "#001")

        importedDataItem.addRawValue("paymentType", "Cash")
        importedDataItem.addRawValue("paymentDate", "12/06/2021 12:00")
        importedDataItem.addRawValue("bonusCardNumber", "12345-67890")
        importedDataItem.addRawValue("bonusAmount", "0")

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = entityPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        checkOrder(order, "#001", null, null)
        checkPaymentDetails(order.paymentDetails, '12/06/2021 12:00', PaymentType.CASH, '12345-67890', BigDecimal.ZERO)
    }
}
