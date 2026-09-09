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

package datamodel

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.datatools.datamodel.DataModel
import io.jmix.datatools.datamodel.DataModelRegistry
import io.jmix.datatools.datamodel.RelationType
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.security.SecurityConfiguration
import io.jmix.securitydata.SecurityDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.DataModelTestConfiguration
import test_support.StubDataModelContributor
import test_support.TestContextInititalizer

@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                   SecurityConfiguration, SecurityDataConfiguration,
                   DataModelTestConfiguration],
        initializers = [TestContextInititalizer]
)
class DataModelRegistryTest extends Specification {

    @Autowired
    DataModelRegistry dataModelRegistry

    def "contributed entity appears in the data model with its physical store and table"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")

        then:
        dataModel != null
        dataModel.dataStore() == StubDataModelContributor.STUB_STORE
        dataModel.entityModel().tableName == StubDataModelContributor.STUB_TABLE
        dataModel.entityModel().dynamic
    }

    def "a JPA entity is still reported as not dynamic"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels("main").get("test_DataModelHostEntity")

        then:
        dataModel != null
        dataModel.entityModel().tableName == "TEST_DATA_MODEL_HOST_ENTITY"
        dataModel.entityModel().dynamic == false
    }

    def "a contributed attribute on a JPA entity is qualified by its own table"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels("main").get("test_DataModelHostEntity")
        def attribute = dataModel.attributeModels().find { it.attributeName == "sideValue" }

        then:
        attribute != null
        attribute.columnName == "DYN_TEST_DATA_MODEL_HOST_ENTITY.SIDE_VALUE"
        attribute.dbType == "varchar(100)"
        attribute.dynamic
    }

    def "a contributed attribute in the entity's own table keeps a bare column name"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")
        def attribute = dataModel.attributeModels().find { it.attributeName == "name" }

        then:
        attribute != null
        attribute.columnName == "NAME"
        attribute.dbType == "varchar(50)"
        attribute.dynamic
    }

    def "an attribute with no column shows the explanatory text and no DB type"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")
        def attribute = dataModel.attributeModels().find { it.attributeName == "calculatedName" }

        then:
        attribute != null
        attribute.columnName == "calculated"
        attribute.dbType == ""
        attribute.dynamic
    }

    def "a contributed relation is registered"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")
        def relations = dataModel.relations().get(RelationType.MANY_TO_ONE)

        then:
        relations != null
        relations.any { it.referencedClass() == "test_DataModelHostEntity" }
    }

    def "an undescribed non-JPA property is skipped"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")

        then:
        dataModel.attributeModels().every { it.attributeName != "id" }
    }

    def "a contributor that throws does not stop the other contributors"() {
        when: "a contributor whose every call throws is registered before the stub one"
        DataModel contributed = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")
        DataModel jpa = dataModelRegistry.getDataModels("main").get("test_DataModelHostEntity")

        then: "the entities and attributes of the working contributor are still in the data model"
        contributed != null
        contributed.entityModel().tableName == StubDataModelContributor.STUB_TABLE
        jpa != null
        jpa.attributeModels().any { it.attributeName == "sideValue" }
    }

    def "the diagram marks a dynamic attribute and leaves a static one plain"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels("main").get("test_DataModelHostEntity")

        then:
        dataModel.entityDescription().contains("sideValue : String <<dynamic>>")
        dataModel.entityDescription().contains("name : String\n")
    }

    def "the diagram marks a contributed dynamic entity and leaves its attributes plain"() {
        when:
        DataModel dataModel = dataModelRegistry.getDataModels(StubDataModelContributor.STUB_STORE)
                .get("test_DataModelStubEntity")
        def lines = dataModel.entityDescription().lines().toList()

        then:
        lines.first().startsWith("entity test_DataModelStubEntity")
        lines.first().endsWith("<<dynamic>> {")
        lines.tail().every { !it.contains("<<dynamic>>") }
    }

    def "contributors are reported as registered"() {
        expect:
        dataModelRegistry.hasContributors()
    }
}
