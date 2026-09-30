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

package entity_states

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jmix.core.DataManager
import io.jmix.core.EntitySerialization
import io.jmix.core.EntityStates
import io.jmix.core.EntityStates.PropertyLoadedState
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlans
import io.jmix.core.entity.BaseEntityEntry
import io.jmix.core.entity.EntitySystemAccess
import io.jmix.core.entity.EntityValues
import io.jmix.core.impl.ReferenceLoadedPropertiesInfo
import io.jmix.core.impl.StandardSerialization
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestCompositeKeyEntity
import test_support.entity.TestEntityKey
import test_support.entity.TestEntityWithNonPersistentRef
import test_support.entity.dto.TestUuidDto
import test_support.entity.entity_extension.Client
import test_support.entity.importexport.Model
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.Status

class EntityStatesReferenceTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    EntityStates entityStates
    @Autowired
    FetchPlans fetchPlans
    @Autowired
    EntitySerialization entitySerialization
    @Autowired
    StandardSerialization standardSerialization

    def "reference reports only its id as loaded"() {
        given:
        def order = saveOrder()

        when:
        def ref = dataManager.getReference(Order, order.id)

        then:
        entityStates.isLoaded(ref, 'id')
        ['number', 'version', 'createTs', 'customer', 'orderLines'].each {
            assert !entityStates.isLoaded(ref, it)
        }
    }

    def "isLoadedSafe answers YES for the id and NO for other attributes of a reference"() {
        given:
        def order = saveOrder()

        when:
        def ref = dataManager.getReference(Order, order.id)

        then:
        entityStates.isLoadedSafe(ref, 'id') == PropertyLoadedState.YES
        ['number', 'version', 'customer', 'orderLines'].each {
            assert entityStates.isLoadedSafe(ref, it) == PropertyLoadedState.NO
        }
    }

    def "reference conforms only to a fetch plan with the id"() {
        given:
        def order = saveOrder()
        def ref = dataManager.getReference(Order, order.id)
        def idOnlyFetchPlan = fetchPlans.builder(Order).add('id').build()

        expect:
        !entityStates.isLoadedWithFetchPlan(ref, FetchPlan.BASE)
        entityStates.isLoadedWithFetchPlan(ref, idOnlyFetchPlan)

        when:
        entityStates.checkLoadedWithFetchPlan(ref, FetchPlan.BASE)

        then:
        thrown(IllegalArgumentException)
    }

    def "current fetch plan of a reference contains only the id"() {
        given:
        def order = saveOrder()

        when:
        def ref = dataManager.getReference(Order, order.id)

        then:
        entityStates.getCurrentFetchPlan(ref).properties*.name == ['id']
    }

    def "non-persistent and method-based attributes of a reference are not loaded"() {
        when:
        def ref = dataManager.getReference(TestEntityWithNonPersistentRef, UUID.randomUUID())
        def clientRef = dataManager.getReference(Client, UUID.randomUUID())

        then:
        !entityStates.isLoaded(ref, 'customer')
        !entityStates.isLoaded(clientRef, 'htmlName')
        !entityStates.isLoaded(clientRef, 'label1')
    }

    def "values assigned while a reference is created are cleared and not loaded"() {
        when:
        def ref = dataManager.getReference(Model, UUID.randomUUID())

        then:
        ref.name == null
        ref.numberOfSeats == null
        !entityStates.isLoaded(ref, 'name')
        !entityStates.isLoaded(ref, 'numberOfSeats')
    }

    def "value set on a reference is saved when it equals the creation default"() {
        given:
        def model = dataManager.create(Model)
        model.name = 'm1'
        model.numberOfSeats = 2
        dataManager.save(model)

        when: "4 is the value that a @PostConstruct method assigns on creation"
        def ref = dataManager.getReference(Model, model.id)
        ref.numberOfSeats = 4

        then:
        entityStates.isLoaded(ref, 'numberOfSeats')

        when:
        dataManager.save(ref)
        def reloaded = dataManager.load(Model).id(model.id).one()

        then:
        reloaded.numberOfSeats == 4
        reloaded.name == 'm1'

        cleanup:
        jdbc.update("delete from TESTIMPORTEXPORT_MODEL where ID = '${model.id}'".toString())
    }

    def "attribute set on a reference is loaded"() {
        given:
        def customer = saveCustomer()

        when:
        def ref = dataManager.getReference(Customer, customer.id)
        ref.name = 'c2'
        EntityValues.setValue(ref, 'status', Status.NOT_OK)

        then:
        entityStates.isLoaded(ref, 'name')
        entityStates.isLoaded(ref, 'status')
        !entityStates.isLoaded(ref, 'version')
    }

    def "attribute set on a reference stays loaded after Java serialization"() {
        given:
        def customer = saveCustomer()
        def ref = dataManager.getReference(Customer, customer.id)
        ref.name = 'c2'

        when:
        Customer copy = standardSerialization.deserialize(standardSerialization.serialize(ref)) as Customer

        then:
        entityStates.isLoaded(copy, 'id')
        entityStates.isLoaded(copy, 'name')
        !entityStates.isLoaded(copy, 'status')

        when:
        copy.status = Status.OK

        then:
        entityStates.isLoaded(copy, 'status')
    }

    def "method-based attribute of a reference is loaded after an attribute it depends on is set"() {
        when:
        def ref = dataManager.getReference(Client, UUID.randomUUID())
        ref.name = 'n'

        then: "the entity fires change events for the read-only attributes that depend on 'name'"
        entityStates.isLoaded(ref, 'htmlName')
        entityStates.isLoaded(ref, 'label1')
        !entityStates.isLoaded(ref, 'addressCity')
    }

    def "saving a reference writes only the attributes set on it"() {
        given:
        def model = dataManager.create(Model)
        model.name = 'm1'
        model.numberOfSeats = 2
        model.manufacturer = 'x'
        dataManager.save(model)

        when:
        def ref = dataManager.getReference(Model, model.id)
        ref.manufacturer = 'y'
        dataManager.save(ref)
        def reloaded = dataManager.load(Model).id(model.id).one()

        then:
        reloaded.manufacturer == 'y'
        reloaded.name == 'm1'
        reloaded.numberOfSeats == 2

        cleanup:
        jdbc.update("delete from TESTIMPORTEXPORT_MODEL where ID = '${model.id}'".toString())
    }

    def "saving an entity that refers to a reference keeps the referenced entity"() {
        given:
        def customer = saveCustomer()

        when:
        def order = dataManager.create(Order)
        order.number = '2'
        order.customer = dataManager.getReference(Customer, customer.id)
        dataManager.save(order)
        def reloaded = dataManager.load(Order).id(order.id).fetchPlanProperties('number', 'customer.name').one()

        then:
        reloaded.customer.id == customer.id
        reloaded.customer.name == 'c1'
    }

    def "entity returned by save has the local attributes of a referenced entity set as a reference"() {
        given:
        def customer = saveCustomer()
        def order = dataManager.create(Order)
        order.number = '2'
        order.customer = dataManager.getReference(Customer, customer.id)

        when:
        def saved = dataManager.save(order)

        then:
        saved.customer.id == customer.id
        saved.customer.name == 'c1'
        saved.customer.status == Status.OK
        entityStates.isLoaded(saved.customer, 'name')
    }

    def "reference returned by save has its local attributes loaded"() {
        given:
        def customer = saveCustomer()
        def ref = dataManager.getReference(Customer, customer.id)
        ref.name = 'c2'

        when:
        def saved = dataManager.save(ref)

        then:
        saved.name == 'c2'
        saved.status == Status.OK
        entityStates.isLoaded(saved, 'status')
    }

    def "entity can be removed by reference"() {
        given:
        def order = saveOrder()

        when:
        dataManager.remove(dataManager.getReference(Order, order.id))

        then:
        !dataManager.load(Order).id(order.id).optional().isPresent()
    }

    def "reference with a composite key reports the key as loaded"() {
        when:
        def key = new TestEntityKey(tenant: 1, entityId: 10L)
        def ref = dataManager.getReference(TestCompositeKeyEntity, key)

        then:
        entityStates.isLoaded(ref, 'id')
        !entityStates.isLoaded(ref, 'name')
    }

    def "DTO reference reports only its id and the attributes set on it as loaded"() {
        given:
        def id = UUID.randomUUID()

        when:
        def ref = dataManager.getReference(TestUuidDto, id)

        then:
        ref.id == id
        entityStates.isLoaded(ref, 'id')
        !entityStates.isLoaded(ref, 'name')

        when:
        ref.name = 'n'

        then:
        entityStates.isLoaded(ref, 'name')
    }

    def "reference is serialized with its id and the attributes set on it"() {
        given:
        def customer = saveCustomer()
        def ref = dataManager.getReference(Customer, customer.id)
        ref.name = 'c2'

        when:
        JsonNode node = new ObjectMapper().readTree(entitySerialization.toJson(ref))

        then:
        node.get('_entityName').asText() == 'sales_Customer'
        node.get('id').asText() == customer.id.toString()
        node.get('name').asText() == 'c2'
        !node.has('status')
        !node.has('version')
        !node.has('createTs')
    }

    def "copying the entity entry of a reference does not duplicate its listener"() {
        given:
        def ref1 = dataManager.getReference(Customer, UUID.randomUUID())
        def ref2 = dataManager.getReference(Customer, ref1.id)
        BaseEntityEntry entry1 = EntitySystemAccess.getEntityEntry(ref1) as BaseEntityEntry
        BaseEntityEntry entry2 = EntitySystemAccess.getEntityEntry(ref2) as BaseEntityEntry

        when:
        entry2.copy(entry1)
        entry2.copy(entry1)

        then:
        entry2.propertyChangeListeners.size() == 1
        entry2.propertyChangeListeners[0].is(ReferenceLoadedPropertiesInfo.MarkingLoadedOnSetListener.INSTANCE)
    }

    def "listener of a reference is the shared instance after Java serialization"() {
        given:
        def ref = dataManager.getReference(Customer, UUID.randomUUID())

        when:
        Customer copy = standardSerialization.deserialize(standardSerialization.serialize(ref)) as Customer
        BaseEntityEntry entry = EntitySystemAccess.getEntityEntry(copy) as BaseEntityEntry

        then:
        entry.propertyChangeListeners.size() == 1
        entry.propertyChangeListeners[0].is(ReferenceLoadedPropertiesInfo.MarkingLoadedOnSetListener.INSTANCE)
    }

    private Customer saveCustomer() {
        def customer = dataManager.create(Customer)
        customer.name = 'c1'
        customer.status = Status.OK
        return dataManager.save(customer)
    }

    private Order saveOrder() {
        def order = dataManager.create(Order)
        order.number = '1'
        order.customer = saveCustomer()
        return dataManager.save(order)
    }
}
