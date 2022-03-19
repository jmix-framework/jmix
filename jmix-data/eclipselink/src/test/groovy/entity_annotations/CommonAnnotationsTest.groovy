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
import io.jmix.core.metamodel.model.MetaClass
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestLicense
import test_support.entity.auditing.SoftDeleteAuditableEntity
import test_support.entity.common.*
import test_support.entity.soft_delete.AnnotatedUuidEntity

class CommonAnnotationsTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    Metadata metadata;

    def "HasUuid annotation test"() {
        setup:
        AnnotatedUuidEntity entity = dataManager.save(dataManager.create(AnnotatedUuidEntity))
        UUID someId = UUID.fromString("7ca7670b-b352-4e62-87a2-de9ca59ad2c1")
        UUID anotherId = UUID.fromString('e61703bf-bf36-4364-ba66-67655e1810d6')

        expect:
        metadataTools.hasUuid(metadata.getClass(AnnotatedUuidEntity))

        "someNotPrimaryId".equals(metadataTools.getUuidPropertyName(AnnotatedUuidEntity))

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
    }


    def "System properties test"() {
        setup:
        MetaClass checkingClass = metadata.getClass(SoftDeleteAuditableEntity)

        def metadataSystem = metadataTools.getSystemProperties(checkingClass) as Set
        def mustBeSystem = ["id", "creator", "birthDate", "touchedBy", "touchDate", "version", "whoDeleted", "whenDeleted"] as Set

        expect:
        metadataTools.isSystem(checkingClass.getProperty("id"))
        metadataTools.isSystem(checkingClass.getProperty("creator"))
        metadataTools.isSystem(checkingClass.getProperty("birthDate"))
        metadataTools.isSystem(checkingClass.getProperty("touchedBy"))
        metadataTools.isSystem(checkingClass.getProperty("touchDate"))
        metadataTools.isSystem(checkingClass.getProperty("version"))
        metadataTools.isSystem(checkingClass.getProperty("whoDeleted"))
        metadataTools.isSystem(checkingClass.getProperty("whenDeleted"))

        !metadataTools.isSystem(checkingClass.getProperty("title"))
        !metadataTools.isSystem(checkingClass.getProperty("reason"))

        metadataSystem == mustBeSystem
    }

    def "@Transient @Temporal field test"() {
        when:
        TestLicense license = metadata.create(TestLicense)
        license.setExpirationDate(new Date())
        then:
        noExceptionThrown()
    }

    def "Wildcards in collections works for entities"() {

        when:
        WildBaseEntity base1 = metadata.create(WildBaseEntity)
        base1.setName("base1")

        WildBaseEntity base2 = metadata.create(WildBaseEntity)
        base2.setName("base2")

        WildExtEntity ext1 = metadata.create(WildExtEntity)
        ext1.setName("ext1Name")
        ext1.setExtName("ext1ExtName")

        WildExtEntity ext2 = metadata.create(WildExtEntity)
        ext2.setName("ext2Name")
        ext2.setExtName("ext2ExtName")

        WildParentEntity parent1 = metadata.create(WildParentEntity)
        parent1.setChildren(new LinkedList<WildBaseEntity>())
        parent1.getChildren().add(base1)
        parent1.getChildren().add(ext1)

        WildParentEntity parent2 = metadata.create(WildParentEntity)
        parent2.setChildren(new LinkedList<WildExtEntity>())
        parent2.getChildren().add(ext1)
        parent2.getChildren().add(ext2)

        dataManager.save(base1, base2, ext1, ext2, parent1, parent2)

        then:
        noExceptionThrown()
        parent1.children.contains(base1)
        parent1.children.contains(ext1)

        parent2.children.contains(ext1)
        parent2.children.contains(ext2)

        cleanup:
        dataManager.remove(parent1, parent2, base1, base2, ext1, ext2)

    }

    def "Wildcards in collections works for DTOs"() {

        when:
        WildBaseDTO base1 = metadata.create(WildBaseDTO)
        base1.setName("base1")

        WildBaseDTO base2 = metadata.create(WildBaseDTO)
        base2.setName("base2")

        WildExtDTO ext1 = metadata.create(WildExtDTO)
        ext1.setName("ext1Name")
        ext1.setExtName("ext1ExtName")

        WildExtDTO ext2 = metadata.create(WildExtDTO)
        ext2.setName("ext2Name")
        ext2.setExtName("ext2ExtName")

        WildParentDTO parent1 = metadata.create(WildParentDTO)
        parent1.setChildren(new LinkedList<WildBaseDTO>())
        parent1.getChildren().add(base1)
        parent1.getChildren().add(ext1)

        WildParentDTO parent2 = metadata.create(WildParentDTO)
        parent2.setChildren(new LinkedList<WildExtDTO>())
        parent2.getChildren().add(ext1)
        parent2.getChildren().add(ext2)

        then:
        noExceptionThrown()
        parent1.children.contains(base1)
        parent1.children.contains(ext1)

        parent2.children.contains(ext1)
        parent2.children.contains(ext2)
    }

}
