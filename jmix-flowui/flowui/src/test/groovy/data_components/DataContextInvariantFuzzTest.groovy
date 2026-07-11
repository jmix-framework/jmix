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
import io.jmix.core.EntityStates
import io.jmix.core.Id
import io.jmix.core.entity.EntityValues
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import io.jmix.flowui.model.MergeOptions
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.DataContextSpec

/**
 * Measurement harness, not a regression suite: generates random scenarios of
 * loads-with-random-fetch-plans, merges (fresh and stale) and user edits against a DataContext,
 * and counts violations of proposed merge invariants instead of failing on the first one.
 *
 * Invariants checked after every operation:
 *  I1  edits-preserved      — a user edit on a managed instance (attribute or reference) survives
 *                             subsequent merges (violations split by the op that clobbered it)
 *  I2  loaded-not-regressed — an attribute loaded on a managed instance never becomes unloaded
 *  I3  no-collection-dups   — to-many collections contain no duplicates (by id or instance)
 *  I4  identity-map         — every reachable reference is THE managed instance of its id
 *  I5  dirty-not-missed     — a user-edited entity is present in getModified()
 *  I6  dirty-not-phantom    — a never-edited entity is not in getModified()
 *  I7  membership-preserved — a line added to an order's collection is still there
 *  EX  exception            — an operation threw
 *
 * The run always passes; it prints a violation report. Reproduce a scenario by its printed seed.
 * <p>
 * Slow test: excluded from the regular suite, run with {@code -PincludeSlowTests=true}.
 */
@IgnoreIf({ env["slowTests"] != 'true' })
@IgnoreIf({ Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING")) })
class DataContextInvariantFuzzTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    EntityStates entityStates
    @Autowired
    DataManager dataManager

    static final int SCENARIOS = Integer.getInteger('fuzz.scenarios', 200)
    static final int OPS_PER_SCENARIO = 10
    static final long BASE_SEED = Long.getLong('fuzz.baseSeed', 20260710L)
    static final boolean TRACE = Boolean.getBoolean('fuzz.trace')

    static final List<List<String>> ORDER_PLANS = [
            ['number'],
            ['number', 'description'],
            ['number', 'customer.name'],
            ['number', 'orderLines.quantity'],
            ['number', 'description', 'customer.name', 'orderLines.quantity', 'orderLines.description'],
    ]
    static final List<List<String>> LINE_PLANS = [
            ['quantity'],
            ['quantity', 'description'],
            ['quantity', 'order.number'],
            ['quantity', 'description', 'order.number', 'order.customer.name'],
    ]

    // ---- violation bookkeeping ----------------------------------------------------------------

    Map<String, Integer> violationCounts = [:].withDefault { 0 }
    Map<String, Set<Long>> violationSeeds = [:].withDefault { new LinkedHashSet<Long>() }
    List<String> samples = []

    void violation(String invariant, String opType, long seed, int opIndex, String detail) {
        String key = "$invariant|$opType"
        violationCounts[key] = violationCounts[key] + 1
        violationSeeds[invariant] << seed
        if (samples.size() < 40) {
            samples << String.format('%-24s op=%-22s seed=%d opIndex=%d  %s', invariant, opType, seed, opIndex, detail)
        }
    }

    // ---- oracle state per scenario ------------------------------------------------------------

    static class Oracle {
        // key -> attr -> expected value (ids for references)
        Map<String, Map<String, Object>> userEdits = [:].withDefault { [:] }
        // orderKey -> set of line ids expected in orderLines
        Map<String, Set<Object>> membership = [:].withDefault { new HashSet<>() }
        // keys of entities the user edited (must be dirty)
        Set<String> editedKeys = new HashSet<>()
        // phantom-dirty entities already reported (count once)
        Set<String> phantomSeen = new HashSet<>()
        // dedup for I2/I4: same broken fact reported once per scenario
        Set<String> reportedFacts = new HashSet<>()
    }

    static String key(Object entity) {
        entity.getClass().simpleName + '-' + EntityValues.getId(entity)
    }

    // ---- scenario ------------------------------------------------------------------------------

    def "merge invariant fuzz"() {
        given:
        int scenariosRun = 0

        when:
        for (int s = 0; s < SCENARIOS; s++) {
            long seed = BASE_SEED + s
            try {
                runScenario(seed)
            } catch (Throwable t) {
                violation('EX-scenario-abort', 'setup', seed, -1, t.class.simpleName + ': ' + String.valueOf(t.message).take(500))
            }
            scenariosRun++
        }
        printReport(scenariosRun)

        then:
        scenariosRun == SCENARIOS
    }

    void runScenario(long seed) {
        Random rnd = new Random(seed)

        // persisted baseline: 2 customers, 2 orders, 3 lines
        Customer c1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        Customer c2 = dataManager.save(new Customer(name: 'c2', address: new Address()))
        Order o1 = dataManager.save(new Order(number: 'o1', description: 'd1', customer: c1))
        Order o2 = dataManager.save(new Order(number: 'o2', description: 'd2', customer: c2))
        List<OrderLine> lines = []
        3.times { i ->
            lines << dataManager.save(new OrderLine(quantity: i + 1, description: "l$i",
                    order: (i < 2 ? o1 : o2)))
        }
        List<Order> orders = [o1, o2]
        List<Customer> customers = [c1, c2]
        // scenario-creation order of ids, used as a JVM-run-stable sort key for pickManaged
        List<Object> creationOrder = ([c1, c2, o1, o2] + lines).collect { EntityValues.getId(it) }

        DataContext context = factory.createDataContext()
        Oracle oracle = new Oracle()

        for (int op = 0; op < OPS_PER_SCENARIO; op++) {
            String opType = 'none'
            Map<String, Set<String>> loadedBefore = snapshotLoaded(context)
            try {
                switch (rnd.nextInt(6)) {
                    case 0:
                        opType = 'load-order-fresh'
                        def order = orders[rnd.nextInt(orders.size())]
                        def plan = ORDER_PLANS[rnd.nextInt(ORDER_PLANS.size())]
                        def loaded = dataManager.load(Id.of(order)).fetchPlan { it.addAll(plan as String[]) }.one()
                        def managed = context.merge(loaded, new MergeOptions().setFresh(true))
                        // by current design a fresh merge resets incoming loaded attrs to DB state:
                        // drop oracle expectations for attrs the plan covered, so I1 counts only
                        // non-fresh clobbering (the bug class) — fresh clobbering is design-intended
                        forgetEditsCoveredByPlan(oracle, managed, plan)
                        break
                    case 1:
                        opType = 'merge-stale-order'
                        def order = orders[rnd.nextInt(orders.size())]
                        def plan = ORDER_PLANS[rnd.nextInt(ORDER_PLANS.size())]
                        def loaded = dataManager.load(Id.of(order)).fetchPlan { it.addAll(plan as String[]) }.one()
                        context.merge(loaded)
                        break
                    case 2:
                        opType = 'merge-stale-line'
                        def line = lines[rnd.nextInt(lines.size())]
                        def plan = LINE_PLANS[rnd.nextInt(LINE_PLANS.size())]
                        def loaded = dataManager.load(Id.of(line)).fetchPlan { it.addAll(plan as String[]) }.one()
                        context.merge(loaded)
                        break
                    case 3:
                        opType = 'edit-local'
                        def managed = pickManaged(context, rnd, creationOrder)
                        if (managed == null) continue
                        def attrs = localAttrs(managed).findAll { entityStates.isLoaded(managed, it) }
                        if (attrs.empty) continue
                        String attr = attrs[rnd.nextInt(attrs.size())]
                        def value = (attr == 'quantity') ? rnd.nextInt(1000) : ('v' + rnd.nextInt(1000))
                        if (EntityValues.getValue(managed, attr) != value) {
                            EntityValues.setValue(managed, attr, value)
                            oracle.userEdits[key(managed)][attr] = value
                            oracle.editedKeys << key(managed)
                        }
                        break
                    case 4:
                        opType = 'set-order-customer'
                        Order managedOrder = context.find(Order, orders[rnd.nextInt(orders.size())].id)
                        if (managedOrder == null || !entityStates.isLoaded(managedOrder, 'customer')) continue
                        def customer = customers[rnd.nextInt(customers.size())]
                        def managedCustomer = context.merge(
                                dataManager.load(Id.of(customer)).fetchPlan { it.addAll('name') }.one())
                        if (!managedCustomer.is(managedOrder.customer)) {
                            managedOrder.customer = managedCustomer
                            oracle.userEdits[key(managedOrder)]['customer'] = EntityValues.getId(managedCustomer)
                            oracle.editedKeys << key(managedOrder)
                        }
                        break
                    case 5:
                        opType = 'add-line-to-order'
                        // the AddAction flow: merge a slim copy of the line, set the back reference,
                        // add to the order's collection if loaded
                        Order managedOrder = context.find(Order, orders[rnd.nextInt(orders.size())].id)
                        if (managedOrder == null) continue
                        def line = lines[rnd.nextInt(lines.size())]
                        def loaded = dataManager.load(Id.of(line)).fetchPlan { it.addAll('quantity') }.one()
                        OrderLine managedLine = context.merge(loaded)
                        boolean refChanged = !entityStates.isLoaded(managedLine, 'order') ||
                                !managedOrder.is(managedLine.order)
                        managedLine.order = managedOrder
                        if (refChanged) {
                            oracle.userEdits[key(managedLine)]['order'] = EntityValues.getId(managedOrder)
                            oracle.editedKeys << key(managedLine)
                        }
                        if (entityStates.isLoaded(managedOrder, 'orderLines') && managedOrder.orderLines != null) {
                            if (!managedOrder.orderLines.contains(managedLine)) {
                                managedOrder.orderLines.add(managedLine)
                                oracle.editedKeys << key(managedOrder)
                            }
                            oracle.membership[key(managedOrder)] << EntityValues.getId(managedLine)
                        }
                        break
                }
            } catch (Throwable t) {
                violation('EX-op-threw', opType, seed, op, t.class.simpleName + ': ' + String.valueOf(t.message).take(120))
            }
            if (TRACE) {
                def impl = (io.jmix.flowui.model.impl.DataContextImpl) context
                println "TRACE seed=$seed op=$op $opType"
                println "  edited=${oracle.editedKeys}"
                println "  modified=${impl.modifiedInstances.collect { key(it) }}"
            }
            checkInvariants(context, oracle, loadedBefore, seed, op, opType)
        }
    }

    // ---- ops helpers ---------------------------------------------------------------------------

    Object pickManaged(DataContext context, Random rnd, List<Object> creationOrder) {
        // sorted by scenario-creation index so op targets are deterministic per seed across
        // JVM runs (sorting by key(it) is stable within a run but keyed to per-run random
        // UUIDs, which reshuffles which entity a given rnd index picks between runs)
        def all = ((io.jmix.flowui.model.impl.DataContextImpl) context).all.toList().sort {
            creationOrder.indexOf(EntityValues.getId(it))
        }
        all.empty ? null : all[rnd.nextInt(all.size())]
    }

    static List<String> localAttrs(Object entity) {
        switch (entity.getClass()) {
            // Order.description is excluded: its setter is fluent (returns Order), and
            // SettersEnhancingStep only enhances void setters (MetaModelUtil.isSetterMethod),
            // so no property-change event fires and DataContext cannot see the edit —
            // the I5 oracle cannot apply to it (framework enhancer limitation, not a merge bug)
            case Order: return ['number']
            case OrderLine: return ['quantity', 'description']
            case Customer: return ['name', 'email']
            default: return []
        }
    }

    void forgetEditsCoveredByPlan(Oracle oracle, Object managed, List<String> plan) {
        def rootAttrs = plan.collect { it.split('\\.')[0] }.toSet()
        oracle.userEdits[key(managed)]?.keySet()?.removeAll(rootAttrs)
        if (rootAttrs.contains('orderLines')) {
            oracle.membership.remove(key(managed))
        }
    }

    // ---- invariants ----------------------------------------------------------------------------

    Map<String, Set<String>> snapshotLoaded(DataContext context) {
        Map<String, Set<String>> result = [:]
        for (Object entity : ((io.jmix.flowui.model.impl.DataContextImpl) context).all) {
            def loaded = (localAttrs(entity) + refAttrs(entity)).findAll {
                entityStates.isLoaded(entity, it)
            } as Set
            result[key(entity)] = loaded
        }
        result
    }

    static List<String> refAttrs(Object entity) {
        switch (entity.getClass()) {
            case Order: return ['customer', 'orderLines']
            case OrderLine: return ['order']
            default: return []
        }
    }

    void checkInvariants(DataContext context, Oracle oracle, Map<String, Set<String>> loadedBefore,
                         long seed, int op, String opType) {
        def impl = (io.jmix.flowui.model.impl.DataContextImpl) context
        def all = impl.all.toList()
        def byKey = all.collectEntries { [(key(it)): it] }

        // I1 edits-preserved (references compared by id)
        oracle.userEdits.each { entityKey, edits ->
            def managed = byKey[entityKey]
            if (managed == null) return
            def clobbered = []
            edits.each { attr, expected ->
                if (!entityStates.isLoaded(managed, attr)) {
                    clobbered << "$attr expected=$expected actual=<unloaded>"
                    return
                }
                def actual = EntityValues.getValue(managed, attr)
                if (attr in ['customer', 'order']) {
                    actual = actual == null ? null : EntityValues.getId(actual)
                }
                if (actual != expected) {
                    clobbered << "$attr expected=$expected actual=$actual"
                }
            }
            clobbered.each { detail ->
                violation('I1-edit-clobbered', opType, seed, op, "$entityKey $detail")
            }
            // count each loss once: re-baseline to current state
            clobbered.each { String d -> edits.remove((d as String).split(' ')[0]) }
        }

        // I2 loaded-not-regressed
        loadedBefore.each { entityKey, attrs ->
            def managed = byKey[entityKey]
            if (managed == null) return
            def regressed = attrs.findAll { !entityStates.isLoaded(managed, it) }
                    .findAll { oracle.reportedFacts.add("I2|$entityKey|$it" as String) }
            if (regressed) {
                violation('I2-loaded-regressed', opType, seed, op, "$entityKey attrs=$regressed")
            }
        }

        // I3 no duplicates + I4 identity map + reachability
        all.each { entity ->
            refAttrs(entity).findAll { entityStates.isLoaded(entity, it) }.each { attr ->
                def value = EntityValues.getValue(entity, attr)
                if (value == null) return
                def targets = (value instanceof Collection) ? value : [value]
                if (value instanceof Collection) {
                    def ids = targets.collect { EntityValues.getId(it) }
                    if (ids.size() != ids.toSet().size() || targets.size() != targets.collect { System.identityHashCode(it) }.toSet().size()) {
                        violation('I3-collection-dups', opType, seed, op, "${key(entity)}.$attr ids=$ids")
                    }
                }
                targets.each { target ->
                    def managedTarget = context.find(target.getClass(), EntityValues.getId(target))
                    String fact = "I4|${key(entity)}|$attr|${EntityValues.getId(target)}"
                    if (managedTarget == null) {
                        if (oracle.reportedFacts.add(fact)) {
                            violation('I4-ref-not-in-context', opType, seed, op, "${key(entity)}.$attr -> ${key(target)}")
                        }
                    } else if (!managedTarget.is(target)) {
                        if (oracle.reportedFacts.add(fact)) {
                            violation('I4-ref-not-identical', opType, seed, op, "${key(entity)}.$attr -> ${key(target)}")
                        }
                    }
                }
            }
        }

        // I7 membership preserved
        oracle.membership.each { orderKey, lineIds ->
            Order managedOrder = byKey[orderKey] as Order
            if (managedOrder == null || !entityStates.isLoaded(managedOrder, 'orderLines') || managedOrder.orderLines == null) return
            def actualIds = managedOrder.orderLines.collect { EntityValues.getId(it) }.toSet()
            def missing = lineIds.findAll { !(it in actualIds) }
            if (missing) {
                violation('I7-membership-lost', opType, seed, op, "$orderKey missing=$missing")
                lineIds.removeAll(missing) // count once
            }
        }

        // I5 dirty-not-missed / I6 dirty-not-phantom
        def modifiedKeys = impl.modifiedInstances.collect { key(it) }.toSet()
        def missed = oracle.editedKeys.findAll { byKey[it] != null && !(it in modifiedKeys) }
        missed.each { violation('I5-dirty-missed', opType, seed, op, it as String) }
        oracle.editedKeys.removeAll(missed) // count once
        modifiedKeys.each { entityKey ->
            if (!(entityKey in oracle.editedKeys) && !(entityKey in oracle.phantomSeen)
                    && byKey[entityKey] != null && !entityStates.isNew(byKey[entityKey])) {
                violation('I6-dirty-phantom', opType, seed, op, entityKey as String)
                oracle.phantomSeen << entityKey // count once
            }
        }
    }

    // ---- report --------------------------------------------------------------------------------

    void printReport(int scenariosRun) {
        println '\n================ DataContext invariant fuzz report ================'
        println "scenarios=${scenariosRun}, ops/scenario=${OPS_PER_SCENARIO}, baseSeed=${BASE_SEED}"
        if (violationCounts.isEmpty()) {
            println 'No invariant violations.'
        } else {
            println String.format('%-26s %-22s %8s', 'invariant', 'op', 'count')
            violationCounts.sort { -it.value }.each { k, v ->
                def (inv, opType) = k.split('\\|')
                println String.format('%-26s %-22s %8d', inv, opType, v)
            }
            println '\naffected scenarios per invariant:'
            violationSeeds.each { inv, seeds ->
                println String.format('%-26s %5d scenarios, sample seeds: %s', inv, seeds.size(), seeds.take(8).toList())
            }
            println '\nsample violations:'
            samples.each { println '  ' + it }
        }
        println '===================================================================\n'
    }
}
