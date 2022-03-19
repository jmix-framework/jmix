/*
 * Copyright (c) 2008-2019 Haulmont.
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
package spec.haulmont.cuba.core

import com.haulmont.cuba.core.model.selfinherited.ChildEntity
import com.haulmont.cuba.core.model.selfinherited.ChildEntityDetail
import com.haulmont.cuba.core.model.selfinherited.ChildEntityReferrer
import com.haulmont.cuba.core.global.DataManager
import io.jmix.core.Metadata
import com.haulmont.cuba.core.Persistence
import org.springframework.jdbc.core.JdbcTemplate

import org.springframework.beans.factory.annotation.Autowired

class JoinedInheritanceTestClass extends CoreTestSpecification {
    @Autowired
    private Persistence persistence
    @Autowired
    private Metadata metadata
    @Autowired
    private DataManager dataManager


    void cleanup() {
        def jdbcTemplate = new JdbcTemplate(persistence.dataSource)
        jdbcTemplate.update('delete from TEST_CHILD_ENTITY_DETAIL')
        jdbcTemplate.update('delete from TEST_ROOT_ENTITY_DETAIL')
        jdbcTemplate.update('delete from TEST_CHILD_ENTITY_REFERRER')
        jdbcTemplate.update('delete from TEST_CHILD_ENTITY')
        jdbcTemplate.update('delete from TEST_ROOT_ENTITY')
    }

    def "store master-detail"() {
        when:
        persistence.runInTransaction({ em ->
            ChildEntity childEntity = metadata.create(ChildEntity)
            childEntity.name = 'name'
            childEntity.description = 'description'
            em.persist(childEntity)

            ChildEntityDetail childEntityDetail = metadata.create(ChildEntityDetail)
            childEntityDetail.childEntity = childEntity
            childEntityDetail.info = 'info'
            em.persist(childEntityDetail)
        })

        then:
        noExceptionThrown()
    }

    def "store root-joined-inheritance-and-referer"() {
        when:
        persistence.runInTransaction({ em ->
            ChildEntity childEntity = metadata.create(ChildEntity)
            childEntity.name = 'name'
            childEntity.description = 'description'
            em.persist(childEntity)

            ChildEntityReferrer childEntityReferrer = metadata.create(ChildEntityReferrer)
            childEntityReferrer.childEntity = childEntity
            childEntityReferrer.info = 'info'
            em.persist(childEntityReferrer)
        })

        then:
        noExceptionThrown()
    }
}
