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

package role_localization

import io.jmix.core.MetadataTools
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider
import io.jmix.security.impl.role.provider.AnnotatedRowLevelRoleProvider
import io.jmix.security.model.ResourceRole
import io.jmix.security.model.RoleModelConverter
import io.jmix.security.model.RowLevelRole
import io.jmix.security.role.RoleLocalizationSupport
import org.springframework.beans.factory.annotation.Autowired
import test_support.RoleLocalizationSpecification
import test_support.role_localization.TestBlankMessageRole
import test_support.role_localization.TestDefaultLocaleOnlyRole
import test_support.role_localization.TestLiteralRole
import test_support.role_localization.TestMissingKeyRole
import test_support.role_localization.TestShortReferenceRole
import test_support.role_localization.TestShortReferenceRowLevelRole

class RoleLocalizationSupportTest extends RoleLocalizationSpecification {

    static final Locale PT_BR = Locale.forLanguageTag('pt-BR')
    static final Locale SR_LATN = Locale.forLanguageTag('sr-Latn')

    @Autowired
    RoleLocalizationSupport roleLocalizationSupport
    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder
    @Autowired
    AnnotatedResourceRoleProvider annotatedResourceRoleProvider
    @Autowired
    AnnotatedRowLevelRoleProvider annotatedRowLevelRoleProvider
    @Autowired
    RoleModelConverter roleModelConverter
    @Autowired
    MetadataTools metadataTools

    /**
     * A design-time role as its provider holds it: with the texts of the default locale and the keys of the messages.
     */
    ResourceRole designTimeRole(String code) {
        annotatedResourceRoleProvider.findRoleByCode(code)
    }

    RowLevelRole designTimeRowLevelRole(String code) {
        annotatedRowLevelRoleProvider.findRoleByCode(code)
    }

    /**
     * A role the way a database role arrives: a default text and localized values, no message key.
     */
    static ResourceRole databaseRole(String localizedNames, String localizedDescriptions = null) {
        def role = new ResourceRole()
        role.code = 'order-manager'
        role.name = 'Manager'
        role.description = 'Manages orders'
        role.localizedNames = localizedNames
        role.localizedDescriptions = localizedDescriptions
        return role
    }

    def "a role shows its text in the user's locale, or in the locale given"() {
        given:
        authenticate(Locale.ENGLISH)
        def designTime = designTimeRole(TestShortReferenceRole.CODE)
        def database = databaseRole('de=Leiter', 'de=Verwaltet Bestellungen')

        expect:
        roleLocalizationSupport.getLocalizedName(designTime) == 'Short reference role'
        roleLocalizationSupport.getLocalizedDescription(designTime) == 'A role named through a short reference'
        roleLocalizationSupport.getLocalizedName(database, Locale.GERMAN) == 'Leiter'
        roleLocalizationSupport.getLocalizedDescription(database, Locale.GERMAN) == 'Verwaltet Bestellungen'
    }

    def "a message missing or blank in the user's locale falls back to the default locale, then to the key"() {
        given: "the English lookup reaches only the locale-less bundle: it lacks one message and blanks another"
        authenticate(Locale.ENGLISH)
        def missing = designTimeRole(TestDefaultLocaleOnlyRole.CODE)

        expect:
        roleLocalizationSupport.getLocalizedName(missing) == 'Nur in der Standardsprache'
        roleLocalizationSupport.getLocalizedDescription(missing) == 'Nur in der Standardsprache beschrieben'
        roleLocalizationSupport.getLocalizedName(designTimeRole(TestBlankMessageRole.CODE)) == 'Mit leerer Übersetzung'
        roleLocalizationSupport.getLocalizedName(designTimeRole(TestMissingKeyRole.CODE)) ==
                'test_support.role_localization/roles.missing.name'
    }

    def "the message key wins over localized names"() {
        given:
        authenticate(Locale.GERMAN)
        def role = annotatedRoleBuilder.createResourceRole(TestShortReferenceRole.name)
        role.localizedNames = 'de=Übersetzt'

        expect:
        roleLocalizationSupport.getLocalizedName(role) == 'Rolle mit kurzer Referenz'
    }

    def "a localized value comes from the exact locale key, the language or the default text: #situation"() {
        given: "one role localizes only its name, another only its description, so that each reads values of its own"
        authenticate(locale)
        def named = databaseRole(localized, null)
        def described = databaseRole(null, localized)

        expect:
        roleLocalizationSupport.getLocalizedName(named) == name
        roleLocalizationSupport.getLocalizedDescription(named) == 'Manages orders'
        roleLocalizationSupport.getLocalizedName(described) == 'Manager'
        roleLocalizationSupport.getLocalizedDescription(described) == description

        where:
        situation                  | locale        | localized                      || name         | description
        'the exact key'            | PT_BR         | 'pt=Gerente\npt_BR=Gerente BR' || 'Gerente BR' | 'Gerente BR'
        'a key with a script'      | SR_LATN       | 'sr=Менаџер\nsr-Latn=Menadžer' || 'Menadžer'   | 'Menadžer'
        'the language'             | PT_BR         | 'pt=Gerente\nde=Leiter'        || 'Gerente'    | 'Gerente'
        'nothing for the locale'   | Locale.GERMAN | 'ru=Менеджер'                  || 'Manager'    | 'Manages orders'
        'nothing localized at all' | Locale.GERMAN | null                           || 'Manager'    | 'Manages orders'
        'an empty exact value'     | PT_BR         | 'pt_BR=\npt=Gerente'           || 'Gerente'    | 'Gerente'
        'a value of spaces'        | Locale.GERMAN | 'de=\\ \\ '                    || 'Manager'    | 'Manages orders'
        'an unreadable bundle'     | Locale.GERMAN | 'de=Leiter\nru=\\u00'          || 'Manager'    | 'Manages orders'
    }

    def "a bundle that cannot be read is reported where the role is shown at debug only, on one line"() {
        given: "the provider of a database role reports such a bundle at warn where it builds the role"
        authenticate(Locale.GERMAN)
        def role = databaseRole('de=Leiter\nru=\\u00')

        when:
        def records = standardErrorOf { roleLocalizationSupport.getLocalizedName(role) }
                .readLines()
                .findAll { it.contains(RoleLocalizationSupport.class.name) }

        then: "the record of the resolver reaches the capture, so a warning of it could not pass unseen"
        records.size() == 1
        records[0].contains(' DEBUG ')
        records[0].contains('de=Leiter\\nru=\\u00')
    }

    /**
     * What the code writes to the standard error, where the simple SLF4J logger of the tests prints.
     */
    static String standardErrorOf(Closure code) {
        def standardError = System.err
        def captured = new ByteArrayOutputStream()
        System.setErr(new PrintStream(captured, true, 'UTF-8'))
        try {
            code()
        } finally {
            System.setErr(standardError)
        }
        captured.toString('UTF-8')
    }

    def "a role without a name is shown with an empty one and a role without a description with none"() {
        given: "a database role keeps null, a design-time role the empty description its annotation declares by default"
        authenticate(Locale.GERMAN)
        def database = databaseRole(null)
        database.name = null
        database.description = null
        def designTime = designTimeRole(TestLiteralRole.CODE)

        expect:
        roleLocalizationSupport.getLocalizedName(database) == ''
        roleLocalizationSupport.getLocalizedName(roleModelConverter.createResourceRoleModel(database)) == ''
        roleLocalizationSupport.getLocalizedDescription(database) == null
        roleLocalizationSupport.getLocalizedDescription(designTime) == null
        roleLocalizationSupport.getLocalizedDescription(roleModelConverter.createResourceRoleModel(designTime)) == null
    }

    def "a role model is localized the way its role is, the description from values of its own"() {
        given: "the user's locale is not the default one, so the design-time role keeps a German text"
        authenticate(Locale.ENGLISH)
        def designTimeModel = roleModelConverter.createResourceRoleModel(designTimeRole(TestShortReferenceRole.CODE))
        def databaseModel = roleModelConverter.createResourceRoleModel(
                databaseRole('en=Leader', 'en=Leads the orders'))

        expect:
        roleLocalizationSupport.getLocalizedName(designTimeModel) == 'Short reference role'
        roleLocalizationSupport.getLocalizedDescription(designTimeModel) == 'A role named through a short reference'
        roleLocalizationSupport.getLocalizedName(databaseModel) == 'Leader'
        roleLocalizationSupport.getLocalizedDescription(databaseModel) == 'Leads the orders'
        roleLocalizationSupport.getLocalizedName(databaseModel, Locale.GERMAN) == 'Manager'
        roleLocalizationSupport.getLocalizedDescription(databaseModel, Locale.GERMAN) == 'Manages orders'
    }

    def "the instance name of both role models is their localized name"() {
        given:
        authenticate(Locale.ENGLISH)
        def designTimeModel = roleModelConverter.createResourceRoleModel(designTimeRole(TestShortReferenceRole.CODE))
        def databaseModel = roleModelConverter.createResourceRoleModel(databaseRole('en=Leader'))
        def designTimeRowLevelModel = roleModelConverter.createRowLevelRoleModel(
                designTimeRowLevelRole(TestShortReferenceRowLevelRole.CODE))
        def databaseRowLevelModel = roleModelConverter.createRowLevelRoleModel(
                new RowLevelRole(code: 'order-reader', name: 'Reader', localizedNames: 'en=Order reader'))

        expect:
        metadataTools.getInstanceName(designTimeModel) == 'Short reference role'
        metadataTools.getInstanceName(databaseModel) == 'Leader'
        metadataTools.getInstanceName(designTimeRowLevelModel) == 'Short reference row-level role'
        metadataTools.getInstanceName(databaseRowLevelModel) == 'Order reader'
    }
}
