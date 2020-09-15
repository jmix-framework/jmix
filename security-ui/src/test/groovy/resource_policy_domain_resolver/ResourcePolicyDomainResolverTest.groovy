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

package resource_policy_domain_resolver

import io.jmix.security.model.ResourcePolicy
import io.jmix.security.model.ResourcePolicyType
import io.jmix.securityui.model.ResourcePolicyDomainResolver
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityUiSpecification

class ResourcePolicyDomainResolverTest extends SecurityUiSpecification {

    @Autowired
    ResourcePolicyDomainResolver resourcePolicyDomainResolver

    def "resolve domain for entity policy if entity class has @Domain annotation"() {
        def policy = new ResourcePolicy(ResourcePolicyType.ENTITY, "test_Order", "create");

        when:
        def domain = resourcePolicyDomainResolver.resolveDomain(policy)

        then:
        domain == 'Order'
    }

    def "resolve domain for entity policy if entity police doesn't have @Domain annotation"() {
        def policy = new ResourcePolicy(ResourcePolicyType.ENTITY, "test_Customer", "create");

        when:
        def domain = resourcePolicyDomainResolver.resolveDomain(policy)

        then:
        domain == 'test_Customer' //domain equals entity name
    }

    def "resolve domain for entity attribute policy"() {
        def policy = new ResourcePolicy(ResourcePolicyType.ENTITY_ATTRIBUTE, "test_Order.number", "create");

        when:
        def domain = resourcePolicyDomainResolver.resolveDomain(policy)

        then:
        domain == 'Order' //entity domain is returned
    }

    def "resolve domain for screen policy if screen has @Domain annotation"() {
        def policy = new ResourcePolicy(ResourcePolicyType.SCREEN, "test_ScreenWithDomain", "allow");

        when:
        def domain = resourcePolicyDomainResolver.resolveDomain(policy)

        then:
        domain == 'ScreenWithDomainValue' //@Domain value is returned
    }

    def "resolve domain for editor screen policy with no @Domain annotation"() {
        def policy = new ResourcePolicy(ResourcePolicyType.SCREEN, "test_Order.edit", "allow");

        when:
        def domain = resourcePolicyDomainResolver.resolveDomain(policy)

        then:
        domain == 'Order' //domain value of the related entity is returned
    }

    def "resolve domain for lookup screen policy with no @Domain annotation"() {
        def policy = new ResourcePolicy(ResourcePolicyType.SCREEN, "test_Order.lookup", "allow");

        when:
        def domain = resourcePolicyDomainResolver.resolveDomain(policy)

        then:
        domain == 'Order' //domain value of the related entity is returned
    }
}
