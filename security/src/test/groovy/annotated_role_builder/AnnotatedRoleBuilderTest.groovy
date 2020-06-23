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

package annotated_role_builder


import io.jmix.security.model.*
import io.jmix.security.role.builder.AnnotatedRoleBuilder
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecuritySpecification
import test_support.annotated_role_builder.*
import test_support.entity.TestOrder

class AnnotatedRoleBuilderTest extends SecuritySpecification {

    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder

    def "different resource policies types defined in a single method"() {

        when:

        Role role = annotatedRoleBuilder.createRole(TestDifferentResourcePoliciesOnMethodRole.class.getCanonicalName())
        def policies = role.resourcePolicies
        def entityPolicies = policies.findAll { it.type == ResourcePolicyType.ENTITY }
        def entityAttributePolicies = policies.findAll { it.type == ResourcePolicyType.ENTITY_ATTRIBUTE }
        def specificPolicies = policies.findAll { it.type == ResourcePolicyType.SPECIFIC }

        then:

        entityPolicies.size() == 2
        entityAttributePolicies.size() == 2
        specificPolicies.size() == 2
    }

    def "resource policies created in static method with @ExplicitResourcePolicies"() {
        when:

        Role role = annotatedRoleBuilder.createRole(TestExplicitResourcePoliciesRole.class.getCanonicalName())
        def policies = role.resourcePolicies

        then:

        policies.size() == 2
    }

    def "predicate row-level policy"() {

        when:

        TestOrder order1 = new TestOrder();
        order1.number = "aaa"
        TestOrder order2 = new TestOrder();
        order2.number = "bbb"

        Role role = annotatedRoleBuilder.createRole(TestPredicateRoleLevelPolicyRole.class.getCanonicalName())
        def policies = role.rowLevelPolicies

        then:

        policies.size() == 2

        def createPolicy = policies.find { it.action == RowLevelPolicyAction.CREATE }
        createPolicy != null
        createPolicy.entityName == 'test_Order'

        policies.find { it.action == RowLevelPolicyAction.UPDATE } != null

        RowLevelPolicy policy = policies[0]
        policy.predicate.test(order1) == true
        policy.predicate.test(order2) == false
    }

    def "JPQL row-level policy"() {

        when:

        Role role = annotatedRoleBuilder.createRole(TestJpqlRoleLevelPolicyRole.class.getCanonicalName())
        def policies = role.rowLevelPolicies

        then:

        policies.size() == 2

        def policyWithoutJoin = policies.find { !it.joinClause }
        def policyWithJoin = policies.find { it.joinClause }

        policyWithoutJoin.whereClause == "where1"
        policyWithoutJoin.joinClause == ""
        policyWithoutJoin.action == RowLevelPolicyAction.READ
        policyWithoutJoin.entityName == "test_Order"

        policyWithJoin.whereClause == "where2"
        policyWithJoin.joinClause == "join2"
        policyWithJoin.action == RowLevelPolicyAction.READ
        policyWithJoin.entityName == "test_Order"
    }

    def "EntityPolicyAction.ALL"() {
        when:

        Role role = annotatedRoleBuilder.createRole(TestEntityPolicyAllCrudRole.class.getCanonicalName())
        def resourcePolicies = role.resourcePolicies

        then:

        resourcePolicies.size() == 4
        resourcePolicies.find {it.action == EntityPolicyAction.CREATE.id}
        resourcePolicies.find {it.action == EntityPolicyAction.READ.id}
        resourcePolicies.find {it.action == EntityPolicyAction.UPDATE.id}
        resourcePolicies.find {it.action == EntityPolicyAction.DELETE.id}
    }
}
