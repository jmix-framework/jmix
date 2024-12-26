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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.entity.cache.CacheableEntity

class CachedQueryTest extends DataSpec {

    @Autowired
    TransactionTemplate transaction;

    @Autowired
    DataManager dataManager

    def "test query with null param"() {
        setup:
        generateEntities(10)

        when:
        def entities = dataManager.load(CacheableEntity)
                .query('e.name = :name')
                .parameter('name', null)
                .cacheable(true)
                .list()

        then:
        entities.isEmpty()

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
