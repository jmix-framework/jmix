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

package role_assignment_synchronizer

import io.jmix.core.SaveContext
import io.jmix.core.UnconstrainedDataManager
import io.jmix.data.PersistenceHints
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.role.assignment.RoleAssignment
import io.jmix.security.role.assignment.RoleAssignmentRoleType
import io.jmix.security.role.assignment.RoleAssignmentSource
import io.jmix.security.role.assignment.RoleAssignmentSynchronizer
import io.jmix.securitydata.entity.RoleAssignmentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import test_support.SecurityDataSpecification

class DatabaseRoleAssignmentSynchronizerTest extends SecurityDataSpecification {

    static final String RESOURCE = RoleAssignmentRoleType.RESOURCE
    static final String ROW_LEVEL = RoleAssignmentRoleType.ROW_LEVEL

    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    RoleAssignmentSynchronizer synchronizer

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    def "a #existing row, role asserted: #asserted"() {
        given:
        row('a', RESOURCE, existing)

        when:
        sync(asserted ? [assignment('a', RESOURCE)] : [])

        then:
        sources('a', RESOURCE) == expected

        where:
        existing                    | asserted | expected
        RoleAssignmentSource.OIDC   | true     | [RoleAssignmentSource.OIDC]
        RoleAssignmentSource.OIDC   | false    | []
        null                        | true     | [RoleAssignmentSource.OIDC]
        null                        | false    | []
        RoleAssignmentSource.MANUAL | true     | [RoleAssignmentSource.MANUAL]
        RoleAssignmentSource.MANUAL | false    | [RoleAssignmentSource.MANUAL]
        RoleAssignmentSource.LDAP   | true     | [RoleAssignmentSource.LDAP]
        RoleAssignmentSource.LDAP   | false    | [RoleAssignmentSource.LDAP]
    }

    def "a row without source is adopted, not re-inserted"() {
        given:
        def existing = row('a', RESOURCE, null)

        when:
        sync([assignment('a', RESOURCE)])

        then:
        def rows = rows('a', RESOURCE)
        rows*.id == [existing.id]
        rows*.source == [RoleAssignmentSource.OIDC]
    }

    def "an asserted role without a row is inserted as OIDC"() {
        when:
        sync([assignment('a', RESOURCE)])

        then:
        sources('a', RESOURCE) == [RoleAssignmentSource.OIDC]
    }

    def "a role is keyed by code and type"() {
        given:
        row('a', RESOURCE, RoleAssignmentSource.OIDC)

        when:
        sync([assignment('a', ROW_LEVEL)])

        then:
        sources('a', RESOURCE) == []
        sources('a', ROW_LEVEL) == [RoleAssignmentSource.OIDC]
    }

    def "each row of one key follows its own line"() {
        given:
        row('a', RESOURCE, null)
        row('a', RESOURCE, RoleAssignmentSource.MANUAL)

        when:
        sync([assignment('a', RESOURCE)])

        then:
        sources('a', RESOURCE) == [RoleAssignmentSource.MANUAL, RoleAssignmentSource.OIDC]
    }

    def "nothing asserted"() {
        given:
        row('a', RESOURCE, RoleAssignmentSource.OIDC)
        row('b', RESOURCE, null)
        row('c', RESOURCE, RoleAssignmentSource.MANUAL)

        when:
        def authorities = sync([])

        then:
        allRows()*.roleCode == ['c']
        authorities*.authority == [resourceAuthority('c')]
    }

    def "returns the authorities of manual rows"() {
        given:
        row('a', RESOURCE, RoleAssignmentSource.MANUAL)
        row('b', ROW_LEVEL, RoleAssignmentSource.MANUAL)
        row('c', RESOURCE, RoleAssignmentSource.OIDC)
        row('d', RESOURCE, null)
        row('e', 'custom', RoleAssignmentSource.MANUAL)
        def expected = [resourceAuthority('a'), rowLevelAuthority('b')] as Set

        when:
        def authorities = sync([assignment('c', RESOURCE)])

        then:
        authorities*.authority as Set == expected

        when:
        def rowsBefore = allRows().collectEntries { [it.roleCode, it.source] }
        def manualAuthorities = synchronizer.getManualAuthorities('alice')

        then:
        manualAuthorities*.authority as Set == expected
        allRows().collectEntries { [it.roleCode, it.source] } == rowsBefore
    }

    def "the synchronizer does not save"() {
        given:
        row('a', RESOURCE, RoleAssignmentSource.OIDC)

        when:
        synchronizer.synchronize('alice', [], RoleAssignmentSource.OIDC, new SaveContext())

        then:
        sources('a', RESOURCE) == [RoleAssignmentSource.OIDC]
    }

    def "the deletion mode is left to the caller"() {
        given:
        row('a', RESOURCE, RoleAssignmentSource.OIDC)
        def saveContext = new SaveContext()

        when:
        synchronizer.synchronize('alice', [], RoleAssignmentSource.OIDC, saveContext)
        dataManager.save(saveContext)

        then:
        !saveContext.hints.containsKey(PersistenceHints.SOFT_DELETION)
        sources('a', RESOURCE) == []
        jdbcTemplate.queryForObject(
                "select count(*) from SEC_ROLE_ASSIGNMENT where DELETE_TS is not null", Integer) == 1
    }

    def "an unknown user has no manual authorities"() {
        expect:
        synchronizer.getManualAuthorities('nobody').isEmpty()
    }

    def "the own source must be an add-on's: #source"() {
        when:
        synchronizer.synchronize('alice', [], source, new SaveContext())

        then:
        thrown(IllegalArgumentException)

        where:
        source << [RoleAssignmentSource.MANUAL, null]
    }

    def "an asserted role needs a code and a type: #roleCode, #roleType"() {
        given:
        row('a', RESOURCE, RoleAssignmentSource.OIDC)
        def saveContext = new SaveContext()

        when:
        synchronizer.synchronize('alice', [assignment(roleCode, roleType)], RoleAssignmentSource.OIDC, saveContext)

        then:
        thrown(IllegalArgumentException)
        saveContext.entitiesToSave.isEmpty()
        saveContext.entitiesToRemove.isEmpty()

        where:
        roleCode | roleType
        null     | RESOURCE
        'b'      | null
    }

    def "a manual row of a custom role type is kept but grants no authority"() {
        given:
        row('a', 'custom', RoleAssignmentSource.MANUAL)

        when:
        def authorities = sync([])

        then:
        sources('a', 'custom') == [RoleAssignmentSource.MANUAL]
        authorities.isEmpty()
    }

    RoleAssignmentEntity row(String roleCode, String roleType, String source) {
        def entity = dataManager.create(RoleAssignmentEntity)
        entity.username = 'alice'
        entity.roleCode = roleCode
        entity.roleType = roleType
        entity.source = source
        dataManager.save(entity)
    }

    static RoleAssignment assignment(String roleCode, String roleType) {
        new RoleAssignment('alice', roleCode, roleType)
    }

    Collection<GrantedAuthority> sync(List<RoleAssignment> asserted) {
        def saveContext = new SaveContext()
        def authorities = synchronizer.synchronize('alice', asserted, RoleAssignmentSource.OIDC, saveContext)
        saveContext.setHint(PersistenceHints.SOFT_DELETION, false)
        dataManager.save(saveContext)
        authorities
    }

    List<RoleAssignmentEntity> rows(String roleCode, String roleType) {
        dataManager.load(RoleAssignmentEntity)
                .query("e.username = 'alice' and e.roleCode = :roleCode and e.roleType = :roleType")
                .parameter("roleCode", roleCode)
                .parameter("roleType", roleType)
                .list()
    }

    List<String> sources(String roleCode, String roleType) {
        rows(roleCode, roleType)*.source.sort()
    }

    List<RoleAssignmentEntity> allRows() {
        dataManager.load(RoleAssignmentEntity)
                .query("e.username = 'alice'")
                .list()
    }

    String resourceAuthority(String roleCode) {
        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(roleCode).authority
    }

    String rowLevelAuthority(String roleCode) {
        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(roleCode).authority
    }
}
