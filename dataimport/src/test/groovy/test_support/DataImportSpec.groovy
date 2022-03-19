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

package test_support

import io.jmix.core.DataManager
import io.jmix.core.FetchPlans
import io.jmix.dataimport.extractor.data.ImportedObject
import org.apache.commons.lang3.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.entity.*

import javax.annotation.Nullable

@ContextConfiguration(classes = [DataImportTestConfiguration])
class DataImportSpec extends Specification {
    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    FetchPlans fetchPlans

    void setup() {
        createCustomers()
        createProducts()
    }

    private void createCustomers() {
        def customer1 = dataManager.create(Customer)
        customer1.name = 'Parker Leighton'
        customer1.email = 'leighton@mail.com'
        customer1.grade = CustomerGrade.BRONZE

        def customer2 = dataManager.create(Customer)
        customer2.name = 'Shelby Robinson'
        customer2.email = 's.robinson@mail.com'
        customer2.grade = CustomerGrade.SILVER

        dataManager.save(customer1, customer2)
    }

    private void createProducts() {
        def product1 = dataManager.create(Product)
        product1.name = 'Outback Power Nano-Carbon Battery 12V'
        product1.price = 6.25
        product1.special = true

        def product2 = dataManager.create(Product)
        product2.name = 'Fullriver Sealed Battery 6V'
        product2.price = 5.10
        product2.special = false

        dataManager.save(product1, product2)
    }

    void cleanup() {
        jdbcTemplate.update('delete from SALES_ORDER_LINE')
        jdbcTemplate.update('delete from SALES_ORDER')
        jdbcTemplate.update('delete from SALES_PAYMENT_DETAILS')
        jdbcTemplate.update('delete from SALES_CUSTOMER')
        jdbcTemplate.update('delete from SALES_BONUS_CARD')
        jdbcTemplate.update('delete from SALES_PRODUCT')
    }

    protected ImportedObject createImportedObject(Map<String, Object> rawValues) {
        def importedObject = new ImportedObject()
        importedObject.setRawValues(rawValues)
        return importedObject
    }

    protected Customer loadCustomer(String name, String fetchPlan) {
        return dataManager.load(Customer)
                .query("e.name = :name")
                .parameter("name", name)
                .fetchPlan(fetchPlan)
                .optional().orElse(null)
    }

    @Nullable
    protected Object loadEntity(Class entityClass, Object id, String fetchPlan) {
        return dataManager.load(entityClass)
                .id(id)
                .fetchPlan(fetchPlan)
                .optional()
                .orElse(null)
    }

    static def checkOrder(Order order, String orderNumber, String date, def amount) {
        order.orderNumber == orderNumber
        if (date != null) {
            order.date == DateUtils.parseDate(date, 'dd/MM/yyyy HH:mm')
        } else {
            order.date == null
        }
        order.amount == amount
    }


    static def checkCustomer(Customer customer, String name, String email, CustomerGrade grade) {
        customer != null
        customer.name == name
        customer.email == email
        customer.grade == grade
    }

    static def checkPaymentDetails(PaymentDetails paymentDetails, String date, PaymentType paymentType, String bonusCardNumber, BigDecimal bonusAmount) {
        paymentDetails != null
        paymentDetails.date == DateUtils.parseDate(date, 'dd/MM/yyyy HH:mm')
        paymentDetails.paymentType == paymentType
        if (bonusCardNumber != null) {
            paymentDetails.bonusCard != null
            paymentDetails.bonusCard.cardNumber == bonusCardNumber
        } else {
            paymentDetails.bonusCard == null
        }
        paymentDetails.bonusAmount == bonusAmount
    }

    static def checkDeliveryDetails(DeliveryDetails deliveryDetails, String date, String fullAddress) {
        deliveryDetails != null
        deliveryDetails.deliveryDate == DateUtils.parseDate(date, 'dd/MM/yyyy HH:mm')
        deliveryDetails.fullAddress == fullAddress
    }

    static def checkOrderLine(OrderLine orderLine, String productName, Integer quantity) {
        orderLine != null
        if (productName != null) {
            orderLine.product != null
            orderLine.product.name == productName
        } else {
            orderLine.product == null
        }
        orderLine.quantity == quantity
    }

    static def checkProduct(Product product, String name, BigDecimal price, Boolean special) {
        product != null
        product.name == name
        product.price == price
        product.special == special
    }

    static def checkBonusCard(BonusCard bonusCard, String cardNumber, Boolean isActive, BigDecimal balance) {
        bonusCard != null
        bonusCard.cardNumber == cardNumber
        bonusCard.isActive == isActive
        bonusCard.balance == balance
    }
}
