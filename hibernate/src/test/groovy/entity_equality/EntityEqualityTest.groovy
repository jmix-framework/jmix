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

package entity_equality

import io.jmix.core.entity.EntityValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.entity.equality.GFoo
import test_support.entity.equality.NFoo
import test_support.entity.equality.NGFoo

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class EntityEqualityTest extends DataSpec {

    @PersistenceContext
    EntityManager entityManager

    @Autowired
    TransactionTemplate transaction

    def "entity equality across states"(Class entityClass) {
        Set set = new HashSet<>()
        Object entity = createEntity(entityClass)

        when:
        set.add(entity)
        then:
        set.contains(entity)

        when:
        transaction.executeWithoutResult {
            entityManager.persist(entity)
        }
        then:
        set.contains(entity)

        when:
        def loadedEntity = transaction.execute {
            entityManager.find(entityClass, EntityValues.getId(entity))
        }
        then:
        set.contains(loadedEntity)
        loadedEntity == entity

        when:
        def mergedEntity = transaction.execute {
            entityManager.merge(loadedEntity)
        }
        then:
        set.contains(mergedEntity)
        mergedEntity == entity

        when:
        def removedEntity = transaction.execute {
            entityManager.remove(loadedEntity)
            loadedEntity
        }
        then:
        set.contains(removedEntity)
        removedEntity == entity

        where:
        entityClass << [GFoo, NGFoo, NFoo]

    }

    Object createEntity(Class entityClass) {
        def entity = entityClass.newInstance()
        entity.name = 'foo'
        entity
    }
}
