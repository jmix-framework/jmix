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

package metadata

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.entity.BaseGenericIdEntity
import test_support.base.entity.BaseUuidEntity
import test_support.base.entity.BaseEntity
import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity

import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MetadataTest extends Specification {

    @Autowired
    Metadata metadata

    def "entities are in metadata"() {
        expect:

        metadata.findClass(BaseEntity)
        metadata.findClass(TestAddon1Entity)
    }

    def "ancestors and descendants are collected recursively"() {

        given:

        def pet = metadata.getClass(Pet)
        def standardEntity = metadata.getClass(BaseEntity)
        def baseUuidEntity = metadata.getClass(BaseUuidEntity)
        def baseGenericIdEntity = metadata.getClass(BaseGenericIdEntity)

        expect:

        pet.ancestor == standardEntity
        pet.ancestors[0] == pet.ancestor
        pet.ancestors[1] == baseUuidEntity
        pet.ancestors[2] == baseGenericIdEntity

        and:
        baseGenericIdEntity.descendants.containsAll([baseUuidEntity, standardEntity, pet])
        baseUuidEntity.descendants.containsAll([standardEntity, pet])
        standardEntity.descendants.containsAll([pet])
    }

    def "inherited properties"() {

        def baseMetaClass = metadata.getClass(BaseEntity)
        def baseProp = baseMetaClass.getProperty('createTs')
        def baseIdProp = baseMetaClass.getProperty('id')

        def entityMetaClass = metadata.getClass(TestAddon1Entity)
        def entityProp = entityMetaClass.getProperty('createTs')
        def entityIdProp = entityMetaClass.getProperty('id')

        expect:

        !entityProp.is(baseProp)
        baseProp.domain == baseMetaClass
        entityProp.domain == entityMetaClass

        entityProp.range == baseProp.range
        entityProp.annotatedElement == baseProp.annotatedElement
        entityProp.declaringClass == baseProp.declaringClass
        entityProp.inverse == baseProp.inverse
        entityProp.javaType == baseProp.javaType
        entityProp.mandatory == baseProp.mandatory
        entityProp.readOnly == baseProp.readOnly

        !entityIdProp.is(baseIdProp)
        entityIdProp.domain == entityMetaClass
        entityIdProp.range == baseIdProp.range
        entityIdProp.annotatedElement == baseIdProp.annotatedElement
    }
}
