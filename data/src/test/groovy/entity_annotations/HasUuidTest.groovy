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

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.entity.EntityEntryHasUuid
import io.jmix.data.entity.BaseUuidEntity
import io.jmix.data.entity.dummy.DummyEntity
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.soft_delete.AnnotatedUuidEntity

class HasUuidTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    Metadata metadata;

    def "entities enhanced properly"() {
        setup:
        DummyEntity dummyEntity = dataManager.save(dataManager.create(DummyEntity))
        AnnotatedUuidEntity entity = dataManager.save(dataManager.create(AnnotatedUuidEntity))
        UUID someId = UUID.fromString("7ca7670b-b352-4e62-87a2-de9ca59ad2c1")
        UUID anotherId = UUID.fromString('e61703bf-bf36-4364-ba66-67655e1810d6')

        expect:
        metadataTools.hasUuid(metadata.getClass(AnnotatedUuidEntity))
        metadataTools.hasUuid(metadata.getClass(BaseUuidEntity))

        "someNotPrimaryId".equals(metadataTools.getUuidPropertyName(AnnotatedUuidEntity))
        "id".equals(metadataTools.getUuidPropertyName(BaseUuidEntity))

        dummyEntity.__getEntityEntry() instanceof EntityEntryHasUuid
        ((EntityEntryHasUuid) dummyEntity.__getEntityEntry()).getUuid() != null

        entity.getId() != null
        entity.getSomeNotPrimaryId() != null
        ((EntityEntryHasUuid) entity.__getEntityEntry()).getUuid() != null

        when:
        entity.setSomeNotPrimaryId(someId)

        then:
        ((EntityEntryHasUuid) entity.__getEntityEntry()).getUuid() == someId

        when:
        ((EntityEntryHasUuid) entity.__getEntityEntry()).setUuid(anotherId)

        then:
        entity.getSomeNotPrimaryId() == anotherId

        cleanup:
        if (entity != null) dataManager.remove(entity)
        if (dummyEntity != null) dataManager.remove(dummyEntity)
    }

}
