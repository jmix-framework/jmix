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

package data_components

import io.jmix.core.DataManager
import io.jmix.core.entity.BaseEntityEntry
import io.jmix.core.entity.EntitySystemAccess
import io.jmix.core.impl.ReferenceLoadedPropertiesInfo
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import io.jmix.flowui.model.MergeOptions
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.TestNotGeneratedIdEntity
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sec.Group
import test_support.entity.sec.User
import test_support.spec.DataContextSpec

class DataContextReferenceMergeTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    DataManager dataManager

    def "reference merged as root onto a loaded instance keeps its values and state"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def loaded = context.merge(dataManager.load(Customer).id(customer.id).one())

        when:
        def merged = context.merge(dataManager.getReference(Customer, customer.id))

        then:
        merged.is(loaded)
        merged.name == 'c1'
        merged.email == 'c1@example.com'
        entityStates.isLoaded(merged, 'name')
        entityStates.isLoaded(merged, 'email')
        entityStates.isDetached(merged)
    }

    def "reference merged as root onto a partially loaded instance keeps its unfetched attributes unloaded"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def loaded = context.merge(dataManager.load(Customer).id(customer.id).fetchPlanProperties('name').one())

        when:
        def merged = context.merge(dataManager.getReference(Customer, customer.id))

        then:
        merged.is(loaded)
        merged.name == 'c1'
        entityStates.isLoaded(merged, 'name')
        !entityStates.isLoaded(merged, 'email')
    }

    def "attribute set on a reference in the context is loaded and saved"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()

        when:
        def merged = context.merge(dataManager.getReference(Customer, customer.id))

        then:
        !entityStates.isLoaded(merged, 'name')

        when:
        merged.name = 'c2'

        then:
        entityStates.isLoaded(merged, 'name')

        when:
        context.save()
        def reloaded = dataManager.load(Customer).id(customer.id).one()

        then:
        reloaded.name == 'c2'
        reloaded.email == 'c1@example.com'
    }

    def "loaded instance merged as root onto a reference copies its values"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def reference = context.merge(dataManager.getReference(Customer, customer.id))

        when:
        def merged = context.merge(dataManager.load(Customer).id(customer.id).one())

        then:
        merged.is(reference)
        merged.name == 'c1'
        merged.email == 'c1@example.com'
        entityStates.isLoaded(merged, 'name')
        entityStates.isLoaded(merged, 'email')
        entityStates.isDetached(merged)
    }

    def "partially loaded instance merged onto a reference does not overwrite unfetched columns on save"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def reference = context.merge(dataManager.getReference(Customer, customer.id))

        when:
        def merged = context.merge(dataManager.load(Customer).id(customer.id).fetchPlanProperties('name').one())

        then:
        merged.is(reference)
        merged.name == 'c1'
        entityStates.isLoaded(merged, 'name')
        !entityStates.isLoaded(merged, 'email')

        when:
        merged.name = 'c2'
        context.save()
        def reloaded = dataManager.load(Customer).id(customer.id).one()

        then:
        reloaded.name == 'c2'
        reloaded.email == 'c1@example.com'
    }

    def "attribute set on a reference in the context survives a partial root merge and is saved"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def reference = context.merge(dataManager.getReference(Customer, customer.id))
        reference.email = 'x'

        when:
        def merged = context.merge(dataManager.load(Customer).id(customer.id).fetchPlanProperties('name').one())

        then:
        merged.is(reference)
        merged.email == 'x'
        merged.name == 'c1'
        entityStates.isLoaded(merged, 'email')

        when:
        context.save()
        def reloaded = dataManager.load(Customer).id(customer.id).one()

        then:
        reloaded.email == 'x'
        reloaded.name == 'c1'
    }

    def "fresh merge of a graph copies the values of a referenced instance held as a reference"() {
        given:
        def customer = saveCustomer()
        def order = dataManager.save(new Order(number: '1', customer: customer))
        DataContext context = factory.createDataContext()
        def reference = context.merge(dataManager.getReference(Customer, customer.id))
        def loadedOrder = dataManager.load(Order).id(order.id)
                .fetchPlanProperties('number', 'customer.name', 'customer.email')
                .one()

        when:
        def merged = context.merge(loadedOrder, new MergeOptions().setFresh(true))

        then:
        merged.customer.is(reference)
        reference.name == 'c1'
        reference.email == 'c1@example.com'
        entityStates.isLoaded(reference, 'name')
        entityStates.isLoaded(reference, 'email')
    }

    def "reference held as a non-root node of a fresh merge does not overwrite a loaded instance in the context"() {
        given:
        def customer = saveCustomer()
        def order = dataManager.save(new Order(number: '1', customer: customer))
        DataContext context = factory.createDataContext()
        def loaded = context.merge(dataManager.load(Customer).id(customer.id).one())
        def order2 = dataManager.load(Order).id(order.id).fetchPlanProperties('number').one()
        order2.customer = dataManager.getReference(Customer, customer.id)

        when:
        context.merge(order2, new MergeOptions().setFresh(true))

        then:
        loaded.name == 'c1'
        loaded.email == 'c1@example.com'
        entityStates.isLoaded(loaded, 'email')
        entityStates.isDetached(loaded)
    }

    def "non-fresh merge of a graph copies the loaded values into a referenced instance held as a reference"() {
        given:
        def customer = saveCustomer()
        def order = dataManager.save(new Order(number: '1', customer: customer))
        DataContext context = factory.createDataContext()
        def reference = context.merge(dataManager.getReference(Customer, customer.id))
        def loadedOrder = dataManager.load(Order).id(order.id)
                .fetchPlanProperties('number', 'customer.name', 'customer.email')
                .one()

        when:
        def merged = context.merge(loadedOrder)

        then:
        merged.customer.is(reference)
        reference.name == 'c1'
        reference.email == 'c1@example.com'
        entityStates.isLoaded(reference, 'name')
        entityStates.isLoaded(reference, 'email')
        !entityStates.isLoaded(reference, 'status')
    }

    def "reference merged onto a reference keeps the attributes set on both"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def ref1 = dataManager.getReference(Customer, customer.id)
        ref1.name = 'n1'
        def tracked = context.merge(ref1)

        when:
        def ref2 = dataManager.getReference(Customer, customer.id)
        ref2.email = 'e2'
        def merged = context.merge(ref2)

        then:
        merged.is(tracked)
        merged.name == 'n1'
        merged.email == 'e2'
        entityStates.isLoaded(merged, 'name')
        entityStates.isLoaded(merged, 'email')
        !entityStates.isLoaded(merged, 'status')
    }

    def "reference held by an instance saved from the context has its local attributes after save"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def order = context.create(Order)
        order.number = '1'
        order.customer = context.merge(dataManager.getReference(Customer, customer.id))

        when:
        context.save()
        def contextCustomer = context.find(Customer, customer.id)

        then:
        order.customer.is(contextCustomer)
        contextCustomer.name == 'c1'
        entityStates.isLoaded(contextCustomer, 'name')
    }

    def "reference merged into the context does not report its generated id as loaded and does not save it"() {
        given:
        def entity = metadata.create(TestNotGeneratedIdEntity)
        entity.id = 5660
        entity.name = 'n1'
        def uuid = dataManager.save(entity).uuid
        DataContext context = factory.createDataContext()

        when:
        def merged = context.merge(dataManager.getReference(TestNotGeneratedIdEntity, 5660))

        then:
        !entityStates.isLoaded(merged, 'uuid')

        when:
        merged.name = 'n2'
        context.save()
        def reloaded = dataManager.load(TestNotGeneratedIdEntity).id(5660).one()

        then:
        reloaded.name == 'n2'
        reloaded.uuid == uuid

        cleanup:
        jdbc.update('delete from TEST_NOT_GENERATED_ID_ENTITY')
    }

    def "listener of a reference is not duplicated by edits in child contexts"() {
        given:
        def customer = saveCustomer()
        DataContext parent = factory.createDataContext()
        def reference = parent.merge(dataManager.getReference(Customer, customer.id))
        parent.merge(dataManager.load(Customer).id(customer.id).one())

        when:
        3.times { i ->
            DataContext child = factory.createDataContext()
            child.setParent(parent)
            def childCustomer = child.merge(reference)
            childCustomer.name = "n$i"
            child.save()
        }
        BaseEntityEntry entry = EntitySystemAccess.getEntityEntry(reference) as BaseEntityEntry

        then:
        reference.name == 'n2'
        entry.propertyChangeListeners.count { it instanceof ReferenceLoadedPropertiesInfo.MarkingLoadedOnSetListener } <= 1
    }

    def "loaded instance merged onto a reference copies its embedded value"() {
        given:
        def customer = dataManager.save(new Customer(name: 'c1', email: 'c1@example.com',
                address: new Address(city: 'Paris', zip: '75000')))
        DataContext context = factory.createDataContext()
        def reference = context.merge(dataManager.getReference(Customer, customer.id))

        when:
        def merged = context.merge(dataManager.load(Customer).id(customer.id).one())

        then:
        merged.is(reference)
        merged.address.city == 'Paris'
        merged.address.zip == '75000'
        entityStates.isLoaded(merged, 'address')
    }

    def "attribute set on a reference is merged into a partially loaded instance and saved"() {
        given:
        def customer = saveCustomer()
        DataContext context = factory.createDataContext()
        def loaded = context.merge(dataManager.load(Customer).id(customer.id).fetchPlanProperties('name').one())
        def ref = dataManager.getReference(Customer, customer.id)
        ref.email = 'new'

        when:
        def merged = context.merge(ref)

        then:
        merged.is(loaded)
        merged.name == 'c1'
        merged.email == 'new'
        entityStates.isLoaded(merged, 'email')
        entityStates.isDetached(merged)

        when:
        merged.name = 'c2'
        context.save()
        def reloaded = dataManager.load(Customer).id(customer.id).one()

        then:
        reloaded.name == 'c2'
        reloaded.email == 'new'
    }

    def "value set on a reference in the context is saved when it equals the constructor default"() {
        given:
        def group = dataManager.save(new Group(name: 'g1'))
        def user = dataManager.save(new User(login: 'u1', active: false, group: group))
        DataContext context = factory.createDataContext()
        def merged = context.merge(dataManager.getReference(User, user.id))

        when:
        merged.active = true
        merged.name = 'changed'

        then:
        entityStates.isLoaded(merged, 'active')

        when:
        context.save()
        def reloaded = dataManager.load(User).id(user.id).one()

        then:
        reloaded.active
        reloaded.name == 'changed'
    }

    private Customer saveCustomer() {
        dataManager.save(new Customer(name: 'c1', email: 'c1@example.com', address: new Address()))
    }
}
