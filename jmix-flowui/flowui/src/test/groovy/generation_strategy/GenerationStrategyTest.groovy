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

package generation_strategy

import io.jmix.core.Metadata
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.flowui.component.ComponentGenerationContext
import io.jmix.flowui.component.UiComponentsGenerator
import io.jmix.flowui.component.checkbox.JmixCheckbox
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.component.datepicker.TypedDatePicker
import io.jmix.flowui.component.select.JmixSelect
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.component.timepicker.TypedTimePicker
import io.jmix.flowui.component.valuepicker.EntityPicker
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker
import io.jmix.flowui.data.ValueSource
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Ignore
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.Status
import test_support.entity.sec.User
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
class GenerationStrategyTest extends FlowuiTestSpecification {

    @Autowired
    UiComponentsGenerator uiComponentsGenerator

    @Autowired
    Metadata metadata

    @Autowired
    DataComponents dataComponents

    Order order
    Customer customer
    User user

    MetaClass orderMetaClass
    MetaClass customerMetaClass
    MetaClass userMetaClass

    InstanceContainer<Order> orderInstanceContainer
    InstanceContainer<Customer> customerInstanceContainer
    InstanceContainer<User> userInstanceContainer

    @Override
    void setup() {
        orderMetaClass = metadata.getClass(Order.class)
        customerMetaClass = metadata.getClass(Customer.class)
        userMetaClass = metadata.getClass(User.class)

        orderInstanceContainer = dataComponents.createInstanceContainer(Order.class)
        customerInstanceContainer = dataComponents.createInstanceContainer(Customer.class)
        userInstanceContainer = dataComponents.createInstanceContainer(User.class)

        order = new Order()
        customer = new Customer()
        user = new User()
        def orderLine1 = new OrderLine()
        def orderLine2 = new OrderLine()

        user.setTimeZoneAuto(false)

        customer.setStatus(Status.NOT_OK)

        order.setAmount(BigDecimal.valueOf(10.10))
        order.setCustomer(customer)
        order.setUser(user)
        order.setDate(LocalDate.now())
        order.setNumber("1414")
        order.setTotal(15.32d)
        order.setTime(LocalTime.now().withNano(0))
        order.setOrderLines(List.of(orderLine1, orderLine2))

        orderInstanceContainer.setItem(order)
        customerInstanceContainer.setItem(customer)
        userInstanceContainer.setItem(user)
    }

    @Ignore
    def "Generate component for collection attribute"() {
        when: "MetaProperty is collection"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, "orderLines")
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, "orderLines")

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "JmixMultiValuePicker component will be generated"
        component instanceof JmixMultiValuePicker
    }

    def "Generate component for association attribute without items"() {
        when: "MetaProperty is entity without items"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, "customer")
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, "customer")

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "EntityPicker will be generated"
        component instanceof EntityPicker<Customer>
        (component as EntityPicker<Customer>).value == customer
    }

    def "Generate component for association attribute with items"() {
        when: "MetaProperty is entity with items"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, "user")
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, "user")

        CollectionContainer<User> collectionContainer = dataComponents.createCollectionContainer(User.class)
        collectionContainer.setItems(List.of(user))

        context.setCollectionItems(collectionContainer)
        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "EntityComboBox will be generated"
        component instanceof EntityComboBox<User>
        (component as EntityComboBox<User>).value == user
    }

    def "Generate component for enum attribute"() {
        when: "MetaProperty is enum"

        ComponentGenerationContext context = new ComponentGenerationContext(customerMetaClass, "status")
        ValueSource<?> valueSource = new ContainerValueSource<>(customerInstanceContainer, "status")

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "JmixSelect will be generated"
        component instanceof JmixSelect<Status>
        (component as JmixSelect<Status>).value == customer.status
    }

    def "Generate component for string attribute"() {
        when: "MetaProperty is string"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, propertyName)
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, propertyName)

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "TypedTextField will be generated"
        component instanceof TypedTextField<String>
        (component as TypedTextField<String>).value == order."$propertyName".toString()

        where:
        propertyName << ['number', 'id']
    }

    def "Generate component for boolean attribute"() {
        when: "MetaProperty is boolean"

        ComponentGenerationContext context = new ComponentGenerationContext(userMetaClass, "timeZoneAuto")
        ValueSource<?> valueSource = new ContainerValueSource<>(userInstanceContainer, "timeZoneAuto")

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "JmixCheckbox will be generated"
        component instanceof JmixCheckbox
        (component as JmixCheckbox).value == user.timeZoneAuto
    }

    def "Generate component for date attribute"() {
        when: "MetaProperty is LocalDate"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, "date")
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, "date")

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "DatePicker will be generated"
        component instanceof TypedDatePicker
        (component as TypedDatePicker).value == order.date
    }

    def "Generate component for time attribute"() {
        when: "MetaProperty is LocalTime"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, "time")
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, "time")

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "TimePicker will be generated"
        component instanceof TypedTimePicker
        (component as TypedTimePicker).value == order.time
    }

    def "Generate component for number attribute"() {
        when: "MetaProperty is Number"

        ComponentGenerationContext context = new ComponentGenerationContext(orderMetaClass, propertyName)
        ValueSource<?> valueSource = new ContainerValueSource<>(orderInstanceContainer, propertyName)

        context.setValueSource(valueSource)

        def component = uiComponentsGenerator.generate(context)

        then: "TypedTextField will be generated"
        component instanceof TypedTextField
        (component as TypedTextField).typedValue == order."$propertyName"

        where:
        propertyName << ['total', 'amount']
    }
}
