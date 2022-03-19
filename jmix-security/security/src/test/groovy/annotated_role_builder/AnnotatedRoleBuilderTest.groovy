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

import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder
import io.jmix.security.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import test_support.SecuritySpecification
import test_support.annotated_role_builder.*
import test_support.entity.Foo
import test_support.entity.TestOrder

class AnnotatedRoleBuilderTest extends SecuritySpecification {

    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder

    @Autowired
    ApplicationContext applicationContext

    def "different resource policies types defined in a single method"() {

        when:

        ResourceRole role = annotatedRoleBuilder.createResourceRole(TestDifferentResourcePoliciesOnMethodRole.class.getCanonicalName())
        def policies = role.resourcePolicies
        def entityPolicies = policies.findAll { it.type == ResourcePolicyType.ENTITY }
        def entityAttributePolicies = policies.findAll { it.type == ResourcePolicyType.ENTITY_ATTRIBUTE }
        def specificPolicies = policies.findAll { it.type == ResourcePolicyType.SPECIFIC }

        then:

        entityPolicies.size() == 2
        entityAttributePolicies.size() == 2
        specificPolicies.size() == 2
    }

    def "string entityName attribute on annotation"() {

        when:

        ResourceRole role = annotatedRoleBuilder.createResourceRole(TestStringEntityNameRole.class.getCanonicalName())
        def policies = role.resourcePolicies
        def entityPolicies = policies.findAll { it.type == ResourcePolicyType.ENTITY }
        def entityAttributePolicies = policies.findAll { it.type == ResourcePolicyType.ENTITY_ATTRIBUTE }

        then:

        entityPolicies.size() == 1
        entityAttributePolicies.size() == 1

        entityPolicies[0].resource == 'test_Order'
        entityAttributePolicies[0].resource == 'test_Order.number'
    }

    def "resource policies created in static method with @ExplicitResourcePolicies"() {
        when:

        ResourceRole role = annotatedRoleBuilder.createResourceRole(TestExplicitResourcePoliciesRole.class.getCanonicalName())
        def policies = role.resourcePolicies

        then:

        policies.size() == 2
    }

    def "predicate row-level policy"() {

        when:

        TestOrder order1 = new TestOrder()
        order1.number = "aaa"
        TestOrder order2 = new TestOrder()
        order2.number = "bbb"

        Foo foo1 = new Foo()
        foo1.name = "aaa"
        Foo foo2 = new Foo()
        foo2.name = "bbb"

        RowLevelRole role = annotatedRoleBuilder.createRowLevelRole(TestPredicateRoleLevelPolicyRole.class.getCanonicalName())
        def policies = role.rowLevelPolicies

        then:

        policies.size() == 4

        def createOrderPolicy = policies.find { it.action == RowLevelPolicyAction.CREATE && it.entityName == 'test_Order' }
        createOrderPolicy != null

        policies.find { it.action == RowLevelPolicyAction.UPDATE } != null

        createOrderPolicy.biPredicate.test(order1, applicationContext) == true
        createOrderPolicy.biPredicate.test(order2, applicationContext) == false

        def createFooPolicy = policies.find { it.action == RowLevelPolicyAction.CREATE && it.entityName == 'test_Foo' }
        createOrderPolicy != null

        createFooPolicy.biPredicate.test(foo1, applicationContext) == true
        createFooPolicy.biPredicate.test(foo2, applicationContext) == false
    }

    def "JPQL row-level policy"() {

        when:

        RowLevelRole role = annotatedRoleBuilder.createRowLevelRole(TestJpqlRoleLevelPolicyRole.class.getCanonicalName())
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

        ResourceRole role = annotatedRoleBuilder.createResourceRole(TestEntityPolicyAllCrudRole.class.getCanonicalName())
        def resourcePolicies = role.resourcePolicies

        then:

        resourcePolicies.size() == 4
        resourcePolicies.find { it.action == EntityPolicyAction.CREATE.id }
        resourcePolicies.find { it.action == EntityPolicyAction.READ.id }
        resourcePolicies.find { it.action == EntityPolicyAction.UPDATE.id }
        resourcePolicies.find { it.action == EntityPolicyAction.DELETE.id }
    }

    def "test role inheritance"() {
        when:

        ResourceRole role = annotatedRoleBuilder.createResourceRole(TestChildRole.class.getCanonicalName())
        def resourcePolicies = role.resourcePolicies

        then:

        resourcePolicies.size() == 2
        resourcePolicies.find { it.action == EntityPolicyAction.CREATE.id }
        resourcePolicies.find { it.action == EntityPolicyAction.UPDATE.id }
    }
}
