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

import com.haulmont.cuba.core.model.entity_annotations.LegacyAuditableEntity
import com.haulmont.cuba.core.model.entity_annotations.LegacySoftDeleteEntity
import com.haulmont.cuba.core.model.primary_keys.IdentityUuidEntity
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.entity.EntityEntryHasUuid
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class MiscInterfacesTest extends CoreTestSpecification {
    @Autowired
    DataManager dataManager
    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools

    def "HasUuid considered"() {
        setup:
        IdentityUuidEntity entity = dataManager.save(dataManager.create(IdentityUuidEntity))
        UUID someId = UUID.fromString("7ca7670b-b352-4e62-87a2-de9ca59ad2c1")
        UUID anotherId = UUID.fromString('e61703bf-bf36-4364-ba66-67655e1810d6')

        expect:
        metadataTools.hasUuid(metadata.getClass(IdentityUuidEntity))

        "uuid".equals(metadataTools.getUuidPropertyName(IdentityUuidEntity))

        entity.__getEntityEntry() instanceof EntityEntryHasUuid
        ((EntityEntryHasUuid) entity.__getEntityEntry()).getUuid() != null


        when:
        entity.setUuid(someId)

        then:
        ((EntityEntryHasUuid) entity.__getEntityEntry()).getUuid() == someId

        when:
        ((EntityEntryHasUuid) entity.__getEntityEntry()).setUuid(anotherId)

        then:
        entity.getUuid() == anotherId

        cleanup:
        if (entity != null) dataManager.remove(entity)
    }

    def "System attributes determined correctly"() {
        setup:
        def actualAuditableSystem = metadataTools.getSystemProperties(metadata.getClass(LegacyAuditableEntity)) as Set
        def expectedAuditableSystem = ["createTs", "createdBy", "updateTs", "updatedBy", "id"] as Set

        def actualSoftDeleteSystem = metadataTools.getSystemProperties(metadata.getClass(LegacySoftDeleteEntity)) as Set
        def expectedSoftDeleteSystem = ["deleteTs", "deletedBy", "id"] as Set

        expect:
        !metadataTools.isSystem(metadata.getClass(LegacyAuditableEntity).getProperty("name"))

        metadataTools.isSystem(metadata.getClass(LegacyAuditableEntity).getProperty("createTs"))
        metadataTools.isSystem(metadata.getClass(LegacyAuditableEntity).getProperty("createdBy"))
        metadataTools.isSystem(metadata.getClass(LegacyAuditableEntity).getProperty("updateTs"))
        metadataTools.isSystem(metadata.getClass(LegacyAuditableEntity).getProperty("updatedBy"))

        metadataTools.isSystem(metadata.getClass(LegacySoftDeleteEntity).getProperty("id"))
        metadataTools.isSystem(metadata.getClass(LegacySoftDeleteEntity).getProperty("deleteTs"))
        metadataTools.isSystem(metadata.getClass(LegacySoftDeleteEntity).getProperty("deletedBy"))

        expectedAuditableSystem == actualAuditableSystem
        expectedSoftDeleteSystem == actualSoftDeleteSystem
    }


}
