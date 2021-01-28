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

package io.jmix.samples.rest.security;

import io.jmix.samples.rest.entity.driver.InsuranceCase;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

import java.util.function.Predicate;

@RowLevelRole(name = InMemoryOneToManyRowLevelRole.NAME, code = InMemoryOneToManyRowLevelRole.NAME)
public interface InMemoryOneToManyRowLevelRole {

    String NAME = "row-level-one-to-many";

    @PredicateRowLevelPolicy(entityClass = InsuranceCase.class,
            actions = RowLevelPolicyAction.READ)
    static Predicate<InsuranceCase> insuranceCase() {
        return entity -> entity.getDescription() == null || !entity.getDescription().startsWith("InsuranceCase#2_")
                && !entity.getDescription().startsWith("InsuranceCase#5_");
    }
}
