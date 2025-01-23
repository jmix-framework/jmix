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
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.model.*
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.role.RolePersistence
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
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    AuthenticationManager authenticationManager

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
}
