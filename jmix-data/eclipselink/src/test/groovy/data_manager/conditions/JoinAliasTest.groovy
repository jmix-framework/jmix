/*
 * Copyright 2025 Haulmont.
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

import io.jmix.core.DataManager
import io.jmix.core.Sort
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.jpql.generator.ConditionGenerationContext
import io.jmix.data.impl.jpql.generator.LogicalConditionGenerator
import io.jmix.data.impl.jpql.generator.PropertyConditionGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.TestPropertySource
import test_support.DataSpec
import test_support.entity.conditions.ModuleA
import test_support.entity.conditions.ModuleB
import test_support.entity.conditions.ModuleC

import static io.jmix.core.querycondition.PropertyCondition.Operation.*

@TestPropertySource(properties = ["jmix.eclipselink.condition-join-alias-prefix = c"])
class JoinAliasTest extends BaseConditionJoinTest {

    def "test condition for nested M2Ms with ORs on different levels"() {
        when: "mixed nested m2m relations are used in condition"
        def res = dataManager.load(ModuleA)
                .query("select distinct a from test_ModuleA a")
                .condition(
                        LogicalCondition.or(
                                LogicalCondition.or(
                                        PropertyCondition.createWithValue("compatibleBs.recommendedCs.maxSpeed", LESS, 15.0),
                                        PropertyCondition.createWithValue("name", CONTAINS, "special")
                                ),
                                PropertyCondition.createWithParameterName("compatibleBs.maxCount", EQUAL, "maxCount"),
                                PropertyCondition.createWithValue("compatibleCs.maxSpeed", EQUAL, 30)
                        )
                )
                .parameter("maxCount", 2)
                .sort(Sort.by("name"))
                .list()

        then: "correct left join executed"
        res.size() == 4
        res[0].name == "A1" // by b.c.maxSpeed
        res[1].name == "A2" // by b.maxCount
        res[2].name == "A4_special" // by name
        res[3].name == "A6" // by compatibleC.maxSpeed
    }

    def "test alias application property used"() {
        when: "join alias changed by application property"
        def condition = LogicalCondition.or(
                LogicalCondition.or(
                        PropertyCondition.createWithValue("compatibleBs.recommendedCs.maxSpeed", LESS, 15.0),
                        PropertyCondition.createWithValue("name", CONTAINS, "special")
                ),
                PropertyCondition.createWithParameterName("compatibleBs.maxCount", EQUAL, "maxCount"),
                PropertyCondition.createWithValue("compatibleCs.maxSpeed", EQUAL, 30)
        )

        def context = new ConditionGenerationContext(condition)
        context.entityName = "test_ModuleA"
        context.entityAlias = "e"

        propagatePropertiesToChildContexts(context)

        def join = logicalConditionGenerator.generateJoin(context)
        def where = logicalConditionGenerator.generateWhere(context)


        then: "correct prefix is used"
        join == " left join e.compatibleBs c0 left join c0.recommendedCs c1   left join e.compatibleBs c2  left join e.compatibleCs c3"
        where.contains("c1.maxSpeed < ")
        where.contains("e.name like ")
        where.contains("c2.maxCount = :maxCount")
        where.contains("c3.maxSpeed = ")
    }

    private void propagatePropertiesToChildContexts(ConditionGenerationContext generationContext) {
        for (Condition childCondition : generationContext.getChildContexts().keySet()) {
            ConditionGenerationContext childContext = generationContext.getChildContexts().get(childCondition);
            childContext.copy(generationContext);
            propagatePropertiesToChildContexts(childContext);
        }
    }
}
