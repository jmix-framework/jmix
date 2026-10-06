/*
 * Copyright 2025 Haulmont.
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

package role_persistence

import io.jmix.core.DataManager
import io.jmix.core.EntitySerialization
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.model.*
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.role.RolePersistence
import io.jmix.security.role.assignment.RoleAssignment
import io.jmix.security.role.assignment.RoleAssignmentPersistence
import io.jmix.security.role.assignment.RoleAssignmentRoleType
import io.jmix.securitydata.impl.role.assignment.DatabaseRoleAssignmentProvider
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.role.TestFullAccessRole

import java.nio.charset.StandardCharsets

class RolePersistenceTest extends SecurityDataSpecification {

    @Autowired
    DataManager dataManager
    @Autowired
    RolePersistence rolePersistence
    @Autowired
    DatabaseResourceRoleProvider resourceRoleProvider
    @Autowired
    DatabaseRowLevelRoleProvider rowLevelRoleProvider
    @Autowired
    RoleModelConverter roleModelConverter
    @Autowired
    RoleAssignmentPersistence roleAssignmentPersistence
    @Autowired
    DatabaseRoleAssignmentProvider roleAssignmentProvider
    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    AuthenticationManager authenticationManager
    @Autowired
    EntitySerialization entitySerialization

    UserDetails user1
    Authentication systemAuthentication

    def setup() {
        systemAuthentication = SecurityContextHelper.getAuthentication()

        user1 = User.builder()
                .username("user1")
                .password("{noop}123")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestFullAccessRole.CODE))
                .build()
        userRepository.addUser(user1)

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user1.username, '123'))
        SecurityContextHelper.setAuthentication(authentication)
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)
        userRepository.removeUser(user1)
    }

    def "test save resource role"() {
        when: "create new role"
        def policyModel = dataManager.create(ResourcePolicyModel)
        policyModel.type = ResourcePolicyType.ENTITY
        policyModel.resource = 'User'
        policyModel.action = EntityPolicyAction.READ.id
        policyModel.effect = ResourcePolicyEffect.ALLOW

        def roleModel = dataManager.create(ResourceRoleModel)
        roleModel.name = 'Test Role'
        roleModel.code = 'test-role'
        roleModel.resourcePolicies = [policyModel]

        rolePersistence.save(roleModel)

        then: "role and policy are saved"
        def role = resourceRoleProvider.getRoleByCode('test-role')

        role.name == 'Test Role'
        role.resourcePolicies.find {policy ->
            policy.resource == 'User' &&
            policy.type == ResourcePolicyType.ENTITY &&
            policy.action == EntityPolicyAction.READ.id &&
            policy.effect == ResourcePolicyEffect.ALLOW
        }

        when: "add policy to role"
        roleModel = roleModelConverter.createResourceRoleModel(role)

        def policyModel1 = dataManager.create(ResourcePolicyModel)
        policyModel1.type = ResourcePolicyType.ENTITY
        policyModel1.resource = 'User'
        policyModel1.action = EntityPolicyAction.CREATE.id
        policyModel1.effect = ResourcePolicyEffect.ALLOW

        roleModel.resourcePolicies.add(policyModel1)

        rolePersistence.save(roleModel)

        then: "policy is saved"
        def role1 = resourceRoleProvider.getRoleByCode('test-role')

        role1.resourcePolicies.size() == 2
        role1.resourcePolicies.find {policy ->
            policy.resource == 'User' &&
                    policy.type == ResourcePolicyType.ENTITY &&
                    policy.action == EntityPolicyAction.CREATE.id &&
                    policy.effect == ResourcePolicyEffect.ALLOW
        }

        when: "remove policy from role"
        roleModel = roleModelConverter.createResourceRoleModel(role1)

        roleModel.resourcePolicies.removeIf { it.action == EntityPolicyAction.READ.id }

        rolePersistence.save(roleModel)

        then: "policy is removed"
        def role2 = resourceRoleProvider.getRoleByCode('test-role')

        role2.resourcePolicies.size() == 1
        role2.resourcePolicies.find {policy ->
            policy.resource == 'User' &&
                    policy.type == ResourcePolicyType.ENTITY &&
                    policy.action == EntityPolicyAction.CREATE.id &&
                    policy.effect == ResourcePolicyEffect.ALLOW
        }
    }

    def "test save row-level role"() {
        when: "create new role"
        def policyModel = dataManager.create(RowLevelPolicyModel)
        policyModel.type = RowLevelPolicyType.JPQL
        policyModel.action = RowLevelPolicyAction.READ
        policyModel.entityName = 'User'
        policyModel.whereClause = 'some query'

        def roleModel = dataManager.create(RowLevelRoleModel)
        roleModel.name = 'Test Role'
        roleModel.code = 'test-role'
        roleModel.rowLevelPolicies = [policyModel]

        rolePersistence.save(roleModel)

        then: "role and policy are saved"
        def role = rowLevelRoleProvider.getRoleByCode('test-role')

        role.name == 'Test Role'
        role.rowLevelPolicies.find {policy ->
            policy.entityName == 'User' &&
                    policy.whereClause == 'some query'
        }

        when: "add policy to role"
        roleModel = roleModelConverter.createRowLevelRoleModel(role)

        def policyModel1 = dataManager.create(RowLevelPolicyModel)
        policyModel1.type = RowLevelPolicyType.JPQL
        policyModel1.action = RowLevelPolicyAction.READ
        policyModel1.entityName = 'User'
        policyModel1.whereClause = 'other query'

        roleModel.rowLevelPolicies.add(policyModel1)

        rolePersistence.save(roleModel)

        then: "policy is saved"
        def role1 = rowLevelRoleProvider.getRoleByCode('test-role')

        role1.rowLevelPolicies.size() == 2
        role1.rowLevelPolicies.find {policy ->
            policy.entityName == 'User' &&
                    policy.whereClause == 'other query'
        }

        when: "remove policy from role"
        roleModel = roleModelConverter.createRowLevelRoleModel(role1)

        roleModel.rowLevelPolicies.removeIf { it.whereClause == 'some query' }

        rolePersistence.save(roleModel)

        then: "policy is removed"
        def role2 = rowLevelRoleProvider.getRoleByCode('test-role')

        role2.rowLevelPolicies.size() == 1
        role2.rowLevelPolicies.find {policy ->
            policy.entityName == 'User' &&
                    policy.whereClause == 'other query'
        }
    }

    def "a role keeps its localized values through the persistence until they are cleared"() {
        when: "new roles are saved with localized values"
        saveResourceRole('accounting', 'en=Accounting', 'en=Keeps the books')
        saveRowLevelRole('archive', 'en=Archive', 'en=Sees the archived orders')

        then:
        with(resourceRoleProvider.getRoleByCode('accounting')) {
            localizedNames == 'en=Accounting'
            localizedDescriptions == 'en=Keeps the books'
        }
        with(rowLevelRoleProvider.getRoleByCode('archive')) {
            localizedNames == 'en=Archive'
            localizedDescriptions == 'en=Sees the archived orders'
        }

        when: "models of the stored roles are saved with the values cleared"
        saveResourceRole('accounting', null, null)
        saveRowLevelRole('archive', null, null)

        then:
        with(resourceRoleProvider.getRoleByCode('accounting')) {
            localizedNames == null
            localizedDescriptions == null
        }
        with(rowLevelRoleProvider.getRoleByCode('archive')) {
            localizedNames == null
            localizedDescriptions == null
        }
    }

    def "a resource role is exported and imported with its localized values as they are in the file"() {
        given: "a file exported from a role with localized values"
        saveResourceRole('accounting', 'en=Accounting', 'en=Keeps the books')
        def file = rolePersistence.exportResourceRoles([storedResourceRoleModel('accounting')], false)

        expect: "the file holds them"
        with(exportedRole(file)) {
            localizedNames == 'en=Accounting'
            localizedDescriptions == 'en=Keeps the books'
        }

        when: "the stored values change and the file is imported"
        saveResourceRole('accounting', 'en=Bookkeeping', null)
        rolePersistence.importResourceRoles(file, false)

        then: "the role has the values of the file"
        with(resourceRoleProvider.getRoleByCode('accounting')) {
            localizedNames == 'en=Accounting'
            localizedDescriptions == 'en=Keeps the books'
        }

        when: "a file exported from the role without localized values is imported after values are stored"
        saveResourceRole('accounting', null, null)
        def fileWithoutValues = rolePersistence.exportResourceRoles([storedResourceRoleModel('accounting')], false)
        saveResourceRole('accounting', 'en=Bookkeeping', 'en=Keeps the books')
        rolePersistence.importResourceRoles(fileWithoutValues, false)

        then: "the role has no localized values, as in the file"
        with(resourceRoleProvider.getRoleByCode('accounting')) {
            localizedNames == null
            localizedDescriptions == null
        }
    }

    /**
     * Saves a new resource role or the stored one under the code, with the given localized values.
     */
    void saveResourceRole(String code, String localizedNames, String localizedDescriptions) {
        def roleModel = resourceRoleProvider.findRoleByCode(code) == null
                ? dataManager.create(ResourceRoleModel).tap { it.code = code; it.name = code }
                : storedResourceRoleModel(code)
        roleModel.localizedNames = localizedNames
        roleModel.localizedDescriptions = localizedDescriptions
        rolePersistence.save(roleModel)
    }

    void saveRowLevelRole(String code, String localizedNames, String localizedDescriptions) {
        def roleModel = rowLevelRoleProvider.findRoleByCode(code) == null
                ? dataManager.create(RowLevelRoleModel).tap { it.code = code; it.name = code }
                : storedRowLevelRoleModel(code)
        roleModel.localizedNames = localizedNames
        roleModel.localizedDescriptions = localizedDescriptions
        rolePersistence.save(roleModel)
    }

    /**
     * The first role of an exported file, read as the import reads it.
     */
    Object exportedRole(byte[] file) {
        entitySerialization.entitiesCollectionFromJson(new String(file, StandardCharsets.UTF_8), null)[0]
    }

    ResourceRoleModel storedResourceRoleModel(String code) {
        roleModelConverter.createResourceRoleModel(resourceRoleProvider.getRoleByCode(code))
    }

    RowLevelRoleModel storedRowLevelRoleModel(String code) {
        roleModelConverter.createRowLevelRoleModel(rowLevelRoleProvider.getRoleByCode(code))
    }

    def "export includes policies even for models loaded without policies"() {
        given: "a saved role with one entity policy"
        def policyModel = dataManager.create(ResourcePolicyModel)
        policyModel.type = ResourcePolicyType.ENTITY
        policyModel.resource = 'User'
        policyModel.action = EntityPolicyAction.READ.id
        policyModel.effect = ResourcePolicyEffect.ALLOW

        def roleModel = dataManager.create(ResourceRoleModel)
        roleModel.name = 'Export Role'
        roleModel.code = 'export-role'
        roleModel.resourcePolicies = [policyModel]
        rolePersistence.save(roleModel)

        and: "a light model as produced by the list view (no policies, id from databaseId)"
        def lightModel = roleModelConverter.createResourceRoleModel(
                resourceRoleProvider.getRoleByCode('export-role'), false)

        expect: "the light model carries no policies but has the database id"
        lightModel.resourcePolicies == null
        lightModel.id != null

        when:
        def json = new String(rolePersistence.exportResourceRoles([lightModel], false),
                StandardCharsets.UTF_8)

        then: "exported JSON still contains the policy reloaded from the database"
        json.contains('resourcePolicies')
        json.contains('User')
    }

    def "removing a role entity keeps the assignment of a same-code role of another type"() {
        given: "a resource role and a row-level role sharing the same code"
        def resourcePolicy = dataManager.create(ResourcePolicyModel)
        resourcePolicy.type = ResourcePolicyType.ENTITY
        resourcePolicy.resource = 'User'
        resourcePolicy.action = EntityPolicyAction.READ.id
        resourcePolicy.effect = ResourcePolicyEffect.ALLOW

        def resourceRoleModel = dataManager.create(ResourceRoleModel)
        resourceRoleModel.name = 'Shared Resource Role'
        resourceRoleModel.code = 'shared-role'
        resourceRoleModel.resourcePolicies = [resourcePolicy]
        rolePersistence.save(resourceRoleModel)

        def rowLevelPolicy = dataManager.create(RowLevelPolicyModel)
        rowLevelPolicy.type = RowLevelPolicyType.JPQL
        rowLevelPolicy.action = RowLevelPolicyAction.READ
        rowLevelPolicy.entityName = 'User'
        rowLevelPolicy.whereClause = 'some query'

        def rowLevelRoleModel = dataManager.create(RowLevelRoleModel)
        rowLevelRoleModel.name = 'Shared Row-Level Role'
        rowLevelRoleModel.code = 'shared-role'
        rowLevelRoleModel.rowLevelPolicies = [rowLevelPolicy]
        rolePersistence.save(rowLevelRoleModel)

        and: "both roles are assigned to the same user"
        roleAssignmentPersistence.save([
                new RoleAssignment('assignmentUser', 'shared-role', RoleAssignmentRoleType.RESOURCE),
                new RoleAssignment('assignmentUser', 'shared-role', RoleAssignmentRoleType.ROW_LEVEL)
        ])

        when: "the resource role entity is removed"
        def modelToRemove = roleModelConverter.createResourceRoleModel(
                resourceRoleProvider.getRoleByCode('shared-role'))
        rolePersistence.removeRoles([modelToRemove])

        then: "only the resource assignment is removed, the row-level assignment survives"
        def assignments = roleAssignmentProvider.getAssignmentsByUsername('assignmentUser')
        assignments*.roleType == [RoleAssignmentRoleType.ROW_LEVEL]
        assignments*.roleCode == ['shared-role']
    }
}
