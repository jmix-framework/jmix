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

package test_support.annotated_role_builder;

import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.Role;
import test_support.entity.ManyToManyFirstEntity;
import test_support.entity.ManyToManySecondEntity;
import test_support.entity.ManyToOneEntity;
import test_support.entity.OneToManyEntity;

import java.util.function.Predicate;

@Role(name = TestLazyLoadingRole.NAME, code = TestLazyLoadingRole.NAME)
public interface TestLazyLoadingRole {
    String NAME = "TestLazyLoadingRole";

    @EntityPolicy(entityClass = OneToManyEntity.class, actions = EntityPolicyAction.READ)
    @PredicateRowLevelPolicy(entityClass = OneToManyEntity.class,
            actions = RowLevelPolicyAction.READ)
    static Predicate<OneToManyEntity> testOneToMany() {
        return oneToManyEntity -> oneToManyEntity.getName().contains("a");
    }

    @EntityPolicy(entityClass = ManyToOneEntity.class, actions = EntityPolicyAction.READ)
    @PredicateRowLevelPolicy(entityClass = ManyToOneEntity.class,
            actions = RowLevelPolicyAction.READ)
    static Predicate<ManyToOneEntity> testManyToOne() {
        return manyToOneEntity -> manyToOneEntity.getName().contains("a");
    }

    @EntityPolicy(entityClass = ManyToManyFirstEntity.class, actions = EntityPolicyAction.READ)
    @PredicateRowLevelPolicy(entityClass = ManyToManyFirstEntity.class,
            actions = RowLevelPolicyAction.READ)
    static Predicate<ManyToManyFirstEntity> testManyToManyFirst() {
        return manyToManyFirstEntity -> manyToManyFirstEntity.getName().contains("a");
    }

    @EntityPolicy(entityClass = ManyToManySecondEntity.class, actions = EntityPolicyAction.READ)
    @PredicateRowLevelPolicy(entityClass = ManyToManySecondEntity.class,
            actions = RowLevelPolicyAction.READ)
    static Predicate<ManyToManySecondEntity> testManyToManySecond() {
        return manyToManySecondEntity -> manyToManySecondEntity.getName().contains("a");
    }
}
