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
import io.jmix.data.impl.jpql.generator.LogicalConditionGenerator
import io.jmix.data.impl.jpql.generator.PropertyConditionGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import test_support.DataSpec
import test_support.entity.conditions.ModuleA
import test_support.entity.conditions.ModuleB
import test_support.entity.conditions.ModuleC

class BaseConditionJoinTest extends DataSpec {

    @Autowired
    protected DataManager dataManager

    @Autowired
    @Qualifier("data_PropertyConditionGenerator")
    protected PropertyConditionGenerator propertyConditionGenerator

    @Autowired
    @Qualifier("data_LogicalConditionGenerator")
    protected LogicalConditionGenerator logicalConditionGenerator


    def setup() {

        def c1 = dataManager.create(ModuleC)
        c1.name = "C1"
        c1.maxSpeed = 10
        def c2 = dataManager.create(ModuleC)
        c2.name = "C2"
        c2.maxSpeed = 20
        def c3 = dataManager.create(ModuleC)
        c3.name = "C3"
        c3.maxSpeed = 30

        dataManager.save(c1, c2, c3)

        def b1 = dataManager.create(ModuleB)
        b1.name = "B1"
        b1.maxCount = 1
        b1.recommendedCs = [c1, c2]
        def b2 = dataManager.create(ModuleB)
        b2.name = "B2"
        b2.maxCount = 2
        b2.recommendedCs = [c2, c3]
        def b3 = dataManager.create(ModuleB)
        b3.name = "B3"
        b3.maxCount = 3
        b3.recommendedCs = [c3]

        dataManager.save(b1, b2, b3)

        def a1 = dataManager.create(ModuleA)
        a1.name = "A1"
        a1.compatibleBs = [b1]
        a1.compatibleCs = [c1]
        def a2 = dataManager.create(ModuleA)
        a2.name = "A2"
        a2.compatibleBs = [b2]
        def a3 = dataManager.create(ModuleA)
        a3.name = "A3"
        a3.compatibleBs = [b3]
        def a4 = dataManager.create(ModuleA)
        a4.name = "A4_special"

        def a5 = dataManager.create(ModuleA)
        a5.name = "A5"
        a5.compatibleCs = [c1]

        def a6 = dataManager.create(ModuleA)
        a6.name = "A6"
        a6.compatibleCs = [c3]
        dataManager.save(a1, a2, a3, a4, a5, a6)
    }

    def cleanup() {
        jdbc.update("DELETE FROM TEST_REC_A_C_LINK")
        jdbc.update("DELETE FROM TEST_COMP_A_B_LINK")
        jdbc.update("DELETE FROM TEST_COMP_A_C_LINK")
        jdbc.update("DELETE FROM TEST_CONDITIONS_MODULE_A")
        jdbc.update("DELETE FROM TEST_CONDITIONS_MODULE_B")
        jdbc.update("DELETE FROM TEST_CONDITIONS_MODULE_C")
    }
}