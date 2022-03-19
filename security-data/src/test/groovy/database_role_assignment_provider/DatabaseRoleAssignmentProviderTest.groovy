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

package database_role_assignment_provider


import io.jmix.core.Metadata
import io.jmix.core.SaveContext
import io.jmix.core.UnconstrainedDataManager
import io.jmix.security.role.assignment.RoleAssignmentRoleType
import io.jmix.securitydata.entity.RoleAssignmentEntity
import io.jmix.securitydata.impl.role.assignment.DatabaseRoleAssignmentProvider
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityDataSpecification

class DatabaseRoleAssignmentProviderTest extends SecurityDataSpecification {

    @Autowired
    DatabaseRoleAssignmentProvider databaseRoleAssignmentProvider

    @Autowired
    Metadata metadata

    @Autowired
    UnconstrainedDataManager dataManager

    def setup() {
        prepareTestData()
    }

    protected prepareTestData() {
        def assignments = []
        assignments << createRoleAssignmentEntity('role1', 'user1')
        assignments << createRoleAssignmentEntity('role2', 'user2')
        assignments << createRoleAssignmentEntity('role3', 'user2')

        dataManager.save(new SaveContext().saving(assignments))
    }

    protected RoleAssignmentEntity createRoleAssignmentEntity(String roleCode, String username) {
        def roleAssignment = metadata.create(RoleAssignmentEntity)
        roleAssignment.roleCode = roleCode
        roleAssignment.username = username
        roleAssignment.roleType = RoleAssignmentRoleType.RESOURCE
        return roleAssignment
    }

    def "getAllAssignments"() {
        when:
        def assignments = databaseRoleAssignmentProvider.getAllAssignments()

        then:
        assignments.size() == 3
    }

    def "getAssignmentsByUsername"() {
        when:
        def assignments1 = databaseRoleAssignmentProvider.getAssignmentsByUsername("user1")

        then:
        assignments1.size() == 1
        assignments1.find { it.roleCode == 'role1' } != null

        when:
        def assignments2 = databaseRoleAssignmentProvider.getAssignmentsByUsername("user2")

        then:
        assignments2.size() == 2
        assignments2.find { it.roleCode == 'role2' } != null
        assignments2.find { it.roleCode == 'role3' } != null
    }
}
