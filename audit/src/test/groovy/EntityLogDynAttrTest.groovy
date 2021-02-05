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
import io.jmix.eclipselink.EclipselinkConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import test_support.AuditTestConfiguration
import test_support.testmodel.dynattr.AdditionalEntity
import test_support.testmodel.dynattr.FirstEntity
import test_support.testmodel.dynattr.SecondEntity

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
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration, AuditConfiguration, DynAttrConfiguration, AuditTestConfiguration]
)
class EntityLogDynAttrTest extends AbstractEntityLogTest {

    @Autowired
    protected DataManager dataManager
    @Autowired
    protected DynAttrMetadata dynamicModelConfiguration
    @Autowired
    protected Metadata metadata
    @Autowired
    protected EntityStates entityStates

    protected Category firstCategory, secondCategory

    protected CategoryAttribute firstAttribute, notLoggedAttribute, entityAttribute, entityCollectionAttribute, dottedAttribute

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
                entityAttribute,
                entityCollectionAttribute,
                dottedAttribute)

        dynamicModelConfiguration.reload()
    }

    protected void initEntityLogConfiguration() {
        saveEntityLogAutoConfFor('dynaudit$FirstEntity', '+firstAttribute')

        saveEntityLogAutoConfFor('dynaudit$SecondEntity', '*')
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
        firstAttribute.name = 'First Attribute'
        firstAttribute.code = 'firstAttribute'
        firstAttribute.dataType = AttributeType.STRING
        firstAttribute.categoryEntityType = 'dynaudit$FirstEntity'
        firstAttribute.category = firstCategory
        firstAttribute.defaultEntity = new ReferenceToEntity()

        notLoggedAttribute = metadata.create(CategoryAttribute)
        notLoggedAttribute.name = 'Not Logged Attribute'
        notLoggedAttribute.code = 'notLoggedAttribute'
        notLoggedAttribute.dataType = AttributeType.STRING
        notLoggedAttribute.categoryEntityType = 'dynaudit$FirstEntity'
        notLoggedAttribute.category = firstCategory
        notLoggedAttribute.defaultEntity = new ReferenceToEntity()
    }

    private void createSecondEntityAttributes() {
        entityAttribute = metadata.create(CategoryAttribute)
        entityAttribute.name = 'Entity Attribute'
        entityAttribute.code = 'entityAttribute'
        entityAttribute.dataType = AttributeType.ENTITY
        entityAttribute.categoryEntityType = 'dynaudit$SecondEntity'
        entityAttribute.category = secondCategory
        entityAttribute.entityClass = 'test_support.testmodel.dynattr.AdditionalEntity'
        entityAttribute.defaultEntity = new ReferenceToEntity()

        entityCollectionAttribute = metadata.create(CategoryAttribute)
        entityCollectionAttribute.name = 'Entity Collection Attribute'
        entityCollectionAttribute.code = 'entityCollectionAttribute'
        entityCollectionAttribute.dataType = AttributeType.ENTITY
        entityCollectionAttribute.categoryEntityType = 'dynaudit$SecondEntity'
        entityCollectionAttribute.category = secondCategory
        entityCollectionAttribute.entityClass = 'test_support.testmodel.dynattr.AdditionalEntity'
        entityCollectionAttribute.isCollection = true
        entityCollectionAttribute.defaultEntity = new ReferenceToEntity()

        dottedAttribute = metadata.create(CategoryAttribute)
        dottedAttribute.name = 'Attribute with dots in name'
        dottedAttribute.code = 'dotted.attribute'
        dottedAttribute.dataType = AttributeType.BOOLEAN
        dottedAttribute.categoryEntityType = 'dynaudit$SecondEntity'
        dottedAttribute.category = secondCategory
        dottedAttribute.defaultEntity = new ReferenceToEntity()
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
        firstChange.changes.count("\n") == 2
        firstChange.changes.contains("+firstAttribute=changed first time\n")
        firstChange.changes.contains("+firstAttribute-oldVl=\n")

        secondChange.type == EntityLogItem.Type.MODIFY

        secondChange.changes.count("\n") == 2
        secondChange.changes.contains("+firstAttribute=changed second time\n")
        secondChange.changes.contains("+firstAttribute-oldVl=changed first time\n")


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
        restoreRecord.changes.count("\n") == 2
        restoreRecord.changes.contains("+firstAttribute=changed second time\n")
        restoreRecord.changes.contains("+firstAttribute-oldVl=\n")
    }

    def "test attribute types"() {

        when: 'entity created'
        SecondEntity secondEntity = metadata.create(SecondEntity)
        dataManager.save(secondEntity)
        def creationRecord = getLatestEntityLogItem('dynaudit$SecondEntity', secondEntity.getId())

        then: 'All attributes considered'
        creationRecord.type == EntityLogItem.Type.CREATE
        creationRecord.changes.contains("stringAttribute=\n")
        creationRecord.changes.contains("id=${secondEntity.id}\n")
        creationRecord.changes.contains("+entityAttribute=\n")
        creationRecord.changes.contains("+entityCollectionAttribute=\n")
        creationRecord.changes.contains("+dotted.attribute=\n")

        when: 'Entity modified'

        AdditionalEntity prevExtraEntity = metadata.create(AdditionalEntity)
        prevExtraEntity.name = "prev"
        AdditionalEntity anotherExtraEntity = metadata.create(AdditionalEntity)
        anotherExtraEntity.name = "2"
        dataManager.save(prevExtraEntity, anotherExtraEntity)

        EntityValues.setValue(secondEntity, '+entityAttribute', prevExtraEntity)
        EntityValues.setValue(secondEntity, '+entityCollectionAttribute', [prevExtraEntity, anotherExtraEntity])

        EntityValues.setValue(secondEntity, '+dotted.attribute', true)
        dataManager.save(secondEntity)

        AdditionalEntity newExtraEntity = metadata.create(AdditionalEntity)
        newExtraEntity.name = "new"
        dataManager.save(newExtraEntity)
        secondEntity = reloadWithDynamicAttributes(secondEntity)
        secondEntity.stringAttribute = "new string value"
        EntityValues.setValue(secondEntity, '+entityAttribute', newExtraEntity)
        EntityValues.setValue(secondEntity, '+entityCollectionAttribute', [newExtraEntity, anotherExtraEntity])
        EntityValues.setValue(secondEntity, '+dotted.attribute', false)
        EntityValues.setValue(secondEntity, '+notExistingAttribute', "WIZZARD")
        dataManager.save(secondEntity)

        def modificationRecord = getLatestEntityLogItem('dynaudit$SecondEntity', secondEntity.getId())

        then: 'All changes logged correctly'
        modificationRecord.changes.count("\n") == 10

        modificationRecord.changes.contains("+entityAttribute=>new<\n")
        modificationRecord.changes.contains("+entityAttribute-id=${newExtraEntity.id}\n")

        modificationRecord.changes.contains("+entityAttribute-oldVl=>prev<\n")
        modificationRecord.changes.contains("+entityAttribute-oldVlId=${prevExtraEntity.id}\n")

        modificationRecord.changes.contains("+dotted.attribute=false\n")
        modificationRecord.changes.contains("dotted.attribute-oldVl=true\n")

        modificationRecord.changes.contains("stringAttribute=new string value\n")
        modificationRecord.changes.contains("stringAttribute-oldVl=\n")

        (modificationRecord.changes.contains("+entityCollectionAttribute=[>new<,>2<]\n")
                || modificationRecord.changes.contains("+entityCollectionAttribute=[>2<,>new<]\n"))
        (modificationRecord.changes.contains("+entityCollectionAttribute-oldVl=[>prev<,>2<]\n")
                || modificationRecord.changes.contains("+entityCollectionAttribute-oldVl=[>2<,>prev<]\n"))

    }

    protected Object reloadWithDynamicAttributes(Entity entity) {
        return dataManager.load(Id.of(entity)).hint(DynAttrQueryHints.LOAD_DYN_ATTR, true).one()
    }

}
