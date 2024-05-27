/*
 * Copyright 2024 Haulmont.
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

package cache

import io.jmix.core.DataManager
import io.jmix.data.PersistenceHints
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.entity.cache.CacheableEntity

class MiscCacheTest extends DataSpec {
    public static final String ALL_QUERY = "select e from test_CacheableEntity e where e.name like concat(:name,'%')"

    @Autowired
    DataManager dataManager

    @PersistenceContext
    EntityManager em

    @Autowired
    private TransactionTemplate transaction;

    def "check soft deletion considered"() {
        setup:
        generateEntities(10)

        when: "loading first time"

        def firstQueryResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY)
                .parameter("name", "test_")
                .hints(Map.of(PersistenceHints.SOFT_DELETION, false))
                .cacheable(true)
                .list();

        then: "entities loaded correctly"
        firstQueryResult.size() == 10

        when:
        "Imagine that entity soft deleted after id list got from query cache, but before entities loaded by ids and entity cache is empty" +
                "(e.g. because it is second cluster node: query cache fully synced, entity cache - only invalidated when need " +
                "or because entity cache invalidated after id list already got from query cache)"
        UUID deletedEntityId = firstQueryResult[0].id
        jdbc.update("update TEST_CACHEABLE_ENTITY set DELETED_DATE=NOW(), DELETED_BY='test' where ID='" + deletedEntityId + "'")
        em.getEntityManagerFactory().getCache().evictAll();

        def loadedWithoutSoftDeletion
        def loaded

        transaction.executeWithoutResult {

            loadedWithoutSoftDeletion = dataManager.load(CacheableEntity)
                    .query(ALL_QUERY)
                    .parameter("name", "test_")
                    .cacheable(true)
                    .hints(Map.of(PersistenceHints.SOFT_DELETION, false))
                    .list()

            em.getEntityManagerFactory().getCache().evictAll();

            loaded = dataManager.load(CacheableEntity)
                    .query(ALL_QUERY)
                    .parameter("name", "test_")
                    .cacheable(true)
                    .list()

        }

        then:
        "Soft deleted entity is not is not loaded if no appropriate hint passed"
        loaded.size() == 9
        loaded.stream().filter { it -> (it.id == deletedEntityId) }.findAny().isEmpty()

        loadedWithoutSoftDeletion.size() == 10
        loadedWithoutSoftDeletion.stream().filter { it -> (it.id == deletedEntityId) }.findAny().isPresent()

        cleanup:
        dropAllEntities()
    }

    def "Check order preserved"() {
        setup:
        generateEntities(10)

        when:
        def firstNameSortedResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY + "order by e.name")
                .parameter("name", "test_")
                .cacheable(true)
                .list()

        def firstNoteSortedResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY + "order by e.note")
                .parameter("name", "test_")
                .cacheable(true)
                .list()

        em.getEntityManagerFactory().getCache().evictAll();
        def secondNameSortedResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY + "order by e.name")
                .parameter("name", "test_")
                .cacheable(true)
                .list()

        em.getEntityManagerFactory().getCache().evictAll();
        def secondNoteSortedResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY + "order by e.note")
                .parameter("name", "test_")
                .cacheable(true)
                .list()

        then:
        secondNameSortedResult[0].name == "test_0000"
        secondNameSortedResult[1].name == "test_0001"
        secondNameSortedResult[2].name == "test_0002"
        secondNameSortedResult[3].name == "test_0003"
        secondNameSortedResult[4].name == "test_0004"
        secondNameSortedResult[5].name == "test_0005"
        secondNameSortedResult[6].name == "test_0006"
        secondNameSortedResult[7].name == "test_0007"
        secondNameSortedResult[8].name == "test_0008"
        secondNameSortedResult[9].name == "test_0009"

        secondNoteSortedResult[0].note == "note_0001"
        secondNoteSortedResult[1].note == "note_0002"
        secondNoteSortedResult[2].note == "note_0003"
        secondNoteSortedResult[3].note == "note_0004"
        secondNoteSortedResult[4].note == "note_0005"
        secondNoteSortedResult[5].note == "note_0006"
        secondNoteSortedResult[6].note == "note_0007"
        secondNoteSortedResult[7].note == "note_0008"
        secondNoteSortedResult[8].note == "note_0009"
        secondNoteSortedResult[9].note == "note_0010"

        cleanup:
        dropAllEntities()
    }

    def "check large query result works"() {
        setup:
        generateEntities(3210)

        when:
        def firstQueryResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY + " order by e.name")
                .parameter("name", "test_")
                .hints(Map.of(PersistenceHints.SOFT_DELETION, false))
                .cacheable(true)
                .list();

        def secondQueryResult = dataManager.load(CacheableEntity)
                .query(ALL_QUERY + " order by e.name")
                .parameter("name", "test_")
                .hints(Map.of(PersistenceHints.SOFT_DELETION, false))
                .cacheable(true)
                .list();

        then:
        noExceptionThrown()
        secondQueryResult.size() == 3210
        secondQueryResult[911].name == "test_0911"
        secondQueryResult[1234].name == "test_1234"
        secondQueryResult[2077].name == "test_2077"
        secondQueryResult[2049].name == "test_2049"
        secondQueryResult[3111].name == "test_3111"

        cleanup:
        dropAllEntities()
    }

    def generateEntities(int count) {
        for (int i = 0; i < count; i++) {
            jdbc.update(String.format(
                    "insert into TEST_CACHEABLE_ENTITY(ID, NAME, NOTE, VERSION, CREATED_BY, LAST_MODIFIED_BY) " +
                            "values('%s', 'test_%04d', 'note_%04d', 1, 'test','test')",
                    UUID.randomUUID(),
                    i,
                    count - i));
        }

    }

    def dropAllEntities() {
        jdbc.update("delete from TEST_CACHEABLE_ENTITY")
    }
}
