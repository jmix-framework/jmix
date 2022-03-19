/*
 * Copyright 2022 Haulmont.
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

package events

import io.jmix.core.DataManager
import io.jmix.core.Id
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.chained_entity_listeners.ChainedUpdateEntityOne
import test_support.entity.chained_entity_listeners.ChainedUpdateEntityThree
import test_support.entity.chained_entity_listeners.ChainedUpdateEntityTwo

class EntityChangedEventChainingTest extends DataSpec {

    @Autowired
    private DataManager dataManager
    @Autowired
    JdbcTemplate jdbc

    private ChainedUpdateEntityOne entityOne
    private ChainedUpdateEntityTwo entityTwo
    private ChainedUpdateEntityThree entityThree

    @Override
    void setup() {
        entityOne = dataManager.create(ChainedUpdateEntityOne)

        entityTwo = dataManager.create(ChainedUpdateEntityTwo)
        entityTwo.entityOne = entityOne

        entityThree = dataManager.create(ChainedUpdateEntityThree)
        entityThree.entityTwo = entityTwo

        dataManager.save(entityOne, entityTwo, entityThree)
    }

    void cleanup() {
        jdbc.update('delete from TEST_CHAINEDUPDATEENTITYTHREE')
        jdbc.update('delete from TEST_CHAINEDUPDATEENTITYTWO')
        jdbc.update('delete from TEST_CHAINEDUPDATEENTITYONE')
    }

    def "events are chained - update of entityOne is propagated to entityThree"() {
        when:
        def entity1 = dataManager.load(Id.of(entityOne)).one()
        entity1.setAmount(100)
        dataManager.save(entity1)

        def entity3 = dataManager.load(Id.of(entityThree)).one()

        then:
        entity3.amount == 100
    }

}
