/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.metadata_tools

import com.haulmont.cuba.core.model.Owner
import com.haulmont.cuba.core.model.Pet
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.model.common.UserSessionEntity
import com.haulmont.cuba.core.model.not_persistent.CustomerWithNonPersistentRef
import com.haulmont.cuba.core.model.not_persistent.NotPersistentStringIdEntity
import com.haulmont.cuba.core.model.not_persistent.TestNotPersistentEntity
import com.haulmont.cuba.core.model.primary_keys.EntityKey
import com.haulmont.cuba.core.model.primary_keys.StringKeyEntity
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class MetadataToolsTest extends CoreTestSpecification {
    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools

    def "primary key name for persistent entities"() {

        when:

        def primaryKeyName = metadataTools.getPrimaryKeyName(metadata.getClass(User))

        then:

        primaryKeyName == 'id'

        when:

        primaryKeyName = metadataTools.getPrimaryKeyName(metadata.getClass(StringKeyEntity))

        then:

        primaryKeyName == 'code'
    }

    def "primary key name for non-persistent entity"() {

        def primaryKeyName

        when:

        primaryKeyName = metadataTools.getPrimaryKeyName(metadata.getClass(UserSessionEntity))

        then:

        primaryKeyName == 'id'

        when:

        primaryKeyName = metadataTools.getPrimaryKeyName(metadata.getClass(NotPersistentStringIdEntity))

        then:

        primaryKeyName == 'identifier'
    }

    def "deepCopy supports non-persistent and embedded references"() {

        def entity = metadata.create(CustomerWithNonPersistentRef)
        entity.name = 'foo'

        def embedded = metadata.create(EntityKey)
        entity.entityKey = embedded

        def notPersistentEntity = metadata.create(TestNotPersistentEntity)
        entity.notPersistentEntity = notPersistentEntity

        when:

        def copy = metadataTools.deepCopy(entity)

        then:

        !copy.is(entity)
        copy.name == entity.name

        copy.entityKey == entity.entityKey
        !copy.entityKey.is(entity.entityKey)

        copy.notPersistentEntity == entity.notPersistentEntity
        !copy.notPersistentEntity.is(entity.notPersistentEntity)
    }

    def "deepCopy handles entities with same ids correctly #2488"() {
        def id = new UUID(0, 1)
        def owner = new Owner(id: id, name: 'Joe')
        def pet = new Pet(id: id, name: 'Rex', owner: owner)

        when:
        def petCopy = metadataTools.deepCopy(pet)

        then:
        petCopy.owner == owner
        !petCopy.owner.is(owner)
    }
}
