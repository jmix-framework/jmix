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

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.SaveContext
import io.jmix.security.model.*
import io.jmix.securitydata.entity.ResourcePolicyEntity
import io.jmix.securitydata.entity.RoleEntity
import io.jmix.securitydata.entity.RowLevelPolicyEntity
import io.jmix.securitydata.role.provider.DatabaseRoleProvider
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityDataSpecification

class DatabaseRoleProviderTest extends SecurityDataSpecification {

    @Autowired
    DatabaseRoleProvider databaseRoleProvider

    @Autowired
    DataManager dataManager

    @Autowired
    Metadata metadata

    def setup() {
        prepareTestData()
    }

    def "getAllRoles"() {
        when:
        def roles = databaseRoleProvider.getAllRoles()

        then:
        roles.size() == 2

        def role1 = roles.find {it.code == 'role1'}
        def role2 = roles.find {it.code == 'role2'}

        role1 != null
        role2 != null

        role1.resourcePolicies.size() == 2

        def screen1ResourcePolicy = role1.resourcePolicies.find {it.resource == 'screen1'}
        with (screen1ResourcePolicy) {
            action == ResourcePolicy.DEFAULT_ACTION
            effect == ResourcePolicy.DEFAULT_EFFECT
            type == ResourcePolicyType.SCREEN
        }

        role1.rowLevelPolicies.size() == 2
        def rowLevelPolicy = role1.rowLevelPolicies.find {it.entityName == 'test_Order'}
        with (rowLevelPolicy) {
            type == RowLevelPolicyType.JPQL
            whereClause == 'where1'
            joinClause == 'join1'
        }
    }

    def "getRoleByCode"() {
        when:
        Role role = databaseRoleProvider.getRoleByCode('role1')

        then:
        with (role) {
            code == 'role1'
            name == 'Role1'
            resourcePolicies.size() == 2
            rowLevelPolicies.size() == 2
        }
    }

    private void prepareTestData() {
        RoleEntity role1 = metadata.create(RoleEntity)
        role1.code = 'role1'
        role1.name = 'Role1'

        def entitiesToSave = []

        entitiesToSave << createResourcePolicyEntity(ResourcePolicyType.SCREEN, 'screen1',
                ResourcePolicy.DEFAULT_ACTION, ResourcePolicy.DEFAULT_EFFECT, role1)
        entitiesToSave << createResourcePolicyEntity(ResourcePolicyType.SCREEN, 'screen2',
                ResourcePolicy.DEFAULT_ACTION, ResourcePolicy.DEFAULT_EFFECT, role1)

        entitiesToSave << createRowLevelPolicyEntity('test_Order', 'where1', 'join1', role1)
        entitiesToSave << createRowLevelPolicyEntity('test_Customer', 'where2', 'join2', role1)

        RoleEntity role2 = metadata.create(RoleEntity)
        role2.code = 'role2'
        role2.name = 'Role2'

        entitiesToSave << role1
        entitiesToSave << role2
        dataManager.save(new SaveContext().saving(entitiesToSave))
    }

    private ResourcePolicyEntity createResourcePolicyEntity(String type, String resource, String action,
                                                            String effect, RoleEntity roleEntity) {
        def resourcePolicy = metadata.create(ResourcePolicyEntity)
        resourcePolicy.type = type
        resourcePolicy.resource = resource
        resourcePolicy.action = action
        resourcePolicy.effect = effect
        resourcePolicy.role = roleEntity
        return resourcePolicy
    }

    private RowLevelPolicyEntity createRowLevelPolicyEntity(String entityName, String whereClause, String joinClause,
                                                            RoleEntity role) {
        RowLevelPolicyEntity rowLevelPolicy = metadata.create(RowLevelPolicyEntity)
        rowLevelPolicy.type = RowLevelPolicyType.JPQL
        rowLevelPolicy.entityName = entityName
        rowLevelPolicy.whereClause = whereClause
        rowLevelPolicy.joinClause = joinClause
        rowLevelPolicy.action = RowLevelPolicyAction.READ
        rowLevelPolicy.role = role
        return rowLevelPolicy
    }
}
