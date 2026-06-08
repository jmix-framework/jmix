/*
 * Copyright 2026 Haulmont.
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

package component.jpqlfilter

import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.jpqlfilter.JpqlFilter
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class JpqlFilterEntityParameterTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Autowired
    Metadata metadata

    @Autowired
    SingleFilterSupport singleFilterSupport

    def "entity parameter is passed to query condition as entity object, not as its ID"() {
        given: "a JpqlFilter with Customer as the parameter type"
        def jf = uiComponents.create(JpqlFilter) as JpqlFilter<Customer>
        jf.setConditionModificationDelegated(true)
        jf.setParameterName("customer")
        jf.setParameterClass(Customer)
        jf.setCondition("{E}.customer = :customer", null)
        def vc = singleFilterSupport.generateValueComponent(metadata.getClass(Customer), false, Customer)
        jf.setValueComponent(vc)

        when: "user selects a Customer entity"
        def customer = new Customer(name: "Acme")
        jf.setValue(customer)

        then: "the query parameter is the Customer entity object, not its ID"
        def paramValue = jf.getQueryCondition().getParameterValuesMap().get("customer")
        paramValue instanceof Customer
        paramValue.is(customer)
    }

    def "entity parameter with null value clears the query condition"() {
        given:
        def jf = uiComponents.create(JpqlFilter) as JpqlFilter<Customer>
        jf.setConditionModificationDelegated(true)
        jf.setParameterName("customer")
        jf.setParameterClass(Customer)
        jf.setCondition("{E}.customer = :customer", null)
        def vc = singleFilterSupport.generateValueComponent(metadata.getClass(Customer), false, Customer)
        jf.setValueComponent(vc)
        jf.setValue(new Customer(name: "Acme"))

        when: "value is cleared"
        jf.setValue(null)

        then:
        jf.getQueryCondition().getParameterValuesMap().get("customer") == null
    }

    def "scalar parameter type is not affected by the entity check"() {
        given: "a JpqlFilter with String as the parameter type"
        def jf = uiComponents.create(JpqlFilter) as JpqlFilter<String>
        jf.setConditionModificationDelegated(true)
        jf.setParameterName("num")
        jf.setParameterClass(String)
        jf.setCondition("{E}.number = :num", null)
        def vc = singleFilterSupport.generateValueComponent(metadata.getClass(Customer), false, String)
        jf.setValueComponent(vc)

        when:
        jf.setValue("ORD-001")

        then: "scalar value is passed through unchanged"
        jf.getQueryCondition().getParameterValuesMap().get("num") == "ORD-001"
    }
}
