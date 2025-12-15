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
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import test_support.DataSpec
import test_support.entity.conditions.ModuleA

import static io.jmix.core.querycondition.PropertyCondition.Operation.CONTAINS
import static io.jmix.core.querycondition.PropertyCondition.Operation.EQUAL
import static io.jmix.core.querycondition.PropertyCondition.Operation.EQUAL
import static io.jmix.core.querycondition.PropertyCondition.Operation.LESS

@TestPropertySource(properties = ["jmix.eclipselink.use-inner-join-in-condition = true"])
class LegacyBehaviourTest extends BaseConditionJoinTest {

    def "test condition for nested M2Ms with ORs on different levels"() {
        when: "legacy behaviour enabled by jmix.eclipselink.use-inner-join-in-condition"
        def res = dataManager.load(ModuleA)
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

        then: "inner join generated: only A1 has both compatibleBs and compatibleCs which have been inner-joined"
        res.size() == 1
        res[0].name == "A1"
    }

}
