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

package database_role_provider


import io.jmix.core.Metadata
import io.jmix.core.SaveContext
import io.jmix.core.UnconstrainedDataManager
import io.jmix.security.model.*
import io.jmix.securitydata.entity.ResourcePolicyEntity
import io.jmix.securitydata.entity.ResourceRoleEntity
import io.jmix.securitydata.entity.RowLevelPolicyEntity
import io.jmix.securitydata.entity.RowLevelRoleEntity
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import test_support.SecurityDataSpecification
import test_support.entity.TestOrder

class DatabaseRoleProviderTest extends SecurityDataSpecification {

    static final String ROLE1_LOCALIZED_NAMES = 'de=Rolle eins\nen=Role one'
    static final String ROLE1_LOCALIZED_DESCRIPTIONS = 'en=The first role'
    static final String ROLE3_LOCALIZED_NAMES = 'de=Rolle drei\nen=Role three'
    static final String ROLE3_LOCALIZED_DESCRIPTIONS = 'en=The third role'

    @Autowired
    DatabaseResourceRoleProvider databaseResourceRoleProvider

    @Autowired
    DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider

    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    Metadata metadata

    @Autowired
    ApplicationContext applicationContext

    def setup() {
        prepareTestData()
    }

    def "get all resource roles"() {
        when:
        def roles = databaseResourceRoleProvider.getAllRoles()

        then:
        roles.size() == 1

        def role1 = roles.find { it.code == 'role1' }

        role1 != null

        role1.resourcePolicies.size() == 2

        def screen1ResourcePolicy = role1.resourcePolicies.find { it.resource == 'screen1' }
        with(screen1ResourcePolicy) {
            action == ResourcePolicy.DEFAULT_ACTION
            effect == ResourcePolicy.DEFAULT_EFFECT
            policyGroup == 'policyGroup1'
            type == ResourcePolicyType.SCREEN
        }
    }

    def "get all row level roles"() {
        when:
        def roles = databaseRowLevelRoleProvider.getAllRoles()

        then:
        roles.size() == 2

        def role1 = roles.find { it.code == 'role2' }
        def role3 = roles.find { it.code == 'role3' }

        role1 != null
        role3 != null

        role1.rowLevelPolicies.size() == 1

        role3.rowLevelPolicies.size() == 2
        def rowLevelPolicy = role3.rowLevelPolicies.find { it.entityName == 'test_Order' }
        with(rowLevelPolicy) {
            type == RowLevelPolicyType.JPQL
            whereClause == 'where1'
            joinClause == 'join1'
        }
    }

    def "get resource role by code"() {
        when:
        ResourceRole role = databaseResourceRoleProvider.getRoleByCode('role1')

        then:
        with(role) {
            code == 'role1'
            name == 'Role1'
            description == 'Role1\nrole1'
            resourcePolicies.size() == 2
        }
    }

    def "get row level resource role by code"() {
        when:
        RowLevelRole role = databaseRowLevelRoleProvider.getRoleByCode('role3')

        then:
        with(role) {
            code == 'role3'
            name == 'Role3'
            rowLevelPolicies.size() == 2
        }
    }

    def "predicate created from script"() {
        when:
        RowLevelRole role2 = databaseRowLevelRoleProvider.getRoleByCode('role2')

        then:

        role2.rowLevelPolicies.size() == 1


        when:

        def rowLevelPolicy = role2.rowLevelPolicies[0]
        def testOrder = new TestOrder()
        testOrder.number = '1'

        then:

        rowLevelPolicy.biPredicate.test(testOrder, applicationContext) == false

        when:

        testOrder.number = '2'

        then:

        rowLevelPolicy.biPredicate.test(testOrder, applicationContext) == true

    }

    def "get all resource roles without policies"() {
        when:
        def roles = databaseResourceRoleProvider.getAllRoles(false)

        then:
        roles.size() == 1
        def role1 = roles.find { it.code == 'role1' }
        role1 != null
        role1.resourcePolicies.isEmpty()
    }

    def "get all resource roles with policies via flag"() {
        when:
        def roles = databaseResourceRoleProvider.getAllRoles(true)

        then:
        def role1 = roles.find { it.code == 'role1' }
        role1.resourcePolicies.size() == 2
    }

    def "get all row level roles without policies"() {
        when:
        def roles = databaseRowLevelRoleProvider.getAllRoles(false)

        then:
        roles.size() == 2
        roles.every { it.rowLevelPolicies.isEmpty() }
    }

    def "find resource role by code still loads policies"() {
        when:
        def role = databaseResourceRoleProvider.getRoleByCode('role1')

        then:
        role.resourcePolicies.size() == 2
    }

    def "a #kind role carries the localized values of its entity, whichever way its provider builds it"() {
        given:
        def provider = kind == 'resource' ? databaseResourceRoleProvider : databaseRowLevelRoleProvider

        when: "the role is built with its policies, without them and by its code"
        def roles = [provider.getAllRoles(true).find { it.code == code },
                     provider.getAllRoles(false).find { it.code == code },
                     provider.findRoleByCode(code)]

        then:
        roles*.localizedNames == [localizedNames] * 3
        roles*.localizedDescriptions == [localizedDescriptions] * 3

        where:
        kind        | code    || localizedNames        | localizedDescriptions
        'resource'  | 'role1' || ROLE1_LOCALIZED_NAMES | ROLE1_LOCALIZED_DESCRIPTIONS
        'row-level' | 'role3' || ROLE3_LOCALIZED_NAMES | ROLE3_LOCALIZED_DESCRIPTIONS
    }

    def "values that cannot be read are kept as stored and reported at warn with the code, once for the same values"() {
        given: "the lists build their roles on every load"
        def brokenValues = brokenLocalizedValues('reported once')
        saveRoleWithLocalizedValues(brokenValues)

        expect: "the first build reports them and the next ones do not; the role carries them as they are stored"
        reportsBothBundles(brokenValueWarnings { databaseResourceRoleProvider.getAllRoles(false) })
        brokenValueWarnings { databaseResourceRoleProvider.getAllRoles(false) }.isEmpty()
        brokenValueWarnings { databaseResourceRoleProvider.findRoleByCode('broken') }.isEmpty()
        databaseResourceRoleProvider.findRoleByCode('broken').localizedNames == brokenValues

        when: "the values change and still cannot be read"
        saveRoleWithLocalizedValues(brokenLocalizedValues('changed'))

        then:
        reportsBothBundles(brokenValueWarnings { databaseResourceRoleProvider.findRoleByCode('broken') })

        when: "the values are repaired and later broken the same way again"
        saveRoleWithLocalizedValues('de=Repariert')
        databaseResourceRoleProvider.getAllRoles(false)
        saveRoleWithLocalizedValues(brokenLocalizedValues('changed'))

        then:
        reportsBothBundles(brokenValueWarnings { databaseResourceRoleProvider.getAllRoles(false) })
    }

    /**
     * Localized values with a malformed escape.
     */
    private static String brokenLocalizedValues(String text) {
        "de=${text}\nru=\\u00"
    }

    /**
     * Saves a resource role with the code 'broken' and the localized values, or gives the role saved before these
     * values.
     */
    private void saveRoleWithLocalizedValues(String localizedValues) {
        ResourceRoleEntity role = dataManager.load(ResourceRoleEntity)
                .query('e.code = :code')
                .parameter('code', 'broken')
                .optional()
                .orElseGet { metadata.create(ResourceRoleEntity).tap { code = 'broken'; name = 'Broken' } }
        role.localizedNames = localizedValues
        role.localizedDescriptions = localizedValues
        dataManager.save(role)
    }

    private static boolean reportsBothBundles(List<String> warnings) {
        ['localizedNames', 'localizedDescriptions'].every { property ->
            warnings.any { it.contains("Cannot read the ${property} of role 'broken'") }
        }
    }

    /**
     * The warnings about the localized values of the role 'broken' that building roles logs. The simple SLF4J logger of
     * the tests prints to the standard error as it is when a record is logged.
     */
    private static List<String> brokenValueWarnings(Closure buildRoles) {
        def standardError = System.err
        def captured = new ByteArrayOutputStream()
        System.setErr(new PrintStream(captured, true, 'UTF-8'))
        try {
            buildRoles()
        } finally {
            System.setErr(standardError)
        }
        captured.toString('UTF-8').readLines().findAll { it.contains(' WARN ') && it.contains("of role 'broken'") }
    }

    private void prepareTestData() {
        ResourceRoleEntity role1 = metadata.create(ResourceRoleEntity)
        role1.code = 'role1'
        role1.name = 'Role1'
        role1.description = 'Role1\nrole1'
        role1.localizedNames = ROLE1_LOCALIZED_NAMES
        role1.localizedDescriptions = ROLE1_LOCALIZED_DESCRIPTIONS

        def entitiesToSave = []

        entitiesToSave << createResourcePolicyEntity(ResourcePolicyType.SCREEN, 'screen1',
                ResourcePolicy.DEFAULT_ACTION, ResourcePolicy.DEFAULT_EFFECT, 'policyGroup1', role1)
        entitiesToSave << createResourcePolicyEntity(ResourcePolicyType.SCREEN, 'screen2',
                ResourcePolicy.DEFAULT_ACTION, ResourcePolicy.DEFAULT_EFFECT, 'policyGroup2', role1)

        RowLevelRoleEntity role2 = metadata.create(RowLevelRoleEntity)
        role2.code = 'role2'
        role2.name = 'Role2'

        String script = "return {E}.number == '2'"
        entitiesToSave << createScriptRowLevelPolicyEntity('test_Order', RowLevelPolicyAction.CREATE, script, role2)

        RowLevelRoleEntity role3 = metadata.create(RowLevelRoleEntity)
        role3.code = 'role3'
        role3.name = 'Role3'
        role3.localizedNames = ROLE3_LOCALIZED_NAMES
        role3.localizedDescriptions = ROLE3_LOCALIZED_DESCRIPTIONS

        entitiesToSave << createJpqlRowLevelPolicyEntity('test_Order', 'where1', 'join1', role3)
        entitiesToSave << createJpqlRowLevelPolicyEntity('test_Customer', 'where2', 'join2', role3)

        entitiesToSave << role1
        entitiesToSave << role2
        entitiesToSave << role3

        dataManager.save(new SaveContext().saving(entitiesToSave))
    }

    private ResourcePolicyEntity createResourcePolicyEntity(String type,
                                                            String resource,
                                                            String action,
                                                            String effect,
                                                            String policyGroup,
                                                            ResourceRoleEntity roleEntity) {
        def resourcePolicy = metadata.create(ResourcePolicyEntity)
        resourcePolicy.type = type
        resourcePolicy.resource = resource
        resourcePolicy.action = action
        resourcePolicy.effect = effect
        resourcePolicy.policyGroup = policyGroup
        resourcePolicy.role = roleEntity
        return resourcePolicy
    }

    private RowLevelPolicyEntity createJpqlRowLevelPolicyEntity(String entityName,
                                                                String whereClause,
                                                                String joinClause,
                                                                RowLevelRoleEntity role) {
        RowLevelPolicyEntity rowLevelPolicy = metadata.create(RowLevelPolicyEntity)
        rowLevelPolicy.type = RowLevelPolicyType.JPQL
        rowLevelPolicy.entityName = entityName
        rowLevelPolicy.whereClause = whereClause
        rowLevelPolicy.joinClause = joinClause
        rowLevelPolicy.action = RowLevelPolicyAction.READ
        rowLevelPolicy.role = role
        return rowLevelPolicy
    }

    private RowLevelPolicyEntity createScriptRowLevelPolicyEntity(String entityName,
                                                                  RowLevelPolicyAction action,
                                                                  String script,
                                                                  RowLevelRoleEntity role) {
        RowLevelPolicyEntity rowLevelPolicy = metadata.create(RowLevelPolicyEntity)
        rowLevelPolicy.type = RowLevelPolicyType.PREDICATE
        rowLevelPolicy.entityName = entityName
        rowLevelPolicy.action = action
        rowLevelPolicy.script = script
        rowLevelPolicy.role = role
        return rowLevelPolicy
    }
}
