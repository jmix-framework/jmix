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

package fetch_groups

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlans
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.eclipselink.impl.FetchGroupManager
import org.eclipse.persistence.config.QueryHints
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.Customer
import test_support.entity.sales.Order

class JoinFetchConditionOnReferenceTest extends DataSpec {

    @Autowired
    DataManager dm
    @Autowired
    EntityStates entityStates
    @Autowired
    FetchPlans fetchPlans
    @Autowired
    FetchGroupManager fetchGroupManager

    Customer customer
    Order orderWithCustomer
    Order orderWithoutCustomer

    @Override
    void setup() {
        customer = dm.create(Customer)
        customer.name = 'cust1'

        orderWithCustomer = dm.create(Order)
        orderWithCustomer.number = 'ord-with-customer'
        orderWithCustomer.customer = customer

        orderWithoutCustomer = dm.create(Order)
        orderWithoutCustomer.number = 'ord-without-customer'

        dm.save(customer, orderWithCustomer, orderWithoutCustomer)
    }

    def "order without customer matches OR condition when customer is join-fetched, JPQL condition"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .build()

        when:
        def orders = dm.load(Order)
                .query('select e from sales_Order e where e.customer = :customer or e.number = :number')
                .parameter('customer', customer)
                .parameter('number', 'ord-without-customer')
                .fetchPlan(fetchPlan)
                .list()

        then:
        orders.size() == 2
        orders.find { it == orderWithCustomer } != null
        orders.find { it == orderWithoutCustomer } != null
        entityStates.isLoaded(orders.find { it == orderWithCustomer }, 'customer')
    }

    def "order without customer matches OR condition when customer is join-fetched, PropertyConditions"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .build()

        when:
        def orders = dm.load(Order)
                .condition(LogicalCondition.or(
                        PropertyCondition.equal('customer', customer),
                        PropertyCondition.contains('number', 'without')
                ))
                .fetchPlan(fetchPlan)
                .list()

        then:
        orders.size() == 2
    }

    def "batch fetching is used instead of join fetching for reference used in query condition"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .build()

        when: "reference is used in a query condition"
        def description = fetchGroupManager.calculateFetchGroup(
                'select e from sales_Order e where e.customer = :customer or e.number = :number',
                fetchPlan, false, true)

        then:
        description.hints['e.customer'] == QueryHints.BATCH

        when: "reference is not used in query conditions"
        description = fetchGroupManager.calculateFetchGroup(
                'select e from sales_Order e where e.number = :number',
                fetchPlan, false, true)

        then:
        description.hints['e.customer'] == QueryHints.LEFT_FETCH

        when: "condition is on a nested attribute of the reference"
        description = fetchGroupManager.calculateFetchGroup(
                'select e from sales_Order e where e.customer.name = :name',
                fetchPlan, false, true)

        then:
        description.hints['e.customer'] == QueryHints.LEFT_FETCH

        when: "reference is used in an explicit left join, condition is on the join variable"
        description = fetchGroupManager.calculateFetchGroup(
                'select e from sales_Order e left join e.customer cje_0 where (lower(cje_0.name) like :name or e.number = :number)',
                fetchPlan, false, true)

        then:
        description.hints['e.customer'] == QueryHints.LEFT_FETCH
    }
}
