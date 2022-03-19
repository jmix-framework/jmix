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

package database_role_provider


import io.jmix.core.Metadata
import io.jmix.core.SaveContext
import io.jmix.core.UnconstrainedDataManager
import io.jmix.security.model.*
import io.jmix.securitydata.entity.ResourcePolicyEntity
import io.jmix.securitydata.entity.ResourceRoleEntity
import io.jmix.securitydata.entity.RowLevelPolicyEntity
import io.jmix.securitydata.entity.RowLevelRoleEntity
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import test_support.SecurityDataSpecification
import test_support.entity.TestOrder

class DatabaseRoleProviderTest extends SecurityDataSpecification {

    @Autowired
    DatabaseResourceRoleProvider databaseResourceRoleProvider

    @Autowired
    DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider

    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    Metadata metadata

    @Autowired
    ApplicationContext applicationContext

    def setup() {
        prepareTestData()
    }

    def "get all resource roles"() {
        when:
        def roles = databaseResourceRoleProvider.getAllRoles()

        then:
        roles.size() == 1

        def role1 = roles.find { it.code == 'role1' }

        role1 != null

        role1.resourcePolicies.size() == 2

        def screen1ResourcePolicy = role1.resourcePolicies.find { it.resource == 'screen1' }
        with(screen1ResourcePolicy) {
            action == ResourcePolicy.DEFAULT_ACTION
            effect == ResourcePolicy.DEFAULT_EFFECT
            policyGroup == 'policyGroup1'
            type == ResourcePolicyType.SCREEN
        }
    }

    def "get all row level roles"() {
        when:
        def roles = databaseRowLevelRoleProvider.getAllRoles()

        then:
        roles.size() == 2

        def role1 = roles.find { it.code == 'role2' }
        def role3 = roles.find { it.code == 'role3' }

        role1 != null
        role3 != null

        role1.rowLevelPolicies.size() == 1

        role3.rowLevelPolicies.size() == 2
        def rowLevelPolicy = role3.rowLevelPolicies.find { it.entityName == 'test_Order' }
        with(rowLevelPolicy) {
            type == RowLevelPolicyType.JPQL
            whereClause == 'where1'
            joinClause == 'join1'
        }
    }

    def "get resource role by code"() {
        when:
        ResourceRole role = databaseResourceRoleProvider.getRoleByCode('role1')

        then:
        with(role) {
            code == 'role1'
            name == 'Role1'
            description == 'Role1\nrole1'
            resourcePolicies.size() == 2
        }
    }

    def "get row level resource role by code"() {
        when:
        RowLevelRole role = databaseRowLevelRoleProvider.getRoleByCode('role3')

        then:
        with(role) {
            code == 'role3'
            name == 'Role3'
            rowLevelPolicies.size() == 2
        }
    }

    def "predicate created from script"() {
        when:
        RowLevelRole role2 = databaseRowLevelRoleProvider.getRoleByCode('role2')

        then:

        role2.rowLevelPolicies.size() == 1


        when:

        def rowLevelPolicy = role2.rowLevelPolicies[0]
        def testOrder = new TestOrder()
        testOrder.number = '1'

        then:

        rowLevelPolicy.biPredicate.test(testOrder, applicationContext) == false

        when:

        testOrder.number = '2'

        then:

        rowLevelPolicy.biPredicate.test(testOrder, applicationContext) == true

    }

    private void prepareTestData() {
        ResourceRoleEntity role1 = metadata.create(ResourceRoleEntity)
        role1.code = 'role1'
        role1.name = 'Role1'
        role1.description = 'Role1\nrole1'

        def entitiesToSave = []

        entitiesToSave << createResourcePolicyEntity(ResourcePolicyType.SCREEN, 'screen1',
                ResourcePolicy.DEFAULT_ACTION, ResourcePolicy.DEFAULT_EFFECT, 'policyGroup1', role1)
        entitiesToSave << createResourcePolicyEntity(ResourcePolicyType.SCREEN, 'screen2',
                ResourcePolicy.DEFAULT_ACTION, ResourcePolicy.DEFAULT_EFFECT, 'policyGroup2', role1)

        RowLevelRoleEntity role2 = metadata.create(RowLevelRoleEntity)
        role2.code = 'role2'
        role2.name = 'Role2'

        String script = "return {E}.number == '2'"
        entitiesToSave << createScriptRowLevelPolicyEntity('test_Order', RowLevelPolicyAction.CREATE, script, role2)

        RowLevelRoleEntity role3 = metadata.create(RowLevelRoleEntity)
        role3.code = 'role3'
        role3.name = 'Role3'

        entitiesToSave << createJpqlRowLevelPolicyEntity('test_Order', 'where1', 'join1', role3)
        entitiesToSave << createJpqlRowLevelPolicyEntity('test_Customer', 'where2', 'join2', role3)

        entitiesToSave << role1
        entitiesToSave << role2
        entitiesToSave << role3

        dataManager.save(new SaveContext().saving(entitiesToSave))
    }

    private ResourcePolicyEntity createResourcePolicyEntity(String type,
                                                            String resource,
                                                            String action,
                                                            String effect,
                                                            String policyGroup,
                                                            ResourceRoleEntity roleEntity) {
        def resourcePolicy = metadata.create(ResourcePolicyEntity)
        resourcePolicy.type = type
        resourcePolicy.resource = resource
        resourcePolicy.action = action
        resourcePolicy.effect = effect
        resourcePolicy.policyGroup = policyGroup
        resourcePolicy.role = roleEntity
        return resourcePolicy
    }

    private RowLevelPolicyEntity createJpqlRowLevelPolicyEntity(String entityName,
                                                                String whereClause,
                                                                String joinClause,
                                                                RowLevelRoleEntity role) {
        RowLevelPolicyEntity rowLevelPolicy = metadata.create(RowLevelPolicyEntity)
        rowLevelPolicy.type = RowLevelPolicyType.JPQL
        rowLevelPolicy.entityName = entityName
        rowLevelPolicy.whereClause = whereClause
        rowLevelPolicy.joinClause = joinClause
        rowLevelPolicy.action = RowLevelPolicyAction.READ
        rowLevelPolicy.role = role
        return rowLevelPolicy
    }

    private RowLevelPolicyEntity createScriptRowLevelPolicyEntity(String entityName,
                                                                  RowLevelPolicyAction action,
                                                                  String script,
                                                                  RowLevelRoleEntity role) {
        RowLevelPolicyEntity rowLevelPolicy = metadata.create(RowLevelPolicyEntity)
        rowLevelPolicy.type = RowLevelPolicyType.PREDICATE
        rowLevelPolicy.entityName = entityName
        rowLevelPolicy.action = action
        rowLevelPolicy.script = script
        rowLevelPolicy.role = role
        return rowLevelPolicy
    }
}
