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

package importexport

import com.google.gson.JsonParser
import io.jmix.core.AccessManager
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.EntityImportExport
import io.jmix.core.EntityImportPlan
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import io.jmix.core.FetchPlans
import io.jmix.core.Id
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.accesscontext.EntityAttributeContext
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.data.DataConfiguration
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorListView
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.security.SecurityConfiguration
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.role.RowLevelRoleRepository
import io.jmix.securitydata.SecurityDataConfiguration
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import test_support.EntityInspectorPolicyTestConfiguration
import test_support.TestContextInititalizer
import test_support.entity.InspectorPolicyComposedEntity
import test_support.entity.InspectorPolicyEmbeddable
import test_support.entity.InspectorPolicyEntity
import test_support.entity.InspectorPolicyRelatedEntity
import test_support.role.InspectorAssociationImportPartialRole
import test_support.role.InspectorCompositionExportDeniedRole
import test_support.role.InspectorCompositionImportPartialRole
import test_support.role.InspectorEmbeddedExportDeniedRole
import test_support.role.InspectorEmbeddedImportPartialRole
import test_support.role.InspectorExportDeniedRole
import test_support.role.InspectorImportDeniedRole
import test_support.role.InspectorImportViewOnlyRole
import test_support.role.InspectorJsonImportPartialRole
import test_support.role.InspectorNestedExportDeniedRole
import test_support.role.InspectorRowLevelExportRole

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                   SecurityConfiguration, SecurityDataConfiguration,
                   EntityInspectorPolicyTestConfiguration],
        initializers = [TestContextInititalizer]
)
class EntityInspectorPolicyConstraintTest extends Specification {

    private static final String PASSWORD = "123"

    @Autowired
    AccessManager accessManager

    @Autowired
    EntityImportExport entityImportExport

    @Autowired
    EntityImportPlanJsonBuilder importPlanJsonBuilder

    @Autowired
    FetchPlans fetchPlans

    @Autowired
    FetchPlanRepository fetchPlanRepository

    @Autowired
    Metadata metadata

    @Autowired
    MetadataTools metadataTools

    @Autowired
    DataManager dataManager

    @Autowired
    UnconstrainedDataManager unconstrainedDataManager

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    ResourceRoleRepository resourceRoleRepository

    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    @Autowired
    JdbcTemplate jdbcTemplate

    UserDetails exportDeniedUser
    UserDetails importDeniedUser
    UserDetails importViewOnlyUser
    UserDetails jsonImportPartialUser
    UserDetails nestedExportDeniedUser
    UserDetails rowLevelExportUser
    UserDetails embeddedExportDeniedUser
    UserDetails embeddedImportPartialUser
    UserDetails compositionExportDeniedUser
    UserDetails compositionImportPartialUser
    UserDetails associationImportPartialUser
    Authentication systemAuthentication

    def setup() {
        systemAuthentication = SecurityContextHelper.getAuthentication()

        exportDeniedUser = User.builder()
                .username("entity-inspector-export-denied")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorExportDeniedRole.NAME))
                .build()
        userRepository.addUser(exportDeniedUser)

        importViewOnlyUser = User.builder()
                .username("entity-inspector-import-view-only")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorImportViewOnlyRole.NAME))
                .build()
        userRepository.addUser(importViewOnlyUser)

        importDeniedUser = User.builder()
                .username("entity-inspector-import-denied")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorImportDeniedRole.NAME))
                .build()
        userRepository.addUser(importDeniedUser)

        jsonImportPartialUser = User.builder()
                .username("entity-inspector-json-import-partial")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorJsonImportPartialRole.NAME))
                .build()
        userRepository.addUser(jsonImportPartialUser)

        nestedExportDeniedUser = User.builder()
                .username("entity-inspector-nested-export-denied")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorNestedExportDeniedRole.NAME))
                .build()
        userRepository.addUser(nestedExportDeniedUser)

        rowLevelExportUser = User.builder()
                .username("entity-inspector-row-level-export")
                .password("{noop}$PASSWORD")
                .authorities(
                        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorRowLevelExportRole.NAME),
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(InspectorRowLevelExportRole.NAME))
                .build()
        userRepository.addUser(rowLevelExportUser)

        embeddedExportDeniedUser = User.builder()
                .username("entity-inspector-embedded-export-denied")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorEmbeddedExportDeniedRole.NAME))
                .build()
        userRepository.addUser(embeddedExportDeniedUser)

        embeddedImportPartialUser = User.builder()
                .username("entity-inspector-embedded-import-partial")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorEmbeddedImportPartialRole.NAME))
                .build()
        userRepository.addUser(embeddedImportPartialUser)

        compositionExportDeniedUser = User.builder()
                .username("entity-inspector-composition-export-denied")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorCompositionExportDeniedRole.NAME))
                .build()
        userRepository.addUser(compositionExportDeniedUser)

        compositionImportPartialUser = User.builder()
                .username("entity-inspector-composition-import-partial")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorCompositionImportPartialRole.NAME))
                .build()
        userRepository.addUser(compositionImportPartialUser)

        associationImportPartialUser = User.builder()
                .username("entity-inspector-association-import-partial")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(InspectorAssociationImportPartialRole.NAME))
                .build()
        userRepository.addUser(associationImportPartialUser)
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)
        userRepository.removeUser(exportDeniedUser)
        userRepository.removeUser(importDeniedUser)
        userRepository.removeUser(importViewOnlyUser)
        userRepository.removeUser(jsonImportPartialUser)
        userRepository.removeUser(nestedExportDeniedUser)
        userRepository.removeUser(rowLevelExportUser)
        userRepository.removeUser(embeddedExportDeniedUser)
        userRepository.removeUser(embeddedImportPartialUser)
        userRepository.removeUser(compositionExportDeniedUser)
        userRepository.removeUser(compositionImportPartialUser)
        userRepository.removeUser(associationImportPartialUser)
        jdbcTemplate.execute("delete from TEST_INSPECTOR_POLICY_ENTITY")
        jdbcTemplate.execute("delete from TEST_INSPECTOR_POLICY_COMPOSED_ENTITY")
        jdbcTemplate.execute("delete from TEST_INSPECTOR_POLICY_RELATED_ENTITY")
    }

    def "export plan filters denied attributes and exported json omits them"() {
        given:
        def entity = metadata.create(InspectorPolicyEntity)
        entity.name = "secret-value"
        unconstrainedDataManager.save(entity)

        authenticate(exportDeniedUser.username)
        def nameContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEntity), "name")
        accessManager.applyRegisteredConstraints(nameContext)

        when:
        def view = entityInspectorListView()
        def fetchPlan = view.buildExportPlan(metadata.getClass(InspectorPolicyEntity))
        def json = view.exportJson([entity], fetchPlan)

        then:
        !nameContext.canView()
        fetchPlan.getProperty("name") == null
        json.contains('"id"')
        json.contains(entity.id.toString())
        !json.contains('"name"')
        !json.contains("secret-value")
    }

    def "zip import plan filters non-modifiable attributes and import keeps original value"() {
        given:
        def existing = metadata.create(InspectorPolicyEntity)
        existing.name = "original"
        unconstrainedDataManager.save(existing)

        def json = """
[
  {
    "_entityName": "test_InspectorPolicyEntity",
    "id": "${existing.id}",
    "name": "policy-bypass"
  }
]
"""
        def zip = zip(json)

        authenticate(importViewOnlyUser.username)
        def nameContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEntity), "name")
        accessManager.applyRegisteredConstraints(nameContext)

        when:
        entityInspectorListView().importZip(zip, metadata.getClass(InspectorPolicyEntity))

        then:
        nameContext.canView()
        !nameContext.canModify()
        unconstrainedDataManager.load(Id.of(existing)).one().name == "original"
    }

    def "zip import ignores attributes without view and modify permissions"() {
        given:
        def existing = metadata.create(InspectorPolicyEntity)
        existing.name = "original"
        unconstrainedDataManager.save(existing)

        def json = """
[
  {
    "_entityName": "test_InspectorPolicyEntity",
    "id": "${existing.id}",
    "name": "policy-bypass"
  }
]
"""
        def zip = zip(json)

        authenticate(importDeniedUser.username)
        def nameContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEntity), "name")
        accessManager.applyRegisteredConstraints(nameContext)

        when:
        entityInspectorListView().importZip(zip, metadata.getClass(InspectorPolicyEntity))

        then:
        !nameContext.canView()
        !nameContext.canModify()
        unconstrainedDataManager.load(Id.of(existing)).one().name == "original"
    }

    def "json import plan keeps allowed properties when denied properties are present"() {
        given:
        def related = metadata.create(InspectorPolicyRelatedEntity)
        related.deniedField = "denied-related"
        unconstrainedDataManager.save(related)

        def imported = metadata.create(InspectorPolicyEntity)
        imported.id = UUID.randomUUID()

        def json = """
[
  {
    "_entityName": "test_InspectorPolicyEntity",
    "id": "${imported.id}",
    "name": "allowed-name",
    "allowedRelation": {
      "_entityName": "test_InspectorPolicyRelatedEntity",
      "id": "${related.id}",
      "deniedField": "denied-related"
    }
  }
]
"""

        authenticate(jsonImportPartialUser.username)

        when:
        def view = entityInspectorListView()
        def importPlan = view.buildImportPlanFromContent(json, metadata.getClass(InspectorPolicyEntity))
        entityImportExport.importEntitiesFromJson(json, importPlan)

        then:
        importPlan.getProperty("name") != null
        importPlan.getProperty("allowedRelation") == null

        def loaded = unconstrainedDataManager.load(InspectorPolicyEntity).id(imported.id).one()
        loaded.name == "allowed-name"
        loaded.allowedRelation == null
    }

    def "export plan preserves local relation semantics and omits denied nested attributes"() {
        given:
        def related = metadata.create(InspectorPolicyRelatedEntity)
        related.deniedField = "nested-secret"
        unconstrainedDataManager.save(related)

        def entity = metadata.create(InspectorPolicyEntity)
        entity.allowedRelation = related
        unconstrainedDataManager.save(entity)

        authenticate(nestedExportDeniedUser.username)
        def relationContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEntity), "allowedRelation")
        accessManager.applyRegisteredConstraints(relationContext)
        def nestedFieldContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyRelatedEntity), "deniedField")
        accessManager.applyRegisteredConstraints(nestedFieldContext)

        when:
        def view = entityInspectorListView()
        def fetchPlan = view.buildExportPlan(metadata.getClass(InspectorPolicyEntity))
        def json = view.exportJson([entity], fetchPlan)
        def relationProperty = fetchPlan.getProperty("allowedRelation")

        then:
        relationContext.canView()
        !nestedFieldContext.canView()
        relationProperty != null
        relationProperty.fetchPlan != null
        relationProperty.fetchPlan.getProperty("deniedField") == null
        relationProperty.fetchPlan.getProperty("version") != null
        json.contains('"allowedRelation"')
        json.contains('"version"')
        !json.contains('"deniedField"')
        !json.contains("nested-secret")
    }

    def "embedded export omits denied nested attributes"() {
        given:
        def entity = metadata.create(InspectorPolicyEntity)
        entity.embeddedDetails = new InspectorPolicyEmbeddable(
                allowedField: "embedded-visible",
                deniedField: "embedded-secret"
        )
        unconstrainedDataManager.save(entity)

        authenticate(embeddedExportDeniedUser.username)
        def embeddedContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEntity), "embeddedDetails")
        accessManager.applyRegisteredConstraints(embeddedContext)
        def allowedFieldContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEmbeddable), "allowedField")
        accessManager.applyRegisteredConstraints(allowedFieldContext)
        def deniedFieldContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEmbeddable), "deniedField")
        accessManager.applyRegisteredConstraints(deniedFieldContext)

        when:
        def view = entityInspectorListView()
        def fetchPlan = view.buildExportPlan(metadata.getClass(InspectorPolicyEntity))
        def json = view.exportJson([entity], fetchPlan)
        def embeddedProperty = fetchPlan.getProperty("embeddedDetails")

        then:
        embeddedContext.canView()
        allowedFieldContext.canView()
        !deniedFieldContext.canView()
        embeddedProperty != null
        embeddedProperty.fetchPlan != null
        embeddedProperty.fetchPlan.getProperty("allowedField") != null
        embeddedProperty.fetchPlan.getProperty("deniedField") == null
        json.contains('"embeddedDetails"')
        json.contains('"allowedField"')
        json.contains("embedded-visible")
        !json.contains('"deniedField"')
        !json.contains("embedded-secret")
    }

    def "embedded zip import keeps allowed nested attributes and ignores denied ones"() {
        given:
        def importedId = UUID.randomUUID()
        def json = """
[
  {
    "_entityName": "test_InspectorPolicyEntity",
    "id": "${importedId}",
    "embeddedDetails": {
      "allowedField": "embedded-import-allowed",
      "deniedField": "embedded-import-denied"
    }
  }
]
"""

        authenticate(embeddedImportPartialUser.username)

        when:
        def view = entityInspectorListView()
        def importPlan = view.buildImportPlanFromContent(json, metadata.getClass(InspectorPolicyEntity))
        view.importZip(zip(json), metadata.getClass(InspectorPolicyEntity))

        then:
        importPlan.getProperty("embeddedDetails") != null
        importPlan.getProperty("embeddedDetails").plan != null
        importPlan.getProperty("embeddedDetails").plan.getProperty("allowedField") != null
        importPlan.getProperty("embeddedDetails").plan.getProperty("deniedField") == null

        def loaded = unconstrainedDataManager.load(InspectorPolicyEntity).id(importedId).one()
        loaded.embeddedDetails != null
        loaded.embeddedDetails.allowedField == "embedded-import-allowed"
        loaded.embeddedDetails.deniedField == null
    }

    def "composition export omits denied nested attributes"() {
        given:
        def composed = metadata.create(InspectorPolicyComposedEntity)
        composed.allowedField = "composition-visible"
        composed.deniedField = "composition-secret"

        def entity = metadata.create(InspectorPolicyEntity)
        entity.composedDetails = composed
        unconstrainedDataManager.save(entity)

        authenticate(compositionExportDeniedUser.username)
        def compositionContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyEntity), "composedDetails")
        accessManager.applyRegisteredConstraints(compositionContext)
        def allowedFieldContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyComposedEntity), "allowedField")
        accessManager.applyRegisteredConstraints(allowedFieldContext)
        def deniedFieldContext = new EntityAttributeContext(metadata.getClass(InspectorPolicyComposedEntity), "deniedField")
        accessManager.applyRegisteredConstraints(deniedFieldContext)

        when:
        def view = entityInspectorListView()
        def fetchPlan = view.buildExportPlan(metadata.getClass(InspectorPolicyEntity))
        def json = view.exportJson([entity], fetchPlan)
        def composedProperty = fetchPlan.getProperty("composedDetails")

        then:
        compositionContext.canView()
        allowedFieldContext.canView()
        !deniedFieldContext.canView()
        composedProperty != null
        composedProperty.fetchPlan != null
        composedProperty.fetchPlan.getProperty("allowedField") != null
        composedProperty.fetchPlan.getProperty("deniedField") == null
        json.contains('"composedDetails"')
        json.contains('"allowedField"')
        json.contains("composition-visible")
        !json.contains('"deniedField"')
        !json.contains("composition-secret")
    }

    def "composition zip import keeps allowed nested attributes and ignores denied ones"() {
        given:
        def importedId = UUID.randomUUID()
        def composedId = UUID.randomUUID()
        def json = """
[
  {
    "_entityName": "test_InspectorPolicyEntity",
    "id": "${importedId}",
    "composedDetails": {
      "_entityName": "test_InspectorPolicyComposedEntity",
      "id": "${composedId}",
      "allowedField": "composition-import-allowed",
      "deniedField": "composition-import-denied"
    }
  }
]
"""

        authenticate(compositionImportPartialUser.username)

        when:
        def view = entityInspectorListView()
        def importPlan = view.buildImportPlanFromContent(json, metadata.getClass(InspectorPolicyEntity))
        view.importZip(zip(json), metadata.getClass(InspectorPolicyEntity))

        then:
        importPlan.getProperty("composedDetails") != null
        importPlan.getProperty("composedDetails").plan != null
        importPlan.getProperty("composedDetails").plan.getProperty("allowedField") != null
        importPlan.getProperty("composedDetails").plan.getProperty("deniedField") == null

        def loaded = unconstrainedDataManager.load(InspectorPolicyEntity).id(importedId).one()
        loaded.composedDetails != null
        loaded.composedDetails.allowedField == "composition-import-allowed"
        loaded.composedDetails.deniedField == null
    }

    def "association zip import links reference without updating nested denied attributes"() {
        given:
        def related = metadata.create(InspectorPolicyRelatedEntity)
        related.deniedField = "relation-original"
        unconstrainedDataManager.save(related)

        def importedId = UUID.randomUUID()
        def json = """
[
  {
    "_entityName": "test_InspectorPolicyEntity",
    "id": "${importedId}",
    "allowedRelation": {
      "_entityName": "test_InspectorPolicyRelatedEntity",
      "id": "${related.id}",
      "deniedField": "relation-bypass"
    }
  }
]
"""

        authenticate(associationImportPartialUser.username)

        when:
        def view = entityInspectorListView()
        def importPlan = view.buildImportPlanFromContent(json, metadata.getClass(InspectorPolicyEntity))
        view.importZip(zip(json), metadata.getClass(InspectorPolicyEntity))

        then:
        importPlan.getProperty("allowedRelation") != null
        importPlan.getProperty("allowedRelation").plan == null

        def loaded = unconstrainedDataManager.load(InspectorPolicyEntity).id(importedId).one()
        loaded.allowedRelation != null
        loaded.allowedRelation.id == related.id
        unconstrainedDataManager.load(Id.of(related)).one().deniedField == "relation-original"
    }

    def "zip export omits denied attributes"() {
        given:
        def entity = metadata.create(InspectorPolicyEntity)
        entity.name = "secret-value"
        unconstrainedDataManager.save(entity)

        authenticate(exportDeniedUser.username)

        when:
        def view = entityInspectorListView()
        def fetchPlan = view.buildExportPlan(metadata.getClass(InspectorPolicyEntity))
        def zip = view.exportZip([entity], fetchPlan)
        def json = unzip(zip)

        then:
        json.contains('"id"')
        json.contains(entity.id.toString())
        fetchPlan.getProperty("name") == null
        !json.contains('"name"')
        !json.contains("secret-value")
    }

    @Disabled
    def "export reload respects row-level constraints"() {
        given:
        def allowed = metadata.create(InspectorPolicyEntity)
        allowed.name = "allowed_entity"
        unconstrainedDataManager.save(allowed)

        def denied = metadata.create(InspectorPolicyEntity)
        denied.name = "blocked_entity"
        unconstrainedDataManager.save(denied)

        authenticate(rowLevelExportUser.username)

        when:
        def view = entityInspectorListView()
        def fetchPlan = view.buildExportPlan(metadata.getClass(InspectorPolicyEntity))
        def json = view.exportJson([allowed, denied], fetchPlan)
        def exported = JsonParser.parseString(json).getAsJsonArray()

        then:
        exported.size() == 1
        json.contains("allowed_entity")
        !json.contains("blocked_entity")
        !json.contains(denied.id.toString())
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }

    protected TestEntityInspectorListView entityInspectorListView() {
        def view = new TestEntityInspectorListView()
        ReflectionTestUtils.setField(view, "metadata", metadata)
        ReflectionTestUtils.setField(view, "accessManager", accessManager)
        ReflectionTestUtils.setField(view, "fetchPlans", fetchPlans)
        ReflectionTestUtils.setField(view, "fetchPlanRepository", fetchPlanRepository)
        ReflectionTestUtils.setField(view, "metadataTools", metadataTools)
        ReflectionTestUtils.setField(view, "importPlanJsonBuilder", importPlanJsonBuilder)
        ReflectionTestUtils.setField(view, "entityImportExport", entityImportExport)
        ReflectionTestUtils.setField(view, "dataManager", dataManager)
        return view
    }

    protected static byte[] zip(String content) {
        def outputStream = new ByteArrayOutputStream()
        def zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)
        zipOutputStream.putNextEntry(new ZipEntry("entities.json"))
        zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8))
        zipOutputStream.closeEntry()
        zipOutputStream.close()
        return outputStream.toByteArray()
    }

    protected static String unzip(byte[] bytes) {
        def zipInputStream = new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)
        try {
            if (zipInputStream.getNextEntry() == null) {
                return ""
            }
            return new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8)
        } finally {
            zipInputStream.close()
        }
    }

    static class TestEntityInspectorListView extends EntityInspectorListView {
        FetchPlan buildExportPlan(def metaClass) {
            return createEntityExportPlan(metaClass)
        }

        EntityImportPlan buildImportPlanFromContent(String content, def metaClass) {
            return createEntityImportPlan(content, metaClass)
        }

        String exportJson(Collection<Object> entities, FetchPlan fetchPlan) {
            return exportEntitiesToJson(entities, fetchPlan)
        }

        byte[] exportZip(Collection<Object> entities, FetchPlan fetchPlan) {
            return exportEntitiesToZip(entities, fetchPlan)
        }

        Collection<Object> importZip(byte[] zipBytes, def metaClass) {
            return importEntitiesFromZip(zipBytes, metaClass)
        }
    }
}
