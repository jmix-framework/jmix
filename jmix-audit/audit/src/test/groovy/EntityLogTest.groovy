import io.jmix.audit.entity.EntityLogItem
import io.jmix.core.DataManager
import io.jmix.core.Entity
import io.jmix.core.entity.EntityValues
import io.jmix.core.event.AttributeChanges
import io.jmix.data.AttributeChangesProvider
import org.springframework.beans.factory.annotation.Autowired
import test_support.testmodel.IdentityEntity
import test_support.testmodel.IntIdentityEntity
import test_support.testmodel.StringKeyEntity
import test_support.testmodel.cascade.CascOrder
import test_support.testmodel.cascade.CascOrderReview

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

class EntityLogTest extends AbstractEntityLogTest {


    @Autowired
    DataManager dataManager;

    @Autowired
    protected AttributeChangesProvider attributeChangesProvider

    void setup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")

        withTransaction {
            clearTable(em, "AUDIT_ENTITY_LOG")
            initEntityLogConfiguration()
        }

        initEntityLogAPI()
    }

    protected void initEntityLogConfiguration() {

        saveEntityLogAutoConfFor('test$IntIdentityEntity', 'name')

        saveEntityLogAutoConfFor('test$IdentityEntity', 'name')

        saveEntityLogAutoConfFor('cascop_Order', '*')
        saveEntityLogAutoConfFor('cascop_OrderReview', '*')

        saveManualEntityLogAutoConfFor('test$StringKeyEntity', 'name', 'description')
    }


    void cleanup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")
    }


    def "Logging is working for a creation of a BaseIdentityIdEntity"() {

        given:

        def identityEntity = new IdentityEntity(name: 'test1')

        when:

        saveEntity(identityEntity)

        then:

        def entityLogItem = getLatestEntityLogItem('test$IdentityEntity', identityEntity)

        loggedValueMatches(entityLogItem, 'name', 'test1')
        loggedOldValueMatches(entityLogItem, 'name', null)

        cleanup:

        clearEntityById(identityEntity, 'TEST_IDENTITY')
    }

    def "No 'null' log items created"() {

        given:

        IdentityEntity identityEntity = new IdentityEntity(name: 'test1')

        when: "not logged attribute changed"

        saveEntity(identityEntity)

        withTransaction {
            IdentityEntity e = em.find(IdentityEntity, identityEntity.id)
            e.email = "testEmail"
        }

        def entityLogItem = getLatestEntityLogItem('test$IdentityEntity', identityEntity)

        then: "No exception and no new log item"
        noExceptionThrown()
        entityLogItem != null
        entityLogItem.type == EntityLogItem.Type.CREATE//no additional log item created


        cleanup:

        clearEntityById(identityEntity, 'TEST_IDENTITY')
    }


    def "Logging is working for an update of a BaseIdentityIdEntity"() {

        given:

        def identityEntity = new IdentityEntity(name: 'test1')

        and:

        saveEntity(identityEntity)

        when:

        findEntityAndUpdateNameInTransaction(identityEntity, 'test2')

        then:

        def logItem = getLatestEntityLogItem('test$IdentityEntity', identityEntity)

        loggedValueMatches(logItem, 'name', 'test2')
        loggedOldValueMatches(logItem, 'name', 'test1')

        cleanup:

        clearEntityById(identityEntity, 'TEST_IDENTITY')
    }


    def "Logging is working for a creation of a BaseIntIdentityIdEntity"() {

        when:
        def intIdentityEntity = new IntIdentityEntity(name: 'test1')
        saveEntity(intIdentityEntity)

        then:

        noExceptionThrown()

        and:

        def logItem = getLatestEntityLogItem('test$IntIdentityEntity', intIdentityEntity)


        loggedValueMatches(logItem, 'name', 'test1')
        loggedOldValueMatches(logItem, 'name', null)

        cleanup:

        clearEntityById(intIdentityEntity, 'TEST_INT_IDENTITY')
    }

    def "Logging is working for an update of a BaseIntIdentityIdEntity"() {

        given:

        def intIdentityEntity = new IntIdentityEntity(name: 'test1')

        and:

        saveEntity(intIdentityEntity)

        when:

        findEntityAndUpdateNameInTransaction(intIdentityEntity, 'test2')

        then:

        List<EntityLogItem> logItems = getEntityLogItems('test$IntIdentityEntity', EntityValues.getId(intIdentityEntity))
        EntityLogItem logItem = logItems.first()

        logItems.size() == 2
        logItem.type == EntityLogItem.Type.MODIFY
        loggedValueMatches(logItem, 'name', 'test2')
        loggedOldValueMatches(logItem, 'name', 'test1')


        cleanup:

        clearEntityById(intIdentityEntity, 'TEST_INT_IDENTITY')
    }


    def "Logging is working for a creation of a MetaProperty"() {

        given:

        def stringKeyEntity = new StringKeyEntity(code: 'code1', name: 'test1')

        when:

        saveEntity(stringKeyEntity)

        then:

        noExceptionThrown()

        cleanup:

        clearEntityByCode(stringKeyEntity, 'TEST_STRING_KEY')

    }

    def "Logging is working for an update of a MetaProperty"() {

        given:

        def stringKeyEntity = new StringKeyEntity(code: 'code1', name: 'test1')

        and:

        saveEntity(stringKeyEntity)

        when:

        withTransaction {
            StringKeyEntity e = em.find(StringKeyEntity, EntityValues.getId(stringKeyEntity))
            e.name = 'test2'
            e.description = 'description2'

            AttributeChanges changes = AttributeChanges.Builder.ofChanges(attributeChangesProvider.getAttributeChanges(e))
                    .withChange('description', 'description1')
                    .build()

            entityLog.registerModify(e, false, changes)
        }

        then:

        def item2 = getLatestEntityLogItem('test$StringKeyEntity', stringKeyEntity)

        loggedValueMatches(item2, 'name', 'test2')
        loggedOldValueMatches(item2, 'name', 'test1')

        loggedValueMatches(item2, 'description', 'description2')
        loggedOldValueMatches(item2, 'description', 'description1')


        cleanup:

        clearEntityByCode(stringKeyEntity, 'TEST_STRING_KEY')

    }

    def "Logging for cascade saved entities works"() {
        when:
        CascOrder order = metadata.create(CascOrder)
        CascOrderReview review = metadata.create(CascOrderReview)
        review.text = "Passed"

        order.setReview(review)
        dataManager.save(order)

        CascOrderReview loaded = dataManager.load(CascOrderReview.class).id(review.getId()).one()

        then:
        loaded != null

        def entityLogItem = getLatestEntityLogItem('cascop_Order', order)

        entityLogItem != null
        def entityLogItem2 = getLatestEntityLogItem('cascop_OrderReview', review)
        entityLogItem2 != null
        entityLogItem2.changes.contains("text=Passed")

        cleanup:

        runSqlUpdate("delete from CASCOP_ORDER")
    }

    protected saveEntity(Entity entity) {
        withTransaction {
            em.persist(entity)
        }
    }

    private clearEntityById(Entity entity, String tableName) {
        Object id = EntityValues.getId(entity)
        if (entity && id) {
            runSqlUpdate("delete from $tableName where id = ${id}")
        }
    }

    private clearEntityByCode(StringKeyEntity entity, String tableName) {
        if (entity && EntityValues.getId(entity)) {
            runSqlUpdate("delete from $tableName where code = '${EntityValues.getId(entity)}'")
        }
    }


    protected findEntityAndUpdateNameInTransaction(IdentityEntity identityEntity, String name) {
        withTransaction {
            IdentityEntity e = em.find(IdentityEntity, identityEntity.id)
            e.name = name
        }
    }

    protected findEntityAndUpdateNameInTransaction(IntIdentityEntity identityEntity, String name) {
        withTransaction {
            IntIdentityEntity e = em.find(IntIdentityEntity, identityEntity.id)
            e.name = name
        }
    }
}
