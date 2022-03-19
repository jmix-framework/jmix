/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.tx_listeners

import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.testsupport.TestSupport
import com.haulmont.cuba.core.tx_listener.TestBeforeCommitTxListener
import io.jmix.core.Metadata
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Transaction
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired


class UsingTransactionsInBeforeCommitTest extends CoreTestSpecification {
    @Autowired
    Metadata metadata
    @Autowired
    Persistence persistence
    @Autowired
    TestSupport testSupport

    private Group companyGroup

    void setup() {
        companyGroup = persistence.callInTransaction { em ->
            Group group = new Group(name: 'Company')
            em.persist(group)
            return group
        }
    }

    void cleanup() {
        def jdbcTemplate = new JdbcTemplate(persistence.getDataSource())
        jdbcTemplate.update("delete from TEST_USER_ROLE")
        jdbcTemplate.update("delete from TEST_USER")
        testSupport.deleteRecord(companyGroup)
    }

    def "create entity in new transaction"() {
        TestBeforeCommitTxListener.test = "testCreateEntityInNewTransaction"

        User u = metadata.create(User)
        u.setLogin("u-$u.id")
        u.setGroup(companyGroup)

        when:

        Transaction tx = persistence.createTransaction()
        try {
            persistence.getEntityManager().persist(u)
            tx.commit()
        } finally {
            tx.end()
            TestBeforeCommitTxListener.test = null
        }

        then:

        persistence.callInTransaction { em -> em.find(User, TestBeforeCommitTxListener.createdEntityId) } != null
    }

    def "create entity in same transaction"() {
        TestBeforeCommitTxListener.test = "testCreateEntityInSameTransaction"

        User u = metadata.create(User)
        u.setLogin("u-$u.id")
        u.setGroup(companyGroup)

        when:

        Transaction tx = persistence.createTransaction()
        try {
            persistence.getEntityManager().persist(u)
            tx.commit()
        } finally {
            tx.end()
            TestBeforeCommitTxListener.test = null
        }

        then:

        persistence.callInTransaction { em -> em.find(User, TestBeforeCommitTxListener.createdEntityId) } != null
    }

    def "create entity in new transaction and rollback"() {
        TestBeforeCommitTxListener.test = "testCreateEntityInNewTransactionAndRollback"

        User u = metadata.create(User)
        u.setLogin("u-$u.id")
        u.setGroup(companyGroup)

        when:

        Transaction tx = persistence.createTransaction()
        try {
            persistence.getEntityManager().persist(u)
            tx.commit()
        } finally {
            tx.end()
            TestBeforeCommitTxListener.test = null
        }

        then:

        def e = thrown(RuntimeException)
        e.message == 'some error'

        persistence.callInTransaction { em -> em.find(User, TestBeforeCommitTxListener.createdEntityId) } != null
    }

    def "create entity in same transaction and rollback"() {
        TestBeforeCommitTxListener.test = "testCreateEntityInSameTransactionAndRollback"

        User u = metadata.create(User)
        u.setLogin("u-$u.id")
        u.setGroup(companyGroup)

        when:

        Transaction tx = persistence.createTransaction()
        try {
            persistence.getEntityManager().persist(u)
            tx.commit()
        } finally {
            tx.end()
            TestBeforeCommitTxListener.test = null
        }

        then: "entity was not saved"

        def e = thrown(RuntimeException)
        e.message == 'some error'

        persistence.callInTransaction { em -> em.find(User, TestBeforeCommitTxListener.createdEntityId) } == null
    }
}
