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

package entity_scanning

import io.jmix.core.CoreConfiguration
import io.jmix.core.Entity
import io.jmix.core.Metadata
import io.jmix.core.entity.EntityPropertyChangeEvent
import io.jmix.core.metamodel.model.MetaClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.app.TestAppConfiguration
import test_support.app.entity.jmix_entities.*
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestAppConfiguration, TestBaseConfiguration])
class JmixEntityTest extends Specification {

    @Autowired
    Metadata metadata;

    def "JmixEntity annotation mandatory for all entities"() {
        expect:
        metadata.findClass(EntityWithJmix) != null
        metadata.findClass(EntityWithoutJmix) == null

        metadata.findClass(EmbeddableWithJmix) != null
        metadata.findClass(EmbeddableWithoutJmix) == null

        metadata.findClass(MappedWithJmix) != null
        metadata.findClass(MappedWithoutJmix) == null

        metadata.findClass(NonJpaEntity) != null
        metadata.getClass(AnnotatedNonJpaEntity) != null


        metadata.getClass(EntityWithJmix).getName().equals("test_entityJmix")
        metadata.getClass(MappedWithJmix).getName().equals("test_MappedWithJmix")
        metadata.getClass(EmbeddableWithJmix).getName().equals("EmbeddableWithJmix")
        metadata.getClass(NonJpaEntity).getName().equals("test_nonJpaEntity")
        metadata.getClass(AnnotatedNonJpaEntity).getName().equals("AnnotatedNonJpaEntity")
    }

    def "JPA entity properties added to metadata correctly"() {
        setup:
        MetaClass simpleJpaClass = metadata.getClass(EntityWithJmix)
        MetaClass annotatedJpaClass = metadata.getClass(MappedWithJmix)

        expect: "Included all fields for JPA entity but not @Transient. Transient fields require @JmixProperty annotation."
        simpleJpaClass.findProperty("uuid") != null
        simpleJpaClass.findProperty("name") != null
        simpleJpaClass.findProperty("mappedWithJmix") != null
        simpleJpaClass.findProperty("embeddableWithJmix") != null
        simpleJpaClass.findProperty("calculatedId") != null

        simpleJpaClass.findProperty("transientField") == null
        simpleJpaClass.findProperty("consideredTransientField") != null

        simpleJpaClass.findProperty("fieldWithoutGetter") == null


        annotatedJpaClass.findProperty("uuid") != null
        annotatedJpaClass.findProperty("name") != null
        annotatedJpaClass.findProperty("qualifier") != null

        annotatedJpaClass.findProperty("data") == null
    }

    def "Notpersistent entity properties test"() {
        setup:
        MetaClass simpleEntityMClass = metadata.getClass(NonJpaEntity)
        MetaClass annotatedEntityMClass = metadata.getClass(AnnotatedNonJpaEntity)

        expect:
        simpleEntityMClass.findProperty("name") != null
        simpleEntityMClass.findProperty("entities") != null
        simpleEntityMClass.findProperty("oddDate") != null

        simpleEntityMClass.findProperty("notExistingProperty") == null

        annotatedEntityMClass.findProperty("id") != null
        annotatedEntityMClass.findProperty("allowedProperty") != null
        annotatedEntityMClass.findProperty("forbiddenProperty") == null
    }

    def "@JmixProperty on method enhanced"() {
        setup:
        AnnotatedNonJpaEntity testEntity = metadata.create(AnnotatedNonJpaEntity)
        EntityPropertyChangeEvent changeEvent = null
        ((Entity) testEntity).__getEntityEntry().addPropertyChangeListener({ e ->
            changeEvent = e;
        })

        expect:
        metadata.getClass(AnnotatedNonJpaEntity).findProperty('methodOnlyProperty') != null
        metadata.getClass(AnnotatedNonJpaEntity).findProperty('methodAnnotatedProperty') != null

        metadata.getClass(AnnotatedNonJpaEntity).findProperty('methodAnnotatedProperty').isMandatory()

        when:
        testEntity.setMethodAnnotatedProperty('1')
        testEntity.setMethodAnnotatedProperty('2')

        then:
        changeEvent != null
        changeEvent.getProperty() == 'methodAnnotatedProperty'
        changeEvent.getPrevValue() == '1'
        changeEvent.getValue() == '2'
    }
}
