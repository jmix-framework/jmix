/*
 * Copyright 2020 Haulmont.
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

package entity_fetcher

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlans
import io.jmix.core.SaveContext
import io.jmix.eclipselink.impl.JmixEntityFetchGroup
import org.eclipse.persistence.queries.FetchGroupTracker
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestEntityWithNonPersistentRef
import test_support.entity.complex_references.Employee
import test_support.entity.complex_references.Position
import test_support.entity.complex_references.Unit
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.Status

class EntityFetcherTest extends DataSpec {

    @Autowired
    EntityStates entityStates
    @Autowired
    DataManager dataManager
    @Autowired
    FetchPlans fetchPlans


    private Order order;
    private OrderLine orderLine;
    private Employee selfSupervised;
    private Employee supervised;
    private Position position;
    private Unit unit;

    def setup() {
        order = dataManager.create(Order)
        order.number = "1"
        order.amount = BigDecimal.ONE
        orderLine = dataManager.create(OrderLine)
        orderLine.order = order
        orderLine.quantity = 1
        dataManager.save(orderLine, order)


        unit = dataManager.create(Unit)
        unit.title = "First"
        unit.address = "561728"
        dataManager.save(unit)
        position = dataManager.create(Position)
        position.title = "Main"
        position.description = "-"
        position.factor = 1d
        position.defaultUnit = unit
        dataManager.save(position)
        selfSupervised = dataManager.create(Employee)
        selfSupervised.name = "Twoflower"
        selfSupervised.supervisor = selfSupervised
        selfSupervised.position = position
        selfSupervised.unit = unit
        dataManager.save(selfSupervised)

        supervised = dataManager.create(Employee)
        supervised.name = "Rincewind"
        supervised.supervisor = selfSupervised
        supervised.position = position
        supervised.unit = unit
        dataManager.save(supervised)
    }

    def cleanup() {
        dataManager.remove(order, orderLine, supervised, selfSupervised, position, unit)
    }

    def "fetching entity with non-persistent reference"() {
        // setup the entity like it is stored in a custom datastore and linked as transient property
        def npCustomer = dataManager.create(Customer)
        npCustomer.status = Status.OK

        entityStates.makeDetached(npCustomer)
        ((FetchGroupTracker) npCustomer)._persistence_setFetchGroup(new JmixEntityFetchGroup(['id', 'status'], entityStates))

        def entity = dataManager.create(TestEntityWithNonPersistentRef)
        entity.name = 'c'
        entity.customer = npCustomer

        def view = fetchPlans.builder(TestEntityWithNonPersistentRef).addAll('name', 'customer.name').build()

        when:
        def committed = dataManager.save(new SaveContext().saving(entity, view)).get(entity)

        then:
        noExceptionThrown()
        committed == entity
    }

    def "different views on different layers: case 1"() {
        when: "same entity with different fetchPlans appears on different layers of fetch plan"
        OrderLine orderLine = dataManager.load(OrderLine.class).query("select ol from sales_OrderLine ol")
                .fetchPlan(fetchPlans.builder(OrderLine.class)
                        .add("order.orderLines.order.number")
                        .add("order.amount")
                        .build())
                .one()

        then: "each occurence of this entity loaded correctly and no 'Unfetched attribute' exception thrown"
        orderLine.order.orderLines[0].order.number != null
        orderLine.order.amount != null
    }


    def "different views on different layers: case 2 with self ref"() {
        when: "same entity with different fetchPlans appears on different layers through self-reference"
        Employee employee = dataManager.load(Employee.class)
                .id(selfSupervised.id)
                .fetchPlan(fetchPlans.builder(Employee)
                        .add("supervisor.position.description")
                        .add("position.title")
                        .add("name")
                        .add("unit.title")
                        .add("unit.address")
                        .build())
                .one()

        then: "employee position loaded with description"
        employee.supervisor.position.description != null
        employee.name != null
    }

    def "different views on different layers: case 3 through different entities"() {
        when: "same entity with different fetchPlans appears on different layers through other entity"
        Employee employee = dataManager.load(Employee.class)
                .id(selfSupervised.id)
                .fetchPlan(fetchPlans.builder(Employee)
                        .add("supervisor.position.description")
                        .add("unit.employees.position.title")
                        .build())
                .one()

        then: "employee position loaded with both name and description"
        employee.supervisor.position.title != null
        employee.supervisor.position.description != null
        employee.unit.employees[0].position.title != null
        employee.unit.employees[0].position.description != null
    }

    def "different views on different layers: case 4 complex property"() {
        when:
        Employee employee = dataManager.load(Employee.class)
                .id(selfSupervised.id)
                .fetchPlan(fetchPlans.builder(Employee)
                        .add("supervisor.position.defaultUnit.title")
                        .add("supervisor.position.defaultUnit.address")
                        .add("position.title")
                        .build())
                .one()

        then:
        employee.position.defaultUnit.title != null
        employee.position.defaultUnit.address != null
    }

    def "Test local properties complementing"() {
        when: "Some local property appears in one entity in entity graph"
        Employee employee = dataManager.load(Employee.class)
                .id(supervised.id)
                .fetchPlan(fetchPlans.builder(Employee)
                        .add("name")
                        .add("supervisor.position.defaultUnit.title")
                        .add("position.title")
                        .build())
                .one()
        then: "All entities of the same class have this local property loaded"
        employee.supervisor.name != null
    }
}
