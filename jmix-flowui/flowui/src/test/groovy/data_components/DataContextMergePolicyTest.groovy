package data_components

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.Id
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.DataContextSpec

class DataContextMergePolicyTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    EntityStates entityStates
    @Autowired
    DataManager dataManager

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "mutating a transplanted collection marks the owner modified"() {
        given: "managed order without orderLines; a copy carrying them is merged over it"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))

        Order managed = context.merge(dataManager.load(Id.of(order)).fetchPlan { it.add('number') }.one())
        context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'orderLines.quantity') }.one(),
                new io.jmix.flowui.model.MergeOptions().setFresh(true))
        context.setModified(managed, false) // start clean

        when: "the user mutates the (transplanted) collection"
        def newLine = context.create(OrderLine)
        newLine.quantity = 2
        newLine.order = managed
        managed.orderLines.add(newLine)

        then:
        context.isModified(managed)
        'orderLines' in context.getModifiedAttributes(managed)

        cleanup:
        dataManager.remove(line, order, customer)
    }

    def "non-fresh merge does not overwrite a dirty scalar"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())
        managed.name = 'edited'

        when:
        context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())

        then:
        managed.name == 'edited'
        context.getModifiedAttributes(managed) == ['name'] as Set

        cleanup:
        dataManager.remove(customer)
    }

    def "fresh merge keeps a dirty scalar but rebaselines it"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())
        managed.name = 'edited'

        when: "the database has moved on and a fresh copy arrives"
        Customer freshCopy = dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one()
        freshCopy.name = 'db-new'
        makeDetached(freshCopy, ['name'])
        context.merge(freshCopy, new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "user's value survives, still dirty (against the new baseline)"
        managed.name == 'edited'
        context.getModifiedAttributes(managed) == ['name'] as Set

        when: "a fresh copy equal to the user's value un-dirties"
        Customer equalCopy = dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one()
        equalCopy.name = 'edited'
        makeDetached(equalCopy, ['name'])
        context.merge(equalCopy, new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then:
        managed.name == 'edited'
        context.getModifiedAttributes(managed).empty

        cleanup:
        dataManager.remove(customer)
    }

    def "non-fresh merge does not overwrite a dirty reference"() {
        given:
        DataContext context = factory.createDataContext()
        Customer c1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer c2 = dataManager.save(new Customer(name: 'c2', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: c1))
        Order managed = context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one())
        Customer managedC2 = context.merge(c2)
        managed.customer = managedC2

        when:
        context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one())

        then:
        managed.customer.is(managedC2)
        'customer' in context.getModifiedAttributes(managed)

        cleanup:
        dataManager.remove(order, c1, c2)
    }

    def "non-fresh merge does not replace a dirty collection"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))
        Order managed = context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one())
        managed.orderLines.remove(0)

        expect:
        'orderLines' in context.getModifiedAttributes(managed)

        when:
        context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one())

        then: "the user's emptied collection is preserved"
        managed.orderLines.empty
        'orderLines' in context.getModifiedAttributes(managed)

        cleanup:
        dataManager.remove(line, order, customer)
    }

    def "incoming graph nodes still enter the context when a dirty reference copy is skipped"() {
        given:
        DataContext context = factory.createDataContext()
        Customer c1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer c2 = dataManager.save(new Customer(name: 'c2', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: c1))
        Order managed = context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one())
        managed.customer = context.merge(c2)

        when: "a copy referencing c1 is merged; the reference copy is skipped but c1 must be tracked"
        context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one())

        then:
        context.find(Customer, c1.id) != null

        cleanup:
        dataManager.remove(order, c1, c2)
    }

    def "child save unions dirty attributes into the parent and child edits win"() {
        given:
        DataContext parent = factory.createDataContext()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Customer customer = dataManager.save(new Customer(name: 'c1', email: 'e1', address: new Address()))
        Customer parentManaged = parent.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'email') }.one())
        parentManaged.email = 'parent-edit'
        Customer childManaged = child.merge(parentManaged)

        when: "child edits name (and the parent had independently edited email)"
        childManaged.name = 'child-edit'
        child.save()

        then: "child's edit landed, parent's own edit survived, both attrs dirty in parent"
        parentManaged.name == 'child-edit'
        parentManaged.email == 'parent-edit'
        parent.getModifiedAttributes(parentManaged) == ['name', 'email'] as Set
        parent.isModified(parentManaged)

        when: "conflicting edit: child also edited email"
        Customer childManaged2 = child.merge(parentManaged)
        childManaged2.email = 'child-email'
        child.save()

        then: "the child's later intent wins"
        parentManaged.email == 'child-email'

        cleanup:
        dataManager.remove(customer)
    }

    def "child save of an attribute unloaded on the parent lands the value and marks it dirty"() {
        given:
        DataContext parent = factory.createDataContext()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Customer customer = dataManager.save(new Customer(name: 'c1', email: 'e1', address: new Address()))
        Customer parentManaged = parent.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())

        when: "child works with a fuller state and edits an attribute the parent never loaded"
        Customer childManaged = child.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'email') }.one())
        childManaged.email = 'child-email'
        child.save()

        then: "the child's value (not null) landed in the parent and the attribute is dirty there"
        parentManaged.email == 'child-email'
        'email' in parent.getModifiedAttributes(parentManaged)

        cleanup:
        dataManager.remove(customer)
    }

    def "child save lands edits of several entities in the parent with per-entity dirty attributes"() {
        given:
        DataContext parent = factory.createDataContext()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))
        Order parentOrder = parent.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one())
        OrderLine parentLine = parentOrder.orderLines[0]

        when: "child edits an Order attribute and a nested OrderLine attribute"
        Order childOrder = child.merge(parentOrder)
        childOrder.number = 'o1-child'
        childOrder.orderLines[0].quantity = 42
        child.save()

        then: "both edits landed and both entities are dirty in the parent on the right attributes"
        parentOrder.number == 'o1-child'
        parentLine.quantity == 42
        'number' in parent.getModifiedAttributes(parentOrder)
        'quantity' in parent.getModifiedAttributes(parentLine)

        cleanup:
        dataManager.remove(line, order, customer)
    }

    def "child edit of an embedded sub-attribute overrides the parent's own conflicting edit"() {
        given:
        DataContext parent = factory.createDataContext()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address(city: 'Rome')))
        Customer parentManaged = parent.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one())
        parentManaged.address.city = 'parent-city'

        when: "child (branched from the parent's state) edits the same embedded sub-attribute"
        Customer childManaged = child.merge(parentManaged)
        childManaged.address.city = 'child-city'
        child.save()

        then: "the child's later intent wins and the path stays dirty in the parent"
        parentManaged.address.city == 'child-city'
        'address.city' in parent.getModifiedAttributes(parentManaged)

        cleanup:
        dataManager.remove(customer)
    }

    def "child reassignment of a reference and mutation of a collection union into the parent"() {
        given:
        DataContext parent = factory.createDataContext()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Customer c1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer c2 = dataManager.save(new Customer(name: 'c2', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: c1))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))
        Order parentOrder = parent.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'customer.name', 'orderLines.quantity') }.one())

        when: "child reassigns the reference and mutates the collection"
        Order childOrder = child.merge(parentOrder)
        childOrder.customer = child.merge(dataManager.load(Id.of(c2)).one())
        childOrder.orderLines.remove(0)
        child.save()

        then: "both landed in the parent and both attributes are dirty there"
        parentOrder.customer.name == 'c2'
        parentOrder.orderLines.empty
        'customer' in parent.getModifiedAttributes(parentOrder)
        'orderLines' in parent.getModifiedAttributes(parentOrder)

        cleanup:
        dataManager.remove(line, order, c1, c2)
    }

    def "child edit propagates through two levels of parent contexts"() {
        given:
        DataContext grand = factory.createDataContext()
        DataContext mid = factory.createDataContext()
        DataContext child = factory.createDataContext()
        mid.setParent(grand)
        child.setParent(mid)
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer grandManaged = grand.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())
        Customer midManaged = mid.merge(grandManaged)
        Customer childManaged = child.merge(midManaged)

        when:
        childManaged.name = 'deep-edit'
        child.save()
        mid.save()

        then:
        grandManaged.name == 'deep-edit'
        'name' in grand.getModifiedAttributes(grandManaged)

        cleanup:
        dataManager.remove(customer)
    }

    def "non-fresh merge does not overwrite a dirty embedded sub-attribute"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address(city: 'Rome')))
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one())
        managed.address.city = 'Pisa'

        when:
        context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one())

        then:
        managed.address.city == 'Pisa'
        context.getModifiedAttributes(managed) == ['address.city'] as Set

        cleanup:
        dataManager.remove(customer)
    }

    def "fresh merge keeps a dirty reference but rebaselines it"() {
        given:
        DataContext context = factory.createDataContext()
        Customer c1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer c2 = dataManager.save(new Customer(name: 'c2', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: c1))
        Order managed = context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one())
        Customer managedC2 = context.merge(c2)
        managed.customer = managedC2

        when: "the database has moved on (still c1) and a fresh copy arrives"
        Order freshCopy = dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one()
        context.merge(freshCopy, new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "user's reference survives, still dirty (against the new baseline, still c1)"
        managed.customer.is(managedC2)
        'customer' in context.getModifiedAttributes(managed)

        when: "the database itself moves to the user's chosen customer and a fresh copy arrives"
        Order dbOrder = dataManager.load(Id.of(order)).one()
        dbOrder.customer = c2
        dbOrder = dataManager.save(dbOrder)
        Order equalCopy = dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one()
        context.merge(equalCopy, new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "the reference un-dirties (ids equal)"
        managed.customer.is(managedC2)
        context.getModifiedAttributes(managed).empty

        cleanup:
        dataManager.remove(dbOrder, c1, c2)
    }

    def "fresh merge keeps a dirty collection's contents but rebaselines its membership"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine l1 = dataManager.save(new OrderLine(quantity: 1, order: order))
        OrderLine l2 = dataManager.save(new OrderLine(quantity: 2, order: order))
        Order managed = context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one())
        def managedL2 = managed.orderLines.find { it.id == l2.id }
        managed.orderLines.remove(managedL2)

        expect:
        'orderLines' in context.getModifiedAttributes(managed)

        when: "the database has moved on (still [l1, l2]) and a fresh copy arrives"
        context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one(),
                new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "user's emptied-of-l2 contents survive, still dirty (membership differs from the new baseline)"
        managed.orderLines*.id == [l1.id]
        'orderLines' in context.getModifiedAttributes(managed)

        when: "the user brings the contents back to match the (rebaselined) incoming membership"
        managed.orderLines.add(managedL2)

        then: "the collection un-dirties"
        managed.orderLines*.id.toSet() == [l1.id, l2.id].toSet()
        context.getModifiedAttributes(managed).empty

        cleanup:
        dataManager.remove(l1, l2, order, customer)
    }

    def "fresh merge keeps a dirty embedded sub-attribute but rebaselines it"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address(city: 'Rome')))
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one())
        managed.address.city = 'Pisa'

        when: "the database has moved on (still 'Rome') and a fresh copy arrives"
        context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one(),
                new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "user's value survives, still dirty (against the new baseline, still 'Rome')"
        managed.address.city == 'Pisa'
        'address.city' in context.getModifiedAttributes(managed)

        when: "a fresh copy equal to the user's value arrives"
        Customer equalCopy = dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one()
        equalCopy.address.city = 'Pisa'
        makeDetached(equalCopy, ['name', 'address'])
        context.merge(equalCopy, new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "the path un-dirties"
        managed.address.city == 'Pisa'
        context.getModifiedAttributes(managed).empty

        cleanup:
        dataManager.remove(customer)
    }

    def "fresh merge of a clean collection rebaselines it to the newly installed membership"() {
        given: "managed order with two clean lines"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine l1 = dataManager.save(new OrderLine(quantity: 1, order: order))
        OrderLine l2 = dataManager.save(new OrderLine(quantity: 2, order: order))
        Order managed = context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one())
        def managedL1 = managed.orderLines.find { it.id == l1.id }
        def managedL2 = managed.orderLines.find { it.id == l2.id }

        expect: "clean right after the initial merge"
        context.getModifiedAttributes(managed).empty

        when: "the DB side drops l2 (no user edit involved) and a fresh copy is merged in"
        dataManager.remove(l2)
        context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one(),
                new io.jmix.flowui.model.MergeOptions().setFresh(true))

        then: "the managed collection reflects the DB and stays clean -- merge legitimately changed a clean collection"
        managed.orderLines*.id == [l1.id]
        context.getModifiedAttributes(managed).empty

        when: "the user removes l1"
        managed.orderLines.remove(managedL1)

        then: "dirty, measured against the fresh post-merge baseline [l1]"
        'orderLines' in context.getModifiedAttributes(managed)

        when: "the user re-adds l1"
        managed.orderLines.add(managedL1)

        then: "clean again: current membership [l1] matches the fresh post-merge baseline, not the stale pre-merge [l1, l2]"
        context.getModifiedAttributes(managed).empty

        when: "the user also adds l2 back (it was dropped from the DB and is not part of the merged baseline)"
        managed.orderLines.add(managedL2)

        then: "dirty -- under a stale [l1, l2] baseline this would be a false clean (N1 pin)"
        'orderLines' in context.getModifiedAttributes(managed)

        cleanup:
        dataManager.remove(order, customer)
    }
}
