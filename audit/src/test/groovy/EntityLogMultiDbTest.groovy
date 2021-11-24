import io.jmix.audit.entity.EntityLogItem
import io.jmix.core.DataManager
import io.jmix.core.EntitySet
import io.jmix.core.security.SystemAuthenticator
import io.jmix.data.StoreAwareLocator
import org.springframework.beans.factory.annotation.Autowired
import test_support.testmodel.Db1Entity
import test_support.testmodel.UuidEntity

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

class EntityLogMultiDbTest extends AbstractEntityLogTest {
    @Autowired
    DataManager dataManager
    @Autowired
    SystemAuthenticator authenticator
    @Autowired
    StoreAwareLocator locator


    void setup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")

        withTransaction {
            clearTable(em, "AUDIT_ENTITY_LOG")
            initEntityLogConfiguration()
        }

        initEntityLogAPI()
    }

    protected void initEntityLogConfiguration() {
        saveEntityLogAutoConfFor('test_UuidEntity', 'name', 'db1EntityId', 'db1Entity')

        saveEntityLogAutoConfFor('test_Db1Entity', 'name')
    }

    void cleanup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")
    }


    def "Logging is working for CREATE operation"() {

        given:

        authenticator.begin()

        def db1Entity = metadata.create(Db1Entity)
        db1Entity.name = 'test1'

        def uuidEntity = metadata.create(UuidEntity)
        uuidEntity.name = 'test2'
        uuidEntity.db1Entity = db1Entity

        when:

        EntitySet entities = saveEntities(uuidEntity, db1Entity)
        Db1Entity reloadedDb1Entity = (Db1Entity) entities.stream()
                .filter({ e -> e instanceof Db1Entity })
                .findFirst().orElse(null)

        then:

        def entityLogItems = getEntityLogItems('test_UuidEntity', uuidEntity.id)

        entityLogItems.size() == 2

        loggedValueMatches(entityLogItems[1], 'name', 'test2')
        loggedOldValueMatches(entityLogItems[1], 'name', null)
        loggedValueMatches(entityLogItems[1], 'db1Entity', 'test1')
        entityLogItems[1].type == EntityLogItem.Type.CREATE

        loggedValueMatches(entityLogItems[0], 'db1EntityId', reloadedDb1Entity.id.toString())
        entityLogItems[0].type == EntityLogItem.Type.MODIFY

        cleanup:

        clearTable('TEST_UUID_ENTITY')
        authenticator.end()
    }

    def "Logging is working for UPDATE operation"() {

        given:

        authenticator.begin()

        def db1Entity = metadata.create(Db1Entity)
        db1Entity.name = 'test1'

        def uuidEntity = metadata.create(UuidEntity)
        uuidEntity.name = 'test2'
        uuidEntity.db1Entity = db1Entity

        when:

        EntitySet entities = saveEntities(uuidEntity, db1Entity)

        Db1Entity reloadedDb1Entity1 = (Db1Entity) entities.stream()
                .filter({ e -> e instanceof Db1Entity })
                .findFirst().orElse(null)

        db1Entity = metadata.create(Db1Entity)
        db1Entity.name = 'test3'

        uuidEntity = (UuidEntity) entities.stream()
                .filter({ e -> e instanceof UuidEntity })
                .findFirst().orElse(null)

        uuidEntity.setDb1Entity(db1Entity)

        entities = saveEntities(uuidEntity, db1Entity)

        Db1Entity reloadedDb1Entity2 = (Db1Entity) entities.stream()
                .filter({ e -> e instanceof Db1Entity })
                .findFirst().orElse(null)

        then:

        def entityLogItems = getEntityLogItems('test_UuidEntity', uuidEntity.id)

        entityLogItems.size() == 3

        loggedValueMatches(entityLogItems[0], 'db1EntityId', reloadedDb1Entity2.id.toString())
        loggedOldValueMatches(entityLogItems[0], 'db1EntityId', reloadedDb1Entity1.id.toString())
        entityLogItems[0].type == EntityLogItem.Type.MODIFY

        cleanup:
        locator.getJdbcTemplate('db1').update('delete from TEST_DB1_ENTITY')
        clearTable('TEST_UUID_ENTITY')
        authenticator.end()
    }

    def "Additional datastore entity update logged"() {
        given:
        authenticator.begin()
        def db1Entity = metadata.create(Db1Entity)
        db1Entity.name = 'test1'
        saveEntities(db1Entity)

        when:
        db1Entity.name = "test2"
        saveEntities(db1Entity)

        then:
        def entityLogItems = getEntityLogItems('test_Db1Entity', db1Entity.id)
        entityLogItems.size() == 2
        entityLogItems[1].type == EntityLogItem.Type.CREATE
        entityLogItems[0].type == EntityLogItem.Type.MODIFY
        entityLogItems[0].changes.contains("name=test2")
        entityLogItems[0].changes.contains("name-oldVl=test1")

        cleanup:
        locator.getJdbcTemplate('db1').update('delete from TEST_DB1_ENTITY')
        authenticator.end()
    }

    protected EntitySet saveEntities(Object... entities) {
        return dataManager.save(entities)
    }
}
