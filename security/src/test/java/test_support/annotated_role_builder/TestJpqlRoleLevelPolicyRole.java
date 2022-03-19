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

import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;
import test_support.entity.TestOrder;

@RowLevelRole(name = "TestAnnotatedRoleBuilderRole 5", code = "TestJpqlRoleLevelPolicyRole")
public interface TestJpqlRoleLevelPolicyRole {

    @JpqlRowLevelPolicy(entityClass = TestOrder.class, where = "where1")
    @JpqlRowLevelPolicy(entityClass = TestOrder.class,
            join = "join2",
            where = "where2")
    void orderRowLevelPolicies();

}
