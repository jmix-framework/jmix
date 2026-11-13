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

package user_substitution_persistence

import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.usersubstitution.UserSubstitutionModel
import io.jmix.security.usersubstitution.UserSubstitutionPersistence
import io.jmix.securitydata.entity.UserSubstitutionEntity
import jakarta.persistence.OptimisticLockException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.role.TestFullAccessRole

import java.text.SimpleDateFormat

class DatabaseUserSubstitutionPersistenceTest extends SecurityDataSpecification {

    @Autowired
    UserSubstitutionPersistence persistence
    @Autowired
    UnconstrainedDataManager dataManager
    @Autowired
    Metadata metadata
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    AuthenticationManager authenticationManager
    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")

    UserDetails admin
    Authentication systemAuthentication

    def setup() {
        systemAuthentication = SecurityContextHelper.getAuthentication()

        admin = User.builder()
                .username("admin")
                .password("{noop}123")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestFullAccessRole.CODE))
                .build()
        userRepository.addUser(admin)

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(admin.username, '123'))
        SecurityContextHelper.setAuthentication(authentication)
    }

    def cleanup() {
        jdbcTemplate.execute('delete from SEC_USER_SUBSTITUTION')
        SecurityContextHelper.setAuthentication(systemAuthentication)
        userRepository.removeUser(admin)
    }

    def "a new record is saved"() {
        def model = metadata.create(UserSubstitutionModel)
        model.username = 'user1'
        model.substitutedUsername = 'user2'
        model.startDate = sdf.parse('2026-01-01')

        when:
        def saved = persistence.save(model)

        then:
        def entity = dataManager.load(UserSubstitutionEntity).id(saved.id).one()
        entity.username == 'user1'
        entity.substitutedUsername == 'user2'
        entity.startDate == sdf.parse('2026-01-01')
        entity.endDate == null
    }

    def "clearing the start date of a stored record is saved"() {
        def id = createRecord()
        def model = persistence.load(id)

        when:
        model.startDate = null
        persistence.save(model)

        then:
        def entity = dataManager.load(UserSubstitutionEntity).id(id).one()
        entity.startDate == null
        entity.endDate == sdf.parse('2026-12-31')
    }

    def "clearing the end date of a stored record is saved"() {
        def id = createRecord()
        def model = persistence.load(id)

        when:
        model.endDate = null
        persistence.save(model)

        then:
        def entity = dataManager.load(UserSubstitutionEntity).id(id).one()
        entity.startDate == sdf.parse('2026-01-01')
        entity.endDate == null
    }

    def "changing the dates of a stored record keeps its other attributes"() {
        def id = createRecord()
        def created = dataManager.load(UserSubstitutionEntity).id(id).one()
        def model = persistence.load(id)

        when:
        model.startDate = sdf.parse('2026-02-01')
        model.endDate = sdf.parse('2026-06-30')
        def saved = persistence.save(model)

        then:
        def entity = dataManager.load(UserSubstitutionEntity).id(id).one()
        entity.startDate == sdf.parse('2026-02-01')
        entity.endDate == sdf.parse('2026-06-30')
        entity.username == 'user1'
        entity.substitutedUsername == 'user2'
        entity.createTs == created.createTs
        entity.createdBy == created.createdBy
        saved.version == created.version + 1
    }

    def "saving a record changed since it was loaded fails"() {
        def id = createRecord()
        def first = persistence.load(id)
        def second = persistence.load(id)

        first.endDate = sdf.parse('2026-06-30')
        persistence.save(first)

        when:
        second.startDate = null
        persistence.save(second)

        then:
        thrown(OptimisticLockException)
        def entity = dataManager.load(UserSubstitutionEntity).id(id).one()
        entity.startDate == sdf.parse('2026-01-01')
        entity.endDate == sdf.parse('2026-06-30')
    }

    def "saving a record removed since it was loaded fails"() {
        def id = createRecord()
        def model = persistence.load(id)
        persistence.remove([persistence.load(id)])

        when:
        model.startDate = null
        persistence.save(model)

        then:
        thrown(OptimisticLockException)
        jdbcTemplate.queryForObject('select count(*) from SEC_USER_SUBSTITUTION', Integer) == 1
        jdbcTemplate.queryForObject('select DELETE_TS from SEC_USER_SUBSTITUTION where ID = ?', Date, id.toString()) != null
    }

    def "removed records are no longer loaded"() {
        def removedId = createRecord()
        def keptId = createRecord()
        def otherUserId = createRecord('user3')

        when:
        persistence.remove(persistence.loadSubstitutionsOf('user1').findAll { it.id == removedId })

        then:
        persistence.loadSubstitutionsOf('user1')*.id == [keptId]
        persistence.loadSubstitutionsOf('user3')*.id == [otherUserId]
        jdbcTemplate.queryForObject('select DELETE_TS from SEC_USER_SUBSTITUTION where ID = ?', Date, removedId.toString()) != null
    }

    UUID createRecord(String username = 'user1') {
        def entity = dataManager.create(UserSubstitutionEntity)
        entity.username = username
        entity.substitutedUsername = 'user2'
        entity.startDate = sdf.parse('2026-01-01')
        entity.endDate = sdf.parse('2026-12-31')
        dataManager.save(entity).id
    }
}
