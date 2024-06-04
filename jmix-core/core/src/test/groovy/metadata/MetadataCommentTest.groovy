/*
 * Copyright 2023 Haulmont.
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
import io.jmix.core.metamodel.annotation.Comment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet
import test_support.base.entity.BaseEntity
import test_support.base.entity.BaseUuidEntity

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MetadataCommentTest extends Specification {

    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools

    def "class comments"() {

        given:

        def pet = metadata.getClass(Pet)
        def standardEntity = metadata.getClass(BaseEntity)
        def baseUuidEntity = metadata.getClass(BaseUuidEntity)

        expect:

        metadataTools.getMetaAnnotationValue(pet, Comment.class) == 'Pet - a domestic animal'
        metadataTools.getMetaAnnotationValue(standardEntity, Comment.class) == 'Base class of all project entities'
        metadataTools.getMetaAnnotationValue(baseUuidEntity, Comment.class) == 'Base class of entities with UUID PK'

        metadataTools.getMetaAnnotationValue(pet.getProperty('name'), Comment.class) == 'Name of the pet'
        metadataTools.getMetaAnnotationValue(pet.getProperty('version'), Comment.class) == 'Used for optimistic locking'
        metadataTools.getMetaAnnotationValue(pet.getProperty('id'), Comment.class) == 'Entity identifier'
    }
}
