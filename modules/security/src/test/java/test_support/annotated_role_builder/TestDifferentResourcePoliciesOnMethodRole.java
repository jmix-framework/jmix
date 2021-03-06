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

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.Role;
import io.jmix.security.role.annotation.SpecificPolicy;
import test_support.entity.TestOrder;

@Role(name = "TestDifferentResourcePoliciesOnMethodRole", code = "TestDifferentResourcePoliciesOnMethodRole")
public interface TestDifferentResourcePoliciesOnMethodRole {

    @EntityPolicy(entityClass = TestOrder.class,
            actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ},
            scope = "rest")
    @EntityAttributePolicy(entityClass = TestOrder.class,
            attributes = {"number", "date"},
            actions = EntityAttributePolicyAction.UPDATE)
    @SpecificPolicy(resources = {"app.order.someSpecificStuff", "app.order.anotherSpecificStuff"})
    void order();
}
