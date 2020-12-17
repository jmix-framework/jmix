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


import io.jmix.audit.AuditConfiguration
import io.jmix.audit.entity.EntityLogItem
import io.jmix.core.*
import io.jmix.core.entity.EntityValues
import io.jmix.data.DataConfiguration
import io.jmix.data.entity.ReferenceToEntity
import io.jmix.dynattr.AttributeType
import io.jmix.dynattr.DynAttrConfiguration
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.dynattr.DynAttrQueryHints
import io.jmix.dynattr.model.Category
import io.jmix.dynattr.model.CategoryAttribute
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import test_support.AuditTestConfiguration
import test_support.testmodel.dynattr.AdditionalEntity
import test_support.testmodel.dynattr.FirstEntity
import test_support.testmodel.dynattr.SecondEntity

import javax.sql.DataSource

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


@ContextConfiguration(
        inheritLocations = false,
        classes = [CoreConfiguration, DataConfiguration, AuditConfiguration, DynAttrConfiguration, AuditTestConfiguration]
)
class EntityLogDynAttrTest extends AbstractEntityLogTest {

    @Autowired
    protected DataManager dataManager
    @Autowired
    protected DynAttrMetadata dynamicModelConfiguration
    @Autowired
    protected Metadata metadata
    @Autowired
    protected DataSource dataSource

    protected Category firstCategory, secondCategory

    protected CategoryAttribute firstAttribute, notLoggedAttribute, entityAttribute

    void setup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")

        withTransaction {
            clearTable(em, "AUDIT_ENTITY_LOG")
            initEntityLogConfiguration()
        }

        initEntityLogAPI()

        firstCategory = metadata.create(Category)
        firstCategory.name = 'firstEntity'
        firstCategory.entityType = 'dynaudit$FirstEntity'


        secondCategory = metadata.create(Category)
        secondCategory.name = 'secondEntity'
        secondCategory.entityType = 'dynaudit$SecondEntity'

        createFirstEntityAttributes()
        createSecondEntityAttributes()

        dataManager.save(
                firstCategory,
                firstAttribute,
                notLoggedAttribute,

                secondCategory,
                entityAttribute)

        dynamicModelConfiguration.reload()
    }

    protected void initEntityLogConfiguration() {
        saveEntityLogAutoConfFor('dynaudit$FirstEntity', '+firstAttribute')

        saveEntityLogAutoConfFor('dynaudit$SecondEntity', '+entityAttribute')//todo taimanov try '*'
    }


    void cleanup() {
        clearTables("AUDIT_LOGGED_ATTR",
                "AUDIT_LOGGED_ENTITY",
                "SYS_ATTR_VALUE",
                "SYS_CATEGORY_ATTR",
                "SYS_CATEGORY",
                "DA_FIRST_ENTITY",
                "DA_SECOND_ENTITY",
                "DA_ADDITIONAL_ENTITY")
    }

    private void createFirstEntityAttributes() {
        firstAttribute = metadata.create(CategoryAttribute)
        firstAttribute.name = 'firstAttribute'
        firstAttribute.code = 'firstAttribute'
        firstAttribute.dataType = AttributeType.STRING
        firstAttribute.categoryEntityType = 'dynaudit$FirstEntity'
        firstAttribute.category = firstCategory
        firstAttribute.defaultEntity = new ReferenceToEntity()

        notLoggedAttribute = metadata.create(CategoryAttribute)
        notLoggedAttribute.name = 'notLoggedAttribute'
        notLoggedAttribute.code = 'notLoggedAttribute'
        notLoggedAttribute.dataType = AttributeType.STRING
        notLoggedAttribute.categoryEntityType = 'dynaudit$FirstEntity'
        notLoggedAttribute.category = firstCategory
        notLoggedAttribute.defaultEntity = new ReferenceToEntity()
    }

    private void createSecondEntityAttributes() {
        entityAttribute = metadata.create(CategoryAttribute)
        entityAttribute.name = 'additionalEntityAttribute'
        entityAttribute.code = 'additionalEntityAttribute'
        entityAttribute.dataType = AttributeType.ENTITY
        entityAttribute.categoryEntityType = 'dynaudit$SecondEntity'
        entityAttribute.category = secondCategory
        entityAttribute.entityClass = 'test_support.testmodel.dynattr.AdditionalEntity'
        entityAttribute.defaultEntity = new ReferenceToEntity()

        /*userGroupCollectionAttribute = metadata.create(CategoryAttribute)//todo taimanov finish this and other types later
        userGroupCollectionAttribute.name = 'userGroupCollectionAttribute'
        userGroupCollectionAttribute.code = 'userGroupCollectionAttribute'
        userGroupCollectionAttribute.dataType = AttributeType.ENTITY
        userGroupCollectionAttribute.categoryEntityType = 'logdynattr$User'
        userGroupCollectionAttribute.category = userCategory
        userGroupCollectionAttribute.entityClass = 'test_support.testmodel.dynattr.entity.Group'
        userGroupCollectionAttribute.isCollection = true
        userGroupCollectionAttribute.defaultEntity = new ReferenceToEntity()*/
    }

    @Ignore
//todo known issue
    def "do not loose extra state"() {
        when: "Entity saved through dataManager"
        def firstEntity = metadata.create(FirstEntity)
        EntityValues.setValue(firstEntity, '+firstAttribute', 'extraValue')
        def reloaded = dataManager.save(firstEntity)

        then: "Extra state for saved instance is loaded"
        EntityValues.setValue(reloaded, '+firstAttribute', 'newExtraValue')//should not throw exception
    }

    def "test dynamic only by operation types"() {
        when: 'entity created'
        def firstEntity = metadata.create(FirstEntity)
        dataManager.save(firstEntity)
        def creationRecord = getLatestEntityLogItem('dynaudit$FirstEntity', firstEntity.getId())

        then: 'creation logged'
        creationRecord.type == EntityLogItem.Type.CREATE
        creationRecord.changes == "+firstAttribute=\n"


        when: 'entity modified'
        firstEntity = reloadWithDynamicAttributes(firstEntity)
        EntityValues.setValue(firstEntity, '+firstAttribute', 'changed first time')
        dataManager.save(firstEntity)
        def firstChange = getLatestEntityLogItem('dynaudit$FirstEntity', firstEntity.getId())

        EntityValues.setValue(firstEntity, '+firstAttribute', 'changed second time')
        dataManager.save(firstEntity)
        def secondChange = getLatestEntityLogItem('dynaudit$FirstEntity', firstEntity.getId())

        then: 'modification logged'
        firstChange.type == EntityLogItem.Type.MODIFY
        firstChange.changes == "+firstAttribute=changed first time\n+firstAttribute-oldVl=\n"

        secondChange.type == EntityLogItem.Type.MODIFY
        secondChange.changes == "+firstAttribute=changed second time\n+firstAttribute-oldVl=changed first time\n"


        when: 'entity deleted'
        firstEntity = reloadWithDynamicAttributes(firstEntity)
        dataManager.remove(firstEntity)
        def removeRecord = getLatestEntityLogItem('dynaudit$FirstEntity', firstEntity.getId())

        then: 'deletion logged'
        removeRecord.type == EntityLogItem.Type.DELETE
        removeRecord.changes == '+firstAttribute=changed second time\n'


        when: 'entity restored'
        FirstEntity deletedEntity = dataManager.load(Id.of(firstEntity))
                .softDeletion(false)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        deletedEntity.setDeledetBy(null)
        deletedEntity.setDeletedDate(null)
        dataManager.save(deletedEntity)

        def restoreRecord = getLatestEntityLogItem('dynaudit$FirstEntity', firstEntity.getId())

        then: 'restoration logged'
        restoreRecord.type == EntityLogItem.Type.RESTORE
        restoreRecord.changes == "+firstAttribute=changed second time\n+firstAttribute-oldVl=\n"
    }

    def "test attribute types"() {

        when: 'entity created'
        def secondEntity = metadata.create(SecondEntity)
        dataManager.save(secondEntity)
        def creationRecord = getLatestEntityLogItem('dynaudit$SecondEntity', secondEntity.getId())

        then: 'All attributes considered'
        creationRecord.type == EntityLogItem.Type.CREATE
        creationRecord.changes == "+entityAttribute=\n"


        when: 'Entity modified'

        AdditionalEntity prevExtraEntity = metadata.create(AdditionalEntity)
        dataManager.save(prevExtraEntity)

        EntityValues.setValue(secondEntity, '+entityAttribute', prevExtraEntity)
        dataManager.save(secondEntity)

        AdditionalEntity newExtraEntity = metadata.create(AdditionalEntity)
        dataManager.save(newExtraEntity)
        //secondEntity = reloadWithDynamicAttributes(secondEntity)//todo discuss: entity not loaded
        EntityValues.setValue(secondEntity, '+entityAttribute', newExtraEntity)
        dataManager.save(secondEntity)

        def modificationRecord = getLatestEntityLogItem('dynaudit$SecondEntity', secondEntity.getId())

        then: 'All changes logged correctly'

        modificationRecord.changes == "+entityAttribute-oldVlId=${prevExtraEntity.id}\n" +
                "+entityAttribute=test_support.testmodel.dynattr.AdditionalEntity-${newExtraEntity.id} [detached]\n" +
                "+entityAttribute-oldVl=test_support.testmodel.dynattr.AdditionalEntity-${prevExtraEntity.id} [detached]\n" +
                "+entityAttribute-id=${newExtraEntity.id}\n"


    }

    protected Object reloadWithDynamicAttributes(Entity entity) {
        return dataManager.load(Id.of(entity)).hint(DynAttrQueryHints.LOAD_DYN_ATTR, true).one()
    }

}
