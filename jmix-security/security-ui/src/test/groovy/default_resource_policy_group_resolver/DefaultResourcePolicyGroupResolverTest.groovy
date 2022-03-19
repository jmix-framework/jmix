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

package default_resource_policy_group_resolver

import io.jmix.security.model.ResourcePolicy
import io.jmix.security.model.ResourcePolicyType
import io.jmix.securityui.model.DefaultResourcePolicyGroupResolver
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityUiSpecification

class DefaultResourcePolicyGroupResolverTest extends SecurityUiSpecification {

    @Autowired
    DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver

    def "resolve policy group for entity policy"() {
        def policy = ResourcePolicy.builder(ResourcePolicyType.ENTITY, "test_Order")
                .withAction("create")
                .build()

        when:
        def policyGroup = resourcePolicyGroupResolver.resolvePolicyGroup(policy)

        then:
        policyGroup == 'test_Order'
    }

    def "resolve policy group for entity attribute policy"() {
        def policy = ResourcePolicy.builder(ResourcePolicyType.ENTITY_ATTRIBUTE, "test_Order.number")
                .withAction("create")
                .build()

        when:
        def domain = resourcePolicyGroupResolver.resolvePolicyGroup(policy)

        then:
        domain == 'test_Order' //policy group of the entity is returned
    }

    def "resolve policy group for editor screen policy"() {
        def policy = ResourcePolicy.builder(ResourcePolicyType.SCREEN, "test_Order.edit")
                .build()

        when:
        def domain = resourcePolicyGroupResolver.resolvePolicyGroup(policy)

        then:
        domain == 'test_Order' //domain value of the related entity is returned
    }

    def "resolve policy group for lookup screen policy"() {
        def policy = ResourcePolicy.builder(ResourcePolicyType.SCREEN, "test_Order.lookup")
                .build()

        when:
        def domain = resourcePolicyGroupResolver.resolvePolicyGroup(policy)

        then:
        domain == 'test_Order' //domain value of the related entity is returned
    }
}
