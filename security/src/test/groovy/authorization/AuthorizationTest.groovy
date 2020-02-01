/*
 * Copyright 2019 Haulmont.
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

package authorization

import io.jmix.core.DataManager
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.security.ConstraintOperationType
import io.jmix.core.security.PermissionType
import io.jmix.core.security.Security
import io.jmix.core.security.UserSessionManager
import io.jmix.data.JmixDataConfiguration
import io.jmix.data.PersistenceTools
import io.jmix.security.JmixSecurityConfiguration
import io.jmix.security.entity.*
import io.jmix.security.impl.StandardUserSession
import test_support.JmixSecurityTestConfiguration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixSecurityConfiguration, JmixSecurityTestConfiguration])
@TestPropertySource(properties = ["jmix.securityImplementation = standard"])
class AuthorizationTest extends Specification {

    @Inject
    DataManager dataManager

    @Inject
    PersistenceTools persistenceTools

    @Inject
    UserSessionManager userSessionManager

    @Inject
    Metadata metadata

    @Inject
    Security security

    def "create session and check permissions"() {

        def role1 = new Role(name: 'role1', type: RoleType.STANDARD, permissions: [])
        def role2 = new Role(name: 'role2', type: RoleType.DENYING, permissions: [])

        def permission11 = new Permission(role: role1, type: PermissionType.SCREEN, target: 'someScreen', value: 1)
        role1.permissions.add(permission11)

        def user = new User(login: 'user1', password: '{noop}123', userRoles: [])

        def userRole1 = new UserRole(user: user, role: role1)
        user.userRoles.add(userRole1)
        def userRole2 = new UserRole(user: user, role: role2)
        user.userRoles.add(userRole2)

        dataManager.commit(permission11, role1, role2, userRole1, userRole2, user)

        when:

        def token = new UsernamePasswordAuthenticationToken('user1', '123')
        StandardUserSession session = userSessionManager.createSession(token) as StandardUserSession

        then:

        session.user == user
        session.getPermissionValue(PermissionType.SCREEN, 'someScreen') == 1

        security.isScreenPermitted('someScreen')
        !security.isScreenPermitted('someOtherScreen')

        cleanup:

        userSessionManager.removeSession()
        persistenceTools.deleteRecord(userRole1, userRole2, permission11, role1, role2, user)
    }

    def "create session and check constraints"() {

        def group = new Group(name: 'group1', constraints: [])
        def constraint = new Constraint(group: group, checkType: ConstraintCheckType.DATABASE, operationType: ConstraintOperationType.READ, entityName: 'test_Foo', whereClause: '{E}.createdBy = :session_userLogin')
        group.constraints.add(constraint)

        def user = new User(login: 'user1', password: '{noop}123', group: group)

        dataManager.commit(constraint, group, user)

        when:

        def token = new UsernamePasswordAuthenticationToken('user1', '123')
        StandardUserSession session = userSessionManager.createSession(token) as StandardUserSession

        then:

        session.user == user
        session.getConstraints('test_Foo').size() == 1

        security.hasConstraints()
        security.hasConstraints(metadata.getClass('test_Foo'))

        cleanup:

        userSessionManager.removeSession()
        persistenceTools.deleteRecord(user, constraint, group)
    }
}
