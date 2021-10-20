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

package entity_annotations

import com.haulmont.cuba.core.model.entity_annotations.AnnotatedSoftDeleteEntity
import com.haulmont.cuba.core.model.entity_annotations.LegacySoftDeleteEntity
import io.jmix.core.DataManager
import io.jmix.core.MetadataTools
import io.jmix.core.entity.EntityEntrySoftDelete
import io.jmix.core.entity.EntityValues
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class LegacySoftDeleteTest extends CoreTestSpecification {
    @Autowired
    DataManager dataManager

    @Autowired
    MetadataTools metadataTools

    void "softDeletion should work for legacy entity and new entities"() {
        setup: "Legacy (implements SoftDelete) and new (annotated) entities"

        LegacySoftDeleteEntity legacyEntity = dataManager.save(dataManager.create(LegacySoftDeleteEntity))
        AnnotatedSoftDeleteEntity newEntity = dataManager.save(dataManager.create(AnnotatedSoftDeleteEntity))


        expect: "Entity enhanced correctly and metadataTools works"

        legacyEntity.__getEntityEntry() instanceof EntityEntrySoftDelete
        newEntity.__getEntityEntry() instanceof EntityEntrySoftDelete

        "deleteTs".equals(metadataTools.findDeletedDateProperty(legacyEntity.getClass()))
        "deletedBy".equals(metadataTools.findDeletedByProperty(legacyEntity.getClass()))

        "whenDeleted".equals(metadataTools.findDeletedDateProperty(newEntity.getClass()))
        "whoDeleted".equals(metadataTools.findDeletedByProperty(newEntity.getClass()))

        metadataTools.getSoftDeleteProperties(legacyEntity.getClass()).containsAll(["deleteTs", "deletedBy"])
        metadataTools.getSoftDeleteProperties(legacyEntity.getClass()).size() == 2
        metadataTools.isSoftDeletable(legacyEntity.getClass())

        metadataTools.getSoftDeleteProperties(newEntity.getClass()).containsAll(["whoDeleted", "whenDeleted"])
        metadataTools.getSoftDeleteProperties(newEntity.getClass()).size() == 2
        metadataTools.isSoftDeletable(newEntity.getClass())


        when: "Entities soft-deleted"

        dataManager.remove(legacyEntity)
        legacyEntity = dataManager.load(LegacySoftDeleteEntity).id(legacyEntity.getId()).softDeletion(false).one()

        dataManager.remove(newEntity)
        newEntity = dataManager.load(AnnotatedSoftDeleteEntity).id(newEntity.getId()).softDeletion(false).one()


        then: "All entity fields set correctly"

        legacyEntity.isDeleted()
        legacyEntity.getDeleteTs() != null
        legacyEntity.getDeletedBy() == "test_admin"

        EntityEntrySoftDelete legacyEntityEntry = legacyEntity.__getEntityEntry() as EntityEntrySoftDelete
        legacyEntityEntry.isDeleted()
        legacyEntityEntry.getDeletedDate() != null
        legacyEntityEntry.getDeletedBy() == "test_admin"

        EntityEntrySoftDelete newEntityEntry = newEntity.__getEntityEntry() as EntityEntrySoftDelete
        newEntityEntry.isDeleted()
        newEntityEntry.getDeletedDate() != null
        newEntityEntry.getDeletedBy() == "test_admin"

        EntityValues.isSoftDeleted(legacyEntity)
        EntityValues.isSoftDeleted(newEntity)
    }

}
