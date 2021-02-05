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

import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestAppEntity

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class PersistenceTest extends DataSpec {

    @PersistenceContext
    EntityManager entityManager
    @Autowired
    Metadata metadata

    def "persist and load entity"() {

        def entity = metadata.create(TestAppEntity)
        entity.name = 'test1'

        when:

        transaction.executeWithoutResult {
            entityManager.persist(entity)
        }

        def foundEntity = transaction.execute {
            return entityManager.find(TestAppEntity, entity.id)
        }

        then:

        foundEntity != null
        foundEntity.version > 0
    }
}
