package data_components

import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.Metadata
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.impl.DataContextChangeTracker
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.TestNullableIdEntity
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.DataContextSpec

class DataContextChangeTrackerTest extends DataContextSpec {

    @Autowired
    Metadata metadata

    @Autowired
    DataManager dataManager

    @Autowired
    DataComponents factory

    def dirty = [], clean = []
    def tracker = new DataContextChangeTracker({ e -> dirty << e }, { e -> clean << e })

    def "first change creates baseline and dirties the entity"() {
        given:
        Order order = metadata.create(Order)

        when:
        tracker.trackChange(order, 'number', 'o1', 'x1', false)

        then:
        tracker.isAttributeDirty(order, 'number')
        tracker.getModifiedAttributes(order) == ['number'] as Set
        dirty == [order]

        when: "changing again does not move the baseline"
        tracker.trackChange(order, 'number', 'x1', 'x2', false)

        then:
        tracker.isAttributeDirty(order, 'number')
        dirty == [order]
    }

    def "reverting to the baseline un-dirties attribute and entity"() {
        given:
        Order order = metadata.create(Order)
        tracker.trackChange(order, 'number', 'o1', 'x1', false)

        when:
        tracker.trackChange(order, 'number', 'x1', 'o1', false)

        then:
        !tracker.isAttributeDirty(order, 'number')
        tracker.getModifiedAttributes(order).empty
        clean == [order]
    }

    def "references are compared by id"() {
        given:
        Order order = metadata.create(Order)
        Customer c1 = metadata.create(Customer)
        Customer c1copy = metadata.create(Customer)
        c1copy.id = c1.id

        when: "assigning a different instance with the same id counts as revert"
        tracker.trackChange(order, 'customer', c1, c1copy, true)

        then:
        !tracker.isAttributeDirty(order, 'customer')
    }

    def "collection dirty follows membership, not order or instance"() {
        given:
        Order order = metadata.create(Order)
        def l1 = metadata.create(OrderLine)
        def l2 = metadata.create(OrderLine)
        tracker.snapshotCollectionBaseline(order, 'orderLines', [l1, l2])

        when: "same membership in different order is clean"
        tracker.trackCollectionChange(order, 'orderLines', [l2, l1])

        then:
        !tracker.isAttributeDirty(order, 'orderLines')

        when: "removing an element dirties"
        tracker.trackCollectionChange(order, 'orderLines', [l2])

        then:
        tracker.isAttributeDirty(order, 'orderLines')

        when: "re-adding it un-dirties"
        tracker.trackCollectionChange(order, 'orderLines', [l1, l2])

        then:
        !tracker.isAttributeDirty(order, 'orderLines')
    }

    def "collection rebaseline keeps the new baseline usable after un-dirtying"() {
        given:
        Order order = metadata.create(Order)
        def l1 = metadata.create(OrderLine)
        def l2 = metadata.create(OrderLine)
        tracker.snapshotCollectionBaseline(order, 'orderLines', [l1])

        when: "adding an element dirties"
        tracker.trackCollectionChange(order, 'orderLines', [l1, l2])

        then:
        tracker.isAttributeDirty(order, 'orderLines')

        when: "rebaseline with incoming equal to current un-dirties"
        tracker.rebaselineCollection(order, 'orderLines', [l1, l2], [l1, l2])

        then:
        !tracker.isAttributeDirty(order, 'orderLines')

        when: "mutating again dirties against the new baseline"
        tracker.trackCollectionChange(order, 'orderLines', [l1])

        then:
        tracker.isAttributeDirty(order, 'orderLines')

        when: "reverting to the new baseline un-dirties, no coarse-dirty fallback"
        tracker.trackCollectionChange(order, 'orderLines', [l1, l2])

        then:
        !tracker.isAttributeDirty(order, 'orderLines')
    }

    def "fresh rebaseline keeps dirty when values differ and un-dirties when equal"() {
        given:
        Order order = metadata.create(Order)
        tracker.trackChange(order, 'number', 'o1', 'edited', false)

        when: "fresh value differs from the user's value"
        tracker.rebaseline(order, 'number', 'db-new', 'edited', false)

        then:
        tracker.isAttributeDirty(order, 'number')

        when: "fresh value equals the user's value"
        tracker.rebaseline(order, 'number', 'edited', 'edited', false)

        then:
        !tracker.isAttributeDirty(order, 'number')
    }

    def "drop and clear remove state"() {
        given:
        Order order = metadata.create(Order)
        tracker.trackChange(order, 'number', 'a', 'b', false)

        when:
        tracker.drop(order)

        then:
        tracker.getModifiedAttributes(order).empty
        clean == [order]

        when: "clear() wipes all tracked state without firing the clean callback"
        tracker.trackChange(order, 'number', 'a', 'b', false)

        and:
        tracker.clear()

        then:
        tracker.getModifiedAttributes(order).empty
        clean == [order]
    }

    def "baseline does not move while the attribute stays dirty"() {
        given:
        Order order = metadata.create(Order)

        when: "first change establishes the baseline"
        tracker.trackChange(order, 'number', 'o1', 'x1', false)

        and: "a further change moves the current value but not the baseline"
        tracker.trackChange(order, 'number', 'x1', 'x2', false)

        and: "moving back to the first edited value is still not the original baseline"
        tracker.trackChange(order, 'number', 'x2', 'x1', false)

        then:
        tracker.isAttributeDirty(order, 'number')

        when: "only returning to the original baseline un-dirties"
        tracker.trackChange(order, 'number', 'x1', 'o1', false)

        then:
        !tracker.isAttributeDirty(order, 'number')
    }

    def "getModifiedAttributes reflects user edits through the context"() {
        given:
        def context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(customer)

        expect:
        context.getModifiedAttributes(managed).empty

        when:
        managed.name = 'c2'

        then:
        context.getModifiedAttributes(managed) == ['name'] as Set
        context.isModified(managed)

        when: "reverting un-dirties entity-level state too"
        managed.name = 'c1'

        then:
        context.getModifiedAttributes(managed).empty
        !context.isModified(managed)

        cleanup:
        dataManager.remove(customer)
    }

    def "setModified manual flag survives tracker transitions"() {
        given:
        def context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(customer)
        context.setModified(managed, true)

        when: "an edit-and-revert cycle happens"
        managed.name = 'x'
        managed.name = 'c1'

        then: "the manual flag keeps the entity modified"
        context.isModified(managed)
        context.getModifiedAttributes(managed).empty

        when:
        context.setModified(managed, false)

        then:
        !context.isModified(managed)

        cleanup:
        dataManager.remove(customer)
    }

    def "embedded sub-attribute changes are tracked under dotted paths"() {
        given:
        def context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1',
                address: new Address(city: 'Rome')))
        Customer managed = context.merge(
                dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name', 'address') }.one())

        when:
        managed.address.city = 'Pisa'

        then:
        context.getModifiedAttributes(managed) == ['address.city'] as Set

        when:
        managed.address.city = 'Rome'

        then:
        context.getModifiedAttributes(managed).empty

        cleanup:
        dataManager.remove(customer)
    }

    def "lifecycle drops tracker state"() {
        given:
        def context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(customer)
        managed.name = 'x'

        when:
        context.evict(managed)

        then:
        context.getModifiedAttributes(managed).empty

        when: "re-merged instance starts clean"
        Customer again = context.merge(customer)

        then:
        context.getModifiedAttributes(again).empty
        !context.isModified(again)

        cleanup:
        dataManager.remove(customer)
    }

    def "primary key changes are not tracked as attribute modifications"() {
        given:
        def context = factory.createDataContext()
        def entity = context.merge(metadata.create(TestNullableIdEntity))

        when: "id is assigned like the saveDelegate DTO pattern does"
        entity.id = 10L

        then:
        !context.getModifiedAttributes(entity).contains('id')

        when: "a normal attribute edit still tracks"
        entity.name = 'n1'

        then:
        context.getModifiedAttributes(entity) == ['name'] as Set
    }

    def "successful save clears attribute state"() {
        given:
        def context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer managed = context.merge(customer)
        managed.name = 'c2'

        when:
        context.save()

        then:
        context.getModifiedAttributes(managed).empty
        !context.hasChanges()

        cleanup:
        deleteRecord(managed)
    }

    def "collection mutation dirties the attribute and revert un-dirties it"() {
        given:
        def context = factory.createDataContext()
        Customer customer = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Order order = dataManager.save(new Order(number: 'o1', customer: customer))
        def line = dataManager.save(new OrderLine(quantity: 1, order: order))

        Order managed = context.merge(dataManager.load(Id.of(order))
                .fetchPlan { it.addAll('number', 'orderLines.quantity') }.one())
        def managedLine = managed.orderLines[0]

        when:
        managed.orderLines.remove(managedLine)

        then:
        context.getModifiedAttributes(managed) == ['orderLines'] as Set
        context.isModified(managed)

        when: "re-adding the same element restores the baseline membership"
        managed.orderLines.add(managedLine)

        then:
        context.getModifiedAttributes(managed).empty
        !context.isModified(managed)

        cleanup:
        dataManager.remove(line, order, customer)
    }
}
