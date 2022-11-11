/*
 * Copyright (c) 2008-2017 Haulmont.
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

import org.springframework.beans.factory.annotation.Autowired
import test_support.testmodel.UuidEntity
import test_support.testmodel.UuidEntityChangedEventListener

class EntityLogAndEntityChangedEventTest extends AbstractEntityLogTest {

    private UUID entityId

    @Autowired
    protected UuidEntityChangedEventListener uuidEntityChangedEventListener

    void setup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")

        withTransaction {
            clearTable("AUDIT_ENTITY_LOG")
            initEntityLogConfiguration()
        }

        initEntityLogAPI()

        uuidEntityChangedEventListener.enabled = true
    }

    protected void initEntityLogConfiguration() {
        saveEntityLogAutoConfFor('test_UuidEntity', 'name', 'description')
    }

    void cleanup() {
        uuidEntityChangedEventListener.enabled = false

        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")
    }


    def "entity log saving for when entity is changed in EntityChangedEvent listener"() {
        given:

        withTransaction {
            UuidEntity entity = metadata.create(UuidEntity)

            entityId = entity.id
            entity.name = 'Entity#1'

            em.persist(entity)
        }

        and:

        getEntityLogItems('test_UuidEntity', entityId).size() == 1
        def logItem = getLatestEntityLogItem('test_UuidEntity', entityId)

        loggedValueMatches(logItem, 'name', 'Entity#1')
        loggedValueMatches(logItem, 'description', 'Entity#1')
    }
}
