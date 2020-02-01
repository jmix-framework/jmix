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

package persistence

import io.jmix.data.Persistence
import io.jmix.data.Transaction
import test_support.entity.TestAppEntity
import test_support.DataSpec

import javax.inject.Inject

class PersistenceTest extends DataSpec {

    @Inject
    Persistence persistence

    def "dataSource is initialized"() {
        expect:
        persistence.getDataSource() != null
    }

    def "create and commit transaction"() {
        when:

        Transaction tx = persistence.createTransaction()
        try {

            tx.commit()
        } finally {
            tx.end()
        }

        then:

        noExceptionThrown()
    }

    def "persist and load entity"() {

        def entity = new TestAppEntity(name: 'test1')

        when:

        Transaction tx = persistence.createTransaction()
        try {
            def entityManager = persistence.getEntityManager()
            entityManager.persist(entity)
            tx.commit()
        } finally {
            tx.end()
        }

        def foundEntity

        tx = persistence.createTransaction()
        try {
            def entityManager = persistence.getEntityManager()
            foundEntity = entityManager.find(TestAppEntity, entity.id)
            tx.commit()
        } finally {
            tx.end()
        }

        then:

        foundEntity != null
        foundEntity.version > 0
    }
}
