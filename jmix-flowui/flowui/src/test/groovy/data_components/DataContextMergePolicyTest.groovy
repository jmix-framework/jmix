package data_components

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.Id
import io.jmix.core.FetchPlans
import io.jmix.core.entity.EntityPropertyChangeEvent
import io.jmix.core.entity.EntityPropertyChangeListener
import io.jmix.core.entity.EntitySystemAccess
import io.jmix.core.entity.EntityValues
import test_support.entity.sales.OrderLineParam
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import io.jmix.flowui.model.MergeOptions
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.Status
import test_support.entity.sec.Role
import test_support.entity.sec.User
import test_support.entity.sec.UserRole
import test_support.spec.DataContextSpec

class DataContextMergePolicyTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    EntityStates entityStates
    @Autowired
    DataManager dataManager
    @Autowired
    FetchPlans fetchPlans

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "mutating a transplanted collection marks the owner modified"() {
        given: "managed order without orderLines; a copy carrying them is merged over it"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))

        Order managed = context.merge(dataManager.load(Id.of(order)).fetchPlan { it.add('number') }.one())
        context.merge(dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'orderLines.quantity') }.one(),
                new MergeOptions().setFresh(true))
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
        context.merge(freshCopy, new MergeOptions().setFresh(true))

        then: "user's value survives, still dirty (against the new baseline)"
        managed.name == 'edited'
        context.getModifiedAttributes(managed) == ['name'] as Set

        when: "a fresh copy equal to the user's value un-dirties"
        Customer equalCopy = dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one()
        equalCopy.name = 'edited'
        makeDetached(equalCopy, ['name'])
        context.merge(equalCopy, new MergeOptions().setFresh(true))

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
        context.merge(freshCopy, new MergeOptions().setFresh(true))

        then: "user's reference survives, still dirty (against the new baseline, still c1)"
        managed.customer.is(managedC2)
        'customer' in context.getModifiedAttributes(managed)

        when: "the database itself moves to the user's chosen customer and a fresh copy arrives"
        Order dbOrder = dataManager.load(Id.of(order)).one()
        dbOrder.customer = c2
        dbOrder = dataManager.save(dbOrder)
        Order equalCopy = dataManager.load(Id.of(order)).fetchPlan { it.addAll('number', 'customer.name') }.one()
        context.merge(equalCopy, new MergeOptions().setFresh(true))

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
                new MergeOptions().setFresh(true))

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
                new MergeOptions().setFresh(true))

        then: "user's value survives, still dirty (against the new baseline, still 'Rome')"
        managed.address.city == 'Pisa'
        'address.city' in context.getModifiedAttributes(managed)

        when: "a fresh copy equal to the user's value arrives"
        Customer equalCopy = dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one()
        equalCopy.address.city = 'Pisa'
        makeDetached(equalCopy, ['name', 'address'])
        context.merge(equalCopy, new MergeOptions().setFresh(true))

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
                new MergeOptions().setFresh(true))

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

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "setting a value on an unfetched reference marks it loaded"() {
        given: "a managed order line whose 'order' reference was not fetched"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order1 = dataManager.save(new Order(number: 'o1', customer: customer))
        Order order2 = dataManager.save(new Order(number: 'o2', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order1))

        Order managedOrder2 = context.merge(dataManager.load(Id.of(order2)).fetchPlan { it.add('number') }.one())
        OrderLine managedLine = context.merge(dataManager.load(Id.of(line)).fetchPlan { it.add('quantity') }.one())

        expect: "'order' is not loaded on the managed line"
        !entityStates.isLoaded(managedLine, 'order')

        when: "the user sets the reference to a DIFFERENT order through the managed instance"
        // set to order2 (not the line's existing order1): the enhanced setter reads the old value to build
        // the change event, which lazily materializes order1; a same-id set would not be a change, so the
        // new value must differ for the edit to be both loaded and dirty
        managedLine.order = managedOrder2

        then: "the attribute is now reported loaded and tracked dirty"
        entityStates.isLoaded(managedLine, 'order')
        'order' in context.getModifiedAttributes(managedLine)

        cleanup:
        dataManager.remove(line, order1, order2, customer)
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "child save reaching a line non-root marks its deeper collection loaded on the parent"() {
        given: "a parent holding an order whose lines are loaded WITHOUT their 'params' collection (#4906)"
        DataContext parent = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))
        OrderLineParam param = dataManager.save(new OrderLineParam(name: 'p1', value: 'v1', orderLine: line))

        def shallowPlan = fetchPlans.builder(Order)
                .addFetchPlan('_local')
                .add('customer', '_local')
                .add('orderLines', '_local')
                .build()
        Order parentOrder = parent.merge(dataManager.load(Id.of(order)).fetchPlan(shallowPlan).one())
        OrderLine parentLine = parentOrder.orderLines[0]

        def lineEditorPlan = fetchPlans.builder(OrderLine)
                .addFetchPlan('_local')
                .add('params', '_local')
                .build()

        expect: "the line editor's fetch plan is not satisfied on the parent line - 'params' is unloaded"
        !entityStates.isLoaded(parentLine, 'params')
        !entityStates.isLoadedWithFetchPlan(parentLine, lineEditorPlan)

        when: "a child loads the order with a fuller nested plan (lines WITH params), edits the order, and saves back"
        // editing the order (not the line) makes the ORDER the merge root, so each line is reached NON-ROOT
        // on the child->parent merge - that is the path (mergeUnloadedOrNullReference) where the fix must mark
        // the installed collection loaded. Editing the line itself would merge the line as root, where the
        // root mergeLoadedPropertiesInfo copy (from the #5445 fix) already carries loaded-state and hides the gap.
        def fullerPlan = fetchPlans.builder(Order)
                .addFetchPlan('_local')
                .add('customer', '_local')
                .add('orderLines', { builder -> builder.addFetchPlan('_local').add('params', '_local') })
                .build()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Order childOrder = child.merge(dataManager.load(Id.of(order)).fetchPlan(fullerPlan).one())
        childOrder.number = 'o2'
        child.save()

        then: "the deeper 'params' attribute is now loaded on the (non-root) parent line, so the reopen gate passes"
        entityStates.isLoaded(parentLine, 'params')
        entityStates.isLoadedWithFetchPlan(parentLine, lineEditorPlan)

        cleanup:
        dataManager.remove(param, line, order, customer)
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "reopening a line editor after a deep nested save keeps the edit instead of losing it to a stale reload (#4907)"() {
        given: "a parent holding an order whose lines are loaded WITHOUT their 'params' collection"
        DataContext parent = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))
        OrderLineParam param = dataManager.save(new OrderLineParam(name: 'p1', value: 'v1', orderLine: line))

        def shallowPlan = fetchPlans.builder(Order)
                .addFetchPlan('_local')
                .add('customer', '_local')
                .add('orderLines', '_local')
                .build()
        Order parentOrder = parent.merge(dataManager.load(Id.of(order)).fetchPlan(shallowPlan).one())
        OrderLine parentLine = parentOrder.orderLines[0]

        def lineEditorPlan = fetchPlans.builder(OrderLine)
                .addFetchPlan('_local')
                .add('params', '_local')
                .build()

        when: "a child (the line editor) loads the deeper 3-level graph, edits both the order and the nested param, and saves back"
        def deepPlan = fetchPlans.builder(Order)
                .addFetchPlan('_local')
                .add('customer', '_local')
                .add('orderLines', { b -> b.addFetchPlan('_local').add('params', '_local') })
                .build()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Order childOrder = child.merge(dataManager.load(Id.of(order)).fetchPlan(deepPlan).one())
        childOrder.number = 'o2'
        childOrder.orderLines[0].params[0].value = 'v2'
        child.save()

        then: "the reopen gate is now satisfied on the parent's line"
        entityStates.isLoadedWithFetchPlan(parentLine, lineEditorPlan)

        when: "a second child 'reopens' the line editor - since the gate passed, it merges the parent's in-memory " +
                "line directly instead of reloading a stale copy from the database"
        DataContext reopenedChild = factory.createDataContext()
        reopenedChild.setParent(parent)
        OrderLine reopenedLine = reopenedChild.merge(parentLine)

        then: "the nested edit survives the reopen"
        reopenedLine.params[0].value == 'v2'

        cleanup:
        dataManager.remove(param, line, order, customer)
    }

    def "a deep composition edit marks the intermediate owner modified in the parent (#4907 half ii)"() {
        given: "a parent context holding an order whose lines are loaded WITHOUT their params"
        DataContext parent = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order))
        OrderLineParam param = dataManager.save(new OrderLineParam(name: 'p1', value: 'v1', orderLine: line))

        def shallowPlan = fetchPlans.builder(Order)
                .addFetchPlan('_local')
                .add('customer', '_local')
                .add('orderLines', '_local')
                .build()
        Order parentOrder = parent.merge(dataManager.load(Id.of(order)).fetchPlan(shallowPlan).one())
        OrderLine parentLine = parentOrder.orderLines[0]

        when: "a child edits ONLY the nested param (not the line, not the order) and saves back"
        def deepPlan = fetchPlans.builder(Order)
                .addFetchPlan('_local')
                .add('customer', '_local')
                .add('orderLines', { b -> b
                        .addFetchPlan('_local')
                        .add('order', '_local')
                        .add('params', { p -> p.addFetchPlan('_local').add('orderLine', '_local') }) })
                .build()
        DataContext child = factory.createDataContext()
        child.setParent(parent)
        Order childOrder = child.merge(dataManager.load(Id.of(order)).fetchPlan(deepPlan).one())
        childOrder.orderLines[0].params[0].value = 'v2'
        child.save()

        then: "the intermediate owner (the line) is marked modified in the parent, and so is the root order"
        parent.isModified(parentLine)
        parent.isModified(parentOrder)

        cleanup:
        dataManager.remove(param, line, order, customer)
    }

    def "merge bidirectional graph with two java instances of same id does not duplicate collection (#5331)"() {
        given: "two java instances of the same user id, joined by a bidirectional user <-> userRoles graph"
        DataContext context = factory.createDataContext()
        User user1 = new User(login: 'u1', name: 'User 1')
        User user1Dup = new User(id: user1.id, login: 'u1', name: 'User 1')

        Role role1 = new Role(name: 'Role 1')
        Role role2 = new Role(name: 'Role 2')

        UserRole ur1 = new UserRole(user: user1Dup, role: role1)
        UserRole ur2 = new UserRole(user: user1Dup, role: role2)

        user1.userRoles = [ur1, ur2]
        user1Dup.userRoles = [ur1, ur2]

        when: "a root UserRole pulls user1 in as a NON-ROOT reference, so the owner collection merges with replace=false"
        Role rootRole = new Role(name: 'Root Role')
        UserRole rootUr = new UserRole(user: user1, role: rootRole)

        UserRole mergedRoot = context.merge(rootUr)
        User mergedUser = mergedRoot.user

        then: "the re-entrant merge does not duplicate the owner collection"
        mergedUser.userRoles.size() == 2
        mergedUser.userRoles.unique().size() == 2
    }

    def "merge of a bidirectional graph with two java instances of the same id does not duplicate the owner collection"() {
        given: "two java instances of the same user id, with a bidirectional user <-> userRoles graph (#5331)"
        DataContext context = factory.createDataContext()
        User user1 = new User(login: 'u1', name: 'User 1')
        User user1Dup = new User(id: user1.id, login: 'u1', name: 'User 1')

        Role role1 = new Role(name: 'Role 1')
        Role role2 = new Role(name: 'Role 2')

        UserRole ur1 = new UserRole(user: user1Dup, role: role1)
        UserRole ur2 = new UserRole(user: user1Dup, role: role2)

        user1.userRoles = [ur1, ur2]
        user1Dup.userRoles = [ur1, ur2]

        when: "a root UserRole pulls user1 in as a non-root reference, so the owner collection merges with replace=false into an empty list"
        Role rootRole = new Role(name: 'Root Role')
        UserRole rootUr = new UserRole(user: user1, role: rootRole)

        UserRole mergedRoot = context.merge(rootUr)
        User mergedUser = mergedRoot.user

        then: "the re-entrant merge does not duplicate the owner collection"
        mergedUser.userRoles.size() == 2
        mergedUser.userRoles.unique().size() == 2
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "a set reference stays loaded after a fresh merge of a narrower copy"() {
        given: "a managed order line whose 'order' reference was not fetched, then set by the user"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order1 = dataManager.save(new Order(number: 'o1', customer: customer))
        Order order2 = dataManager.save(new Order(number: 'o2', customer: customer))
        OrderLine line = dataManager.save(new OrderLine(quantity: 1, order: order1))

        Order managedOrder2 = context.merge(dataManager.load(Id.of(order2)).fetchPlan { it.add('number') }.one())
        OrderLine managedLine = context.merge(dataManager.load(Id.of(line)).fetchPlan { it.add('quantity') }.one())
        // set 'order' to a DIFFERENT managed order so the change fires and marks 'order' loaded (increment 04);
        // a same-id set would be a no-op change and would neither track dirty nor mark loaded
        managedLine.order = managedOrder2

        expect: "the set makes 'order' loaded"
        entityStates.isLoaded(managedLine, 'order')

        when: "a FRESH merge brings a narrower copy of the same line (no 'order' in its fetch plan)"
        // fresh + root -> mergeLoadedPropertiesInfo copies the narrow source's loaded-info wholesale onto the
        // managed line, reverting the set-loaded flag unless it is re-applied from the marker registry
        context.merge(dataManager.load(Id.of(line)).fetchPlan { it.add('quantity') }.one(),
                new MergeOptions().setFresh(true))

        then: "'order' stays loaded and keeps the user's value"
        entityStates.isLoaded(managedLine, 'order')
        managedLine.order == managedOrder2

        cleanup:
        dataManager.remove(line, order1, order2, customer)
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "a fetched reference stays loaded after a fresh merge of a narrower copy"() {
        given: "an order merged with 'customer' fetched wide"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order1 = dataManager.save(new Order(number: 'o1', customer: customer))

        Order managedWide = context.merge(
                dataManager.load(Id.of(order1)).fetchPlan { it.addAll('number', 'customer.name') }.one())

        expect: "'customer' is loaded and present"
        entityStates.isLoaded(managedWide, 'customer')
        managedWide.customer != null

        when: "a FRESH merge brings a narrower copy of the same order (no 'customer' in its fetch plan)"
        // fresh + pre-existing managed instance: mergeLoadedPropertiesInfo copies the narrow source's loaded-info
        // wholesale onto the managed order, and its source-relative negative for 'customer' shadows the unioned
        // fetch group, reverting the flag - unless the cold-reset recompute is generalized to the fresh path
        context.merge(dataManager.load(Id.of(order1)).fetchPlan { it.add('number') }.one(),
                new MergeOptions().setFresh(true))

        then: "'customer' stays loaded and keeps its value"
        entityStates.isLoaded(managedWide, 'customer')
        managedWide.customer != null
        managedWide.customer.name == 'c1'

        cleanup:
        dataManager.remove(order1, customer)
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "a fresh narrower merge then save does not null out a fetched reference"() {
        given: "an order merged with 'customer' fetched wide"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order1 = dataManager.save(new Order(number: 'o1', customer: customer))

        Order managed = context.merge(
                dataManager.load(Id.of(order1)).fetchPlan { it.addAll('number', 'customer.name') }.one())

        when: "a FRESH narrower merge (no 'customer'), then a datatype edit and a save"
        context.merge(dataManager.load(Id.of(order1)).fetchPlan { it.add('number') }.one(),
                new MergeOptions().setFresh(true))
        managed.number = 'o1-edited'
        context.save()

        then: "the DB round-trip proves the customer FK survived (not nulled by the save)"
        def reloaded = dataManager.load(Id.of(order1)).fetchPlan { it.addAll('number', 'customer.name') }.one()
        reloaded.number == 'o1-edited'
        reloaded.customer != null
        reloaded.customer.name == 'c1'

        cleanup:
        // remove the post-save reference: context.save() bumped order1's DB version, so the pre-save
        // 'order1' object is stale and would fail the optimistic-lock check on delete
        dataManager.remove(reloaded, customer)
    }

    def "a merge that changes a managed instance scalar fires a ChangeEvent (#4071)"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())

        and: "a change listener recording the entities it is notified about"
        def events = []
        context.addChangeListener { events << it.entity }

        when: "a fresh merge brings a changed value for the same managed instance"
        def reloaded = dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one()
        reloaded.name = 'changed'
        context.merge(reloaded, new MergeOptions().setFresh(true))

        then: "exactly one ChangeEvent fires, for the managed instance"
        managed.name == 'changed'
        events == [managed]

        cleanup:
        dataManager.remove(customer)
    }

    def "a plain merge of unchanged data fires no ChangeEvent; a new instance fires once after the merge"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())

        def events = []
        context.addChangeListener { events << it.entity }

        when: "the same unchanged data is merged again"
        context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one())

        then: "nothing fires (no value actually changed)"
        events.isEmpty()

        when: "a brand-new instance is merged"
        def created = context.merge(new Customer(name: 'c2', address: new Address()))

        then: "exactly one ChangeEvent fires, for the new instance"
        events == [created]

        cleanup:
        dataManager.remove(customer)
    }

    def "an edit made by a listener during a merge is tracked without setModified (#1258)"() {
        given:
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', status: Status.OK, address: new Address()))
        // 'status' is fetched here so the managed instance can be read/set directly (detached entities
        // cannot lazily fetch a plain scalar); the fresh merge below deliberately omits it from ITS fetch
        // plan, which is what keeps the merge from ever writing it (see the 'when' block).
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'status') }.one())

        and: "a listener on the managed instance that, when name changes, also edits status"
        EntitySystemAccess.addPropertyChangeListener(managed, new EntityPropertyChangeListener() {
            @Override
            void propertyChanged(EntityPropertyChangeEvent e) {
                if (e.property == 'name') {
                    managed.status = Status.NOT_OK
                }
            }
        })

        when: "a fresh merge changes name, firing the listener mid-merge"
        def reloaded = dataManager.load(Id.of(customer)).fetchPlan { it.add('name') }.one()
        reloaded.name = 'changed'
        context.merge(reloaded, new MergeOptions().setFresh(true))

        then: "the listener's status edit is tracked as a modification, with no manual setModified"
        managed.status == Status.NOT_OK
        context.isModified(managed)
        'status' in context.getModifiedAttributes(managed)

        cleanup:
        dataManager.remove(customer)
    }

    def "a listener-injected edit is tracked even after an earlier merge's flush listener threw"() {
        given: "a managed instance with name and email loaded"
        DataContext context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', email: 'e1@x.com', address: new Address()))
        Customer managed = context.merge(dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'email') }.one())

        and: "a change listener that throws on its first invocation only, then goes inert"
        boolean thrown = false
        context.addChangeListener {
            if (!thrown) {
                thrown = true
                throw new RuntimeException('boom')
            }
        }

        when: "a fresh merge changes name; its post-merge flush fires the throwing listener"
        def reloaded1 = dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'email') }.one()
        reloaded1.name = 'changed1'
        try {
            context.merge(reloaded1, new MergeOptions().setFresh(true))
        } catch (RuntimeException ignored) {
            // expected: the flush listener throws; this must not stop mergeApplied.clear() from
            // having already run for this merge (see the fix ordering in DataContextImpl)
        }

        then: "the merge itself did apply, regardless of the listener exception"
        managed.name == 'changed1'

        and: "a listener on the managed instance that, when email changes, also edits name (listener-injected edit)"
        EntitySystemAccess.addPropertyChangeListener(managed, new EntityPropertyChangeListener() {
            @Override
            void propertyChanged(EntityPropertyChangeEvent e) {
                if (e.property == 'email') {
                    managed.name = 'injected'
                }
            }
        })

        when: "a second fresh merge changes email only; name is NOT in this merge's fetch plan, so the merge itself never writes it"
        def reloaded2 = dataManager.load(Id.of(customer)).fetchPlan { it.add('email') }.one()
        reloaded2.email = 'changed2@x.com'
        context.merge(reloaded2, new MergeOptions().setFresh(true))

        then: "the injected name edit is tracked as a modification - proof that mergeApplied from the" +
                " earlier (throwing) merge was cleared and did not leak into this merge"
        managed.name == 'injected'
        context.isModified(managed)
        'name' in context.getModifiedAttributes(managed)

        cleanup:
        dataManager.remove(customer)
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "root merge of a wider copy merges a transplanted to-one reference into the context (#418)"() {
        DataContext context = factory.createDataContext()

        given: "an order merged with customer unloaded, then a wider copy carrying customer"
        Customer customer1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order1 = dataManager.save(new Order(number: '1', customer: customer1))

        Order managed = context.merge(
                dataManager.load(Id.of(order1)).fetchPlan { it.add('number') }.one())

        expect:
        !entityStates.isLoaded(managed, 'customer')

        when:
        context.merge(dataManager.load(Id.of(order1))
                .fetchPlan { it.addAll('number', 'customer.name') }.one())

        then: "the reached customer is THE managed instance of its id, not a transplanted copy"
        entityStates.isLoaded(managed, 'customer')
        managed.customer != null
        context.find(Customer, customer1.id).is(managed.customer)

        cleanup:
        dataManager.remove(order1, customer1)
    }

    @IgnoreIf({Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING"))})
    def "non-root graph merge merges transplanted collection elements into the context"() {
        DataContext context = factory.createDataContext()

        given: "an order managed with orderLines unloaded"
        Customer customer1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order1 = dataManager.save(new Order(number: '1', customer: customer1))
        OrderLine line1 = dataManager.save(new OrderLine(quantity: 1, order: order1))

        Order managed = context.merge(
                dataManager.load(Id.of(order1)).fetchPlan { it.add('number') }.one())

        expect:
        !entityStates.isLoaded(managed, 'orderLines')

        when: "a line carrying order.orderLines is merged — order is reached non-root"
        def loadedLine = dataManager.load(Id.of(line1))
                .fetchPlan { it.addAll('quantity', 'order.number', 'order.orderLines.quantity') }.one()
        context.merge(loadedLine)

        then: "each element of the transplanted collection is THE managed instance of its id"
        entityStates.isLoaded(managed, 'orderLines')
        managed.orderLines.size() == 1
        managed.orderLines.every { context.find(OrderLine, EntityValues.getId(it)).is(it) }

        cleanup:
        dataManager.remove(line1, order1, customer1)
    }
}
