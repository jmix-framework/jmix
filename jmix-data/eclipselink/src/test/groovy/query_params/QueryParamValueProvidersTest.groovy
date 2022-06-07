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

package query_params

import io.jmix.core.DataManager
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.TestQueryParamValueProvider
import test_support.entity.sales.Customer
import test_support.entity.sales.Status

class QueryParamValueProvidersTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    TestQueryParamValueProvider testQueryParamValueProvider;

    Customer customer

    @Override
    void setup() {
        customer = dataManager.create(Customer)
        customer.setName('test1')
        customer.setStatus(Status.OK)
        dataManager.save(customer)
    }

    @Override
    void cleanup() {
        dataManager.remove(customer)
    }

    def "parameter in query"() {
        testQueryParamValueProvider.setValue('customerName', 'test1')

        when:
        def customer1 = dataManager.load(Customer).query('e.name = :test_customerName').one()

        then:
        customer1 == customer

        cleanup:
        testQueryParamValueProvider.clear('customerName')
    }

    def "parameter in condition"() {
        testQueryParamValueProvider.setValue('customerName', 'test1')

        when:
        def customer1 = dataManager.load(Customer)
                .condition(PropertyCondition.createWithParameterName('name', PropertyCondition.Operation.EQUAL, 'test_customerName'))
                .one()

        then:
        customer1 == customer

        cleanup:
        testQueryParamValueProvider.clear('customerName')
    }

    def "parameter in values query"() {
        testQueryParamValueProvider.setValue('customerName', 'test1')

        when:
        def customerId = dataManager.loadValues('select e.id from sales_Customer e where e.name = :test_customerName')
                .properties('id')
                .one()
                .getValue('id')

        then:
        customerId == customer.id

        cleanup:
        testQueryParamValueProvider.clear('customerName')
    }

    def "multiple parameters in query"() {
        testQueryParamValueProvider.setValue('customerName', 'test1')
        testQueryParamValueProvider.setValue('customerStatus', Status.OK.id)

        when:
        def customer1 = dataManager.load(Customer)
                .query('e.name = :test_customerName and e.status = :test_customerStatus')
                .one()

        then:
        customer1 == customer

        cleanup:
        testQueryParamValueProvider.clear('customerName')
        testQueryParamValueProvider.clear('customerStatus')
    }

    def "'in' condition"() {
        testQueryParamValueProvider.setValue('customerNames', Arrays.asList('test1'))

        when:
        def customer1 = dataManager.load(Customer).query('e.name in :test_customerNames').one()

        then:
        customer1 == customer

        cleanup:
        testQueryParamValueProvider.clear('customerName')
    }
}
