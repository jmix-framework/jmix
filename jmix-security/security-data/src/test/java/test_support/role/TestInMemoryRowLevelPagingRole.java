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

package test_support.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPredicate;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.RowLevelRole;
import test_support.entity.ManyToOneEntity;
import test_support.entity.OneToManyEntity;

@ResourceRole(name = TestInMemoryRowLevelPagingRole.NAME, code = TestInMemoryRowLevelPagingRole.NAME)
@RowLevelRole(name = TestInMemoryRowLevelPagingRole.NAME, code = TestInMemoryRowLevelPagingRole.NAME)
public interface TestInMemoryRowLevelPagingRole {

    String NAME = "TestInMemoryRowLevelPagingRole";

    String PERMITTED_SUFFIX = "-permitted";

    @EntityPolicy(entityClass = ManyToOneEntity.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ManyToOneEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @PredicateRowLevelPolicy(entityClass = ManyToOneEntity.class, actions = RowLevelPolicyAction.READ)
    static RowLevelPredicate<ManyToOneEntity> manyToOne() {
        return entity -> entity.getName().endsWith(PERMITTED_SUFFIX);
    }

    @EntityPolicy(entityClass = OneToManyEntity.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = OneToManyEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @PredicateRowLevelPolicy(entityClass = OneToManyEntity.class, actions = RowLevelPolicyAction.READ)
    static RowLevelPredicate<OneToManyEntity> oneToMany() {
        return entity -> entity.getName().endsWith(PERMITTED_SUFFIX);
    }
}
