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

import io.jmix.samples.rest.entity.driver.Model;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

import java.util.function.Predicate;

@RowLevelRole(name = InMemoryManyToManyRowLevelRole.NAME, code = InMemoryManyToManyRowLevelRole.NAME)
public interface InMemoryManyToManyRowLevelRole {

    String NAME = "row-level-many-to-many";

    @PredicateRowLevelPolicy(entityClass = Model.class,
            actions = RowLevelPolicyAction.READ)
    static Predicate<Model> model() {
        return entity -> !entity.getName().startsWith("Model#2_")
                && !entity.getName().startsWith("Model#5_");
    }
}
