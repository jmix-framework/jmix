/*
 * Copyright 2026 Haulmont.
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

package entityinspector

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.data.DataConfiguration
import io.jmix.datatools.EntityInspectorSupport
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.security.SecurityConfiguration
import io.jmix.securitydata.SecurityDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.DataModelTestConfiguration
import test_support.TestContextInititalizer
import io.jmix.securitydata.entity.RoleAssignmentEntity
import test_support.entity.CustomStoreEntity
import test_support.entity.CustomStoreSystemEntity
import test_support.entity.DataModelHostEntity
import test_support.entity.DataModelStubEntity
import test_support.entity.InspectorPolicyEmbeddable

@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                   SecurityConfiguration, SecurityDataConfiguration,
                   DataModelTestConfiguration],
        initializers = [TestContextInititalizer]
)
class EntityInspectorSupportTest extends Specification {

    @Autowired
    EntityInspectorSupport entityInspectorSupport

    @Autowired
    Metadata metadata

    @Autowired
    MetadataTools metadataTools

    def "a JPA entity is inspectable"() {
        expect:
        entityInspectorSupport.getInspectableEntityMetaClasses()
                .contains(metadata.getClass(DataModelHostEntity))
    }

    def "a non-persistent entity is not inspectable"() {
        expect:
        !entityInspectorSupport.getInspectableEntityMetaClasses()
                .contains(metadata.getClass(DataModelStubEntity))
    }

    def "a JPA embeddable is not inspectable"() {
        expect:
        !entityInspectorSupport.getInspectableEntityMetaClasses()
                .contains(metadata.getClass(InspectorPolicyEmbeddable))
    }

    def "a JPA entity accepts a JPQL query"() {
        expect:
        entityInspectorSupport.supportsJpqlQuery(metadata.getClass(DataModelHostEntity))
    }

    def "a JPA property is stored and a transient one is not"() {
        given:
        MetaClass metaClass = metadata.getClass(DataModelHostEntity)

        expect:
        entityInspectorSupport.isStoredProperty(metaClass.getProperty("name"))
        !entityInspectorSupport.isStoredProperty(metaClass.getProperty("sideValue"))
    }

    def "an entity of a non-JPA store is inspectable and takes no JPQL query"() {
        given:
        MetaClass metaClass = metadata.getClass(CustomStoreEntity)

        expect:
        entityInspectorSupport.getInspectableEntityMetaClasses().contains(metaClass)
        !entityInspectorSupport.supportsJpqlQuery(metaClass)
    }

    def "a system-level entity of a non-JPA store is not inspectable"() {
        given:
        MetaClass metaClass = metadata.getClass(CustomStoreSystemEntity)

        expect:
        metadataTools.isSystemLevel(metaClass)
        !entityInspectorSupport.getInspectableEntityMetaClasses().contains(metaClass)
    }

    def "a system-level JPA entity stays inspectable"() {
        given:
        MetaClass metaClass = metadata.getClass(RoleAssignmentEntity)

        expect:
        metadataTools.isSystemLevel(metaClass)
        entityInspectorSupport.getInspectableEntityMetaClasses().contains(metaClass)
    }

    def "every JPA entity with a table is still inspectable"() {
        given:
        def jpaEntities = metadataTools.getAllJpaEntityMetaClasses()

        expect:
        entityInspectorSupport.getInspectableEntityMetaClasses().containsAll(jpaEntities)
    }

    def "the identifier of a non-JPA store entity is a stored property"() {
        given:
        MetaClass metaClass = metadata.getClass(CustomStoreEntity)

        expect:
        entityInspectorSupport.isStoredProperty(metaClass.getProperty("id"))
    }

    def "the identifier of a JPA entity is still a stored property"() {
        given:
        MetaClass metaClass = metadata.getClass(DataModelHostEntity)

        expect:
        entityInspectorSupport.isStoredProperty(metaClass.getProperty("id"))
    }

    def "a plain attribute of a non-JPA store entity is a stored property"() {
        given:
        MetaClass metaClass = metadata.getClass(CustomStoreEntity)

        expect: "its fields are persisted by its own store, so the list view must show them"
        entityInspectorSupport.isStoredProperty(metaClass.getProperty("name"))
    }

    def "a transient property of a JPA entity is still not a stored property"() {
        given:
        MetaClass metaClass = metadata.getClass(DataModelHostEntity)

        expect:
        !entityInspectorSupport.isStoredProperty(metaClass.getProperty("sideValue"))
    }
}
