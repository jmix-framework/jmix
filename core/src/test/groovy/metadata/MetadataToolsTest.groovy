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

package metadata

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.app.TestAppConfiguration
import test_support.app.entity.Address
import test_support.app.entity.Owner
import test_support.app.entity.Pet
import test_support.base.entity.BaseEntity

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MetadataToolsTest extends Specification {

    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools

    def "persistent entities"() {
        expect:

        // @Entity
        metadataTools.isJpaEntity(Owner)
        !metadataTools.isJpaEmbeddable(Owner)

        // @Embeddable
        metadataTools.isJpaEmbeddable(Address)
        !metadataTools.isJpaEntity(Address)
    }

    def "non-persistent entities"() {
        expect:

        // @JmixEntity
        !metadataTools.isJpaEntity(TestAddon1Entity)
        !metadataTools.isJpaEmbeddable(TestAddon1Entity)

        // @MappedSuperclass
        !metadataTools.isJpaEntity(BaseEntity)
        !metadataTools.isJpaEmbeddable(BaseEntity)
    }

    def "persistent properties"() {
        def ownerMetaClass = metadata.getClass(Owner)
        def addressMetaClass = metadata.getClass(Address)
        expect:

        // property of @Entity
        metadataTools.isJpa(ownerMetaClass.getProperty('name'))

        // property of @Entity inherited from @MappedSuperclass
        metadataTools.isJpa(ownerMetaClass.getProperty('createTs'))

        // property of @Embeddable
        metadataTools.isJpa(addressMetaClass.getProperty('city'))

        // @Embedded property in @Entity
        metadataTools.isJpa(ownerMetaClass.getProperty('address'))

        // nested property of @Embedded in @Entity
        metadataTools.isJpa(ownerMetaClass.getProperty('address').range.asClass().getProperty('city'))

        // nested property of @Embedded in @Entity, passed as MetaPropertyPath
        metadataTools.isJpa(metadataTools.resolveMetaPropertyPathOrNull(ownerMetaClass, 'address.city'))
    }

    def "non-persistent properties"() {
        expect:

        // @JmixProperty in @Entity
        !metadataTools.isJpa(metadata.getClass(Pet).getProperty('nick'))

        // property of @MappedSuperclass
        !metadataTools.isJpa(metadata.getClass(BaseEntity).getProperty('createTs'))
    }

    def "embedded property"() {
        expect:
        metadataTools.isEmbedded(metadata.getClass(Owner).getProperty('address'))
        !metadataTools.isEmbedded(metadata.getClass(Owner).getProperty('name'))
    }

    def "deepCopy handles entities with same ids correctly #73"() {
        def id = new UUID(0, 1)
        def owner = new Owner(id: id, name: 'Joe')
        def pet = new Pet(id: id, name: 'Rex', owner: owner)

        when:
        def petCopy = metadataTools.deepCopy(pet)

        then:
        petCopy.owner == owner
        !petCopy.owner.is(owner)
    }

    def "dependsOn properties"() {
        List<String> properties

        when:
        properties = metadataTools.getDependsOnProperties(Pet, 'description')

        then:
        properties[0] == 'name'
        properties[1] == 'nick'
    }
}
