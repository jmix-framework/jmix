/*
 * Copyright 2020 Haulmont.
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

import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPredicate;
import io.jmix.security.role.annotation.*;
import test_support.entity.ManyToOneEntity;
import test_support.entity.OneToManyEntity;
import test_support.entity.TestOrder;

import java.util.function.Predicate;

@RowLevelRole(name = TestDefaultConstraintsRole.NAME, code = TestDefaultConstraintsRole.NAME)
public interface TestDefaultConstraintsRole {
    String NAME = "TestDefaultConstraintsRole";

    @EntityPolicy(entityClass = TestOrder.class,
            actions = {EntityPolicyAction.READ})
    @JpqlRowLevelPolicy(entityClass = TestOrder.class, where = "{E}.number like 'allowed_%'")
    void order();

    @EntityPolicy(entityClass = OneToManyEntity.class, actions = EntityPolicyAction.READ)
    @PredicateRowLevelPolicy(entityClass = OneToManyEntity.class,
            actions = RowLevelPolicyAction.READ)
    static RowLevelPredicate<OneToManyEntity> testOneToMany() {
        return oneToManyEntity -> oneToManyEntity.getName().contains("a");
    }

    @EntityPolicy(entityClass = ManyToOneEntity.class, actions = EntityPolicyAction.READ)
    @PredicateRowLevelPolicy(entityClass = ManyToOneEntity.class,
            actions = RowLevelPolicyAction.READ)
    static RowLevelPredicate<ManyToOneEntity> testManyToOne() {
        return manyToOneEntity -> manyToOneEntity.getName().contains("a");
    }
}
