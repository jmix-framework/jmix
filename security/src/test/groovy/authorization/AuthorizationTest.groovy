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


import spock.lang.Ignore
import test_support.SecuritySpecification

//todo MG
@Ignore
class AuthorizationTest extends SecuritySpecification {

//    @Autowired
//    DataManager dataManager
//
//    @Autowired
//    PersistenceTools persistenceTools
//
//    @Autowired
//    Metadata metadata
//
//    @Autowired
//    Security security

//    def "create session and check permissions"() {
//
//        def role1 = new Role(name: 'role1', type: RoleType.STANDARD, permissions: [])
//        def role2 = new Role(name: 'role2', type: RoleType.DENYING, permissions: [])
//
//        def permission11 = new Permission(role: role1, type: PermissionType.SCREEN, target: 'someScreen', value: 1)
//        role1.permissions.add(permission11)
//
//        def user = new User(login: 'user1', password: '{noop}123', userRoles: [])
//
//        def userRole1 = new UserRole(user: user, role: role1)
//        user.userRoles.add(userRole1)
//        def userRole2 = new UserRole(user: user, role: role2)
//        user.userRoles.add(userRole2)
//
//        dataManager.save(permission11, role1, role2, userRole1, userRole2, user)
//
//        when:
//
//        def token = new UsernamePasswordAuthenticationToken('user1', '123')
//        StandardUserSession session = userSessionManager.createSession(token) as StandardUserSession
//
//        then:
//
//        session.user == user
//        session.getPermissionValue(PermissionType.SCREEN, 'someScreen') == 1
//
//        security.isScreenPermitted('someScreen')
//        !security.isScreenPermitted('someOtherScreen')
//
//        cleanup:
//
//        userSessionManager.removeSession()
//        persistenceTools.deleteRecord(userRole1, userRole2, permission11, role1, role2, user)
//    }
//
//    def "create session and check constraints"() {
//
//        def group = new Group(name: 'group1', constraints: [])
//        def constraint = new Constraint(group: group, checkType: ConstraintCheckType.DATABASE, operationType: ConstraintOperationType.READ, entityName: 'test_Foo', whereClause: '{E}.createdBy = :session_userLogin')
//        group.constraints.add(constraint)
//
//        def user = new User(login: 'user1', password: '{noop}123', group: group)
//
//        dataManager.save(constraint, group, user)
//
//        when:
//
//        def token = new UsernamePasswordAuthenticationToken('user1', '123')
//        StandardUserSession session = userSessionManager.createSession(token) as StandardUserSession
//
//        then:
//
//        session.user == user
//        session.getConstraint
//@Scope(value = WebApplicationContext.SCOPE_REQUESTs('test_Foo').size() == 1
//
//        security.hasConstraints()
//        security.hasConstraints(metadata.getClass('test_Foo'))
//
//        cleanup:
//
//        userSessionManager.removeSession()
//        persistenceTools.deleteRecord(user, constraint, group)
//    }
}
