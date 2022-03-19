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
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.role.annotation.ExplicitResourcePolicies;
import io.jmix.security.role.annotation.ResourceRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ResourceRole(name = "Test role", code = "TestExplicitResourcePoliciesRole")
public interface TestExplicitResourcePoliciesRole {

    @ExplicitResourcePolicies
    static Collection<ResourcePolicy> explicitOrderPolicies() {
        List<ResourcePolicy> resourcePolicies = new ArrayList<>();
        ResourcePolicy policy1 = ResourcePolicy.builder(ResourcePolicyType.SCREEN, "sample_Order.browsr").build();
        resourcePolicies.add(policy1);
        ResourcePolicy policy2 = ResourcePolicy.builder(ResourcePolicyType.ENTITY, "sample_Order")
                .withAction(EntityPolicyAction.READ.getId())
                .build();
        resourcePolicies.add(policy2);
        return resourcePolicies;
    }
}