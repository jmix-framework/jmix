/*
 * Copyright 2026 Haulmont.
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

package role_assignment_source

import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.role.assignment.RoleAssignment
import io.jmix.security.role.assignment.RoleAssignmentModel
import io.jmix.security.role.assignment.RoleAssignmentPersistence
import io.jmix.security.role.assignment.RoleAssignmentRoleType
import io.jmix.security.role.assignment.RoleAssignmentSource
import io.jmix.securitydata.entity.RoleAssignmentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import test_support.SecurityDataSpecification
import test_support.role.TestFullAccessRole

class RoleAssignmentSourceTest extends SecurityDataSpecification {

    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    Metadata metadata

    @Autowired
    RoleAssignmentPersistence roleAssignmentPersistence

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    Authentication previousAuthentication

    def setup() {
        previousAuthentication = SecurityContextHelper.getAuthentication()
        // RoleAssignmentPersistence works through the secured DataManager.
        def admin = User.withUsername('admin')
                .password('')
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestFullAccessRole.CODE))
                .build()
        SecurityContextHelper.setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.authorities))
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(previousAuthentication)
    }

    def "an assignment created by DataManager has no source"() {
        // Only the paths where a person assigns a role mark it as manual: an assignment created anywhere else,
        // for example imported from JSON without a source, stays owned by the synchronization.
        expect:
        dataManager.create(RoleAssignmentEntity).source == null
    }

    def "a model created by Metadata is manual"() {
        expect:
        metadata.create(RoleAssignmentModel).source == RoleAssignmentSource.MANUAL
    }

    def "a row inserted by SQL without SOURCE has no source"() {
        when:
        jdbcTemplate.update("insert into SEC_ROLE_ASSIGNMENT (ID, VERSION, USERNAME, ROLE_CODE, ROLE_TYPE) " +
                "values ('6c9e420a-2b7a-4c42-8654-a9027ee14083', 1, 'alice', 'role1', 'resource')")

        then:
        storedSource('alice') == null
    }

    def "persistence keeps the stored source in the model"() {
        given:
        def entity = dataManager.create(RoleAssignmentEntity)
        entity.username = 'alice'
        entity.roleCode = 'role1'
        entity.roleType = RoleAssignmentRoleType.RESOURCE
        entity.source = RoleAssignmentSource.OIDC
        dataManager.save(entity)

        expect:
        roleAssignmentPersistence.loadRoleAssignments('alice', RoleAssignmentRoleType.RESOURCE)*.source ==
                [RoleAssignmentSource.OIDC]
    }

    def "persistence stores a new model as manual"() {
        given:
        def model = metadata.create(RoleAssignmentModel)
        model.username = 'alice'
        model.roleCode = 'role1'
        model.roleType = RoleAssignmentRoleType.RESOURCE

        when:
        roleAssignmentPersistence.save([model], [])

        then:
        storedSource('alice') == RoleAssignmentSource.MANUAL
    }

    def "persistence stores a role assignment as manual"() {
        when:
        roleAssignmentPersistence.save([new RoleAssignment('alice', 'role1', RoleAssignmentRoleType.RESOURCE)])

        then:
        storedSource('alice') == RoleAssignmentSource.MANUAL
    }

    String storedSource(String username) {
        dataManager.load(RoleAssignmentEntity)
                .query("e.username = :username")
                .parameter("username", username)
                .one()
                .source
    }
}
