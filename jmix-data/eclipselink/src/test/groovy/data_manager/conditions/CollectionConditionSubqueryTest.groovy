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

package data_manager.conditions

import io.jmix.core.LoadContext
import io.jmix.core.Metadata
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.conditions.ModuleA
import test_support.entity.conditions.ModuleB
import test_support.entity.conditions.ModuleC

import static io.jmix.core.querycondition.PropertyCondition.Operation.CONTAINS
import static io.jmix.core.querycondition.PropertyCondition.Operation.EQUAL
import static io.jmix.core.querycondition.PropertyCondition.Operation.LESS
import static io.jmix.core.querycondition.PropertyCondition.Operation.NOT_EQUAL

/**
 * A condition on a property path crossing a to-many association is generated as a self-contained
 * 'exists' subquery instead of a top-level join. The main query therefore returns no duplicates
 * even without 'select distinct', which fails on Oracle if the entity contains a LOB attribute
 * (ORA-00932: inconsistent datatypes).
 *
 * Test data (see {@link BaseConditionJoinTest}):
 * C: c1(maxSpeed=10), c2(20), c3(30);
 * B: b1(maxCount=1, recommendedCs=[c1,c2]), b2(2, [c2,c3]), b3(3, [c3]);
 * A: a1(compatibleBs=[b1], compatibleCs=[c1]), a2([b2]), a3([b3]), a4_special, a5(compatibleCs=[c1]), a6(compatibleCs=[c3])
 */
class CollectionConditionSubqueryTest extends BaseConditionJoinTest {

    @Autowired
    protected Metadata metadata

    def "filtering by a nested attribute of a m2m collection produces no duplicates without distinct"() {
        when: "two elements of b2's collection match the condition"
        def list = dataManager.load(ModuleB)
                .condition(PropertyCondition.greaterOrEqual("recommendedCs.maxSpeed", 20d))
                .list()

        then: "each entity is returned exactly once"
        list.size() == 3
        list*.name.toSet() == ['B1', 'B2', 'B3'] as Set
    }

    def "count by a m2m nested attribute condition counts entities, not joined rows"() {
        when:
        def loadContext = new LoadContext<>(metadata.getClass(ModuleB))
        loadContext.setQuery(new LoadContext.Query("select e from test_ModuleB e")
                .setCondition(PropertyCondition.greaterOrEqual("recommendedCs.maxSpeed", 20d)))

        then:
        dataManager.getCount(loadContext) == 3
    }

    def "negative operation on a m2m nested attribute produces no duplicates"() {
        when:
        def list = dataManager.load(ModuleB)
                .condition(PropertyCondition.createWithValue("recommendedCs.maxSpeed", NOT_EQUAL, 10d))
                .list()

        then: "an entity having any element not equal to the value matches exactly once"
        list.size() == 3
        list*.name.toSet() == ['B1', 'B2', 'B3'] as Set
    }

    def "m2m nested condition combined through OR does not lose entities with an empty collection"() {
        when:
        def list = dataManager.load(ModuleA)
                .condition(LogicalCondition.or(
                        PropertyCondition.createWithValue("compatibleCs.maxSpeed", EQUAL, 30d),
                        PropertyCondition.createWithValue("name", CONTAINS, "special")
                ))
                .list()

        then: "the entity without compatibleCs matches by name"
        list.size() == 2
        list*.name.toSet() == ['A4_special', 'A6'] as Set
    }

    def "condition on a path crossing two m2m collections"() {
        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.createWithValue("compatibleBs.recommendedCs.maxSpeed", LESS, 15d))
                .list()

        then:
        list.size() == 1
        list[0].name == 'A1'
    }

    def "'is set' = false on a m2m nested attribute matches entities with an empty collection"() {
        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.isSet("compatibleBs.maxCount", false))
                .list()

        then: "entities without compatibleBs match, as with the left join based generation"
        list.size() == 3
        list*.name.toSet() == ['A4_special', 'A5', 'A6'] as Set
    }

    def "'is set' = true on a m2m nested attribute matches only entities having a matching element"() {
        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.isSet("compatibleBs.maxCount", true))
                .list()

        then:
        list.size() == 3
        list*.name.toSet() == ['A1', 'A2', 'A3'] as Set
    }

    def "'is collection empty' = true on a collection under a m2m collection matches entities with an empty collection"() {
        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.isCollectionEmpty("compatibleBs.recommendedCs", true))
                .list()

        then: "entities without compatibleBs match, as with the left join based generation"
        list.size() == 3
        list*.name.toSet() == ['A4_special', 'A5', 'A6'] as Set
    }

    def "'is collection empty' = false on a collection under a m2m collection"() {
        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.isCollectionEmpty("compatibleBs.recommendedCs", false))
                .list()

        then:
        list.size() == 3
        list*.name.toSet() == ['A1', 'A2', 'A3'] as Set
    }

    def "'member of' on a collection under a m2m collection"() {
        setup:
        def c1 = dataManager.load(ModuleC)
                .condition(PropertyCondition.equal("name", "C1"))
                .one()

        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.memberOfCollection("compatibleBs.recommendedCs", c1))
                .list()

        then:
        list.size() == 1
        list[0].name == 'A1'
    }

    def "'not member of' on a collection under a m2m collection matches entities with an empty collection"() {
        setup:
        def c1 = dataManager.load(ModuleC)
                .condition(PropertyCondition.equal("name", "C1"))
                .one()

        when:
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.notMemberOfCollection("compatibleBs.recommendedCs", c1))
                .list()

        then: "entities without compatibleBs and entities whose every B does not contain the value match"
        list.size() == 5
        list*.name.toSet() == ['A2', 'A3', 'A4_special', 'A5', 'A6'] as Set
    }

    def "condition on a m2m collection behind a to-one reference"() {
        setup: "A1 references B1 whose collection contains C1 and C2"
        def a1 = dataManager.load(ModuleA).condition(PropertyCondition.equal("name", "A1")).one()
        def b1 = dataManager.load(ModuleB).condition(PropertyCondition.equal("name", "B1")).one()
        a1.defaultB = b1
        dataManager.save(a1)

        when: "the subquery is correlated with a collection path starting at a joined to-one reference"
        def list = dataManager.load(ModuleA)
                .condition(PropertyCondition.createWithValue("defaultB.recommendedCs.maxSpeed", LESS, 15d))
                .list()

        then: "entities without the reference do not match and are not lost with an error"
        list.size() == 1
        list[0].name == 'A1'
    }

    def "'and' of two conditions on the same m2m collection checks each element independently"() {
        when: "no single element of b1's collection satisfies both conditions"
        def list = dataManager.load(ModuleB)
                .condition(LogicalCondition.and(
                        PropertyCondition.createWithValue("recommendedCs.maxSpeed", EQUAL, 10d),
                        PropertyCondition.createWithValue("recommendedCs.maxSpeed", EQUAL, 20d)
                ))
                .list()

        then: "each condition is a separate subquery matching different elements of the same collection"
        list.size() == 1
        list[0].name == 'B1'
    }
}
