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

package in_memory_row_level_policy

import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.core.Stores
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.entity.EntitySystemAccess
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import io.jmix.data.StoreAwareLocator
import io.jmix.security.role.RoleGrantedAuthorityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import test_support.SecurityDataSpecification
import test_support.entity.ManyToOneEntity
import test_support.entity.OneToManyEntity
import test_support.role.TestInMemoryRowLevelPagingRole

import javax.sql.DataSource

import static test_support.role.TestInMemoryRowLevelPagingRole.PERMITTED_SUFFIX

class InMemoryRowLevelPolicyPagingTest extends SecurityDataSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    UnconstrainedDataManager unconstrainedDataManager

    @Autowired
    SystemAuthenticator systemAuthenticator

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    @Autowired
    DataSource dataSource

    @Autowired
    PlatformTransactionManager transactionManager

    @Autowired
    StoreAwareLocator storeAwareLocator

    UserDetails user

    def setup() {
        user = User.builder()
                .username("user1")
                .password("{noop}123")
                .authorities(
                        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestInMemoryRowLevelPagingRole.NAME),
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(TestInMemoryRowLevelPagingRole.NAME)
                )
                .build()
        userRepository.addUser(user)

        def saveContext = new SaveContext()

        def forbiddenReference = unconstrainedDataManager.create(OneToManyEntity)
        forbiddenReference.name = 'forbidden'
        saveContext.saving(forbiddenReference)

        // Only rows 000, 098 and 099 of 100 are permitted. A page of 2 is filled from several additional
        // queries: 000 comes from the first one, 098 and 099 from the last one, and 099 is not returned.
        for (int i = 0; i < 100; i++) {
            def entity = unconstrainedDataManager.create(ManyToOneEntity)
            entity.name = String.format('%03d', i) + (i in [0, 98, 99] ? PERMITTED_SUFFIX : '')
            entity.oneToManyEntity = forbiddenReference
            saveContext.saving(entity)
        }

        unconstrainedDataManager.save(saveContext)
    }

    def cleanup() {
        userRepository.removeUser(user)

        new JdbcTemplate(dataSource).execute('delete from TEST_MANY_TO_ONE_ENTITY;' +
                ' delete from TEST_ONE_TO_MANY_ENTITY;')
    }

    def "paged load erases forbidden references in entities from all additional queries"() {
        when:
        List<ManyToOneEntity> entities = systemAuthenticator.withUser('user1') {
            loadFirstPage()
        }

        then:
        entities*.name == ['000' + PERMITTED_SUFFIX, '098' + PERMITTED_SUFFIX]
        entities.every { it.oneToManyEntity == null }
    }

    def "paged load does not erase references in entities that are not returned"() {
        given:
        def notReturnedId = unconstrainedDataManager.load(ManyToOneEntity)
                .query('select e from test_ManyToOneEntity e where e.name = :name')
                .parameter('name', '099' + PERMITTED_SUFFIX)
                .one()
                .id

        when: "the load joins a transaction, so the entities that are not returned stay in its persistence context"
        Collection<String> erasedAttributes = systemAuthenticator.withUser('user1') {
            new TransactionTemplate(transactionManager).execute {
                loadFirstPage()
                def notReturned = storeAwareLocator.getEntityManager(Stores.MAIN).find(ManyToOneEntity, notReturnedId)
                new ArrayList<>(EntitySystemAccess.getSecurityState(notReturned).erasedAttributes)
            }
        }

        then:
        erasedAttributes.isEmpty()
    }

    private List<ManyToOneEntity> loadFirstPage() {
        dataManager.load(ManyToOneEntity)
                .query('select e from test_ManyToOneEntity e order by e.name')
                .maxResults(2)
                .fetchPlanProperties('name', 'oneToManyEntity.name')
                .list()
    }
}
