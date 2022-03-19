/*
 * Copyright 2021 Haulmont.
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

package test_support.role;

import io.jmix.core.DataManager;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPredicate;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;
import test_support.entity.ManyToOneEntity;
import test_support.entity.TestOrder;

import java.util.List;

/**
 * Test role for testing the parsing of old-style row-level policies. Methods for row-lvel predicate has arguments
 * in this role.
 */
@RowLevelRole(code = TestInMemoryRowLevelConstraintsMethodArgsRole.NAME, name = TestInMemoryRowLevelConstraintsMethodArgsRole.NAME)
public interface TestInMemoryRowLevelConstraintsMethodArgsRole {

    String NAME = "TestInMemoryRowLevelConstraintsMethodArgsRole";

    @PredicateRowLevelPolicy(entityClass = TestOrder.class, actions = RowLevelPolicyAction.READ)
    default RowLevelPredicate<TestOrder> numberEndsWithB(DataManager dataManager) {
        return (testOrder) -> {
            //use dataManager just to test that applicationContext may be used here
            List<ManyToOneEntity> allEntities = dataManager.load(ManyToOneEntity.class).all().list();
            return testOrder.getNumber().endsWith("B");
        };
    }
}
